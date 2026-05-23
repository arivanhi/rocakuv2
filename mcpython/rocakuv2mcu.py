import board
import digitalio
import pwmio
import supervisor
import sys
import time

# ==============================
# PIN SETUP & TOMBOL KENDALI AI
# ==============================
def get_pin(*names):
    for name in names:
        if hasattr(board, name):
            return getattr(board, name)
    raise AttributeError(f"Pin tidak ditemukan di board: {names}")

BTN_OK = digitalio.DigitalInOut(get_pin("D23", "IO23", "GPIO23", "GP23"))
BTN_OK.direction = digitalio.Direction.INPUT
BTN_OK.pull = digitalio.Pull.UP

BTN_MAKAN = digitalio.DigitalInOut(get_pin("D22", "IO22", "GPIO22", "GP22"))
BTN_MAKAN.direction = digitalio.Direction.INPUT
BTN_MAKAN.pull = digitalio.Pull.UP

last_btn_ok = True
last_btn_makan = True

def poll_serial_and_buttons(prompt=""):
    global last_btn_ok, last_btn_makan
    if prompt: sys.stdout.write(prompt)

    buf = []
    while True:
        curr_ok = BTN_OK.value
        curr_makan = BTN_MAKAN.value

        if last_btn_ok and not curr_ok:
            sys.stdout.write("CAPTURE\n")
            time.sleep(0.2) 
            
        if last_btn_makan and not curr_makan:
            sys.stdout.write("MAKAN\n")
            time.sleep(0.2) 

        last_btn_ok = curr_ok
        last_btn_makan = curr_makan

        if supervisor.runtime.serial_bytes_available:
            ch = sys.stdin.read(1)
            if ch in ("\r", ""): pass                      
            elif ch == "\n":              
                if buf:
                    sys.stdout.write("\r\n")
                    return "".join(buf).strip()
            elif ch in ("\x08", "\x7f"): 
                if buf:
                    buf.pop()
                    sys.stdout.write("\b \b")
            else:
                buf.append(ch)            
                sys.stdout.write(ch)
        else:
            time.sleep(0.005)

# ==============================
# PANEL KALIBRASI FISIK ROBOT
# ==============================
# 1. Tweak Presisi Magnet (Menggeser seluruh koordinat)
TWEAK_X = 1.1  # Menggeser 1.1mm sesuai ukuran Anda
TWEAK_Y = 0.0  

# 2. Pembalik Arah (Jika G2 lari ke C4, biarkan True untuk membaliknya)
TUKAR_KOLOM = True
TUKAR_BARIS = True

# ==============================
# KONSTANTA MEKANIK CORE XY
# ==============================
STEP_PER_REV = 200
PULLEY_TEETH = 8
BELT_PITCH = 2
MM_PER_REV = PULLEY_TEETH * BELT_PITCH
microstep = 16
STEPS_PER_REV = STEP_PER_REV * microstep
MM_PER_STEP = MM_PER_REV / STEPS_PER_REV

pos_x = 0.0
pos_y = 0.0
X_SIGN = -1   
Y_SIGN = 1    

# Offset Vendor Asli
SQUARE_SIZE = 55  
OFFSET_TRASH = 165
OFFSET_X = 27.5 - OFFSET_TRASH   
OFFSET_Y = 27.5 + 20  
grid4x4_size = 38.75

X_MAX_LIMIT = 440 
Y_MAX_LIMIT = 440

# ==============================
# INITIAL BOARD STATE
# ==============================
board_pieces = {
    "A1": "RW1", "B1": "NW1", "C1": "BW1", "D1": "QW", "E1": "KW", "F1": "BW2", "G1": "NW2", "H1": "RW2",
    "A2": "PW1", "B2": "PW2", "C2": "PW3", "D2": "PW4", "E2": "PW5", "F2": "PW6", "G2": "PW7", "H2": "PW8",
    "A7": "PB1", "B7": "PB2", "C7": "PB3", "D7": "PB4", "E7": "PB5", "F7": "PB6", "G7": "PB7", "H7": "PB8",
    "A8": "RB1", "B8": "NB1", "C8": "BB1", "D8": "QB", "E8": "KB", "F8": "BB2", "G8": "NB2", "H8": "RB2",
}
for row in "3456":
    for col in "ABCDEFGH":
        board_pieces[col + row] = None

INITIAL_BOARD = board_pieces.copy()
moved_pieces = set()

