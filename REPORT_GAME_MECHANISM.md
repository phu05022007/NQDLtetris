# Bao Cao Co Che Game Tetris

## 1) Tong quan kien truc

Game duoc to chuc theo huong tach 3 lop:

- `model`: du lieu game (`Board`, `Tetromino`, `TetrominoFactory`)
- `engine`: logic va state machine (`GameEngine`, cac `GameState`)
- `ui`: JavaFX render + input (`TetrisFxAppExample`, `BoardRenderer`, `HoldPanelRenderer`)

Muc tieu cua cach tach lop:

- Model khong phu thuoc UI
- Engine khong phu thuoc JavaFX
- UI chi lam viec hien thi va chuyen input thanh `GameAction`

## 2) Vong doi chay game

### 2.1 Khoi tao

Trong `TetrisFxAppExample`:

1. Tao `GameEngine`
2. Tao renderer (`BoardRenderer`) va panel hold/next (`HoldPanelRenderer`)
3. Dang ky listener hold/next vao engine
4. Hien overlay chon ngon ngu
5. Sau khi chon ngon ngu -> hien overlay chon level
6. Khi chon level:
   - set level
   - reset game
   - chuyen state sang `PLAYING`
   - start game loop (`engine.start(renderer)`)
   - start input loop (`startInputLoop()`)

### 2.2 Game loop chinh (engine)

`GameEngine` dung `AnimationTimer` voi 2 accumulator:

- `updateAccumulatorNs`: tich luy de goi `update()` theo toc do roi
- `renderAccumulatorNs`: tich luy de render gan 60 FPS

Tinh nang:

- Toc do roi tang theo level (`updateIntervalNs` giam dan)
- Khoa input/update trong luc dang animation clear line hoac swap hold
- Ho tro overlay menu/pause/game over qua `GameRenderer`

## 3) State machine

Game dung State Pattern, gom 4 state:

- `MenuState`
- `PlayingState`
- `PausedState`
- `GameOverState`

### 3.1 Chuyen state

- `Menu -> Playing`: nhan `START`
- `Playing -> Paused`: nhan `PAUSE`
- `Paused -> Playing`: nhan `RESUME`
- `Paused/GameOver -> Playing`: nhan `RESTART` (reset game truoc)
- `Playing -> GameOver`: spawn piece moi bi collision
- `Paused/GameOver -> Menu`: `BACK_TO_MENU`

Muc dich:

- Moi state chi xu ly nhung input hop le cho man hinh hien tai
- Logic gameplay khong bi chay khi dang pause/menu/game over

## 4) Co che model

## 4.1 Board

`Board` la ma tran `20 x 10`:

- `grid[row][col] = 0` la o trong
- `1..7` la id mau cua tetromino da lock

Ham chinh:

- `checkCollision(...)`: va cham tuong trai/phai, day, block da lock
- `lock(tetromino)`: ghi piece vao `grid`
- `getFullLines()`: tim cac hang day
- `removeLines(rows)`: xoa nhieu hang (tu duoi len)
- pending clear API:
  - `setPendingClearRows(...)`
  - `clearPendingClearRows()`
  - phuc vu animation flash truoc khi xoa that

## 4.2 Tetromino va Factory

- `Tetromino` luu vi tri `(x, y)`, `shape`, `id`
- Ho tro xoay xuoi/nguoc chieu kim dong ho
- `TetrominoFactory`:
  - tao piece moi theo type
  - random type
  - map `id <-> TetrominoType`
  - `createFrom(...)` de copy piece (giu nguyen huong xoay), dung cho hold/swap

## 5) Gameplay logic chi tiet

## 5.1 Roi xuong va khoa khoi

Trong `PlayingState.update()` goi `engine.stepDown()`:

1. Neu chua co piece hien tai -> spawn piece
2. Thu dich y + 1
3. Neu collision:
   - lock piece vao board
   - neu co hang day: bat dau animation clear
   - neu khong: clear line ngay, tinh diem, spawn piece tiep
