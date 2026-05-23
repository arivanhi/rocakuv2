
import board
import digitalio
import pwmio
import supervisor
import sys
import time

# ==============================
# SERIAL SHELL via USB (CH340)
# ==============================
# print() sudah otomatis keluar ke CH340 via sys.stdout (REPL CircuitPython).
# Tidak perlu busio.UART atau pin TX/RX eksternal.
# Cukup colok kabel USB yang sama yang dipakai untuk upload program,
# lalu buka SSCOM / Putty / Thonny Serial Monitor di port yang sama.
# Setting: 115200, 8, None, 1.

def uart_readline(prompt=""):
    """
    Baca satu baris dari USB serial (CH340) dengan polling non-blocking.
    Pakai supervisor.runtime.serial_bytes_available agar tidak freeze.
    Handle \\r, \\n, dan \\r\\n (SSCOM default: AddCrLf).
    """
    if prompt:
        sys.stdout.write(prompt)

    buf = []

    while True:
        if supervisor.runtime.serial_bytes_available:
            ch = sys.stdin.read(1)
            if ch in ("\r", ""):
                pass                      # abaikan CR, tunggu LF
            elif ch == "\n":              # LF = akhir baris
                if buf:
                    sys.stdout.write("\r\n")
                    return "".join(buf).strip()
                # baris kosong — abaikan
            elif ch in ("\x08", "\x7f"): # Backspace / DEL
                if buf:
                    buf.pop()
                    sys.stdout.write("\b \b")
            else:
                buf.append(ch)            # echo karakter ke terminal
                sys.stdout.write(ch)
        else:
            time.sleep(0.005)

def get_pin(*names):
    """
    Ambil pin dari board dengan beberapa kemungkinan nama.
    Dipakai supaya kode tetap fleksibel di ESP32 yang bisa menamai pin
    sebagai D12, IO12, GPIO12, atau GP12 tergantung firmware/board.
    """
    for name in names:
        if hasattr(board, name):
            return getattr(board, name)
    raise AttributeError(f"Pin tidak ditemukan di board: {names}")


# ==============================
# KONSTANTA MEKANIK
# ==============================

STEP_PER_REV = 200
PULLEY_TEETH = 8
BELT_PITCH = 2

MM_PER_REV = PULLEY_TEETH * BELT_PITCH

microstep = 16

STEPS_PER_REV = STEP_PER_REV * microstep
MM_PER_STEP = MM_PER_REV / STEPS_PER_REV

VERSION = "1.95"
BOARD_ORIENTATION = "WHITE"  # input shell dibaca dari sisi putih

pos_x = 0.0
pos_y = 0.0

X_SIGN = -1   # X sekarang kebalik: x+ harus ke kanan
Y_SIGN = 1    # Y sudah benar

# Batas area kerja berdasarkan plate mapping
# X_MIN = 25   # Kolom A
# X_MAX = 430   # Kolom H
# Y_MIN = 25   # Baris 1
# Y_MAX = 420  # Baris 8

SQUARE_SIZE = 55  # Ukuran satu kotak dalam mm (bisa diubah-ubah)
OFFSET_TRASH = 165
OFFSET_X = 27.5 - OFFSET_TRASH   # Titik tengah kotak pertama (biasanya SQUARE_SIZE / 2)
OFFSET_Y = 27.5 + 20  # Titik tengah kotak pertama

grid4x4_size = 38.75

X_MAX_LIMIT = 440 
Y_MAX_LIMIT = 440


# ==============================
# INITIAL BOARD STATE (Posisi Awal)
# ==============================

board_pieces = {
    # Baris 1 & 2 (Putih)
    "A1": "RW1", "B1": "NW1", "C1": "BW1", "D1": "QW", "E1": "KW", "F1": "BW2", "G1": "NW2", "H1": "RW2",
    "A2": "PW1", "B2": "PW2", "C2": "PW3", "D2": "PW4", "E2": "PW5", "F2": "PW6", "G2": "PW7", "H2": "PW8",

    # Baris 7 & 8 (Hitam)
    "A7": "PB1", "B7": "PB2", "C7": "PB3", "D7": "PB4", "E7": "PB5", "F7": "PB6", "G7": "PB7", "H7": "PB8",
    "A8": "RB1", "B8": "NB1", "C8": "BB1", "D8": "QB", "E8": "KB", "F8": "BB2", "G8": "NB2", "H8": "RB2",
}

# Isi sisa kotak kosong (A3-H6) dengan None
for row in "3456":
    for col in "ABCDEFGH":
        board_pieces[col + row] = None

INITIAL_BOARD = board_pieces.copy()

# Track bidak yang sudah bergerak (untuk validasi rokade)
moved_pieces = set()

# ==============================
# PLATE MAPPING (ROTASI 180°)
# ==============================
plate = {}
cols = "ABCDEFGH"

for i, col in enumerate(cols):      # i = Indeks Kolom (A=0, B=1, ... H=7)
    for j in range(8):              # j = Indeks Baris (1=0, 2=1, ... 8=7)
        
        # 1. Hitung koordinat murni berdasarkan ukuran kotak
        # Rumus: (Indeks * UkuranKotak) + Offset ke tengah kotak
        orig_x = (i * SQUARE_SIZE) + OFFSET_X
        orig_y = (j * SQUARE_SIZE) + OFFSET_Y
        
        # 2. Rotasi 180°
        # X baru = Max_Limit - X lama
        # Y baru = Max_Limit - Y lama
        new_x = X_MAX_LIMIT - orig_x
        new_y = Y_MAX_LIMIT - orig_y

        key = f"{col}{j+1}"
        plate[key] = (round(new_x), round(new_y))


# ==============================
# GRAVEYARD 4x4 (Area buang bidak yang dimakan)
# ==============================
# Area pembuangan di sisi X setelah kolom H
# Y mengikuti garis tengah baris papan (SQUARE_SIZE)
# Dimensi per sisi: 155mm x 210mm

GRAVE_CELL_X = grid4x4_size          # lebar sel X (~38mm)
GRAVE_HALF_X = GRAVE_CELL_X / 2      # titik tengah sel X

