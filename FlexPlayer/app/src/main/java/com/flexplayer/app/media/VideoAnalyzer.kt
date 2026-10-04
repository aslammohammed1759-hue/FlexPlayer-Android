package com.flexplayer.app.media

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.flexplayer.app.model.VideoInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads resolution, frame rate, codec, bitrate, HDR and duration of a video.
 *  Uses low-level metadata APIs only — the video is never fully decoded or
 *  loaded into memory, keeping analysis cheap and safe for huge files. */
@Singleton
class VideoAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun analyze(uri: Uri): VideoInfo = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var extractor: MediaExtractor? = null
        try {
            retriever.setDataSource(context, uri)
            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val duration = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val bitrate = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            val (w, h) = if (rotation == 90 || rotation == 270) height to width else width to height

            // Probe codec + frame rate + HDR via MediaExtractor / MediaFormat
            extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)
            var codec = "unknown"; var mime = "video/unknown"
            var frameRate = 30f; var hdr = false
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val trackMime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (!trackMime.startsWith("video/")) continue
                mime = trackMime
                codec = trackMime.removePrefix("video/")
                if (fmt.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    frameRate = fmt.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
                }
                // HDR signalling: HDR10/HLG commonly carried in color metadata
                if (fmt.containsKey(MediaFormat.KEY_COLOR_TRANSFER) &&
                    (fmt.getInteger(MediaFormat.KEY_COLOR_TRANSFER) ==
                        MediaFormat.COLOR_TRANSFER_ST2084 ||
                     fmt.getInteger(MediaFormat.KEY_COLOR_TRANSFER) ==
                        MediaFormat.COLOR_TRANSFER_HLG)) {
                    hdr = true
                }
                break
            }

            VideoInfo(
                id = uri.toString().hashCode().toLong(),
                uri = uri.toString(),
                displayName = uri.lastPathSegment ?: "video",
                durationMs = duration,
                width = w, height = h,
                frameRate = frameRate,
                codec = codec,
                mimeType = mime,
                bitrate = bitrate,
                isHdr = hdr,
                sizeBytes = runCatching {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use {
                        it.statSize
                    }
                }.getOrNull() ?: 0L
            )
        } finally {
            extractor?.release()
            retriever.release()
        }
    }
}
