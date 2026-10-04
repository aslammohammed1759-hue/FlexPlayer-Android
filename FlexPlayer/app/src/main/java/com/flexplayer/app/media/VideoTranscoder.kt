package com.flexplayer.app.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Presentation
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.flexplayer.app.model.ConversionPreset
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/** Wraps Media3 Transformer. Always writes to a NEW file — the source is
 *  opened read-only and never modified. Incomplete outputs are deleted. */
@Singleton
class VideoTranscoder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    sealed interface TranscodeEvent {
        data class Progress(val fraction: Float) : TranscodeEvent
        data class Completed(val outputFile: File) : TranscodeEvent
        data class Failed(val cause: Throwable) : TranscodeEvent
        data object Cancelled : TranscodeEvent
    }

    @Volatile private var activeTransformer: Transformer? = null

    fun transcode(
        source: Uri,
        preset: ConversionPreset,
        outputFile: File
    ): Flow<TranscodeEvent> = callbackFlow {
        val (outW, outH) = withContext(Dispatchers.Default) {
            // Caller passes target dims via Presentation; we infer from preset
            preset.maxWidth to preset.maxHeight
        }
        val effects = Effects(
            listOf(),
            listOf(
                Presentation.createForWidthAndHeight(
                    outW, outH, Presentation.LAYOUT_SCALE_TO_FIT
                )
            )
        )
        val edited = EditedMediaItem.Builder(MediaItem.fromUri(source))
            .setEffects(effects)
            .build()

        val request = TransformationRequest.Builder()
            .setVideoMimeType(preset.targetCodecMime)
            .setAudioMimeType(preset.targetAudioMime)
            .build()

        val transformer = Transformer.Builder(context)
            .setTransformationRequest(request)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(
                    composition: Composition,
                    exportResult: ExportResult
                ) {
                    trySend(TranscodeEvent.Completed(outputFile))
                    close()
                }
                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    outputFile.delete() // clean incomplete output
                    trySend(TranscodeEvent.Failed(exportException))
                    close()
                }
            })
            .build()

        activeTransformer = transformer
        runCatching {
            transformer.start(edited, outputFile.absolutePath)
        }.onFailure {
            outputFile.delete()
            trySend(TranscodeEvent.Failed(it))
            close()
        }

        awaitClose {
            runCatching { transformer.cancel() }
            activeTransformer = null
        }
    }

    fun cancel() {
        runCatching { activeTransformer?.cancel() }
    }
}
