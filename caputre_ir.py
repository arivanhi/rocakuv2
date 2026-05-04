from openni import openni2
import numpy as np
import cv2
import sys

# 1. Path diarahkan langsung ke folder 'libs' tempat OpenNI2.dll berada
openni_redist_path = r"C:\\Users\\H10\\Documents\\AstraSDK-v2.1.3\\AstraSDK\\bin"


try:
    print("Inisialisasi OpenNI2...")
    openni2.initialize(openni_redist_path)
except Exception as e:
    print(f"Gagal inisialisasi OpenNI2. Pastikan path folder benar! Error: {e}")
    sys.exit()

print("Mencari kamera...")
try:
    dev = openni2.Device.open_any()
    print("Kamera berhasil ditemukan!")
except Exception as e:
    print("Kamera tidak terdeteksi oleh OpenNI2. Pastikan driver terinstal dan kabel tercolok.")
    sys.exit()

# Membuka sensor Infrared
try:
    ir_stream = dev.create_ir_stream()
    ir_stream.start()
    print("Menampilkan stream Infrared. Tekan 'q' pada keyboard untuk keluar.")
except Exception as e:
    print(f"Gagal membuka stream IR: {e}")
    sys.exit()

try:
    while True:
        frame = ir_stream.read_frame()
        frame_data = frame.get_buffer_as_uint16()
        
        # Konversi data mentah ke gambar Numpy 2D
        img = np.ndarray((frame.height, frame.width), dtype=np.uint16, buffer=frame_data)
        
        # Normalisasi ke 8-bit agar bisa ditampilkan dengan OpenCV
        img_8bit = cv2.normalize(img, None, 0, 255, cv2.NORM_MINMAX, dtype=cv2.CV_8U)
        
        cv2.imshow("IR Stream - Astra Pro Plus (OpenNI2)", img_8bit)
        
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

finally:
    try:
        ir_stream.stop()
    except:
        pass
    openni2.unload()
    cv2.destroyAllWindows()
    print("Stream dihentikan dengan aman.")