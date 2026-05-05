from openni import openni2
import numpy as np
import cv2
import os
import time
import sys
import re

# ==============================================================================
# 1. KONFIGURASI PATH OPENNI2
# ==============================================================================
# WAJIB UBAH BARIS DI BAWAH INI sesuai dengan lokasi folder 'bin' Astra SDK Anda!
openni_redist_path = r"C:\\Users\\H10\\Documents\\AstraSDK-v2.1.3\\AstraSDK\\bin"

# ==============================================================================
# 2. KONFIGURASI CROP / FRAMING KOTAK PAPAN CATUR
# ==============================================================================
# Ubah angka ini untuk menggeser kotak (Batas maksimal Y=480, X=640)
Y_MIN = 85   # Batas atas
Y_MAX = 405  # Batas bawah
X_MIN = 110  # Batas kiri
X_MAX = 480  # Batas kanan (sebelum tablet)

# ==============================================================================
# 3. KONFIGURASI FOLDER (SMART INDEXING & SORTING)
# ==============================================================================
save_folder = "data_new"
folder_rgb = os.path.join(save_folder, "rgb")
folder_ir = os.path.join(save_folder, "ir")
folder_depth = os.path.join(save_folder, "depth")

for folder in [folder_rgb, folder_ir, folder_depth]:
    if not os.path.exists(folder):
        os.makedirs(folder)
        print(f"[INFO] Folder '{folder}' berhasil dibuat.")

def get_highest_image_index(folder_path):
    max_idx = 0
    if os.path.exists(folder_path):
        for filename in os.listdir(folder_path):
            match = re.search(r'img_(\d+)\.jpg', filename)
            if match:
                num = int(match.group(1))
                if num > max_idx:
                    max_idx = num
    return max_idx

# ==============================================================================
# 4. INISIALISASI KAMERA (INFRARED, DEPTH & RGB)
# ==============================================================================
print("\n[INFO] Menginisialisasi OpenNI2 (Kamera IR & Depth)...")
try:
    openni2.initialize(openni_redist_path)
    dev = openni2.Device.open_any()
    ir_stream = dev.create_ir_stream()
    ir_stream.start()
    depth_stream = dev.create_depth_stream()
    depth_stream.start()
    print("[INFO] Stream Infrared dan Depth berhasil terhubung!")
except Exception as e:
    print(f"[ERROR] Gagal inisialisasi sensor Orbbec.\nDetail: {e}")
    sys.exit()

print("\n[INFO] Menginisialisasi OpenCV (Kamera RGB)...")
cap = cv2.VideoCapture(0)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

if not cap.isOpened():
    print("[ERROR] Kamera RGB tidak terdeteksi!")
    ir_stream.stop()
    depth_stream.stop()
    openni2.unload()
    sys.exit()
print("[INFO] Kamera RGB berhasil terhubung!")

# ==============================================================================
# 5. PROGRAM UTAMA CAPTURE
# ==============================================================================
total_photos = 1
capture_delay = 0.5 

print("\n" + "="*60)
print("=== PROGRAM ULTIMATE CAPTURE + AUTO CROP (RGB+IR+DEPTH) ===")
print("="*60)

try:
    while True:
        start_idx = get_highest_image_index(folder_rgb)
        print(f"\n[INFO] Index dataset terakhir: {start_idx}")
        user_input = input(f"=> Tekan ENTER untuk merekam {total_photos} set foto, atau 'q' untuk keluar: ")
        
        if user_input.strip().lower() == 'q':
            break
            
        print(f"\nMemulai sesi foto... (Mulai dari indeks {start_idx+1:03d})")
        
        for i in range(total_photos):
            photo_num = start_idx + i + 1
            
            # --- 1. TANGKAP & CROP RGB ---
            ret, frame_rgb = cap.read()
            if not ret:
                continue
            # Memotong gambar RGB sesuai koordinat
            frame_rgb_cropped = frame_rgb[Y_MIN:Y_MAX, X_MIN:X_MAX]
                
            # --- 2. TANGKAP & CROP INFRARED ---
            frame_ir_raw = ir_stream.read_frame()
            frame_ir_data = frame_ir_raw.get_buffer_as_uint16()
            img_ir = np.ndarray((frame_ir_raw.height, frame_ir_raw.width), dtype=np.uint16, buffer=frame_ir_data)
            img_ir_8bit = cv2.normalize(img_ir, None, 0, 255, cv2.NORM_MINMAX, dtype=cv2.CV_8U)
            img_ir_8bit = cv2.flip(img_ir_8bit, 1)
            # Memotong gambar IR
            img_ir_cropped = img_ir_8bit[Y_MIN:Y_MAX, X_MIN:X_MAX]
            
            # --- 3. TANGKAP & CROP DEPTH ---
            frame_depth_raw = depth_stream.read_frame()
            frame_depth_data = frame_depth_raw.get_buffer_as_uint16()
            img_depth_16bit = np.ndarray((frame_depth_raw.height, frame_depth_raw.width), dtype=np.uint16, buffer=frame_depth_data)
            img_depth_16bit = cv2.flip(img_depth_16bit, 1)
            img_depth_8bit = cv2.normalize(img_depth_16bit, None, 0, 255, cv2.NORM_MINMAX, dtype=cv2.CV_8U)
            # Memotong gambar Depth
            img_depth_cropped = img_depth_8bit[Y_MIN:Y_MAX, X_MIN:X_MAX]
            
            # --- SIMPAN KETIGA GAMBAR YANG SUDAH DI-CROP ---
            filename_rgb = os.path.join(folder_rgb, f"img_{photo_num:03d}.jpg")
            filename_ir = os.path.join(folder_ir, f"img_{photo_num:03d}.png")
            filename_depth = os.path.join(folder_depth, f"img_{photo_num:03d}.png") 
            
            cv2.imwrite(filename_rgb, frame_rgb_cropped)
            cv2.imwrite(filename_ir, img_ir_cropped)
            cv2.imwrite(filename_depth, img_depth_cropped)
            
            print(f"[{i+1}/{total_photos}] Sukses menyimpan set gambar (Cropped): img_{photo_num:03d}")
            time.sleep(capture_delay)

        print(f"\n[SUCCESS] Sesi selesai! File terakhir adalah indeks {start_idx + total_photos:03d}.")

except KeyboardInterrupt:
    print("\n\nProgram dibatalkan secara paksa oleh user (CTRL+C).")

finally:
    print("\n[INFO] Membersihkan resource kamera...")
    cap.release()
    try:
        ir_stream.stop()
        depth_stream.stop()
    except:
        pass
    openni2.unload()
    print("[INFO] Resource kamera telah aman dilepas. Sampai jumpa!")