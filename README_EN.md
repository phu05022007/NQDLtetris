# Tetris (JavaFX)

A simple JavaFX Tetris implementation (canvas-based renderer).

## Overview

This repository contains a small Tetris game implemented in Java using JavaFX. It can be built with Maven and packaged into a platform-specific runtime image using `jlink`/`jpackage` (see Packaging below).

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

If `mvn` is not available in PATH and you used the local Maven provided, run:

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

## Packaging (create a runtime image)

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

## Submission / Zip for transfer

On Windows you can compress the runtime image for submission:

```powershell
Compress-Archive -Path target\Tetris -DestinationPath tetris-windows.zip
```

Or submit the produced zip at `target/Tetris-1.0.0.zip`.

## Files of interest

- `src/main/java/module-info.java`
- `src/main/java/tetris/ui/TetrisFxAppExample.java`
- `src/main/java/tetris/ui/BoardRenderer.java`
- `pom.xml`

## Credits

Author: Prime Studio
