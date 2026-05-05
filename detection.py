import cv2
import time
from ultralytics import YOLO

# ==============================================================================
# 1. KONFIGURASI MODEL & KAMERA
# ==============================================================================
print("[INFO] Memuat model TFLite...")
# Kita harus secara eksplisit mendefinisikan task='detect' untuk TFLite
model = YOLO('model/best2_float32.tflite', task='detect') 

print("[INFO] Membuka kamera RGB...")
cam_index = 0
cap = cv2.VideoCapture(cam_index)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

if not cap.isOpened():
    print("[ERROR] Kamera tidak terdeteksi!")
    exit()

# ==============================================================================
# 2. KONFIGURASI CROP (SESUAI DENGAN DATASET)
# ==============================================================================
Y_MIN = 85   # Batas atas
Y_MAX = 405  # Batas bawah
X_MIN = 110  # Batas kiri
X_MAX = 480  # Batas kanan (sebelum tablet)


print("\n" + "="*50)
print("=== LIVE CHESS DETECTION (YOLOv8 TFLite) ===")
print("Tekan tombol 'q' pada keyboard untuk keluar.")
print("="*50 + "\n")

# Variabel untuk menghitung FPS
prev_frame_time = 0

try:
    while True:
        ret, frame = cap.read()
        if not ret:
            print("[WARNING] Gagal membaca frame dari kamera.")
            continue

        # 1. Potong (Crop) frame agar fokus ke papan catur saja
        frame_cropped = frame[Y_MIN:Y_MAX, X_MIN:X_MAX]

        # 2. Lakukan deteksi objek
        # conf=0.5 berarti hanya menampilkan hasil dengan keyakinan di atas 50%
        # verbose=False agar terminal tidak penuh dengan log setiap detiknya
        results = model.predict(source=frame_cropped, conf=0.5, verbose=False)

        # 3. Gambarkan kotak deteksi ke atas gambar
        annotated_frame = results[0].plot()

        # 4. Hitung dan Tampilkan FPS (Frames Per Second) di pojok kiri atas
        new_frame_time = time.time()
        fps = 1 / (new_frame_time - prev_frame_time)
        prev_frame_time = new_frame_time
        fps_text = f"FPS: {int(fps)}"
        cv2.putText(annotated_frame, fps_text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2, cv2.LINE_AA)

        # 5. Tampilkan hasil *live stream* ke jendela Windows
        cv2.imshow("Live Chess Detection - Orbbec Astra", annotated_frame)

        # 6. Tombol 'q' untuk mematikan program
        if cv2.waitKey(1) & 0xFF == ord('q'):
            print("[INFO] Menutup program...")
            break

except KeyboardInterrupt:
    print("\n[INFO] Program dihentikan paksa (Ctrl+C).")

finally:
    # Selalu pastikan kamera dilepas dengan aman
    cap.release()
    cv2.destroyAllWindows()
    print("[INFO] Selesai.")