# Batas X kolom H (titik tepi papan ke arah graveyard)
GRAVE_X_EDGE = round(X_MAX_LIMIT - (7 * SQUARE_SIZE + OFFSET_X) - SQUARE_SIZE / 2)

# Mapping bidak putih -> (grid_col, board_row_index)
# Row 1 (j=0): K, Q, B1, B2    (major pieces, terjauh dari tengah)
# Row 2 (j=1): R1, R2, N1, N2
# Row 3 (j=2): P1, P2, P3, P4  (pawn, terdekat ke tengah)
# Row 4 (j=3): P5, P6, P7, P8
GRAVE_WHITE = {
    "K":  (0, 0), "Q":  (1, 0), "B1": (2, 0), "B2": (3, 0),
    "R1": (0, 1), "R2": (1, 1), "N1": (2, 1), "N2": (3, 1),
    "P1": (0, 2), "P2": (1, 2), "P3": (2, 2), "P4": (3, 2),
    "P5": (0, 3), "P6": (1, 3), "P7": (2, 3), "P8": (3, 3),
}

# Mapping bidak hitam -> (grid_col, board_row_index)
# Row 5 (j=4): P1, P2, P3, P4  (pawn, terdekat ke tengah)
# Row 6 (j=5): P5, P6, P7, P8
# Row 7 (j=6): N1, N2, R1, K
# Row 8 (j=7): B1, B2, R2, Q   (major pieces, terjauh dari tengah)
GRAVE_BLACK = {
    "P1": (0, 4), "P2": (1, 4), "P3": (2, 4), "P4": (3, 4),
    "P5": (0, 5), "P6": (1, 5), "P7": (2, 5), "P8": (3, 5),
    "N1": (0, 6), "N2": (1, 6), "R1": (2, 6), "K":  (3, 6),
    "B1": (0, 7), "B2": (1, 7), "R2": (2, 7), "Q":  (3, 7),
}

def get_grave_key(piece_id):
    """Konversi piece_id (misal 'RW1','QW','PB3') ke key graveyard ('R1','Q','P3')"""
    kind = piece_id[0]
    if kind in ("Q", "K"):
        return kind
    return kind + piece_id[2:]

def get_graveyard_xy(piece_id):
    """Hitung koordinat fisik slot kuburan berdasarkan garis tengah baris papan"""
    color = piece_color(piece_id)
    key = get_grave_key(piece_id)

    if color == "W":
        gc, row_j = GRAVE_WHITE[key]
    else:
        gc, row_j = GRAVE_BLACK[key]

    # X: dari tepi papan menjauh ke graveyard
    gx = GRAVE_X_EDGE - GRAVE_HALF_X - (gc * GRAVE_CELL_X)

    # Y: mengikuti garis tengah baris papan (row_j = board row index)
    orig_y = (row_j * SQUARE_SIZE) + OFFSET_Y
    gy = Y_MAX_LIMIT - orig_y

    return (round(gx), round(gy))

