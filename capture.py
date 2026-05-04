import cv2
import os
import time
import re  # Modul untuk membaca pola teks/angka

# Konfigurasi Folder
save_folder = "data"
if not os.path.exists(save_folder):
    os.makedirs(save_folder)
    print(f"Folder '{save_folder}' berhasil dibuat.")

def get_highest_image_index(folder_path):
    """Mencari angka tertinggi dari file img_XXX.jpg di folder"""
    max_idx = 0
    for filename in os.listdir(folder_path):
        # Mencari pola 'img_' diikuti angka, diakhiri '.jpg'
        match = re.search(r'img_(\d+)\.jpg', filename)
        if match:
            num = int(match.group(1))
            if num > max_idx:
                max_idx = num
    return max_idx

# Inisialisasi Kamera
cam_index = 0
print("Mencoba terhubung ke kamera...")
cap = cv2.VideoCapture(cam_index)

# Set Resolusi
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

if not cap.isOpened():
    print("Kamera tidak terdeteksi! Pastikan Orbbec Astra tercolok dengan benar.")
    exit()

print("Kamera berhasil terhubung!")

# Variabel Kontrol
total_photos = 25
capture_delay = 0.5  # Jeda 0.5 detik antar foto

print("\n=== PROGRAM CAPTURE DATASET CATUR (SMART INDEXING) ===")

try:
    while True:
        # Cek index terakhir SETIAP KALI mau mulai sesi baru
        start_idx = get_highest_image_index(save_folder)
        
        print(f"\n[INFO] Index foto terakhir di folder: {start_idx}")
        user_input = input("=> Tekan ENTER untuk capture 25 foto, atau ketik 'q' lalu ENTER untuk keluar: ")
        
        # Cek apakah user ingin keluar
        if user_input.strip().lower() == 'q':
            print("\nKeluar dari program...")
            break
            
        print(f"\nMemulai sesi foto... (Mulai dari img_{start_idx+1:03d}.jpg)")
        
        for i in range(total_photos):
            ret, frame = cap.read()
            if not ret:
                print("Gagal mengambil frame dari kamera, berhenti.")
                break
                
            photo_num = start_idx + i + 1
            filename = os.path.join(save_folder, f"img_{photo_num:03d}.jpg")
            cv2.imwrite(filename, frame)
            
            print(f"[{i+1}/{total_photos}] Berhasil menyimpan {filename}")
            
            # Jeda agar Anda punya waktu menggeser bidak/tangan jika diperlukan
            time.sleep(capture_delay)

        print(f"\nSelesai! Sesi ini berhasil ditambahkan. File terakhir adalah img_{start_idx + total_photos:03d}.jpg")

except KeyboardInterrupt:
    print("\n\nProgram dibatalkan oleh user (CTRL+C).")

finally:
    # Selalu bersihkan resource kamera saat selesai atau error
    cap.release()
    print("Resource kamera telah dilepas.")