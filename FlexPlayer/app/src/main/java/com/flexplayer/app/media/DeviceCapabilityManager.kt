package com.flexplayer.app.media

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import com.flexplayer.app.model.DeviceProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Builds the DeviceProfile from the platform's decoder registry.
 * NOTE: this reflects DECODER capability, not screen resolution —
 * a 1080p-screen phone may still decode 4K60 for output elsewhere.
 */
@Singleton
class DeviceCapabilityManager @Inject constructor() {

    suspend fun buildProfile(): DeviceProfile = withContext(Dispatchers.Default) {
        val regular = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            .filter { !it.isEncoder && it.isVideoCodec() }

        val supported = mutableSetOf<String>()
        var maxW = 0; var maxH = 0; var maxFps = 0; var maxBr = 0L

        for (info in regular) {
            for (type in info.supportedTypes) {
                if (!type.startsWith("video/")) continue
                supported += type
                runCatching {
                    val caps = info.getCapabilitiesForType(type)
                    caps.videoCapabilities?.let { vc ->
                        maxW = maxOf(maxW, vc.supportedWidths.upper)
                        maxH = maxOf(maxH, vc.supportedHeights.upper)
                        maxFps = maxOf(maxFps, vc.supportedFrameRates.upper.toInt())
                        maxBr = maxOf(maxBr, vc.bitrateRange.upper.toLong())
                    }
                }
            }
        }

        DeviceProfile(
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            maxDecoderWidth = maxW,
            maxDecoderHeight = maxH,
            maxFrameRate = maxFps,
            supportedCodecs = supported,
            supportsHdr = supported.any { it.contains("10", ignoreCase = true) },
            maxBitrate = maxBr
        )
    }

    private fun MediaCodecInfo.isVideoCodec(): Boolean =
        supportedTypes.any { it.startsWith("video/") }
}
