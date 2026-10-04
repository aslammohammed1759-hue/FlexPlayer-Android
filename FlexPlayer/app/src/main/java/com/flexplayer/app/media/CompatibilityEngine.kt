package com.flexplayer.app.media

import com.flexplayer.app.model.CompatibilityIssue
import com.flexplayer.app.model.CompatibilityResult
import com.flexplayer.app.model.ConversionPreset
import com.flexplayer.app.model.DeviceProfile
import com.flexplayer.app.model.VideoInfo
import javax.inject.Inject
import javax.inject.Singleton

/** Pure decision logic: compare a VideoInfo against a DeviceProfile. */
@Singleton
class CompatibilityEngine @Inject constructor() {

    fun evaluate(
        video: VideoInfo,
        profile: DeviceProfile,
        fallbackPreset: ConversionPreset = ConversionPreset.BALANCED
    ): CompatibilityResult {
        val issues = mutableListOf<CompatibilityIssue>()
        val notes = mutableListOf<String>()

        if (!profile.supports(video.mimeType)) {
            issues += CompatibilityIssue.UNSUPPORTED_CODEC
        }
        if (video.width > profile.maxDecoderWidth ||
            video.height > profile.maxDecoderHeight) {
            issues += CompatibilityIssue.RESOLUTION_EXCEEDS_DECODER
        }
        if (video.frameRate > profile.maxFrameRate) {
            issues += CompatibilityIssue.FRAME_RATE_TOO_HIGH
        }
        if (video.isHdr && !profile.supportsHdr) {
            issues += CompatibilityIssue.HDR_NOT_SUPPORTED
        }
        if (video.bitrate > 0 && profile.maxBitrate > 0 &&
            video.bitrate > profile.maxBitrate) {
            issues += CompatibilityIssue.BITRATE_TOO_HIGH
        }

        return if (issues.isEmpty()) {
            CompatibilityResult.Compatible(video, notes)
        } else {
            val readable = issues.joinToString(", ")
                .lowercase().replace("_", " ")
            CompatibilityResult.NeedsConversion(
                video = video,
                issues = issues,
                recommendedPreset = choosePreset(video, profile, fallbackPreset),
                explanation = "This video has: $readable. " +
                    "A compatible copy will be created; the original stays untouched."
            )
        }
    }

    /** Pick the cheapest preset that fixes every detected issue. */
    private fun choosePreset(
        video: VideoInfo,
        profile: DeviceProfile,
        fallback: ConversionPreset
    ): ConversionPreset {
        val needsMaxCompat =
            profile.maxDecoderWidth in 1..1920 || profile.supports("video/avc") &&
            (video.width > 1920 || video.height > 1080 || video.frameRate > 30)
        return when {
            needsMaxCompat -> ConversionPreset.MAXIMUM_COMPATIBILITY
            video.bitrate > 12_000_000 || video.frameRate > 30 ->
                ConversionPreset.MAXIMUM_QUALITY
            else -> fallback
        }
    }
}
