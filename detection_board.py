import cv2
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
# 2. KONFIGURASI CROP & GRID CATUR
# ==============================================================================
# Ini koordinat crop Anda yang kemarin
Y_MIN = 85   # Batas atas
Y_MAX = 405  # Batas bawah
X_MIN = 110  # Batas kiri
X_MAX = 480  # Batas kanan (sebelum tablet)

# Menghitung lebar dan tinggi gambar setelah di-crop
crop_height = Y_MAX - Y_MIN
crop_width = X_MAX - X_MIN

# Menghitung ukuran tiap kotak (8x8)
# Misalnya crop_width 350, maka lebar 1 kotak = 350/8 = 43.75 pixel
step_x = crop_width / 8
step_y = crop_height / 8

# Nama kolom (a-h) dan baris (1-8). 
# CATATAN: Urutan ini tergantung arah kamera Anda melihat papan. 
# Jika terbalik, Anda tinggal membalik urutan list di bawah ini.
cols = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']
rows = ['8', '7', '6', '5', '4', '3', '2', '1'] 

print("\n" + "="*50)
print("=== CHESS GRID DETECTION SYSTEM ===")
print("="*50 + "\n")

prev_frame_time = 0

try:
    while True:
        ret, frame = cap.read()
        if not ret: continue

        frame_cropped = frame[Y_MIN:Y_MAX, X_MIN:X_MAX].copy()
        
        # Array untuk menyimpan daftar kotak yang "Terisi"
        occupied_squares = set()

        # --- DETEKSI AI ---
        results = model.predict(source=frame_cropped, conf=0.5, verbose=False)
        boxes = results[0].boxes
        
        for box in boxes:
            # Ambil koordinat pixel bounding box dari AI
            x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
            
            # Cari titik tengah bidak (X) dan titik dasar bidak (Y)
            # Y menggunakan y2 (garis bawah) karena bagian bawah bidak pasti menyentuh kotaknya
            center_x = (x1 + x2) / 2
            bottom_y = y2 
            
            # --- MATEMATIKA: MENGUBAH PIXEL JADI NAMA KOTAK ---
            col_idx = int(center_x // step_x)
            row_idx = int(bottom_y // step_y)
            
            # Proteksi agar tidak error jika deteksi AI sedikit meleset keluar garis
            if 0 <= col_idx < 8 and 0 <= row_idx < 8:
                square_name = f"{cols[col_idx]}{rows[row_idx]}"
                occupied_squares.add(square_name)
                
                # Gambar titik merah di tengah dasar bidak untuk indikator
                cv2.circle(frame_cropped, (int(center_x), int(bottom_y)), 5, (0, 0, 255), -1)

        # --- MENGGAMBAR GRID 8x8 & MENAMPILKAN STATUS ---
        for i in range(8):
            for j in range(8):
                # Koordinat tiap kotak
                start_x = int(j * step_x)
                start_y = int(i * step_y)
                end_x = int((j + 1) * step_x)
                end_y = int((i + 1) * step_y)
                
                sq_name = f"{cols[j]}{rows[i]}"
                
                # Jika kotak terisi (ada bidak), warnai kotaknya jadi hijau transparan
                if sq_name in occupied_squares:
                    cv2.rectangle(frame_cropped, (start_x, start_y), (end_x, end_y), (0, 255, 0), 2)
                    cv2.putText(frame_cropped, sq_name, (start_x + 5, start_y + 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
                else:
                    # Kotak kosong warnanya abu-abu
                    cv2.rectangle(frame_cropped, (start_x, start_y), (end_x, end_y), (100, 100, 100), 1)

        # Print ke terminal (Bisa dikirim ke Java via Socket / MQTT nantinya)
        # print(f"Kotak Terisi: {sorted(list(occupied_squares))}")

        # Tampilkan FPS
        new_frame_time = time.time()
        fps = 1 / (new_frame_time - prev_frame_time)
        prev_frame_time = new_frame_time
        cv2.putText(frame_cropped, f"FPS: {int(fps)}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)

        cv2.imshow("Smart Chess Grid", frame_cropped)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

except KeyboardInterrupt:
    pass
finally:
    cap.release()
    cv2.destroyAllWindows()