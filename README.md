# Tetris (JavaFX)

Game Tetris viết bằng Java 17 + JavaFX, render bằng Canvas.

## Yêu cầu

- JDK 17
- Maven 3.6+ (hoặc Maven cục bộ tại `.maven/apache-maven-3.9.4`)

## Chạy nhanh (dev)

```powershell
mvn javafx:run
```

Nếu chưa có `mvn` trong `PATH`:

```powershell
.\.maven\apache-maven-3.9.4\bin\mvn.cmd javafx:run
```

## Build

```powershell
mvn clean package
```

Kết quả: `target/tetris-1.0-SNAPSHOT.jar`

## Điều khiển

- Di chuyển: `Left` / `Right`
- Xoay: `Up`
- Rơi mềm: `Down`
- Hard drop: `Space`
- Hold: `C`
- Bắt đầu: `Enter`
- Tạm dừng: `P`
- Tiếp tục / chơi lại trong pause-gameover: `R` / `Enter` (tùy màn hình)
- Hiện/ẩn trợ giúp: `H`

## Tính điểm

- 1 hàng: 100
- 2 hàng: 300
- 3 hàng: 500
- 4 hàng (Tetris): 800

## Tài liệu cơ chế game

Xem file `REPORT_GAME_MECHANISM.md` để đọc đầy đủ kiến trúc và luồng hoạt động.



