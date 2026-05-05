import cv2
import numpy as np
import time
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
# Resolusi kita perbesar agar jelas di layar tablet!
WARPED_SIZE = 800 
step = WARPED_SIZE / 8 

cols = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'] 
rows = ['1', '2', '3', '4', '5', '6', '7', '8']

pts_src = [] 
warped_matrix = None 

# Kita gunakan SATU nama window saja untuk semuanya
window_name = "Smart Chess AI - System"

def select_points(event, x, y, flags, param):
    global pts_src
    if event == cv2.EVENT_LBUTTONDOWN:
        if len(pts_src) < 4:
            pts_src.append([x, y])
            print(f"[INFO] Titik {len(pts_src)}/4 berhasil diklik pada koordinat: ({x}, {y})")

# MENGIZINKAN WINDOW DI-RESIZE / MAXIMIZE
cv2.namedWindow(window_name, cv2.WINDOW_NORMAL)
cv2.setMouseCallback(window_name, select_points)

print("\n" + "="*60)
print("=== TAHAP KALIBRASI PAPAN CATUR ===")
print("Klik 4 sudut papan catur sesuai urutan:")
print("1. Kiri Atas -> 2. Kanan Atas -> 3. Kanan Bawah -> 4. Kiri Bawah")
print("="*60 + "\n")

prev_frame_time = 0

try:
    while True:
        ret, frame = cap.read()
        if not ret: continue

        # --- TAHAP 1: MODE KALIBRASI ---
        if warped_matrix is None:
            display_frame = frame.copy()
            cv2.putText(display_frame, f"KLIK 4 SUDUT! ({len(pts_src)}/4)", (10, 40), 
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 3)
            
            for pt in pts_src:
                cv2.circle(display_frame, tuple(pt), 6, (0, 0, 255), -1)

            cv2.imshow(window_name, display_frame)

            if len(pts_src) == 4:
                pts_dst = np.array([
                    [0, 0], 
                    [WARPED_SIZE - 1, 0], 
                    [WARPED_SIZE - 1, WARPED_SIZE - 1], 
                    [0, WARPED_SIZE - 1]
                ], dtype="float32")
                
                src_array = np.array(pts_src, dtype="float32")
                warped_matrix = cv2.getPerspectiveTransform(src_array, pts_dst)
                print("\n[SUCCESS] Kalibrasi Selesai! Memulai deteksi AI...\n")

        # --- TAHAP 2: MODE DETEKSI (LIVE) ---
        else:
            warped_frame = cv2.warpPerspective(frame, warped_matrix, (WARPED_SIZE, WARPED_SIZE))
            occupied_squares = set()

            results = model.predict(source=warped_frame, conf=0.5, verbose=False)
            boxes = results[0].boxes
            
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                center_x = (x1 + x2) / 2
                bottom_y = y2 
                
                col_idx = int(center_x // step)
                row_idx = int(bottom_y // step)
                
                if 0 <= col_idx < 8 and 0 <= row_idx < 8:
                    square_name = f"{cols[col_idx]}{rows[row_idx]}"
                    occupied_squares.add(square_name)
                    # Titik merah penanda dasar bidak
                    cv2.circle(warped_frame, (int(center_x), int(bottom_y)), 6, (0, 0, 255), -1)

            # Menggambar Grid 8x8
            for i in range(8):
                for j in range(8):
                    start_x = int(j * step)
                    start_y = int(i * step)
                    end_x = int((j + 1) * step)
                    end_y = int((i + 1) * step)
                    
                    sq_name = f"{cols[j]}{rows[i]}"
                    
                    if sq_name in occupied_squares:
                        # Kotak Hijau Terang (Terisi)
                        cv2.rectangle(warped_frame, (start_x, start_y), (end_x, end_y), (0, 255, 0), 3)
                        cv2.putText(warped_frame, sq_name, (start_x + 5, start_y + 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
                    else:
                        # Kotak Abu-abu (Kosong)
                        cv2.rectangle(warped_frame, (start_x, start_y), (end_x, end_y), (150, 150, 150), 1)

            new_frame_time = time.time()
            fps = int(1 / (new_frame_time - prev_frame_time))
            prev_frame_time = new_frame_time
            cv2.putText(warped_frame, f"FPS: {fps}", (10, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 3)

            cv2.imshow(window_name, warped_frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

except KeyboardInterrupt:
    pass
finally:
    cap.release()
    cv2.destroyAllWindows()