def send_to_graveyard(piece_id, current_pos):
    """
    Angkat bidak yang dimakan, bawa ke slot kuburan 4x4.
    Routing aman: gutter -> transit X (tepi graveyard) -> Y ke slot -> X masuk slot
    """
    sx, sy = plate[current_pos]
    move_to(sx, sy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    # Gutter: setengah langkah ke arah kolom H, antar baris
    col_idx, row_idx = pos_to_idx(current_pos)
    gx, gy = get_xy_from_idx(col_idx + 0.5, row_idx - 0.5)

    # Clamp gutter Y agar tidak keluar batas fisik
    gy = max(5, min(gy, Y_MAX_LIMIT - 5))

    # Target kuburan
    grave_x, grave_y = get_graveyard_xy(piece_id)

    # Transit X: tepat di tepi graveyard (aman dari slot yg sudah terisi)
    transit_x = GRAVE_X_EDGE

    # 1. Gerak diagonal ke gutter
    move_to(gx, gy)

    # 2. Gerak lurus X ke transit (Y tetap di gutter)
    move_to(transit_x, gy)

    # 3. Gerak Y ke baris slot kuburan (X tetap di transit, aman dari slot)
    move_to(transit_x, grave_y)

    # 4. Gerak X masuk ke slot kuburan
    move_to(grave_x, grave_y)

    servo_set(False)
    time.sleep(0.5)
    servo_release()
    print(f"[MAKAN] {piece_id} dibuang ke kuburan ({grave_x},{grave_y})")

def bring_from_graveyard(piece_id, target_pos):
    """
    Ambil bidak dari slot kuburan 4x4 kembali ke board.
    Routing aman: X keluar ke transit -> Y ke gutter board -> X masuk board -> target
    """
    gx, gy = get_graveyard_xy(piece_id)
    move_to(gx, gy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    # Target fisik di board
    tx, ty = plate[target_pos]

    # Gutter untuk masuk board
    col_idx, row_idx = pos_to_idx(target_pos)
    gut_x, gut_y = get_xy_from_idx(col_idx + 0.5, row_idx - 0.5)
    gut_y = max(5, min(gut_y, Y_MAX_LIMIT - 5))

    # Transit X: tepi graveyard
    transit_x = GRAVE_X_EDGE

    # 1. Gerak X keluar dari slot ke transit
    move_to(transit_x, gy)

    # 2. Gerak Y ke arah gutter board
    move_to(transit_x, gut_y)

    # 3. Gerak X masuk ke area board (gutter)
    move_to(gut_x, gut_y)

    # 4. Gerak ke posisi akhir
    move_to(tx, ty)

    servo_set(False)
    time.sleep(0.5)
    servo_release()

def execute_throw(target_color=None):
    """Membuang bidak dari board ke graveyard, urut dari kolom H ke A"""
    label = target_color if target_color else "SEMUA"
    print(f"\n[THROW] Membuang {label} bidak ke graveyard...")

    # Kumpulkan bidak yang akan dibuang
    to_throw = []
    for pos in board_pieces:
        piece = board_pieces[pos]
        if piece is None:
            continue
        if target_color is None or piece_color(piece) == target_color:
            col_idx, row_idx = pos_to_idx(pos)
            to_throw.append((col_idx, row_idx, pos, piece))

    # Urutkan: kolom H (7) dulu -> A (0), biar jalur terdekat dibuang duluan
    to_throw.sort(key=lambda x: (-x[0], x[1]))

    for _, _, pos, piece in to_throw:
        send_to_graveyard(piece, pos)
        board_pieces[pos] = None

def execute_physical_reset():
    """RESET PIECES: Bersihkan board lalu kembalikan semua dari graveyard"""
    print("\n[RESET PIECES] Memulai sinkronisasi fisik total...")

    moved_pieces.clear()

    # 1. Bersihkan board dulu
    execute_throw()

    # 2. Kembalikan semua bidak, urut kolom A -> H
    restore_order = []
    for pos, piece_id in INITIAL_BOARD.items():
        if piece_id is None:
            continue
        col_idx, row_idx = pos_to_idx(pos)
        restore_order.append((col_idx, row_idx, pos, piece_id))

    # Urut A(0) -> H(7) agar jalur dari graveyard semakin pendek
    restore_order.sort(key=lambda x: (x[0], x[1]))

    for _, _, pos, piece_id in restore_order:
        print(f"Restore: {piece_id} ke {pos}")
        bring_from_graveyard(piece_id, pos)
        board_pieces[pos] = piece_id

    print("[RESET PIECES] Papan fisik sudah sinkron dengan posisi awal!")


# ==============================
# SERVO SETUP
# ==============================

servo = pwmio.PWMOut(get_pin("D27", "IO27", "GPIO27", "GP27"), frequency=50)

def servo_release():
    servo.duty_cycle = 0

def set_angle(angle):
    pulse = 500 + (angle / 180) * 2000
    duty = int(pulse / 20000 * 65535)
    servo.duty_cycle = duty

def servo_set(state):

    if state:
        set_angle(40)

    else:
        set_angle(70)

# ==============================
# PIN SETUP
# ==============================

# ESP32 pinout yang dipakai:
# Motor X: STP=D12, DIR=D26
# Motor Y: STP=D14, DIR=D25
# Limit X: D15
# Limit Y: D4
# Servo: D27
# EN: D13
# DIAG_X: D16
# DIAG_Y: D17
# MS1: D34
# MS2: D35
#
# Catatan penting:
# Pada banyak ESP32 klasik, GPIO34/35 hanya input-only.
# Kalau board kamu termasuk varian seperti itu, MS1/MS2 tidak bisa dijadikan OUTPUT
# dan harus dipindah ke pin lain yang mendukung output.

EN = digitalio.DigitalInOut(get_pin("D13", "IO13", "GPIO13", "GP13"))
EN.direction = digitalio.Direction.OUTPUT
EN.value = False

STEP_A = digitalio.DigitalInOut(get_pin("D12", "IO12", "GPIO12", "GP12"))
DIR_A  = digitalio.DigitalInOut(get_pin("D26", "IO26", "GPIO26", "GP26"))

STEP_B = digitalio.DigitalInOut(get_pin("D14", "IO14", "GPIO14", "GP14"))
DIR_B  = digitalio.DigitalInOut(get_pin("D25", "IO25", "GPIO25", "GP25"))

for pin in [STEP_A, DIR_A, STEP_B, DIR_B]:
    pin.direction = digitalio.Direction.OUTPUT

# Optional: DIAG tidak dipakai karena sudah memakai limit switch
# Jika ingin dipakai nanti, cukup aktifkan blok ini.
# DIAG_X = digitalio.DigitalInOut(get_pin("D16", "IO16", "GPIO16", "GP16"))
# DIAG_Y = digitalio.DigitalInOut(get_pin("D17", "IO17", "GPIO17", "GP17"))
# DIAG_X.direction = digitalio.Direction.INPUT
# DIAG_Y.direction = digitalio.Direction.INPUT

# ==============================
# LIMIT SWITCH
# ==============================

LIMIT_X = digitalio.DigitalInOut(get_pin("D15", "IO15", "GPIO15", "GP15"))
LIMIT_X.direction = digitalio.Direction.INPUT
LIMIT_X.pull = digitalio.Pull.UP

LIMIT_Y = digitalio.DigitalInOut(get_pin("D4", "IO4", "GPIO4", "GP4"))
LIMIT_Y.direction = digitalio.Direction.INPUT
LIMIT_Y.pull = digitalio.Pull.UP

# ==============================
# MICROSTEP PIN
# ==============================

MS1 = digitalio.DigitalInOut(get_pin("D2", "IO2", "GPIO2", "GP2"))
MS2 = digitalio.DigitalInOut(get_pin("D21", "IO21", "GPIO21", "GP21"))

MS1.direction = digitalio.Direction.OUTPUT
MS2.direction = digitalio.Direction.OUTPUT

# ==============================
# MICROSTEP CONTROL
# ==============================

def set_microstep(mode):

    global microstep, STEPS_PER_REV, MM_PER_STEP

    microstep = mode
    STEPS_PER_REV = STEP_PER_REV * microstep
    MM_PER_STEP = MM_PER_REV / STEPS_PER_REV

    if mode == 8:
        MS1.value = False
        MS2.value = False

    elif mode == 16:
        MS1.value = True
        MS2.value = False

    elif mode == 32:
        MS1.value = False
        MS2.value = True

    elif mode == 64:
        MS1.value = True
        MS2.value = True

    print("Microstep =", mode)
    print("MM per step =", MM_PER_STEP)

# ==============================
# HOMING
# ==============================

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

# ==============================
# MOVE TO POSITION
# ==============================

def move_to(target_x_mm, target_y_mm, speed=0.00003):
    global pos_x, pos_y

    dx = (target_x_mm - pos_x) * X_SIGN
    dy = (target_y_mm - pos_y) * Y_SIGN

    a_mm = dx + dy
    b_mm = dx - dy

    steps_a = int(abs(a_mm) / MM_PER_STEP)
    steps_b = int(abs(b_mm) / MM_PER_STEP)

    dir_a = a_mm < 0
    dir_b = b_mm < 0

    DIR_A.value = dir_a
    DIR_B.value = dir_b

    EN.value = False

    max_steps = max(steps_a, steps_b)

    for i in range(max_steps):
        if i < steps_a:
            STEP_A.value = True

        if i < steps_b:
            STEP_B.value = True

        time.sleep(speed)

        STEP_A.value = False
        STEP_B.value = False

        time.sleep(speed)

    pos_x = target_x_mm
    pos_y = target_y_mm

    print("POSISI -> X:", pos_x, "mm  Y:", pos_y, "mm")

def midpoint(p1, p2):

    x1, y1 = p1
    x2, y2 = p2

    return ((x1 + x2)/2, (y1 + y2)/2)

# ==============================
# GOTO POSITION
# ==============================

def goto(pos):

    x,y = plate[pos]

    print("Goto",pos,"->",x,y)

    move_to(x,y)

#     time.sleep(1)



# ==============================
# HELPER VALIDASI JALUR
# ==============================

COLS = "ABCDEFGH"
ROWS = "12345678"


def mirror_pos(pos):
    """Cermin 180° untuk membaca input dari sisi putih."""
    if not pos or len(pos) != 2:
        return pos
    col, row = pos[0], pos[1]
    if col not in COLS or row not in ROWS:
        return pos
    mirrored_col = COLS[7 - COLS.index(col)]
    mirrored_row = ROWS[7 - ROWS.index(row)]
    return mirrored_col + mirrored_row


def normalize_shell_move(start_pos, end_pos):
    """Ubah input shell ke orientasi internal papan."""
    return mirror_pos(start_pos), mirror_pos(end_pos)


def pos_to_idx(pos):
    col = pos[0]
    row = pos[1]
    return COLS.index(col), ROWS.index(row)


def idx_to_pos(col_idx, row_idx):
    return COLS[col_idx] + ROWS[row_idx]


def piece_color(piece_id):
    """W = putih, B = hitam"""
    if not piece_id or len(piece_id) < 2:
        return None
    if piece_id[1] == "W":
        return "W"
    if piece_id[1] == "B":
        return "B"
    return None


def piece_kind(piece_id):
    """R, N, B, Q, K, P"""
    if not piece_id:
        return None
    return piece_id[0]


def squares_between_line(start_pos, end_pos):
    """
    Menghasilkan kotak-kotak di antara start dan end untuk gerak lurus/diagonal.
    Tidak memasukkan start dan end.
    """
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)

    dc = ec - sc
    dr = er - sr

    if dc == 0 and dr == 0:
        return None, "Start dan tujuan sama."

    # Lurus
    if dc == 0:
        step_r = 1 if dr > 0 else -1
        blockers = [idx_to_pos(sc, r) for r in range(sr + step_r, er, step_r)]
        return blockers, ""
    if dr == 0:
        step_c = 1 if dc > 0 else -1
        blockers = [idx_to_pos(c, sr) for c in range(sc + step_c, ec, step_c)]
        return blockers, ""

    # Diagonal
    if abs(dc) == abs(dr):
        step_c = 1 if dc > 0 else -1
        step_r = 1 if dr > 0 else -1
        blockers = []
        c, r = sc + step_c, sr + step_r
        while c != ec and r != er:
            blockers.append(idx_to_pos(c, r))
            c += step_c
            r += step_r
        return blockers, ""

    return None, "Gerakan bukan garis lurus atau diagonal."


def path_is_clear(start_pos, end_pos, board_state):
    blockers, msg = squares_between_line(start_pos, end_pos)
    if blockers is None:
        return False, msg

    occupied = [p for p in blockers if board_state.get(p) is not None]
    if occupied:
        return False, f"Lintasan terhalang: {', '.join(occupied)}"
    return True, ""


def validate_rook_move(start_pos, end_pos, board_state):
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)
    if sc != ec and sr != er:
        return False, "Benteng harus bergerak lurus."
    return path_is_clear(start_pos, end_pos, board_state)


