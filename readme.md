# Smart Chess Board AI - Computer Vision Backend ♟️🤖

Sistem pendeteksi papan catur pintar berbasis AI (YOLOv8 TFLite) dan *Computer Vision* (OpenCV). Sistem ini membaca aliran video langsung dari kamera Orbbec Astra, melakukan kalibrasi perspektif (*Bird's-eye view*), mendeteksi keberadaan bidak di atas papan 8x8, dan menghasilkan matriks biner (1/0) secara *real-time* untuk dikirimkan ke mikrokontroler (ESP32).

## 📋 Fitur Utama
- **Perspective Transformation:** Kalibrasi 4 titik sudut papan secara manual dengan memori otomatis (tersimpan di `kalibrasi.json`).
- **YOLOv8 Nano (TFLite):** Deteksi objek sangat ringan dan cepat (FPS tinggi) tanpa memerlukan GPU kelas berat.
- **8x8 Grid Mapping:** Memetakan koordinat *bounding box* AI secara matematis ke dalam 64 kotak catur.
- **ESP32 Data Stream:** *Output* berupa *string* matriks 8x8 biner (1 = Terisi, 0 = Kosong) yang siap ditransmisikan via Serial/UART.

---

## 🛠️ Persyaratan Sistem (Prerequisites)

Sebelum menginstal pustaka Python, **SANGAT PENTING** untuk menyiapkan *environment* Windows Anda karena kita menggunakan kamera *depth/RGB* khusus:

1. **Visual Studio (C++ Build Tools):**
   - Unduh dan instal [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/).
   - Saat proses instalasi, pastikan Anda **mencentang (check)** opsi workload: **"Desktop development with C++"**. Ini wajib untuk mengompilasi beberapa *library* dari OpenCV dan *driver* kamera.
2. **AstraNi SDK:**
   - Sistem ini menggunakan kamera seri Orbbec Astra.
   - Unduh dan instal **AstraNi SDK** (beserta *driver* kameranya) dari situs resmi Orbbec agar kamera dapat terbaca di sistem Windows.
3. **Python:**
   - Pastikan Python 3.9 atau lebih baru sudah terinstal di PC Anda.

---

## 🚀 Instalasi & Persiapan

1. **Clone Repository**
```bash
   git clone https://github.com/arivanhi/rocakuv2
   cd rocakuv2
```

2. **Instalasi Library Python**
    - Buka terminal/Command Prompt, lalu jalankan perintah berikut untuk menginstal semua kebutuhan library:
```bash
    pip install opencv-python numpy ultralytics tensorflow
```

3. **Persiapan Model AI**
   Pastikan file model TFLite (`best_float32.tflite`) berada di dalam folder yang sama dengan *script* utama.

---

## 🎮 Cara Menjalankan Program

1. Colokkan kamera Orbbec Astra ke PC.
2. Jalankan *script* utama:
 ```bash
   python detection_board_auto_send.py
```

**Tahap 1: Kalibrasi Papan (Klik 4 Titik)**

Saat pertama kali dijalankan, program akan meminta Anda mengklik 4 titik sudut area kotak catur pada layar.
- Urutan Klik Wajib:
    - Sudut Kiri Atas
    - Sudut Kanan Atas
    - Sudut Kanan Bawah
    - Sudut Kiri Bawah

Catatan: Setelah 4 titik diklik, data akan otomatis tersimpan di kalibrasi.json. Saat program dijalankan ulang di masa depan, tahap ini akan dilewati otomatis.

**Tahap 2: Live Detection**

Program akan menampilkan papan catur dari sudut pandang atas (Bird's-eye view) secara full screen.
- Kotak Hijau: Ada bidak terdeteksi di atasnya.
- Kotak Abu-abu: Kotak kosong.
- Terminal Output: Akan terus mencetak matriks 8x8 yang siap dikirim ke ESP32.

**Kontrol Keyboard (Hotkeys)**
- Tekan r : Untuk menghapus data kalibrasi (reset) dan mengulang klik 4 titik (gunakan jika posisi kamera bergeser).
- Tekan q : Untuk mematikan program secara aman.

## 📡 Format Output Data (Untuk ESP32)
Program akan mencetak (dan nantinya mengirimkan) data biner berbentuk matriks 8x8 yang merepresentasikan kondisi papan.

Contoh Output:
```bash
[DATA ESP32]: 
1 1 1 1 1 1 1 1 
1 1 1 1 1 1 1 1 
0 0 0 0 0 0 0 0 
0 0 0 0 0 0 0 0 
0 0 0 0 0 0 0 0 
0 0 0 0 0 0 0 0 
1 1 1 1 1 1 1 1 
1 1 1 1 1 1 1 1
```
(Angka 1 berarti terdapat bidak, 0 berarti kotak kosong. Terdapat karakter \n (newline) setiap 8 kotak untuk mempermudah parsing di sisi mikrokontroler).