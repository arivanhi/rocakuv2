import cv2
import numpy as np
import time
import json
import os
from ultralytics import YOLO

# ==============================================================================
# 1. KONFIGURASI MODEL & KAMERA
# ==============================================================================
print("[INFO] Memuat model TFLite...")
model = YOLO('model/best2_float32.tflite', task='detect') 

cap = cv2.VideoCapture(0)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

# ==============================================================================
# 2. VARIABEL KALIBRASI & UKURAN LAYAR
# ==============================================================================
WARPED_SIZE = 800 
step = WARPED_SIZE / 8 

cols = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'] 
rows = ['1', '2', '3', '4', '5', '6', '7', '8']

pts_src = [] 
warped_matrix = None 
CALIB_FILE = "kalibrasi.json"
window_name = "Smart Chess AI - System"

# --- FUNGSI MEMUAT DATA KALIBRASI ---
if os.path.exists(CALIB_FILE):
    try:
        with open(CALIB_FILE, 'r') as f:
            pts_src = json.load(f)
        if len(pts_src) == 4:
            pts_dst = np.array([[0, 0], [WARPED_SIZE - 1, 0], [WARPED_SIZE - 1, WARPED_SIZE - 1], [0, WARPED_SIZE - 1]], dtype="float32")
            src_array = np.array(pts_src, dtype="float32")
            warped_matrix = cv2.getPerspectiveTransform(src_array, pts_dst)
            print("[INFO] Data kalibrasi ditemukan! Melewati mode klik mouse.")
    except Exception as e:
        print(f"[ERROR] Gagal memuat file kalibrasi: {e}")
        pts_src = []

def select_points(event, x, y, flags, param):
    global pts_src, warped_matrix
    if event == cv2.EVENT_LBUTTONDOWN and warped_matrix is None:
        if len(pts_src) < 4:
            pts_src.append([x, y])
            print(f"[INFO] Titik {len(pts_src)}/4 berhasil diklik: ({x}, {y})")
            
            # Jika sudah 4 titik, hitung matrix dan SIMPAN ke JSON
            if len(pts_src) == 4:
                pts_dst = np.array([[0, 0], [WARPED_SIZE - 1, 0], [WARPED_SIZE - 1, WARPED_SIZE - 1], [0, WARPED_SIZE - 1]], dtype="float32")
                src_array = np.array(pts_src, dtype="float32")
                warped_matrix = cv2.getPerspectiveTransform(src_array, pts_dst)
                
                with open(CALIB_FILE, 'w') as f:
                    json.dump(pts_src, f)
                print("\n[SUCCESS] Kalibrasi Selesai & Tersimpan! Memulai deteksi AI...\n")

cv2.namedWindow(window_name, cv2.WINDOW_NORMAL)
cv2.setMouseCallback(window_name, select_points)

if warped_matrix is None:
    print("\n" + "="*60)
    print("=== TAHAP KALIBRASI PAPAN CATUR ===")
    print("Klik 4 sudut papan catur sesuai urutan:")
    print("1. Kiri Atas -> 2. Kanan Atas -> 3. Kanan Bawah -> 4. Kiri Bawah")
    print("="*60 + "\n")

# ==============================================================================
# 3. PROGRAM UTAMA
# ==============================================================================
prev_frame_time = 0

try:
    while True:
        ret, frame = cap.read()
        if not ret: continue

        # --- MODE 1: KALIBRASI MANUAL ---
        if warped_matrix is None:
            display_frame = frame.copy()
            cv2.putText(display_frame, f"KLIK 4 SUDUT! ({len(pts_src)}/4)", (10, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 3)
            for pt in pts_src:
                cv2.circle(display_frame, tuple(pt), 6, (0, 0, 255), -1)
            cv2.imshow(window_name, display_frame)

        # --- MODE 2: DETEKSI & GENERATE DATA ESP32 ---
        else:
            warped_frame = cv2.warpPerspective(frame, warped_matrix, (WARPED_SIZE, WARPED_SIZE))
            occupied_squares = set()

            results = model.predict(source=warped_frame, conf=0.5, verbose=False)
            boxes = results[0].boxes
            
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                center_x, bottom_y = (x1 + x2) / 2, y2 
                col_idx, row_idx = int(center_x // step), int(bottom_y // step)
                
                if 0 <= col_idx < 8 and 0 <= row_idx < 8:
                    square_name = f"{cols[col_idx]}{rows[row_idx]}"
                    occupied_squares.add(square_name)
                    cv2.circle(warped_frame, (int(center_x), int(bottom_y)), 6, (0, 0, 255), -1)

            # --- MEMBUAT DATA 8x8 UNTUK ESP32 ---
            esp32_data = "\n" # Diawali dengan enter agar rapi di terminal

            for i in range(8):
                for j in range(8):
                    start_x, start_y = int(j * step), int(i * step)
                    end_x, end_y = int((j + 1) * step), int((i + 1) * step)
                    sq_name = f"{cols[j]}{rows[i]}"
                    
                    if sq_name in occupied_squares:
                        esp32_data += "1 " # Bidak ADA (pakai spasi agar bentuknya kotak rapi)
                        cv2.rectangle(warped_frame, (start_x, start_y), (end_x, end_y), (0, 255, 0), 3)
                        cv2.putText(warped_frame, sq_name, (start_x + 5, start_y + 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
                    else:
                        esp32_data += "0 " # Bidak KOSONG
                        cv2.rectangle(warped_frame, (start_x, start_y), (end_x, end_y), (150, 150, 150), 1)
                
                # Tambahkan Enter (newline) setiap selesai membaca 1 baris (8 kotak)
                esp32_data += "\n"

            # Menampilkan data yang siap dikirim di Terminal
            print(f"[DATA ESP32]: {esp32_data}")

            # UI FPS
            new_frame_time = time.time()
            fps = int(1 / (new_frame_time - prev_frame_time))
            prev_frame_time = new_frame_time
            cv2.putText(warped_frame, f"FPS: {fps}", (10, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 3)
            
            # UI Info Reset
            cv2.putText(warped_frame, "Tekan 'r' untuk Reset Kalibrasi", (10, 780), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

            cv2.imshow(window_name, warped_frame)

        # --- KONTROL KEYBOARD ---
        key = cv2.waitKey(1) & 0xFF
        if key == ord('q'):
            break
        elif key == ord('r'): # Fitur Reset Kalibrasi
            print("\n[INFO] Mereset Kalibrasi...")
            if os.path.exists(CALIB_FILE):
                os.remove(CALIB_FILE)
            pts_src = []
            warped_matrix = None

except KeyboardInterrupt:
    pass
finally:
    cap.release()
    cv2.destroyAllWindows()