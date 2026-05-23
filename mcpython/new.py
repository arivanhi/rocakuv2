from machine import Pin
import time
import sys

# ==========================================
# 1. INISIALISASI PIN TOMBOL
# ==========================================
btn_ok = Pin(23, Pin.IN, Pin.PULL_UP)     # Tombol Langkah Biasa / Selesai
btn_makan = Pin(22, Pin.IN, Pin.PULL_UP)  # Tombol Angkat Bidak Lawan

# ==========================================
# 2. VARIABEL KONTROL & DEBOUNCE
# ==========================================
action_flag = None
last_press_ok = 0
last_press_makan = 0

# ==========================================
# 3. INTERRUPT SERVICE ROUTINES (ISR)
# ==========================================
def isr_ok(pin):
    global action_flag, last_press_ok
    current_time = time.ticks_ms()
    # Debounce 200ms untuk menyaring noise mekanik tombol
    if time.ticks_diff(current_time, last_press_ok) > 200:
        action_flag = "CAPTURE"
        last_press_ok = current_time

def isr_makan(pin):
    global action_flag, last_press_makan
    current_time = time.ticks_ms()
    # Debounce 200ms untuk menyaring noise mekanik tombol
    if time.ticks_diff(current_time, last_press_makan) > 200:
        action_flag = "MAKAN"
        last_press_makan = current_time

# Pasang perintah interrupt ke masing-masing pin tombol
btn_ok.irq(trigger=Pin.IRQ_FALLING, handler=isr_ok)
btn_makan.irq(trigger=Pin.IRQ_FALLING, handler=isr_makan)

# ==========================================
# 4. LOOP UTAMA
# ==========================================
print("===================================")
print("ESP32 ROCAKU V2 - MODE DEBUGGING 2 TOMBOL")
print("- Pin 23 : Tombol OK / CAPTURE")
print("- Pin 22 : Tombol MAKAN / RESET")
print("===================================")

while True:
    # Mengirim sinyal murni ke Python PC saat ada tombol yang ditekan
    if action_flag:
        sys.stdout.write(action_flag + "\n")
        action_flag = None # Reset flag agar tidak terkirim berulang

    time.sleep_ms(50) # Jeda ringan agar menghemat daya CPU ESP32