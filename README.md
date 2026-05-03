# Tetris (JavaFX)

A simple JavaFX Tetris implementation (canvas-based renderer).

---

## English

### Overview

This repository contains a small Tetris game implemented in Java using JavaFX. It can be built with Maven and packaged into a platform-specific runtime image using `jlink`/`jpackage` (see Packaging below).

### Requirements

- JDK 17 (full JDK required for `jlink`/`jpackage`).
- Maven 3.6+ (or the included local Maven wrapper at `.maven/apache-maven-3.9.4`).
- (Optional) WiX Toolset on Windows to produce an MSI installer with `jpackage`.

Recommended downloads:
- Adoptium Temurin JDK 17: https://adoptium.net/
- OpenJFX (if you need a separate SDK): https://openjfx.io/

### Build

From the project root, run:

```powershell
mvn clean package
```

If `mvn` is not available in PATH and you used the local Maven provided, run:

```powershell
.\.maven\apache-maven-3.9.4\bin\mvn.cmd clean package
```

This produces `target/tetris-1.0-SNAPSHOT.jar`.

### Run (development)

- Run via Maven (recommended for development):

```powershell
mvn javafx:run
```

- Run the packaged JAR (requires JavaFX on the module path):

```powershell
java --module-path "C:\path\to\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.graphics -jar target\tetris-1.0-SNAPSHOT.jar
```

Replace the module-path with your local JavaFX SDK path if needed.

### How to play

Controls:

- Move: Left / Right
- Rotate: Up or Space
- Soft drop: Down
- Hard drop: Space
- Hold: C
- Start: Enter
- Pause / Resume: P / R
- Toggle help overlay: H

Objective:

- Clear horizontal lines to score points. 1 line = 100, 2 lines = 300, 3 lines = 500, 4 lines (Tetris) = 800.

### Packaging (create a runtime image)

This project contains a `native` Maven profile configured to create a runtime image using the JavaFX Maven plugin.

- Create a runtime image (jlink):

```powershell
mvn -Pnative -DskipTests=true org.openjfx:javafx-maven-plugin:0.0.8:jlink
```

On success you will find a zip and runtime image under `target/`:

- Archive: `target/Tetris-1.0.0.zip`
- Runtime image folder: `target/Tetris`

Unzip the archive (or open the runtime folder) and run the game on Windows with:

```powershell
.\target\Tetris\bin\Tetris.bat
# or double-click bin\Tetris.exe
```

- Create a native installer (Windows) with `jpackage` via the same plugin:

```powershell
mvn -Pnative -DskipTests=true org.openjfx:javafx-maven-plugin:0.0.8:jpackage
```

Note: `jpackage` on Windows requires the WiX Toolset to be installed and on `PATH`.

### Submission / Zip for transfer

On Windows you can compress the runtime image for submission:

```powershell
Compress-Archive -Path target\Tetris -DestinationPath tetris-windows.zip
```

Or submit the produced zip at `target/Tetris-1.0.0.zip`.

### Files of interest

- `src/main/java/module-info.java`
- `src/main/java/tetris/ui/TetrisFxAppExample.java`
- `src/main/java/tetris/ui/BoardRenderer.java`
- `pom.xml`

### Credits

Author: Prime Studio

### Changelog / Recent changes (English)

- Hold behavior: holds are now unlimited. When you swap with `C`, the piece brought from hold is placed at the position of the piece that was swapped out. If the swapped-in piece collides at that location, it is treated as a game-over condition (same behavior as a spawn collision).
- Next-piece & Hold UI: the right-side panel now shows a `NEXT` preview above the `HOLD` box, and both previews are centered inside their boxes.
- Info panel: a compact framed info panel was added next to the hold box showing `Score`, `Lines`, and a short legend of control keys (arrow keys, `C`, `Space`, etc.). A separator visually divides status and controls.
- Hard-drop clears: when lines are cleared as a result of a hard drop, the clear animation uses a faster, snappier flash timing to better signal the instant placement.
- Styling: the UI uses the bundled `Fredoka One` font and indigo-accented borders/shadows across overlays.

---