# ==============================
# FUNGSI KALIBRASI KOORDINAT
# ==============================
def calculate_physical_xy(col_idx, row_idx):
    # Membalik array jika fisik terpasang terbalik
    c_idx = 7 - col_idx if TUKAR_KOLOM else col_idx
    r_idx = 7 - row_idx if TUKAR_BARIS else row_idx
    
    orig_x = (c_idx * SQUARE_SIZE) + OFFSET_X
    orig_y = (r_idx * SQUARE_SIZE) + OFFSET_Y
    
    new_x = X_MAX_LIMIT - orig_x + TWEAK_X
    new_y = Y_MAX_LIMIT - orig_y + TWEAK_Y
    
    return (round(new_x), round(new_y))

def get_xy_from_idx(col_idx, row_idx):
    return calculate_physical_xy(col_idx, row_idx)

# ==============================
# PLATE MAPPING & GRAVEYARD
# ==============================
plate = {}
cols = "ABCDEFGH"
for i, col in enumerate(cols):      
    for j in range(8):              
        plate[f"{col}{j+1}"] = calculate_physical_xy(i, j)

GRAVE_CELL_X = grid4x4_size          
GRAVE_HALF_X = GRAVE_CELL_X / 2      
GRAVE_X_EDGE = round(X_MAX_LIMIT - (7 * SQUARE_SIZE + OFFSET_X) - SQUARE_SIZE / 2)

GRAVE_WHITE = {
    "K":  (0, 0), "Q":  (1, 0), "B1": (2, 0), "B2": (3, 0),
    "R1": (0, 1), "R2": (1, 1), "N1": (2, 1), "N2": (3, 1),
    "P1": (0, 2), "P2": (1, 2), "P3": (2, 2), "P4": (3, 2),
    "P5": (0, 3), "P6": (1, 3), "P7": (2, 3), "P8": (3, 3),
}
GRAVE_BLACK = {
    "P1": (0, 4), "P2": (1, 4), "P3": (2, 4), "P4": (3, 4),
    "P5": (0, 5), "P6": (1, 5), "P7": (2, 5), "P8": (3, 5),
    "N1": (0, 6), "N2": (1, 6), "R1": (2, 6), "K":  (3, 6),
    "B1": (0, 7), "B2": (1, 7), "R2": (2, 7), "Q":  (3, 7),
}

def get_grave_key(piece_id):
    kind = piece_id[0]
    return kind if kind in ("Q", "K") else kind + piece_id[2:]

def get_graveyard_xy(piece_id):
    color = piece_color(piece_id)
    key = get_grave_key(piece_id)
    gc, row_j = GRAVE_WHITE[key] if color == "W" else GRAVE_BLACK[key]
    
    gx = GRAVE_X_EDGE - GRAVE_HALF_X - (gc * GRAVE_CELL_X)
    
    r_idx = 7 - row_j if TUKAR_BARIS else row_j
    orig_y = (r_idx * SQUARE_SIZE) + OFFSET_Y
    gy = Y_MAX_LIMIT - orig_y
    
    return (round(gx + TWEAK_X), round(gy + TWEAK_Y))

def pos_to_idx(pos):
    return "ABCDEFGH".index(pos[0]), "12345678".index(pos[1])

def piece_color(piece_id):
    if not piece_id or len(piece_id) < 2: return None
    return piece_id[1]

def piece_kind(piece_id):
    if not piece_id: return None
    return piece_id[0]

