from machine import Pin
import time
import sys
import select

btn = Pin(23, Pin.IN, Pin.PULL_UP)
button_pressed_flag = False
last_press_time = 0

poller = select.poll()
poller.register(sys.stdin, select.POLLIN)

def button_isr(pin):
    global button_pressed_flag, last_press_time
    current_time = time.ticks_ms()
    if time.ticks_diff(current_time, last_press_time) > 200:
        button_pressed_flag = True
        last_press_time = current_time

btn.irq(trigger=Pin.IRQ_FALLING, handler=button_isr)

print("ESP32 Ready. Tekan tombol pada Pin 23.")

while True:
    if button_pressed_flag:
        sys.stdout.write("CAPTURE\n")
        print("\nMenunggu data AI...")
        
        timeout_start = time.ticks_ms()
        buffer = ""
        
        # Menunggu data sampai terkumpul 8 baris (format matriks)
        while time.ticks_diff(time.ticks_ms(), timeout_start) < 5000:
            if poller.poll(10):
                line = sys.stdin.readline()
                buffer += line
                # Matriks 8x8 biasanya selesai dalam 8-10 baris input
                if buffer.count('\n') >= 8:
                    print("\n--- POSISI PAPAN TERBARU ---")
                    print(buffer)
                    print("----------------------------")
                    break
        
        button_pressed_flag = False
    time.sleep_ms(50)