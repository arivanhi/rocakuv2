import cv2
import numpy as np
import time
import json
import os
import serial
from ultralytics import YOLO

# ==============================================================================
# 1. KONFIGURASI SERIAL & MODEL
# ==============================================================================
try:
    esp_serial = serial.Serial('COM5', 115200, timeout=0.1) # Sesuaikan port COM
    print("[INFO] Terhubung dengan ESP32.")
except:
    esp_serial = None
    print("[WARNING] ESP32 tidak terdeteksi.")

model = YOLO('model/best2_float32.tflite', task='detect') 
cap = cv2.VideoCapture(0)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

WARPED_SIZE = 800 
step = WARPED_SIZE / 8 
cols, rows = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'], ['1', '2', '3', '4', '5', '6', '7', '8']
CALIB_FILE = "kalibrasi.json"
pts_src = []
warped_matrix = None
window_name = "Smart Chess AI - Trigger System"

# Memuat kalibrasi jika ada
if os.path.exists(CALIB_FILE):
    with open(CALIB_FILE, 'r') as f:
        pts_src = json.load(f)
    if len(pts_src) == 4:
        pts_dst = np.array([[0, 0], [WARPED_SIZE - 1, 0], [WARPED_SIZE - 1, WARPED_SIZE - 1], [0, WARPED_SIZE - 1]], dtype="float32")
        warped_matrix = cv2.getPerspectiveTransform(np.array(pts_src, dtype="float32"), pts_dst)

def select_points(event, x, y, flags, param):
    global pts_src, warped_matrix
    if event == cv2.EVENT_LBUTTONDOWN and warped_matrix is None:
        if len(pts_src) < 4:
            pts_src.append([x, y])
            if len(pts_src) == 4:
                pts_dst = np.array([[0, 0], [WARPED_SIZE - 1, 0], [WARPED_SIZE - 1, WARPED_SIZE - 1], [0, WARPED_SIZE - 1]], dtype="float32")
                warped_matrix = cv2.getPerspectiveTransform(np.array(pts_src, dtype="float32"), pts_dst)
                with open(CALIB_FILE, 'w') as f:
                    json.dump(pts_src, f)

cv2.namedWindow(window_name, cv2.WINDOW_NORMAL)
cv2.setMouseCallback(window_name, select_points)

last_processed_frame = None

# ==============================================================================
# 2. LOOP UTAMA
# ==============================================================================
try:
    while True:
        ret, frame = cap.read()
        if not ret: continue

        trigger_capture = False
        key = cv2.waitKey(1) & 0xFF

        # --- LOGIKA TOMBOL & RESET ---
        if key == 32: trigger_capture = True # Tombol SPACE
        elif key == ord('r'): # Reset Kalibrasi
            print("[INFO] Reset Kalibrasi...")
            if os.path.exists(CALIB_FILE): os.remove(CALIB_FILE)
            pts_src, warped_matrix, last_processed_frame = [], None, None
        elif key == ord('q'): break

        # Trigger dari ESP32
        if esp_serial and esp_serial.in_waiting > 0:
            if esp_serial.readline().decode('utf-8').strip() == "CAPTURE":
                trigger_capture = True

        # --- PROSES DETEKSI ---
        if trigger_capture and warped_matrix is not None:
            warped_frame = cv2.warpPerspective(frame, warped_matrix, (WARPED_SIZE, WARPED_SIZE))
            occupied_squares = set()
            results = model.predict(source=warped_frame, conf=0.5, verbose=False)
            
            for box in results[0].boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                col_idx, row_idx = int(((x1+x2)/2) // step), int(y2 // step)
                if 0 <= col_idx < 8 and 0 <= row_idx < 8:
                    occupied_squares.add(f"{cols[col_idx]}{rows[row_idx]}")

            # Membuat Data String 8x8
            esp32_data = ""
            for i in range(8):
                for j in range(8):
                    start_x, start_y = int(j * step), int(i * step)
                    sq_name = f"{cols[j]}{rows[i]}"
                    if sq_name in occupied_squares:
                        esp32_data += "1 "
                        cv2.rectangle(warped_frame, (start_x, start_y), (int(start_x+step), int(start_y+step)), (0, 255, 0), 3)
                        cv2.putText(warped_frame, sq_name, (start_x+5, start_y+30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)
                    else:
                        esp32_data += "0 "
                        cv2.rectangle(warped_frame, (start_x, start_y), (int(start_x+step), int(start_y+step)), (100, 100, 100), 1)
                esp32_data += "\n"
            # === TAMBAHKAN DUA BARIS INI ===
            print("\n[INFO] Data dikirim ke ESP32:")
            print(esp32_data)
            # ===============================

            if esp_serial: esp_serial.write(esp32_data.encode('utf-8'))
            last_processed_frame = warped_frame.copy()

        # --- VISUALISASI ---
        if warped_matrix is None:
            temp_frame = frame.copy()
            cv2.putText(temp_frame, f"KLIK 4 SUDUT! ({len(pts_src)}/4)", (10, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
            for pt in pts_src: cv2.circle(temp_frame, tuple(pt), 6, (0, 0, 255), -1)
            cv2.imshow(window_name, temp_frame)
        else:
            display = last_processed_frame if last_processed_frame is not None else frame
            cv2.putText(display, "READY - Tekan 'r' untuk Kalibrasi Ulang", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 1)
            cv2.imshow(window_name, display)

except KeyboardInterrupt: pass
finally:
    cap.release()
    if esp_serial: esp_serial.close()
    cv2.destroyAllWindows()