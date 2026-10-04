# FlexPlayer

Any video. Any device.

FlexPlayer is an Android video player that analyzes a video's compatibility with the
device's actual decoder capabilities, plays compatible originals directly, and creates
a **separate compatible copy** when required — the original file is never modified.

## Features
- **Smart Compatibility analysis** — resolution, frame rate, codec, bitrate, HDR vs. device decoder capability
- **Direct playback** of compatible videos (Jetpack Media3 / ExoPlayer)
- **Local transcoding** with Media3 Transformer (H.264/AAC MP4 output, never overwrites source)
- **Three conversion presets** — Maximum Quality / Balanced (1080p30) / Maximum Compatibility (720p30)
- **Smart Share** — share Original, a Smart Compatible copy, or a Smaller File
- **Conversion cache** — Room-backed, converted copies are reused, deletable
- 100% on-device processing (privacy-friendly)

## Tech stack
Kotlin · Jetpack Compose · Material 3 · MVVM + Clean Architecture · Coroutines/StateFlow
· Media3 (ExoPlayer + Transformer) · MediaCodec · MediaStore · Room · DataStore · Hilt

## Project structure
```
app/src/main/java/com/flexplayer/app/
├── model/        VideoInfo, DeviceProfile, CompatibilityResult, ConversionPreset
├── data/         local (Room), media (VideoScanner), repository (Video/Conversion/Prefs)
├── media/        PlayerManager, VideoAnalyzer, DeviceCapabilityManager,
│                 CompatibilityEngine, VideoTranscoder
├── sharing/      SmartShareManager
└── ui/           Home, Player, Compatibility, Conversion, Share, Converted, Theme
```

## Build
1. Install Android Studio (latest stable) + Android SDK 35
2. Open this folder, let Gradle sync
3. Run on a device/emulator (minSdk 26)

## Notes
- Decoder capability ≠ display resolution: a 4K30 HEVC file may be converted to
  1080p30 H.264 even on a 1080p phone.
- Standard Android sharing cannot reveal recipient hardware, so "Smart Compatible"
  uses a user-selected preset.
