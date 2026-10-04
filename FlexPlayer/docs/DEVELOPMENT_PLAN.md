# FlexPlayer — Development Plan

## Phase 0 — Setup (Day 1)
- Create project in Android Studio (Empty Compose Activity, Kotlin DSL)
- Wire Gradle: Compose BOM, Media3 (exoplayer, ui, transformer, effect),
  Room + KSP, DataStore, Hilt
- Add Hilt application class, manifest entries (FileProvider, VIEW intent filter)

## Phase 1 — Media discovery (Days 2–3)
- `VideoScanner`: MediaStore query, map to `VideoInfo` basics
- `VideoRepository.observeVideos()`: emit basics, then re-emit detailed rows
- `HomeScreen` + `HomeViewModel`: LazyColumn of videos with loading state

## Phase 2 — Analysis engine (Days 4–6)
- `VideoAnalyzer`: MediaMetadataRetriever (duration/size/rotation) +
  MediaExtractor/MediaFormat (codec, fps, HDR transfer function)
- `DeviceCapabilityManager`: enumerate MediaCodecList decoders, aggregate max
  resolution/fps/bitrate/HDR support → `DeviceProfile`
- `CompatibilityEngine`: pure function VideoInfo × DeviceProfile →
  `Compatible | NeedsConversion(issues, recommendedPreset, explanation)`

## Phase 3 — Playback (Days 7–8)
- `PlayerManager` (ExoPlayer wrapper), `PlayerScreen` (PlayerView in AndroidView)
- Play original when compatible; route to conversion flow otherwise

## Phase 4 — Transcoding (Days 9–14)
- `VideoTranscoder` on Media3 Transformer: EditedMediaItem + Presentation effect,
  H.264/AAC MP4 output, callbackFlow events (Progress/Completed/Failed/Cancelled)
- `ConversionScreen/ViewModel`: preset picker, progress bar, cancel
- Output to app-private `filesDir/converted/`; delete partial files on failure/cancel
- Cache: `ConversionRepository` (Room) reuse-by-(source,preset)

## Phase 5 — Smart Share (Days 15–16)
- `SmartShareManager`: Original (content Uri) / Smart Compatible / Smaller File
- FileProvider for converted copies; share sheet via ACTION_SEND

## Phase 6 — Polish & hardening (Days 17–21)
- Compatibility report screen (explain each issue in plain language)
- Converted-copies screen with delete
- PreferencesRepository: default preset via DataStore
- Error handling, dark theme, edge cases (rotation metadata, missing codec info)

## Testing strategy
- Unit tests: `CompatibilityEngine`, `ConversionPreset.scaleFor` (pure logic)
- Device tests: analyzer against real HEVC/HDR files; transcoder on low-end device
- Manual matrix: minSdk 26 emulator + 2 physical devices with different decoder sets

## Risks & mitigations
| Risk | Mitigation |
|---|---|
| Media3 Transformer can't transcode some HDR inputs | Catch ExportException, fall back to "Balanced" preset, message user |
| Codec info missing from container | Treat as "unknown", offer manual conversion |
| Battery/thermal during transcode | Chunked processing, allow cancel, run as foreground service in v1.1 |
| HDR→SDR tone mapping varies by OEM | Document; target SDR H.264 output for compatibility presets |
