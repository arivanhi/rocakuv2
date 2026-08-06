import cv2
import numpy as np
import time
import json
import os
import serial
from ultralytics import YOLO

# ==============================================================================
# 1. KONFIGURASI KONTROL & SERIAL
# ==============================================================================
ORIENTASI_WARNA = "PUTIH" 

try:
    esp_serial = serial.Serial('COM7', 115200, timeout=0.1) 
    esp_serial.setDTR(False)
    esp_serial.setRTS(False)
    time.sleep(0.1)
    esp_serial.setDTR(True)  
    esp_serial.setRTS(True)  
    print("[INFO] ESP32 (Trigger) terhubung di COM7.")
except Exception as e:
    esp_serial = None
    print(f"[WARNING] ESP32 tidak terdeteksi di COM7. Error: {e}")

try:
    java_serial = serial.Serial('COM10', 115200, timeout=0.1) 
    print("[INFO] Jalur ke Java terhubung di COM10.")
except:
    java_serial = None
    print("[WARNING] Virtual COM10 (ke Java) tidak terdeteksi.")

# ==============================================================================
# 2. KONFIGURASI MODEL & VISUAL
# ==============================================================================
model = YOLO('model/best2_float32.tflite', task='detect') 
cap = cv2.VideoCapture(0)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

WARPED_SIZE = 800 
step = WARPED_SIZE / 8 

cols = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a']
rows = ['1', '2', '3', '4', '5', '6', '7', '8']

CALIB_FILE = "kalibrasi.json"
pts_src = []
warped_matrix = None
window_name = "Smart Chess AI - Poka Yoke System"

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

def mirror_move(move_str):
    if len(move_str) < 4: return move_str
    cols_map = {'a':'h', 'b':'g', 'c':'f', 'd':'e', 'e':'d', 'f':'c', 'g':'b', 'h':'a'}
    rows_map = {'1':'8', '2':'7', '3':'6', '4':'5', '5':'4', '6':'3', '7':'2', '8':'1'}
    try:
        return cols_map[move_str[0]] + rows_map[move_str[1]] + cols_map[move_str[2]] + rows_map[move_str[3]]
    except:
        return move_str

cv2.namedWindow(window_name, cv2.WINDOW_NORMAL)
cv2.setMouseCallback(window_name, select_points)

baseline_state = None 
last_processed_frame = None
pending_single_click = False
robot_color_flag = "1"
is_capture_move = False

click_count = 0
last_click_time = 0
JEDA_DOUBLE_CLICK = 0.5  
DEBOUNCE_TIME = 0.1      

