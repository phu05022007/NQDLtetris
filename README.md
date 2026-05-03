# Tetris (JavaFX)

> A simple JavaFX Tetris implementation (canvas-based renderer).

## Overview

This repository contains a small Tetris game implemented in Java using JavaFX. It can be built with Maven and packaged into a platform-specific runtime image using `jlink`/`jpackage` (see Packaging below).

English README is available: [README_EN.md](README_EN.md)

## Requirements

- JDK 17 (full JDK required for `jlink`/`jpackage`).
- Maven 3.6+ (or the included local Maven wrapper at `.maven/apache-maven-3.9.4`).
- (Optional) WiX Toolset on Windows to produce an MSI installer with `jpackage`.

Recommended downloads:
- Adoptium Temurin JDK 17: https://adoptium.net/
- OpenJFX (if you need a separate SDK): https://openjfx.io/

## Build

From the project root, run:

```powershell
mvn clean package
```

If `mvn` is not available in PATH and you used the local Maven we prepared, run:

```powershell
.\.maven\apache-maven-3.9.4\bin\mvn.cmd clean package
```

This produces `target/tetris-1.0-SNAPSHOT.jar`.

## Run (development)

- Run via Maven (recommended for development):

```powershell
mvn javafx:run
```

- Run the packaged JAR (requires JavaFX on the module path):

```powershell
java --module-path "C:\path\to\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.graphics -jar target\tetris-1.0-SNAPSHOT.jar
```

Replace the module-path with your local JavaFX SDK path if needed.

## How to play

Quick controls:

- Di chuyển trái/phải: Left / Right
- Xoay: Up hoặc Space
- Rơi mềm: Down
- Hard drop: Space (instant drop)
- Giữ khối: C
- Bắt đầu: Enter
- Tạm dừng/Resume: P / R
- Toggle help overlay: H

Gameplay:

- Mục tiêu: Hoàn thành các hàng ngang để ghi điểm. 1 hàng = 100, 2 hàng = 300, 3 hàng = 500, 4 hàng (Tetris) = 800.
- Sử dụng phím `C` để giữ một tetromino và đổi nó khi cần chiến lược.
- Bạn có thể mở hướng dẫn trong game bằng phím `H`.

Tips:

- Tập trung lấp đầy các hàng ở phần đáy, tránh tạo lỗ cao.
- Dùng hard-drop để đặt khối nhanh khi cần.


## Packaging (create a runtime image)

This project contains a `native` Maven profile configured to create a runtime image using the JavaFX Maven plugin.

- Create a runtime image (jlink):

```powershell
mvn -Pnative -DskipTests=true org.openjfx:javafx-maven-plugin:0.0.8:jlink
```

On success you will find a zip and runtime image under `target/`:

- Archive: [target/Tetris-1.0.0.zip](target/Tetris-1.0.0.zip)
- Runtime image folder: [target/Tetris](target/Tetris)

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

## Troubleshooting

- jlink error `could not open .../lib/jvm.cfg`: ensure `JAVA_HOME` points to a full JDK 17 distribution (not a JRE). The JDK root must contain `lib\jvm.cfg` and a `jmods` directory.
- If JavaFX modules are not found at runtime, either run via the Maven plugin (`mvn javafx:run`) or supply a JavaFX SDK on `--module-path`.

## Submission / Zip for transfer

On Windows you can compress the runtime image for submission:

```powershell
Compress-Archive -Path target\Tetris -DestinationPath tetris-windows.zip
```

Or compress the produced zip at `target/Tetris-1.0.0.zip` and submit that file.

## Files of interest

- [src/main/java/module-info.java](src/main/java/module-info.java)
- [src/main/java/tetris/ui/TetrisFxAppExample.java](src/main/java/tetris/ui/TetrisFxAppExample.java)
- [src/main/java/tetris/ui/BoardRenderer.java](src/main/java/tetris/ui/BoardRenderer.java)
- [pom.xml](pom.xml)

## Credits

Author: Prime Studio

