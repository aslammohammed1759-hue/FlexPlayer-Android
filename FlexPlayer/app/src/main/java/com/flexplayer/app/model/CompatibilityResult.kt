package com.flexplayer.app.model

enum class CompatibilityIssue {
    UNSUPPORTED_CODEC,
    RESOLUTION_EXCEEDS_DECODER,
    FRAME_RATE_TOO_HIGH,
    HDR_NOT_SUPPORTED,
    BITRATE_TOO_HIGH
}

sealed interface CompatibilityResult {
    val video: VideoInfo

    data class Compatible(
        override val video: VideoInfo,
        val notes: List<String> = emptyList()
    ) : CompatibilityResult

    data class NeedsConversion(
        override val video: VideoInfo,
        val issues: List<CompatibilityIssue>,
        val recommendedPreset: ConversionPreset,
        val explanation: String
    ) : CompatibilityResult
}
