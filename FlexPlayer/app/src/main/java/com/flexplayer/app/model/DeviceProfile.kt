package com.flexplayer.app.model

/**
 * Snapshot of the hardware/software video decoding capabilities of this
 * device, derived from MediaCodecList. Deliberately NOT the display
 * resolution — decoder capability is what matters for playback.
 */
data class DeviceProfile(
    val deviceName: String,
    val maxDecoderWidth: Int,
    val maxDecoderHeight: Int,
    val maxFrameRate: Int,
    val supportedCodecs: Set<String>,   // mime types, e.g. "video/hevc"
    val supportsHdr: Boolean,
    val maxBitrate: Long
) {
    fun supports(mimeType: String): Boolean = mimeType in supportedCodecs
}