def validate_bishop_move(start_pos, end_pos, board_state):
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)
    if abs(ec - sc) != abs(er - sr):
        return False, "Bishop harus bergerak diagonal."
    return path_is_clear(start_pos, end_pos, board_state)


def validate_queen_move(start_pos, end_pos, board_state):
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)
    if sc == ec or sr == er or abs(ec - sc) == abs(er - sr):
        return path_is_clear(start_pos, end_pos, board_state)
    return False, "Queen harus bergerak lurus atau diagonal."


def is_castling(start_pos, end_pos, board_state):
    """Cek apakah gerakan ini adalah rokade yang valid.
    Return: (valid, rook_start, rook_end) atau (False, None, None)"""
    piece = board_state.get(start_pos)
    if not piece or piece_kind(piece) != "K":
        return False, None, None

    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)

    # King harus bergerak 2 kotak horizontal, baris sama
    if er != sr or abs(ec - sc) != 2:
        return False, None, None

    color = piece_color(piece)

    # King sudah pernah bergerak?
    if piece in moved_pieces:
        return False, None, None

    # Tentukan sisi rokade
    if ec > sc:  # Kingside (ke arah H)
        rook_start = idx_to_pos(7, sr)
        rook_end = idx_to_pos(5, sr)
    else:  # Queenside (ke arah A)
        rook_start = idx_to_pos(0, sr)
        rook_end = idx_to_pos(3, sr)

    # Cek rook ada, tipe benar, warna sama, belum pernah gerak
    rook = board_state.get(rook_start)
    if not rook or piece_kind(rook) != "R" or piece_color(rook) != color:
        return False, None, None
    if rook in moved_pieces:
        return False, None, None

    # Cek lintasan kosong antara king dan rook
    ok, msg = path_is_clear(start_pos, rook_start, board_state)
    if not ok:
        return False, None, None

    return True, rook_start, rook_end


def validate_king_move(start_pos, end_pos, board_state):
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)

    # Gerakan normal (1 kotak)
    if max(abs(ec - sc), abs(er - sr)) == 1:
        occupant = board_state.get(end_pos)
        if occupant is not None:
            mover = board_state.get(start_pos)
            if piece_color(occupant) == piece_color(mover):
                return False, f"Tujuan {end_pos} terisi oleh bidak sendiri ({occupant})."
        return True, ""

    # Rokade (2 kotak horizontal)
    valid, rook_start, rook_end = is_castling(start_pos, end_pos, board_state)
    if valid:
        return True, "CASTLE"

    return False, "King hanya boleh maju 1 kotak."