def send_to_graveyard(piece_id, current_pos):
    sx, sy = plate[current_pos]
    move_to(sx, sy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    col_idx, row_idx = pos_to_idx(current_pos)
    gx, gy = get_xy_from_idx(col_idx + 0.5, row_idx - 0.5)
    gy = max(5, min(gy, Y_MAX_LIMIT - 5))
    grave_x, grave_y = get_graveyard_xy(piece_id)
    transit_x = GRAVE_X_EDGE

    move_to(gx, gy)
    move_to(transit_x, gy)
    move_to(transit_x, grave_y)
    move_to(grave_x, grave_y)

    servo_set(False)
    time.sleep(0.5)
    servo_release()

def bring_from_graveyard(piece_id, target_pos):
    gx, gy = get_graveyard_xy(piece_id)
    move_to(gx, gy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    tx, ty = plate[target_pos]
    col_idx, row_idx = pos_to_idx(target_pos)
    gut_x, gut_y = get_xy_from_idx(col_idx + 0.5, row_idx - 0.5)
    gut_y = max(5, min(gut_y, Y_MAX_LIMIT - 5))
    transit_x = GRAVE_X_EDGE

    move_to(transit_x, gy)
    move_to(transit_x, gut_y)
    move_to(gut_x, gut_y)
    move_to(tx, ty)

    servo_set(False)
    time.sleep(0.5)
    servo_release()

def execute_throw(target_color=None):
    to_throw = []
    for pos, piece in board_pieces.items():
        if piece is None: continue
        if target_color is None or piece_color(piece) == target_color:
            col_idx, row_idx = pos_to_idx(pos)
            to_throw.append((col_idx, row_idx, pos, piece))
    to_throw.sort(key=lambda x: (-x[0], x[1]))
    for _, _, pos, piece in to_throw:
        send_to_graveyard(piece, pos)
        board_pieces[pos] = None

def execute_physical_reset():
    print("\n[RESET PIECES] Memulai sinkronisasi fisik total...")
    moved_pieces.clear()
    execute_throw()
    restore_order = []
    for pos, piece_id in INITIAL_BOARD.items():
        if piece_id is None: continue
        col_idx, row_idx = pos_to_idx(pos)
        restore_order.append((col_idx, row_idx, pos, piece_id))
    restore_order.sort(key=lambda x: (x[0], x[1]))
    for _, _, pos, piece_id in restore_order:
        bring_from_graveyard(piece_id, pos)
        board_pieces[pos] = piece_id

# ==============================
# MOTOR & SERVO SETUP
# ==============================
servo = pwmio.PWMOut(get_pin("D27", "IO27", "GPIO27", "GP27"), frequency=50)

def servo_release(): servo.duty_cycle = 0
def set_angle(angle):
    pulse = 500 + (angle / 180) * 2000
    servo.duty_cycle = int(pulse / 20000 * 65535)
def servo_set(state):
    set_angle(40) if state else set_angle(70)

EN = digitalio.DigitalInOut(get_pin("D13", "IO13", "GPIO13", "GP13"))
EN.direction = digitalio.Direction.OUTPUT
EN.value = False

STEP_A = digitalio.DigitalInOut(get_pin("D12", "IO12", "GPIO12", "GP12"))
DIR_A  = digitalio.DigitalInOut(get_pin("D26", "IO26", "GPIO26", "GP26"))
STEP_B = digitalio.DigitalInOut(get_pin("D14", "IO14", "GPIO14", "GP14"))
DIR_B  = digitalio.DigitalInOut(get_pin("D25", "IO25", "GPIO25", "GP25"))

for pin in [STEP_A, DIR_A, STEP_B, DIR_B]:
    pin.direction = digitalio.Direction.OUTPUT

LIMIT_X = digitalio.DigitalInOut(get_pin("D15", "IO15", "GPIO15", "GP15"))
LIMIT_X.direction = digitalio.Direction.INPUT
LIMIT_X.pull = digitalio.Pull.UP
LIMIT_Y = digitalio.DigitalInOut(get_pin("D4", "IO4", "GPIO4", "GP4"))
LIMIT_Y.direction = digitalio.Direction.INPUT
LIMIT_Y.pull = digitalio.Pull.UP

MS1 = digitalio.DigitalInOut(get_pin("D2", "IO2", "GPIO2", "GP2"))
MS2 = digitalio.DigitalInOut(get_pin("D21", "IO21", "GPIO21", "GP21"))
MS1.direction = digitalio.Direction.OUTPUT
MS2.direction = digitalio.Direction.OUTPUT

def set_microstep(mode):
    global microstep, STEPS_PER_REV, MM_PER_STEP
    microstep = mode
    STEPS_PER_REV = STEP_PER_REV * microstep
    MM_PER_STEP = MM_PER_REV / STEPS_PER_REV
    MS1.value = True if mode in (16, 64) else False
    MS2.value = True if mode in (32, 64) else False

def home_manual(limit_sensor, dir_a, dir_b):
    servo_set(False)
    DIR_A.value = dir_a
    DIR_B.value = dir_b
    while limit_sensor.value:
        STEP_A.value = True
        STEP_B.value = True
        time.sleep(0.0005)
        STEP_A.value = False
        STEP_B.value = False
        time.sleep(0.0005)

def move_to(target_x_mm, target_y_mm, speed=0.00003):
    global pos_x, pos_y
    dx = (target_x_mm - pos_x) * X_SIGN
    dy = (target_y_mm - pos_y) * Y_SIGN
    a_mm = dx + dy
    b_mm = dx - dy
    steps_a = int(abs(a_mm) / MM_PER_STEP)
    steps_b = int(abs(b_mm) / MM_PER_STEP)
    DIR_A.value = a_mm < 0
    DIR_B.value = b_mm < 0
    EN.value = False
    max_steps = max(steps_a, steps_b)

    for i in range(max_steps):
        if i < steps_a: STEP_A.value = True
        if i < steps_b: STEP_B.value = True
        time.sleep(speed)
        STEP_A.value = False
        STEP_B.value = False
        time.sleep(speed)

    pos_x = target_x_mm
    pos_y = target_y_mm

def get_direct_waypoints(start_pos, end_pos):
    return [plate[end_pos]]

# === Logika Pergerakan Lengan Robot ===
def bring(start_pos, end_pos):
    piece_id = board_pieces.get(start_pos)
    if piece_id is None: return

    captured = board_pieces.get(end_pos)
    if captured is not None:
        send_to_graveyard(captured, end_pos)
        board_pieces[end_pos] = None

    sx, sy = plate[start_pos]
    move_to(sx, sy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    waypoints = get_direct_waypoints(start_pos, end_pos)
    for wp_x, wp_y in waypoints:
        move_to(wp_x, wp_y)

    time.sleep(0.2)
    servo_set(False)
    time.sleep(0.5)
    servo_release()

    board_pieces[end_pos] = piece_id
    board_pieces[start_pos] = None
    moved_pieces.add(piece_id)

def execute_castling(king_start, king_end, rook_start, rook_end):
    king_id = board_pieces[king_start]
    rook_id = board_pieces[rook_start]

    sx, sy = plate[king_start]
    move_to(sx, sy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    tx, ty = plate[king_end]
    move_to(tx, ty)
    servo_set(False)
    time.sleep(0.5)
    servo_release()
    board_pieces[king_end] = king_id
    board_pieces[king_start] = None

    rx, ry = plate[rook_start]
    move_to(rx, ry)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    rc, rr = pos_to_idx(rook_start)
    tc, tr = pos_to_idx(rook_end)
    gut_offset = 0.5 if rr == 0 else -0.5
    gut_x, gut_y = get_xy_from_idx(rc, rr + gut_offset)
    gut_tx, gut_ty = get_xy_from_idx(tc, tr + gut_offset)

    move_to(gut_x, gut_y)
    move_to(gut_tx, gut_ty)
    rtx, rty = plate[rook_end]
    move_to(rtx, rty)

    servo_set(False)
    time.sleep(0.5)
    servo_release()
    board_pieces[rook_end] = rook_id
    board_pieces[rook_start] = None

# ==============================
# MAIN SHELL (AI INTEGRATION)
# ==============================
def shell():
    global pos_x, pos_y
    simulated_board = board_pieces.copy()

    print("\n=== ROCAKU V2 AI HYBRID MODE ===")
    print("- Membaca Serial PC & Tombol ESP")
    print("=================================\n")

    while True:
        raw_line = poll_serial_and_buttons("AI_READY> ").strip().upper()
        if not raw_line: continue
        print(f"\n[INSTRUKSI PC] {raw_line}")

        if raw_line == "HOME":
            home_manual(LIMIT_X, True, False)
            time.sleep(0.2)
            home_manual(LIMIT_Y, False, False)
            pos_x, pos_y = 0.0, 0.0
            print("Homing Selesai.")
            continue

        if raw_line == "RESET_GAME":
            execute_physical_reset()
            simulated_board = board_pieces.copy()
            sys.stdout.write("OK\n")
            continue

        parts = raw_line.split()
        if len(parts) >= 1:
            move_str = parts[0] 
            
            if len(move_str) == 4 and move_str[0:2] in plate and move_str[2:4] in plate:
                start_sq = move_str[0:2]
                end_sq   = move_str[2:4]
                piece = simulated_board.get(start_sq)
                
                if piece and piece_kind(piece) == "K" and abs(pos_to_idx(start_sq)[0] - pos_to_idx(end_sq)[0]) == 2:
                    print(f"-> Rokade Otomatis: {start_sq} ke {end_sq}")
                    rs = f"H{start_sq[1]}" if end_sq[0] in "GH" else f"A{start_sq[1]}"
                    re = f"F{start_sq[1]}" if end_sq[0] in "GH" else f"D{start_sq[1]}"
                    execute_castling(start_sq, end_sq, rs, re)
                    simulated_board[end_sq] = piece
                    simulated_board[start_sq] = None
                    simulated_board[re] = simulated_board.get(rs)
                    simulated_board[rs] = None
                else:
                    print(f"-> Gerak Otomatis: {start_sq} -> {end_sq}")
                    bring(start_sq, end_sq)
                    simulated_board[end_sq] = piece
                    simulated_board[start_sq] = None
                
                print("Langkah fisik selesai. Mengirim OK ke PC...")
                sys.stdout.write("OK\n")

# ==============================
# INISIALISASI MESIN
# ==============================
print("Homing...")
set_microstep(16)
home_manual(LIMIT_X, True, False)
time.sleep(0.2)
home_manual(LIMIT_Y, False, False)
print("Homing selesai")
pos_x = 0
pos_y = 0

shell()