## Tiếng Việt

### Tổng quan

Kho chứa này chứa một trò chơi Tetris nhỏ được triển khai bằng Java và JavaFX. Có thể build bằng Maven và đóng gói thành runtime image cho nền tảng cụ thể bằng `jlink`/`jpackage` (xem phần Packaging bên dưới).

### Yêu cầu

- JDK 17 (cần JDK đầy đủ để dùng `jlink`/`jpackage`).
- Maven 3.6+ (hoặc Maven bản cục bộ tại `.maven/apache-maven-3.9.4`).
- (Tùy chọn) WiX Toolset trên Windows nếu bạn muốn tạo MSI bằng `jpackage`.

### Xây dựng

Từ thư mục dự án, chạy:

```powershell
mvn clean package
```

Nếu `mvn` không có trong `PATH` và bạn có Maven cục bộ, chạy:

```powershell
.\.maven\apache-maven-3.9.4\bin\mvn.cmd clean package
```

Kết quả: `target/tetris-1.0-SNAPSHOT.jar`.

### Chạy (phát triển)

- Chạy qua Maven (khuyến nghị cho phát triển):

```powershell
mvn javafx:run
```

- Chạy JAR đã đóng gói (cần JavaFX trên module path):

```powershell
java --module-path "C:\path\to\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.graphics -jar target\tetris-1.0-SNAPSHOT.jar
```

### Cách chơi

Phím điều khiển:

- Di chuyển: ← / →
- Xoay: ↑ hoặc Space
- Rơi mềm: ↓
- Rơi nhanh (hard drop): Space
- Giữ khối: C
- Bắt đầu: Enter
- Tạm dừng / Tiếp tục: P / R
- Hiện/ẩn hướng dẫn: H

Mục tiêu:

- Hoàn thành các hàng ngang để ghi điểm. 1 hàng = 100, 2 hàng = 300, 3 hàng = 500, 4 hàng (Tetris) = 800.

### Đóng gói (tạo runtime image)

- Tạo runtime image (jlink):

```powershell
mvn -Pnative -DskipTests=true org.openjfx:javafx-maven-plugin:0.0.8:jlink
```

Sau khi thành công, bạn sẽ tìm thấy file zip và runtime image trong `target/`:

- Zip: `target/Tetris-1.0.0.zip`
- Thư mục runtime: `target/Tetris`

Giải nén hoặc mở thư mục runtime và chạy trên Windows:

```powershell
.\target\Tetris\bin\Tetris.bat
```

### Nộp bài / Nén để gửi

Trên Windows bạn có thể nén runtime image:

```powershell
Compress-Archive -Path target\Tetris -DestinationPath tetris-windows.zip
```

### Tệp quan trọng

- `src/main/java/module-info.java`
- `src/main/java/tetris/ui/TetrisFxAppExample.java`
- `src/main/java/tetris/ui/BoardRenderer.java`
- `pom.xml`

### Ghi chú / Changelog (Tiếng Việt)

- Hold: bây giờ có thể `hold` bao nhiêu lần cũng được. Khi nhấn `C` để hoán đổi, khối được đưa từ ô `HOLD` sẽ xuất hiện tại đúng vị trí của khối hiện thời (khối bị thay). Nếu khối mới va chạm tại vị trí đó, sẽ được xử lý như tình huống `game over`.
- Giao diện `NEXT`/`HOLD`: thanh bên phải hiện hiển thị ô dự đoán `NEXT` ở trên ô `HOLD`, cả hai được căn giữa trong khung.
- Bảng thông tin: thêm khung thông tin hiển thị `Score`, `Lines` và chú giải nhanh các phím điều khiển (mũi tên, `C`, `Space`, ...). Có một đường ngăn cách giữa phần trạng thái và phần hướng dẫn phím.
- Hiệu ứng hard-drop: khi clear hàng do hard-drop, hiệu ứng nhấp nháy sẽ nhanh hơn để nhấn mạnh việc đặt nhanh.
- Giao diện: đã tích hợp font `Fredoka One` và các viền/đổ bóng tông màu indigo cho các overlay.



