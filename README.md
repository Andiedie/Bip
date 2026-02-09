# Bip

<p align="center">
  A simple voice stopwatch with Bluetooth remote support.
</p>

<p align="center">
  <a href="README_CN.md">中文文档</a>
</p>

## Features

- Volume key trigger for timing
- Chinese TTS voice announcement
- Screen tap as backup trigger
- Auto audio ducking when music is playing
- Screen stays on during use

## Usage

1. Press volume key → Start timing (plays "Bip" sound)
2. Press again → Voice announces elapsed time
3. Press again → Reset and start new timing

No manual reset needed.

## Build

```bash
./gradlew assembleDebug
```

Or in Android Studio: `Build` → `Build APK(s)`

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- MVVM architecture
- Coroutines + StateFlow

## Project Structure

```
app/src/main/java/com/bip/
├── MainActivity.kt       # Entry, key interception
├── TimerState.kt         # Timer state sealed class
├── TimerViewModel.kt     # Core logic, debounce
├── AudioService.kt       # TTS + sound effects
└── ui/
    ├── TimerScreen.kt    # Compose UI
    └── theme/            # Colors and theme
```

## Requirements

- Android 8.0+ (API 26)
- TTS engine with Chinese support

## Tested On

- OPPO Find X8s

## Known Issues

Some Chinese Android devices don't have TTS engine pre-installed. The app will prompt you to install one on first launch.

## License

MIT
