# Sample Task App

Kotlin Multiplatform + Compose Multiplatform prototype that guides a field agent through a structured “Sample Task” workflow. The Android build is production-ready; iOS stubs exist so the shared layer compiles but platform implementations still need work.

## Features

- **Guided 7 screen flow** from onboarding to task history.
- **Task types**: Text Reading, Image Description, Photo Capture.
- **Audio control surface**: press-and-hold recording, auto validation (10–20 s), inline playback.
- **Noise gate**: live decibel meter that must remain below 40 dB to proceed.
- **CameraX integration** with runtime binding and previews.
- **Local persistence**: SQLDelight-backed repository with live Flow updates.
- **Sample data fallback** ensures offline usability (Ktor fetch + bundled catalog).

## Technology Stack

### Shared (commonMain)
- Compose Multiplatform (UI + Navigation)
- Ktor Client + kotlinx.serialization
- SQLDelight runtime
- Expect/actual abstractions for recorder/player/noise detector/camera

### Android
- MediaRecorder + MediaPlayer
- CameraX + Lifecycle bindings
- Coil for AsyncImage (invoked via `PlatformAsyncImage` actual)

## Project Structure

```
SampleTaskApp/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/
│   │   │   │   ├── com/joshtalk/sampletask/
│   │   │   │   │   ├── data/          # Repository, API Service
│   │   │   │   │   ├── domain/        # Data models
│   │   │   │   │   ├── navigation/    # Navigation routes
│   │   │   │   │   ├── platform/      # Expect declarations
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   │   │   ├── screens/     # All 7 screens
│   │   │   │   │   │   └── theme/       # Colors
│   │   │   │   │   └── App.kt         # Main App composable
│   │   │   └── sqldelight/            # Database schema
│   │   └── androidMain/
│   │       ├── kotlin/
│   │       │   └── com/joshtalk/sampletask/
│   │       │       ├── platform/      # Android implementations
│   │       │       └── MainActivity.kt
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Setup & Build

### Prerequisites
- Android Studio Iguana+ with Kotlin Multiplatform plugins enabled
- JDK 17 (embedded Studio JDK works)
- Android SDK Platform 34 + CameraX + Google USB driver (for physical devices)

### Build Commands

```bash
# Sync dependencies
./gradlew composeApp:dependencies

# Build Android app
./gradlew composeApp:assembleDebug

# Install on a connected device / emulator
./gradlew composeApp:installDebug

# Run JVM/unit tests
./gradlew test
```

## Permissions Required

The Android flavor requests:

- `INTERNET` – Ktor fetches passages/images from DummyJSON (fallback sample data bundled).
- `CAMERA` – Required for the CameraX capture task.
- `RECORD_AUDIO` – Needed by both Ambient Noise and task recordings.
- `WRITE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` (maxSdk 32) – Legacy media compatibility.

## API Integration

- **GET https://dummyjson.com/products** provides the catalog. On failure or empty result the app falls back to `SampleData.sampleProducts`, so QA can run offline.

## Database Schema

```sql
CREATE TABLE Task (
    taskId TEXT PRIMARY KEY,
    taskType TEXT NOT NULL,
    text TEXT,
    imageUrl TEXT,
    imagePath TEXT,
    audioPath TEXT NOT NULL,
    durationSec INTEGER NOT NULL,
    timestamp TEXT NOT NULL
);
```

## App Flow

1. **Start Screen** → Click "Start Sample Task"
2. **Noise Test** → Measures ambient noise (must be < 40 dB to proceed)
3. **Task Selection** → Choose Text Reading, Image Description, Photo Capture, or jump to History.
4. **Recording Task** → Complete the chosen workflow. Validation blocks out-of-range recordings and missing descriptions.
5. **Task History** → Aggregates count & total duration, previews stored media.

## Key Components

### Audio Recording
- Press and hold to record
- Automatic validation (10-20 seconds)
- Error messages for invalid durations
- Playback support

### Noise Detection
- Real-time decibel meter
- Color-coded gauge (blue = safe, red = too loud)
- 3-second averaging

### Camera Integration
- CameraX binding happens lazily when the first capture is requested.
- Captures land in the cache directory, then feed the history list.
- UI exposes retake, description text box, plus optional audio description.

## Development Notes

- **Platform Abstractions**: `expect/actual` pattern for platform-specific features
- **Navigation**: Jetpack Compose Navigation with sealed class routes
- **State Management**: Compose state and coroutines
- **Data Layer**: Repository pattern with Flow-based reactive data

## Future Enhancements

- Real iOS media implementations (currently stubs returning errors).
- Cloud sync or export pipeline for compliancy reporting.
- Advanced audio heuristics (noise gating, transcripts).
- Multi-language UI copy + translation toggle.
- Agent analytics dashboard.

## APK Build
https://drive.google.com/drive/folders/1i-F2wFAFLWbp7r57WyAT54xrSOV7xl5w

## License

This is a prototype application for demonstration purposes.
