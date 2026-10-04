package com.flexplayer.app.model

/** Immutable description of a video file discovered on device. */
data class VideoInfo(
    val id: Long,
    val uri: String,
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val codec: String,          // e.g. "h264", "hevc", "vp9", "av1"
    val mimeType: String,       // e.g. "video/mp4"
    val bitrate: Long,
    val isHdr: Boolean,
    val sizeBytes: Long
) {
    val resolutionLabel: String get() = "${width}x${height}"
}
