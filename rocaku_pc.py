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
# Fokus saat ini: Manusia = PUTIH (a1 di pojok kanan atas / sesuai kalibrasi Anda)
# Nanti tinggal dihubungkan dengan switch hardware dari ESP32
ORIENTASI_WARNA = "PUTIH" 

try:
    esp_serial = serial.Serial('COM3', 115200, timeout=0.1) 
    # =================================================================
    # WAJIB UNTUK CIRCUITPYTHON: Beritahu ESP32 bahwa PC sedang standby
    esp_serial.dtr = True  
    esp_serial.rts = True  
    # =================================================================
    print("[INFO] ESP32 (Trigger) terhubung di COM3.")
except:
    esp_serial = None
    print("[WARNING] ESP32 tidak terdeteksi di COM3.")

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

# Susunan koordinat standar untuk manusia warna Putih
cols = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a']
rows = ['1', '2', '3', '4', '5', '6', '7', '8']

CALIB_FILE = "kalibrasi.json"
pts_src = []
warped_matrix = None
window_name = "Smart Chess AI - Poka Yoke System"

# Memuat kalibrasi perspektif
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
# ==============================================================================
# FUNGSI PENERJEMAH CERMIN UNTUK ESP32
# ==============================================================================
def mirror_move(move_str):
    """ Membalik koordinat 180 derajat untuk ESP32 jika fisik papan dibalik """
    if len(move_str) < 4: return move_str
    cols_map = {'a':'h', 'b':'g', 'c':'f', 'd':'e', 'e':'d', 'f':'c', 'g':'b', 'h':'a'}
    rows_map = {'1':'8', '2':'7', '3':'6', '4':'5', '5':'4', '6':'3', '7':'2', '8':'1'}
    try:
        return cols_map[move_str[0]] + rows_map[move_str[1]] + cols_map[move_str[2]] + rows_map[move_str[3]]
    except:
        return move_str

cv2.namedWindow(window_name, cv2.WINDOW_NORMAL)
cv2.setMouseCallback(window_name, select_points)

# Memory State untuk mengunci posisi papan (Poka-Yoke Core)
baseline_state = None 
last_processed_frame = None

# Variabel pembantu deteksi Double Click via Hardware Button
pending_single_click = False

robot_color_flag = "1"
is_capture_move = False

