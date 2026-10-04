package com.flexplayer.app.model

/**
 * Conversion targets. H.264/AAC in MP4 is chosen as the universal baseline
 * because virtually every Android device decodes it in hardware.
 */
enum class ConversionPreset(
    val label: String,
    val maxWidth: Int,
    val maxHeight: Int,
    val maxFrameRate: Int,
    val targetCodecMime: String,   // "video/avc"
    val targetAudioMime: String,   // "audio/mp4a-latm"
    val container: String,         // "mp4"
    val targetVideoBitrate: Long,
    val targetAudioBitrate: Int
) {
    MAXIMUM_QUALITY(
        "Maximum Quality", 3840, 2160, 60,
        "video/avc", "audio/mp4a-latm", "mp4",
        40_000_000, 320_000
    ),
    BALANCED(
        "Balanced (1080p)", 1920, 1080, 30,
        "video/avc", "audio/mp4a-latm", "mp4",
        12_000_000, 192_000
    ),
    MAXIMUM_COMPATIBILITY(
        "Maximum Compatibility (720p)", 1280, 720, 30,
        "video/avc", "audio/mp4a-latm", "mp4",
        5_000_000, 128_000
    );

    /** Scale a video to fit inside this preset while keeping aspect ratio. */
    fun scaleFor(width: Int, height: Int): Pair<Int, Int> {
        val wRatio = maxWidth.toDouble() / width
        val hRatio = maxHeight.toDouble() / height
        val scale = minOf(wRatio, hRatio, 1.0)
        val newW = (width * scale).toInt() and 0xFFFC  // multiple of 4 for encoder
        val newH = (height * scale).toInt() and 0xFFFC
        return maxOf(newW, 16) to maxOf(newH, 16)
    }
}