def validate_knight_move(start_pos, end_pos, board_state):
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)
    dc = abs(ec - sc)
    dr = abs(er - sr)
    if (dc, dr) not in ((1, 2), (2, 1)):
        return False, "Kuda harus bergerak 2-1."
    occupant = board_state.get(end_pos)
    if occupant is not None:
        mover = board_state.get(start_pos)
        if piece_color(occupant) == piece_color(mover):
            return False, f"Tujuan {end_pos} terisi oleh bidak sendiri ({occupant})."
    return True, ""


def validate_pawn_move(start_pos, end_pos, board_state):
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)

    piece = board_state.get(start_pos)
    color = piece_color(piece)

    if color == "W":
        forward = 1
        start_row = 1  # row 2
    elif color == "B":
        forward = -1
        start_row = 6  # row 7
    else:
        return False, "Pawn tidak dikenali."

    dc = ec - sc
    dr = er - sr
    target = board_state.get(end_pos)

    # maju lurus
    if dc == 0:
        if dr == forward and target is None:
            return True, ""
        if sr == start_row and dr == 2 * forward and target is None:
            intermediate = idx_to_pos(sc, sr + forward)
            if board_state.get(intermediate) is None:
                return True, ""
            return False, f"Lintasan terhalang: {intermediate}"
        return False, "Pawn hanya bisa maju lurus 1 kotak, atau 2 kotak dari posisi awal."

    # Capture diagonal: boleh jika ada bidak musuh
    if abs(dc) == 1 and dr == forward:
        if target is not None:
            if piece_color(target) != color:
                return True, ""   # capture musuh valid
            return False, f"Tujuan {end_pos} terisi bidak sendiri ({target})."
        return False, "Pawn hanya bisa diagonal saat capture."

    return False, "Gerakan pawn tidak valid."


def can_queue_move(start_pos, end_pos, board_state):
    if start_pos == end_pos:
        return False, "Start dan tujuan sama."

    piece = board_state.get(start_pos)
    if piece is None:
        return False, f"Tidak ada bidak di {start_pos}."

    # Cek capture: boleh jika warna berbeda, tolak jika warna sama
    occupant = board_state.get(end_pos)
    if occupant is not None:
        if piece_color(occupant) == piece_color(piece):
            return False, f"Tujuan {end_pos} terisi bidak sendiri ({occupant})."  
        # Beda warna = capture yang valid (diizinkan, validasi per jenis dilanjutkan di bawah)

    kind = piece_kind(piece)

    if kind == "R":
        return validate_rook_move(start_pos, end_pos, board_state)
    if kind == "B":
        return validate_bishop_move(start_pos, end_pos, board_state)
    if kind == "Q":
        return validate_queen_move(start_pos, end_pos, board_state)
    if kind == "K":
        return validate_king_move(start_pos, end_pos, board_state)
    if kind == "N":
        return validate_knight_move(start_pos, end_pos, board_state)
    if kind == "P":
        return validate_pawn_move(start_pos, end_pos, board_state)

    return False, f"Jenis bidak tidak dikenali: {piece}"


def get_xy_from_idx(col_idx, row_idx):
    # col_idx dan row_idx bisa berupa float (contoh: 0.5 untuk tengah-tengah kolom A dan B)
    orig_x = (col_idx * SQUARE_SIZE) + OFFSET_X
    orig_y = (row_idx * SQUARE_SIZE) + OFFSET_Y

    # Rotasi 180°
    new_x = X_MAX_LIMIT - orig_x
    new_y = Y_MAX_LIMIT - orig_y

    return (round(new_x), round(new_y))


def get_direct_waypoints(start_pos, end_pos):
    """Waypoint aman: angkat bidak, gerak langsung, lalu taruh."""
    return [plate[end_pos]]