4. Neu khong collision: cap nhat vi tri piece

## 5.2 Hard drop

Khi nhan `HARD_DROP`:

1. Tinh `ghostY` (vi tri roi sau cung hop le)
2. Dua piece den `ghostY`
3. Lock piece
4. Neu co line clear -> dung animation nhanh hon
5. Neu khong co line clear -> spawn piece moi

## 5.3 Hold / Swap

Khi nhan `HOLD`:

- Neu chua co hold:
  - copy piece hien tai vao hold
  - spawn piece tiep theo
- Neu da co hold:
  - tao `swapped` tu hold tai vi tri piece hien tai
  - tao `newHeld` tu piece hien tai
  - check collision cho `swapped`, thu kick ngang/doc nhe
  - neu van collision: huy swap
  - neu hop le: bat dau swap flash animation, ket thuc moi commit swap

Luu y: co che hold hien tai la unlimited hold.

## 5.4 Ghost piece

`getGhostY()` mo phong roi thang den vi tri sau cung khong collision.
Renderer ve piece bong mo (opacity thap) de goi y vi tri dat.

## 5.5 Animation clear line

Khi co line day:

1. Danh dau `pendingClearRows`
2. BoardRenderer flash vang/do theo chu ky
3. Het thoi gian animation:
   - xoa line that (`removeLines`)
   - clear pending marker
   - cong diem + tong so line
   - spawn piece moi
   - neu spawn collision -> `GameOverState`

## 6) Input system

UI tach input thanh 2 nhom:

- One-shot (xu ly ngay khi key down): `ENTER`, `UP`, `SPACE`, `P`, `C`, `H`, `R`, `ESC`
- Hold-to-repeat: `LEFT`, `RIGHT`, `DOWN` qua `startInputLoop()`

Co che hold-to-repeat:

- Delay luc dau (`MOVE_INITIAL_DELAY_NS`)
- Lap lai theo chu ky (`MOVE_REPEAT_NS`, `SOFT_DROP_REPEAT_NS`)
- Neu tao input loop moi, timer cu duoc stop truoc de tranh duplicate input

## 7) Render va giao dien

## 7.1 BoardRenderer

Render cac lop:

1. Nen va grid
2. Block da lock
3. Overlay flashing line clear (neu co)
4. Swap flash tetromino (neu co)
5. Ghost tetromino
6. Active tetromino
7. Overlay panel (menu/pause/game over)

## 7.2 HoldPanelRenderer

Panel ben phai ve:

- `NEXT` preview
- `HOLD` preview

Cap nhat qua listener:

- `onNextPieceChanged(...)`
- `onHoldPieceChanged(...)`

## 7.3 Overlay trong UI

Ngoai overlay do state render (`BoardRenderer.drawOverlay`), UI con co:

- Language overlay
- Level overlay
- Help overlay
- Pause overlay

Nhung overlay nay la JavaFX node tren `StackPane`, co the show/hide linh hoat.

## 8) Tinh diem va level

## 8.1 Diem

`calculateScore(clearedLines)`:

- 1 line -> 100
- 2 lines -> 300
- 3 lines -> 500
- 4 lines -> 800

## 8.2 Level va toc do

- Level toi da: 10
- Cong thuc toc do:
  - `updateInterval = base / (LEVEL_SPEED_FACTOR^(level-1))`
- Nghia la level tang thi piece roi nhanh hon

## 9) Cac diem mo rong de phat trien tiep

- Them test cho collision, clear line, hold/swap, transition state
- Them SRS wall-kick day du cho rotate
- Them lock delay, combo, back-to-back, T-spin
- Them save high score va profile nguoi choi
- Dong bo text da ngon ngu hoa cho toan bo label UI

## 10) Ket luan

Game hien tai da co day du co che co ban va nang cao:

- state machine ro rang
- next/hold preview
- hard drop + ghost piece
- clear line animation
- pause/menu/game over overlays

Kien truc tach model-engine-ui giup de bao tri, de test va de mo rong tinh nang trong cac buoc tiep theo.