# ==============================================================================
# 3. LOOP UTAMA
# ==============================================================================
try:
    while True:
        ret, frame = cap.read()
        if not ret: continue

        trigger_capture = False
        is_double_click = False
        
        key = cv2.waitKey(1) & 0xFF
        if key == 32:  
            trigger_capture = True
            is_double_click = False
        elif key == ord('b'):  
            trigger_capture = True
            is_double_click = True
        elif key == ord('r'):  
            print("[INFO] Reset Kalibrasi Lintasan...")
            if os.path.exists(CALIB_FILE): os.remove(CALIB_FILE)
            pts_src, warped_matrix, last_processed_frame, baseline_state = [], None, None, None
        elif key == ord('q'): 
            break

        # ==============================================================================
        # --- LOGIKA PENERIMA DATA DARI ESP32 ---
        # ==============================================================================
        if esp_serial:
            while esp_serial.in_waiting > 0: 
                line = esp_serial.readline().decode('utf-8', errors='ignore').strip()
                if not line: continue

                print(f"[DEBUG ESP32] Menerima data: {line}")
                
                if "MAKAN" in line:
                    is_double_click = True
                    trigger_capture = True
                elif "CAPTURE" in line:
                    is_double_click = False
                    trigger_capture = True
                elif line == "OK": 
                    print("\n[INFO] Robot Fisik Selesai Bergerak! Meneruskan 'OK' ke Java.")
                    if java_serial:
                        # PERBAIKAN: Murni hanya "OK" tanpa ada \n yang membuat Java Stuck!
                        java_serial.write("OK".encode('utf-8'))

        # ==============================================================================
        # --- LOGIKA PENERIMA DATA DARI JAVA ---
        # ==============================================================================
        if java_serial and java_serial.in_waiting > 0:
            perintah_dari_java = java_serial.readline().decode('utf-8').strip()
            
            if len(perintah_dari_java) > 0:
                if perintah_dari_java == "RESET_GAME":
                    print("\n[SYSTEM] Perintah RESET dari Java diterima!")
                    baseline_state = None
                    is_capture_move = False
                    if esp_serial: 
                        esp_serial.write((f"RESET_GAME {robot_color_flag}\n").encode('utf-8'))
                
                elif perintah_dari_java == "SET_WARNA:HITAM":
                    print("\n[SYSTEM] Orientasi Papan: HITAM (Robot = PUTIH [0])")
                    cols = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']
                    rows = ['8', '7', '6', '5', '4', '3', '2', '1']
                    robot_color_flag = "0" 
                    baseline_state = None 
                    
                elif perintah_dari_java == "SET_WARNA:PUTIH":
                    print("\n[SYSTEM] Orientasi Papan: PUTIH (Robot = HITAM [1])")
                    cols = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a']
                    rows = ['1', '2', '3', '4', '5', '6', '7', '8']
                    robot_color_flag = "1" 
                    baseline_state = None

                else:
                    print(f"\n[MESIN CATUR] Instruksi Diterima dari Java: {perintah_dari_java}")
                    parts = perintah_dari_java.split()
                    
                    if len(parts) >= 1:
                        move = parts[0] 
                        if robot_color_flag == "0": 
                            robot_move = mirror_move(move) 
                            print(f"[TRANSLATOR] Memutar koordinat {move} menjadi {robot_move} untuk fisik robot.")
                        else:
                            robot_move = move
                            
                        if esp_serial: 
                            esp_serial.write((robot_move + '\n').encode('utf-8'))
                        
                        if len(move) >= 4 and baseline_state is not None:
                            from_sq = move[0:2]
                            to_sq = move[2:4]
                            if from_sq in baseline_state and to_sq in baseline_state:
                                baseline_state[from_sq] = 0
                                baseline_state[to_sq] = 1
                                print(f"[POKA-YOKE] Memori Otomatis Diperbarui: {from_sq} -> {to_sq}")
                        
                        if len(parts) >= 4:
                            robot_color_flag = parts[3]

                    print("[INFO] Menunggu lengan robot selesai bermanuver...")

        # ==============================================================================
        # PROSES ANALISA CITRA & EKSEKUSI POKA-YOKE
        # ==============================================================================
        if trigger_capture and warped_matrix is not None:
            warped_frame = cv2.warpPerspective(frame, warped_matrix, (WARPED_SIZE, WARPED_SIZE))
            occupied_squares = set()
            
            results = model.predict(source=warped_frame, conf=0.5, verbose=False)
            
            for box in results[0].boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                col_idx = int(((x1 + x2) / 2) // step)
                row_idx = int(y2 // step)
                if 0 <= col_idx < 8 and 0 <= row_idx < 8:
                    occupied_squares.add(f"{cols[col_idx]}{rows[row_idx]}")

            current_state = {f"{c}{r}": (1 if f"{c}{r}" in occupied_squares else 0) for r in rows for c in cols}

            if is_double_click:
                if baseline_state is not None:
                    target_makan = None
                    for sq in current_state:
                        if baseline_state[sq] == 1 and current_state[sq] == 0:
                            target_makan = sq
                            break
                    
                    if target_makan:
                        baseline_state[target_makan] = 0 
                        print(f"\n[POKA-YOKE] Mode Memakan: Bidak lawan di {target_makan} telah diangkat.")
                    else:
                        baseline_state = current_state.copy()
                        print("\n[POKA-YOKE] Baseline Diperbarui (Papan Di-reset manual).")
                else:
                    baseline_state = current_state.copy()
            else:
                if baseline_state is None:
                    baseline_state = current_state.copy()
                    print("\n[INFO] Baseline awal terbentuk otomatis.")
                else:
                    from_squares = []
                    to_squares = []

                    for sq in current_state:
                        if baseline_state[sq] == 1 and current_state[sq] == 0:
                            from_squares.append(sq)
                        elif baseline_state[sq] == 0 and current_state[sq] == 1:
                            to_squares.append(sq)

                    if len(from_squares) == 1 and len(to_squares) == 1:
                        move_string = f"{from_squares[0]}{to_squares[0]}"
                        print(f"\n[POKA-YOKE] Mengirim Langkah ke Java: {move_string}")
                        
                        if java_serial:
                            java_serial.write(move_string.encode('utf-8'))
                            
                        if esp_serial:
                            esp_move = mirror_move(move_string) if robot_color_flag == "0" else move_string
                            esp_serial.write(f"HUMAN {esp_move}\n".encode('utf-8'))
                            print(f"[SYSTEM] Menyinkronkan memori robot: HUMAN {esp_move}")
                            
                        baseline_state = current_state.copy()
                    else:
                        print("\n[PERINGATAN POKA-YOKE] Perubahan kotak tidak sinkron!")
                        print(f"Kotak Lepas: {from_squares} | Kotak Tekan: {to_squares}")
                        print("[SOLUSI] Jika sedang makan bidak: Angkat lawan -> Klik MAKAN -> Taruh bidak Anda -> Klik CAPTURE.")

            for i in range(8):
                for j in range(8):
                    start_x, start_y = int(j * step), int(i * step)
                    sq_name = f"{cols[j]}{rows[i]}"
                    if sq_name in occupied_squares:
                        cv2.rectangle(warped_frame, (start_x, start_y), (int(start_x+step), int(start_y+step)), (0, 255, 0), 2)
                        cv2.putText(warped_frame, sq_name, (start_x+5, start_y+25), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
                    else:
                        cv2.rectangle(warped_frame, (start_x, start_y), (int(start_x+step), int(start_y+step)), (100, 100, 100), 1)
            
            last_processed_frame = warped_frame.copy()

        if warped_matrix is None:
            temp_frame = frame.copy()
            cv2.putText(temp_frame, f"KALIBRASI: KLIK 4 SUDUT PAPAN! ({len(pts_src)}/4)", (10, 40), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
            for pt in pts_src: cv2.circle(temp_frame, tuple(pt), 6, (0, 0, 255), -1)
            cv2.imshow(window_name, temp_frame)
        else:
            display = last_processed_frame if last_processed_frame is not None else frame
            cv2.putText(display, "SISTEM READY | Putih=Manusia, Hitam=Robot", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 1)
            cv2.imshow(window_name, display)

except KeyboardInterrupt: pass
finally:
    cap.release()
    if esp_serial: esp_serial.close()
    if java_serial: java_serial.close()
    cv2.destroyAllWindows()