def get_knight_waypoints(start_pos, end_pos, board_state):
    """
    Kuda bergerak L (2+1 atau 1+2 kotak).
    Prioritas: jalur L melewati tengah kotak asalkan tidak terhalang piece lain.
    Fallback: jalur 'gutter' (antara dua kotak = indeks x.5).
    Kuda boleh melompati piece (hanya kotak start & end yang diperiksa untuk validity).
    Pergerakan fisik magnet hanya tidak bisa melewati kotak yang terisi (tabrakan magnet).
    """
    sc, sr = pos_to_idx(start_pos)
    ec, er = pos_to_idx(end_pos)

    dc = ec - sc
    dr = er - sr

    step_c = 1 if dc > 0 else -1
    step_r = 1 if dr > 0 else -1

    # ==============================
    # KASUS 1: |dc|=1, |dr|=2  (geser 1 kolom, 2 baris)
    # ==============================
    if abs(dc) == 1 and abs(dr) == 2:
        corner_A = idx_to_pos(ec, sr)        # geser 1 kolom dulu, lalu naik 2 baris
        corner_B = idx_to_pos(sc, er)        # naik 2 baris dulu, lalu geser 1 kolom
        mid_row   = idx_to_pos(sc, sr + step_r)  # kotak antara start dan corner_B

        # Path A: geser 1 kolom (sc->ec, same row) -> naik 2 baris
        # Tidak ada intermediate antara start dan corner_A (hanya 1 langkah kolom)
        if board_state.get(corner_A) is None:
            # Periksa juga jalur corner_A -> end: 2 baris, intermediate = (ec, sr+step_r)
            intermediate = idx_to_pos(ec, sr + step_r)
            if board_state.get(intermediate) is None:
                return True, [plate[corner_A], plate[end_pos]]

        # Path B: naik 2 baris (same col) -> geser 1 kolom
        # Harus cek intermediate di baris tengah: (sc, sr+step_r)
        if board_state.get(corner_B) is None and board_state.get(mid_row) is None:
            # Jalur corner_B -> end: hanya 1 langkah kolom, tidak ada intermediate
            return True, [plate[corner_B], plate[end_pos]]

        # Path C: naik 1 baris (ke mid_row) -> diagonal ke target
        # Berguna jika corner_B terisi tapi mid_row kosong
        if board_state.get(mid_row) is None:
            return True, [plate[mid_row], plate[end_pos]]

        # Fallback: jalur gutter dengan transisi diagonal
        # Masuk gutter diagonal dari start, lurus di gutter, keluar diagonal ke target
        mid_c = sc + (0.5 * step_c)
        waypoints = [
            get_xy_from_idx(mid_c, sr + 0.5 * step_r),  # masuk gutter diagonal
            get_xy_from_idx(mid_c, er - 0.5 * step_r),  # posisi keluar gutter
            get_xy_from_idx(ec, er),                     # diagonal ke target
        ]
        return True, waypoints

    # ==============================
    # KASUS 2: |dc|=2, |dr|=1  (geser 2 kolom, 1 baris)
    # ==============================
    elif abs(dc) == 2 and abs(dr) == 1:
        corner_A = idx_to_pos(sc, er)        # naik 1 baris dulu -> geser 2 kolom
        corner_B = idx_to_pos(ec, sr)        # geser 2 kolom dulu -> naik 1 baris
        mid_col   = idx_to_pos(sc + step_c, sr)  # kotak antara start dan corner_B

        # Path A: naik 1 baris (same col) -> geser 2 kolom
        # Tidak ada intermediate start -> corner_A (hanya 1 baris)
        if board_state.get(corner_A) is None:
            # Periksa intermediate saat geser: (sc+step_c, er)
            intermediate = idx_to_pos(sc + step_c, er)
            if board_state.get(intermediate) is None:
                return True, [plate[corner_A], plate[end_pos]]

        # Path B: geser 2 kolom (same row) -> naik 1 baris
        # Periksa intermediate saat geser: (sc+step_c, sr)
        if board_state.get(corner_B) is None and board_state.get(mid_col) is None:
            return True, [plate[corner_B], plate[end_pos]]

        # Path C: geser 1 kolom (ke mid_col) -> diagonal ke target
        # Berguna jika corner_B terisi tapi mid_col kosong
        if board_state.get(mid_col) is None:
            return True, [plate[mid_col], plate[end_pos]]

        # Fallback: jalur gutter
        # Pakai outer gutter HANYA jika sisi-start punya piece tapi sisi-end KOSONG
        # (gaya magnet tidak seimbang -> piece bisa ikut terbawa)
        # Jika kedua sisi ada piece atau keduanya kosong -> gutter inner aman (gaya seimbang)
        inner_mid_r = sr + 0.5 * step_r
        outer_mid_r = er + 0.5 * step_r

        col_range = [c for c in range(sc + step_c, ec, step_c) if 0 <= c <= 7]
        start_side = any(board_state.get(idx_to_pos(c, sr)) for c in col_range)
        dest_side  = any(board_state.get(idx_to_pos(c, er)) for c in col_range)

        # Outer hanya jika start terisi, dest kosong DAN outer row masih dalam board
        if start_side and not dest_side and 0 <= int(outer_mid_r) <= 7:
            mid_r = outer_mid_r
        else:
            mid_r = inner_mid_r

        waypoints = [
            get_xy_from_idx(sc + 0.5 * step_c, mid_r),
            get_xy_from_idx(ec - 0.5 * step_c, mid_r),
            get_xy_from_idx(ec, er),
        ]
        return True, waypoints

    else:
        return False, []


# ==============================
# MOVE PIECE (BRING)
# ==============================

