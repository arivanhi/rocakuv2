from openni import openni2
import numpy as np
import cv2
import sys
import os
import time

# ==============================================================================
# 1. KONFIGURASI PATH OPENNI2
# WAJIB UBAH BARIS DI BAWAH INI sesuai dengan lokasi folder 'bin' Astra SDK Anda!
# ==============================================================================
openni_redist_path = r"C:\\Users\\H10\\Documents\\AstraSDK-v2.1.3\\AstraSDK\\bin"

# ==============================================================================
# 2. INISIALISASI KAMERA
# ==============================================================================
try:
    print("[INFO] Menginisialisasi OpenNI2...")
    openni2.initialize(openni_redist_path)
except Exception as e:
    print(f"[ERROR] Gagal inisialisasi OpenNI2.\nPastikan path folder 'bin' sudah benar!\nDetail: {e}")
    sys.exit()

try:
    print("[INFO] Mencari kamera Orbbec...")
    dev = openni2.Device.open_any()
    print("[INFO] Kamera berhasil ditemukan dan terhubung!")
except Exception as e:
    print("[ERROR] Kamera tidak terdeteksi oleh OpenNI2.\nPastikan tidak ada aplikasi lain (seperti SimpleStreamViewer) yang sedang membuka kamera.\nDetail:", e)
    sys.exit()

# ==============================================================================
# 3. MEMBUKA SENSOR INFRARED
# ==============================================================================
try:
    ir_stream = dev.create_ir_stream()
    ir_stream.start()
    print("[INFO] Stream Infrared berhasil dinyalakan.")
except Exception as e:
    print(f"[ERROR] Gagal membuka stream IR: {e}")
    sys.exit()

# Buat folder untuk menyimpan dataset jika belum ada
save_dir = "dataset_ir_orbbec"
if not os.path.exists(save_dir):
    os.makedirs(save_dir)
    print(f"[INFO] Membuat folder penyimpanan baru: '{save_dir}'")

# ==============================================================================
# 4. LOOP PENGAMBILAN GAMBAR (HEADLESS / SSH MODE)
# ==============================================================================
print("-" * 50)
print(">>> MULAI MENYIMPAN DATA INFRARED <<<")
print(f"Menyimpan ke folder: {save_dir}")
print("TEKAN [CTRL + C] PADA TERMINAL UNTUK BERHENTI.")
print("-" * 50)

count = 0
try:
    while True:
        # Baca frame dari sensor IR
        frame = ir_stream.read_frame()
        frame_data = frame.get_buffer_as_uint16()
        
        # Konversi data mentah (raw) ke gambar Numpy array 2D
        img = np.ndarray((frame.height, frame.width), dtype=np.uint16, buffer=frame_data)
        
        # Normalisasi dari 16-bit ke 8-bit (0-255) agar bisa disimpan sebagai PNG standar
        img_8bit = cv2.normalize(img, None, 0, 255, cv2.NORM_MINMAX, dtype=cv2.CV_8U)
        img_8bit = cv2.flip(img_8bit, 1)
        # Buat nama file berurutan (contoh: ir_frame_0000.png, ir_frame_0001.png)
        filename = os.path.join(save_dir, f"ir_frame_{count:04d}.png")
        
        
        # Simpan gambar
        cv2.imwrite(filename, img_8bit)
        
        # Tampilkan status di terminal SSH setiap 10 frame agar tidak terlalu spam
        if count % 10 == 0:
            print(f"[{count:04d}] Berhasil menyimpan {filename} (Resolusi: {frame.width}x{frame.height})")
            
        count += 1
        
        # Jeda sangat singkat (10ms) agar CPU tidak 100% dan memberi napas pada sistem
        time.sleep(0.01) 

except KeyboardInterrupt:
    # Menangkap interupsi Ctrl + C dari user
    print("\n[INFO] Proses dihentikan oleh user (Ctrl + C).")

# ==============================================================================
# 5. PEMBERSIHAN (WAJIB AGAR KAMERA TIDAK HANG PADA RUN BERIKUTNYA)
# ==============================================================================
finally:
    print("[INFO] Menutup stream dan membersihkan memori...")
    try:
        ir_stream.stop()
    except:
        pass
    
    openni2.unload()
    print(f"[SUCCESS] Selesai! Total gambar tersimpan: {count} frame.")
    print("Kamera aman dilepas atau digunakan kembali.")