# TAMBAHAN VARIABEL KONTROL KLIK
click_count = 0
last_click_time = 0
JEDA_DOUBLE_CLICK = 0.5  # Batas waktu tunggu klik berikutnya (0.5 detik)
DEBOUNCE_TIME = 0.1      # Filter anti-noise tombol mekanik (0.1 detik)

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
        # --- TRIGGER KEYBOARD COMPUTER (UNTUK UJI COBA) ---
        if key == 32:  # Tombol SPACE = Langkah Biasa / Selesai Makan (Single Click)
            trigger_capture = True
            is_double_click = False
        elif key == ord('b'):  # Tombol 'b' = Angkat Lawan / Reset Baseline (Double Click)
            trigger_capture = True
            is_double_click = True
        elif key == ord('r'):  # Reset total kalibrasi gambar
            print("[INFO] Reset Kalibrasi Lintasan...")
            if os.path.exists(CALIB_FILE): os.remove(CALIB_FILE)
            pts_src, warped_matrix, last_processed_frame, baseline_state = [], None, None, None
        elif key == ord('q'): 
            break

        # ==============================================================================
        # --- LOGIKA PENERIMA DATA DARI ESP32 (TOMBOL & BALASAN GERAK ROBOT) ---
        # ==============================================================================
        if esp_serial:
            while esp_serial.in_waiting > 0: 
                line = esp_serial.readline().decode('utf-8', errors='ignore').strip()
                if not line: continue
                
                if "MAKAN" in line:
                    is_double_click = True
                    trigger_capture = True
                    
                elif "CAPTURE" in line:
                    is_double_click = False
                    trigger_capture = True
                    
                # [PERBAIKAN] Gunakan == agar tidak tertipu teks "Mengirim OK ke PC..."
                elif line == "OK": 
                    print("\n[INFO] Robot Fisik Selesai Bergerak! Meneruskan 'OK' ke Java.")
                    if java_serial:
                        # [PERBAIKAN] Hapus \n agar Java tidak stuck
                        java_serial.write("OK".encode('utf-8'))
        # ==============================================================================
        # --- LOGIKA PENERIMA DATA DARI JAVA (GERAKAN ROBOT, RESET, & WARNA) ---
        # ==============================================================================
        if java_serial and java_serial.in_waiting > 0:
            perintah_dari_java = java_serial.readline().decode('utf-8').strip()
            
            if len(perintah_dari_java) > 0:
                
                # Skenario 1: Reset Papan dari tombol SET START
                if perintah_dari_java == "RESET_GAME":
                    print("\n[SYSTEM] Perintah RESET dari Java diterima!")
                    baseline_state = None
                    is_capture_move = False
                    # [PERBAIKAN] Harus RESET_GAME agar memori ESP32 ikut kembali ke posisi awal
                    if esp_serial: esp_serial.write(("RESET_GAME\n").encode('utf-8'))
                
                # Skenario 2: Pilihan Warna Hitam dari GUI
                elif perintah_dari_java == "SET_WARNA:HITAM":
                    print("\n[SYSTEM] Orientasi Papan: HITAM (Robot = PUTIH [0])")
                    # ARRAY DIBALIK UNTUK KAMERA (Manusia di baris 7 & 8)
                    cols = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']
                    rows = ['8', '7', '6', '5', '4', '3', '2', '1']
                    robot_color_flag = "0" # Robot otomatis jadi putih
                    baseline_state = None 
                    
                # Skenario 3: Pilihan Warna Putih dari GUI
                elif perintah_dari_java == "SET_WARNA:PUTIH":
                    print("\n[SYSTEM] Orientasi Papan: PUTIH (Robot = HITAM [1])")
                    # ARRAY NORMAL UNTUK KAMERA (Manusia di baris 1 & 2)
                    cols = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a']
                    rows = ['1', '2', '3', '4', '5', '6', '7', '8']
                    robot_color_flag = "1" # Robot otomatis jadi hitam
                    baseline_state = None

                # Skenario 4: Instruksi Jalan dari Stockfish
                else:
                    print(f"\n[MESIN CATUR] Instruksi Diterima dari Java: {perintah_dari_java}")
                    parts = perintah_dari_java.split()
                    
                    if len(parts) >= 1:
                        move = parts[0] # contoh: e2e4
                        
                        # --- PENERJEMAH KE FISIK ESP32 ---
                        if robot_color_flag == "0": # Jika robot main Putih
                            robot_move = mirror_move(move) # Ubah e2e4 jadi d7d5
                            print(f"[TRANSLATOR] Memutar koordinat {move} menjadi {robot_move} untuk fisik robot.")
                        else:
                            robot_move = move
                            
                        # Kirim koordinat yang sudah diterjemahkan ke ESP32
                        if esp_serial: 
                            esp_serial.write((robot_move + '\n').encode('utf-8'))
                        
                        # --- UPDATE MEMORI KAMERA ---
                        # (Kamera SELALU memakai koordinat Java asli agar Poka-Yoke tidak bingung)
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
        # ELEMEN KENDALI LANGKAH MANUSIA (2 TOMBOL: MAKAN & OK)
        # ==============================================================================
        if trigger_capture and warped_matrix is not None:
            # (Proses YOLO - Tidak berubah)
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

            # --- JIKA TOMBOL "MAKAN / RESET" DITEKAN (Di PC pakai 'b', dari ESP misal "MAKAN") ---
            if is_double_click:
                if baseline_state is not None:
                    target_makan = None
                    # Cari kotak yang bidaknya baru saja diangkat (1 menjadi 0)
                    for sq in current_state:
                        if baseline_state[sq] == 1 and current_state[sq] == 0:
                            target_makan = sq
                            break
                    
                    if target_makan:
                        # Kosongkan bidak musuh dari ingatan kamera
                        baseline_state[target_makan] = 0 
                        print(f"\n[POKA-YOKE] Mode Memakan: Bidak lawan di {target_makan} telah diangkat.")
                    else:
                        # Jika tidak ada yang diangkat, berfungsi sebagai Reset Papan (Kalibrasi manual)
                        baseline_state = current_state.copy()
                        print("\n[POKA-YOKE] Baseline Diperbarui (Papan Di-reset manual).")
                else:
                    baseline_state = current_state.copy()

            # --- JIKA TOMBOL "OK / CAPTURE" DITEKAN (Di PC pakai Space, dari ESP misal "CAPTURE") ---
            else:
                if baseline_state is None:
                    baseline_state = current_state.copy()
                else:
                    from_squares = []
                    to_squares = []

                    for sq in current_state:
                        if baseline_state[sq] == 1 and current_state[sq] == 0:
                            from_squares.append(sq)
                        elif baseline_state[sq] == 0 and current_state[sq] == 1:
                            to_squares.append(sq)

                    # Validasi Poka-Yoke: Harus ada 1 kotak asal dan 1 kotak tujuan
                    if len(from_squares) == 0 and len(to_squares) == 0:
                        # Abaikan sinyal gaib akibat pantulan (noise) mekanik tombol
                        pass 
                    
                    elif len(from_squares) == 1 and len(to_squares) == 1:
                        # FORMAT MURNI: Hanya 4 karakter tanpa embel-embel angka!
                        move_string = f"{from_squares[0]}{to_squares[0]}"
                        print(f"\n[POKA-YOKE] Mengirim Langkah ke Java: {move_string}")
                        if java_serial:
                            # TAMBAHKAN \n DI SINI JUGA
                            java_serial.write((move_string).encode('utf-8'))
                            
                        # ==================================================
                        # TAMBAHAN BARU: UPDATE MEMORI ESP32 SECARA DIAM-DIAM
                        # ==================================================
                        if esp_serial:
                            esp_serial.write(f"HUMAN {move_string}\n".encode('utf-8'))
                            print(f"[SYSTEM] Menyinkronkan memori robot: HUMAN {move_string}")
                        # ==================================================
                        baseline_state = current_state.copy()
                        
                    else:
                        print("\n[PERINGATAN POKA-YOKE] Perubahan kotak tidak sinkron!")
                        print(f"Lepas: {from_squares} | Taruh: {to_squares}")

        # ==============================================================================
        # 4. PROSES ANALISA CITRA & LOGIKA PERBANDINGAN MATRIKS
        # ==============================================================================
        if trigger_capture and warped_matrix is not None:
            warped_frame = cv2.warpPerspective(frame, warped_matrix, (WARPED_SIZE, WARPED_SIZE))
            occupied_squares = set()
            
            # Jalankan AI prediksi objek
            results = model.predict(source=warped_frame, conf=0.5, verbose=False)
            
            for box in results[0].boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                col_idx = int(((x1 + x2) / 2) // step)
                row_idx = int(y2 // step)
                if 0 <= col_idx < 8 and 0 <= row_idx < 8:
                    occupied_squares.add(f"{cols[col_idx]}{rows[row_idx]}")

            # Mapping keadaan fisik nyata saat ini ke bentuk Map (0 dan 1)
            current_state = {}
            for r in rows:
                for c in cols:
                    sq_name = f"{c}{r}"
                    current_state[sq_name] = 1 if sq_name in occupied_squares else 0

            # --- EKSEKUSI SCENARIO POKA-YOKE ---
            if is_double_click:
                # SCENARIO B (Part 1) & SCENARIO C: Ambil koordinat acuan baru (Baseline)
                baseline_state = current_state.copy()
                print("\n[POKA-YOKE] >>> Baseline Diperbarui (Papan Di-reset / Bidak Lawan Telah Diangkat) <<<")
            else:
                # SCENARIO A & SCENARIO B (Part 2): Bandingkan posisi baru dengan baseline lama
                if baseline_state is None:
                    # Pengaman jika pertama kali menyala belum ada memori dasar
                    baseline_state = current_state.copy()
                    print("\n[INFO] Baseline awal terbentuk otomatis. Silakan lakukan langkah pertama Anda.")
                else:
                    from_squares = []
                    to_squares = []

                    # Cari perbedaan biner antara baseline dan kondisi sekarang
                    for sq in current_state:
                        if baseline_state[sq] == 1 and current_state[sq] == 0:
                            from_squares.append(sq) # Bidak pergi dari kotak ini
                        elif baseline_state[sq] == 0 and current_state[sq] == 1:
                            to_squares.append(sq) # Bidak datang ke kotak ini

                    # VALIDASI ANTI-SALAH: Langkah sah harus memiliki 1 kotak asal dan 1 kotak tujuan
                    if len(from_squares) == 1 and len(to_squares) == 1:
                        move_string = f"{from_squares[0]}{to_squares[0]}"
                        print(f"\n[POKA-YOKE] Langkah Sah Terdeteksi: {move_string.strip()}")
                        
                        # LEMPAR KOORDINAT MATANG KE JAVA NETBEANS!
                        if java_serial:
                            java_serial.write(move_string.encode('utf-8'))
                        
                        # Perbarui baseline untuk langkah normal berikutnya
                        baseline_state = current_state.copy()
                    else:
                        print("\n[PERINGATAN POKA-YOKE] Perubahan kotak tidak sinkron!")
                        print(f"Kotak Lepas (From): {from_squares} | Kotak Tekan (To): {to_squares}")
                        print("[SOLUSI] Jika Anda sedang memakan bidak, pastikan langkahnya: Angkat lawan -> Klik 2x -> Taruh bidak Anda -> Klik 1x.")

            # Menggambar ulang kotak grafis visual AI di layar monitor
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

        # --- MANAGEMENT ANTARMUKA MONITOR ---
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