def bring(start_pos, end_pos):
    piece_id = board_pieces.get(start_pos)

    if piece_id is None:
        print(f"Bring gagal: tidak ada bidak di {start_pos}")
        return

    print(f"Memindahkan {piece_id} dari {start_pos} ke {end_pos}")

    # ==============================
    # HANDLE CAPTURE: buang bidak musuh ke kuburan terlebih dahulu
    # ==============================
    captured = board_pieces.get(end_pos)
    if captured is not None:
        print(f"[MAKAN] {piece_id} memakan {captured} di {end_pos}")
        send_to_graveyard(captured, end_pos)
        board_pieces[end_pos] = None   # kosongkan kotak tujuan di state

    # Ambil bidak penyerang
    sx, sy = plate[start_pos]
    move_to(sx, sy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    # Setelah diangkat, gerak mengikuti karakter bidaknya.
    if piece_kind(piece_id) == "N":
        valid, waypoints = get_knight_waypoints(start_pos, end_pos, board_pieces)
        if not valid:
            print("Gerakan kuda tidak valid saat eksekusi!")
            return
    else:
        waypoints = get_direct_waypoints(start_pos, end_pos)

    for wp_x, wp_y in waypoints:
        move_to(wp_x, wp_y)

    # Lepas bidak
    time.sleep(0.2)
    servo_set(False)
    time.sleep(0.5)
    servo_release()

    # Update state
    board_pieces[end_pos] = piece_id
    board_pieces[start_pos] = None
    moved_pieces.add(piece_id)


def execute_castling(king_start, king_end, rook_start, rook_end):
    """Eksekusi rokade: pindahkan raja lalu benteng via gutter"""
    king_id = board_pieces[king_start]
    rook_id = board_pieces[rook_start]

    print(f"[ROKADE] {king_id}: {king_start}->{king_end}, {rook_id}: {rook_start}->{rook_end}")

    # === Pindahkan Raja ===
    sx, sy = plate[king_start]
    move_to(sx, sy)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    tx, ty = plate[king_end]
    move_to(tx, ty)

    time.sleep(0.2)
    servo_set(False)
    time.sleep(0.5)
    servo_release()

    board_pieces[king_end] = king_id
    board_pieces[king_start] = None
    moved_pieces.add(king_id)

    # === Pindahkan Benteng via gutter (hindari raja) ===
    rx, ry = plate[rook_start]
    move_to(rx, ry)
    servo_set(True)
    time.sleep(0.5)
    servo_release()

    # Ambil indeks asal & tujuan benteng
    rc, rr = pos_to_idx(rook_start)
    tc, tr = pos_to_idx(rook_end)

    # Row 1 -> gutter ke atas (0.5)
    # Row 8 -> gutter ke bawah (-0.5)
    gut_offset = 0.5 if rr == 0 else -0.5

    # Masuk gutter dari posisi benteng
    gut_x, gut_y = get_xy_from_idx(rc, rr + gut_offset)

    # Bergerak di gutter menuju file tujuan benteng
    gut_tx, gut_ty = get_xy_from_idx(tc, tr + gut_offset)

    move_to(gut_x, gut_y)
    move_to(gut_tx, gut_ty)

    # Turun ke kotak tujuan
    rtx, rty = plate[rook_end]
    move_to(rtx, rty)

    time.sleep(0.2)
    servo_set(False)
    time.sleep(0.5)
    servo_release()

    board_pieces[rook_end] = rook_id
    board_pieces[rook_start] = None
    moved_pieces.add(rook_id)



# ==============================
# FITUR AUTO RESET
# ==============================

def get_home_pos(piece_id):
    """Mencari letak asli sebuah bidak berdasarkan INITIAL_BOARD"""
    for pos, p_id in INITIAL_BOARD.items():
        if p_id == piece_id:
            return pos
    return None

def get_empty_temp_square(current_board):
    """Mencari kotak kosong di area tengah (baris 4 & 5) untuk parkir sementara"""
    for row in "45": 
        for col in "ABCDEFGH":
            pos = col + row
            if current_board[pos] is None:
                return pos
    # Jika area tengah penuh, cari kotak kosong manapun
    for pos, occupant in current_board.items():
        if occupant is None:
            return pos
    return None

def execute_reset():
    print("\n[RESETTING] Memulai proses pengembalian bidak ke posisi semula...")
    
    while True:
        moved_in_this_pass = False
        misplaced = []

        # 1. Data ulang semua bidak yang tidak ada di rumahnya
        for current_pos, piece in board_pieces.items():
            if piece is None: continue
            
            home_pos = get_home_pos(piece)
            if home_pos and current_pos != home_pos:
                misplaced.append((piece, current_pos, home_pos))

        # Jika array misplaced kosong, berarti semua sudah rapi!
        if not misplaced:
            print("[RESET SELESAI] Semua bidak sudah di posisi awal!")
            break

        # 2. Prioritas 1: Pulangkan bidak yang rumahnya KOSONG
        for piece, current_pos, home_pos in misplaced:
            if board_pieces[home_pos] is None:
                print(f"Reset: {piece} pulang dari {current_pos} ke {home_pos}")
                bring(current_pos, home_pos)
                moved_in_this_pass = True
                break # Keluar dari loop for untuk scan ulang papan terbaru
        
        # 3. Prioritas 2 (Pecah Deadlock): Jika semua rumah tujuan terisi
        if not moved_in_this_pass and misplaced:
            piece, current_pos, home_pos = misplaced[0]
            temp_pos = get_empty_temp_square(board_pieces)
            
            if temp_pos:
                print(f"Pecah Deadlock: Parkir sementara {piece} dari {current_pos} ke {temp_pos}")
                bring(current_pos, temp_pos)
            else:
                print("ERROR: Papan terlalu penuh, tidak ada kotak kosong untuk parkir!")
                break

# ==============================
# SHELL COMMAND
# ==============================

def execute_manual_move(cmd):
    global pos_x, pos_y
    try:
        cmd = cmd.strip()

        if cmd.startswith('X') or cmd.startswith('Y'):
            axis = cmd[0]
            val = float(cmd[1:])
            if axis == 'X':
                target_x = pos_x + val
                print(f"Moving X Relatif: {pos_x} -> {target_x}")
                move_to(target_x, pos_y)
            else:
                target_y = pos_y + val
                print(f"Moving Y Relatif: {pos_y} -> {target_y}")
                move_to(pos_x, target_y)

        elif ',' in cmd:
            parts = cmd.split(',')
            if len(parts) == 2:
                target_x = float(parts[0])
                target_y = float(parts[1])
                print(f"Moving Absolut: ({pos_x},{pos_y}) -> ({target_x},{target_y})")
                move_to(target_x, target_y)

    except Exception as e:
        print(f"Gagal gerak manual: {e}")

def shell():
    global pos_x, pos_y
    queue = []
    accumulated_input = ""
    
    # Inisialisasi simulasi dari kondisi fisik papan saat ini
    simulated_board = board_pieces.copy()

    print("\n=== MODE KONTROL HYBRID (STABLE & VALIDATED) v1.95 ===")
    print("- Input dibaca dari sisi putih: contoh 'A2 A4, B2 B4'")
    print("- Contoh fisik: 'H7 H5' akan dibaca sebagai 'A2 A4'")
    print("- 'START' untuk eksekusi, 'CLEAR' untuk reset antrean")
    print("- 'RESET' untuk kembalikan semua bidak ke posisi awal")
    print("- 'LIST' untuk cek posisi bidak saat ini")
    print("================================================\n")

    while True:
        # Jika antrean kosong, pastikan simulasi sinkron dengan kenyataan fisik
        if not queue and not accumulated_input:
            simulated_board = board_pieces.copy()

        prompt = f"[{len(queue)} langkah] " if not accumulated_input else "... "
        raw_line = uart_readline(f"{prompt}Masukkan perintah: ").strip().upper()

        if not raw_line and not accumulated_input:
            continue

        # Penanganan input multi-line (akhiran koma)
        if raw_line.endswith(','):
            accumulated_input += raw_line
            continue
        else:
            full_input = accumulated_input + raw_line
            accumulated_input = ""

        # --- 1. PERINTAH NAVIGASI & UTILITY ---
        
        if full_input == "HOME":
            print("Homing...")
            home_manual(LIMIT_X, True, False)
            time.sleep(0.2)
            home_manual(LIMIT_Y, False, False)
            pos_x, pos_y = 0.0, 0.0
            print("Homing Selesai.")
            continue

        if full_input == "THROW ALL":
            execute_throw()
            continue

        if full_input == "THROW WHITE":
            execute_throw("W")
            continue

        if full_input == "THROW BLACK":
            execute_throw("B")
            continue

        if full_input == "RESET PIECES":
            execute_physical_reset()
            simulated_board = board_pieces.copy()
            continue

        if full_input == "LIST":
            print("\n--- Posisi Bidak Saat Ini (Fisik) ---")
            white_p = []
            black_p = []
            
            # Urutkan berdasarkan baris (1-8) lalu kolom (A-H)
            for r in "12345678":
                for c in "ABCDEFGH":
                    pos = c + r
                    piece = board_pieces.get(pos)
                    if piece:
                        x, y = plate[pos]
                        info = f"{pos}: {piece} (X: {x}, Y: {y})"
                        if "W" in piece:
                            white_p.append(info)
                        else:
                            black_p.append(info)
            
            print("\n[ WHITE PIECES ]")
            for p in white_p: print(p)
            
            print("\n[ BLACK PIECES ]")
            for p in black_p: print(p)
            continue

        if full_input == "CLEAR":
            queue.clear()
            simulated_board = board_pieces.copy()
            print("Antrean dan simulasi direset.")
            continue

        if full_input == "RESET":
            print("Membersihkan antrean dan memulai pemulihan posisi...")
            queue.clear()
            moved_pieces.clear()
            execute_reset()
            simulated_board = board_pieces.copy()
            continue

        if full_input == "START":
            if not queue:
                print("Antrean kosong!")
            else:
                print(f"Memulai eksekusi {len(queue)} langkah...")
                for i, move in enumerate(queue):
                    if len(move) == 4:
                        ks, ke, rs, re = move
                        print(f"[{i+1}/{len(queue)}] Rokade: {ks}->{ke}, {rs}->{re}")
                        execute_castling(ks, ke, rs, re)
                    else:
                        print(f"[{i+1}/{len(queue)}] {move[0]} -> {move[1]}")
                        bring(move[0], move[1])
                
                queue.clear()
                simulated_board = board_pieces.copy()
                print("Semua langkah selesai dijalankan.")
            continue

        if "UP" in full_input:
            state = "UP" in full_input
            servo_set(state)
            time.sleep(0.5)
            servo_release()
            print(f"{'UP' if state else 'DOWN'}")
            continue
        
        if "DOWN" in full_input:
            state = "DOWN" in full_input
            servo_set(state)
            time.sleep(0.5)
            servo_release()
            print(f"{'UP' if state else 'DOWN'}")
            continue

        # --- 2. LOGIKA PARSING LANGKAH & SIMULASI REAL-TIME ---
        
        parts = full_input.split(',')
        added_count = 0

        for part in parts:
            p = part.strip()
            if not p: continue
            
            sub_parts = p.split()
            # Cek apakah input berupa langkah catur (Misal: "G2 G3")
            if len(sub_parts) == 2 and sub_parts[0] in plate and sub_parts[1] in plate:
                raw_start, raw_end = sub_parts[0], sub_parts[1]
                start, end = normalize_shell_move(raw_start, raw_end)

                if (start, end) != (raw_start, raw_end):
                    print(f"Orientasi WHITE: {raw_start}->{raw_end} dibaca sebagai {start}->{end}")

                # VALIDASI: Menggunakan 'simulated_board' agar tahu bahwa 
                # G2 akan kosong jika langkah sebelumnya adalah G2 G3
                ok, reason = can_queue_move(start, end, simulated_board)
                
                if not ok:
                    print(f"Langkah {raw_start}->{raw_end} Ditolak: {reason}")
                    continue

                # Ambil ID bidak dari simulasi
                piece = simulated_board.get(start)

                # Cek apakah ini rokade
                if piece_kind(piece) == "K":
                    castle_ok, rs, re = is_castling(start, end, simulated_board)
                    if castle_ok:
                        queue.append((start, end, rs, re))
                        rook = simulated_board.get(rs)
                        simulated_board[end] = piece
                        simulated_board[start] = None
                        simulated_board[re] = rook
                        simulated_board[rs] = None
                        added_count += 1
                        continue

                queue.append((start, end))
                
                # UPDATE SIMULASI
                simulated_board[end] = piece
                simulated_board[start] = None
                added_count += 1

            # Cek jika input berupa gerak manual koordinat (Misal: "X100 Y200")
            elif p.startswith(('X', 'Y')) or (len(p) > 0 and p[0].isdigit()):
                execute_manual_move(p)

            else:
                print(f"Format tidak dikenal atau koordinat luar jangkauan: {p}")

        if added_count > 0:
            print(f"Berhasil menambahkan {added_count} langkah. Total antrean: {len(queue)}")

# ==============================
# SCAN ZIGZAG PLATE
# ==============================

def scan_plate(): #abjad

    cols_forward = "ABCDEFGH"
    cols_reverse = "HGFEDCBA"

    for row in range(1,9):

        if row % 2 == 1:
            order = cols_forward
        else:
            order = cols_reverse

        for col in order:

            pos = col + str(row)

            goto(pos)

def test_max_area():
    print(f"Testing boundary for 45cm board (Max: {X_MAX}mm)...")
    
    servo_set(True)
    
    # Jalur: (0,0) -> (450,0) -> (450,450) -> (0,450) -> (0,0)
    path = [(X_MIN, Y_MIN), (X_MAX, Y_MIN), (X_MAX, Y_MAX), (X_MIN, Y_MAX), (X_MIN, Y_MIN)]
    
    for x, y in path:
        move_to(x, y, speed=0.00005)
        time.sleep(0.5)
    print("Test selesai. Pastikan magnet tepat di sudut papan fisik.")
    
    servo_set(False)
# def scan_plate(): #numbers
# 
#     rows_forward = "12345678"
#     rows_reverse = "87654321"
# 
#     cols = "ABCDEFGH"
# 
#     for i,col in enumerate(cols):
# 
#         if i % 2 == 0:
#             order = rows_forward
#         else:
#             order = rows_reverse
# 
#         for row in order:
# 
#             pos = col + row
# 
#             goto(pos)

# ==============================
# MAIN
# ==============================

print("Homing...")

set_microstep(16)

home_manual(LIMIT_X, True, False)
time.sleep(0.2)

home_manual(LIMIT_Y, False, False)

print("Homing selesai")

pos_x = 0
pos_y = 0

time.sleep(1)

# ==============================
# START SCAN
# ==============================

print("Ready command")

shell()
