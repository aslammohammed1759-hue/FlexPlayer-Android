package com.flexplayer.app.sharing

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.flexplayer.app.data.repository.ConversionRepository
import com.flexplayer.app.model.ConversionPreset
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart Share. Standard Android sharing cannot reveal the recipient's exact
 * hardware, so "Smart Compatible" shares a copy produced with the user's
 * selected preset — we never claim automatic recipient detection.
 */
@Singleton
class SmartShareManager @Inject constructor(
    private val conversionRepo: ConversionRepository
) {
    sealed interface ShareMode {
        data object OriginalQuality : ShareMode
        data class SmartCompatible(val preset: ConversionPreset) : ShareMode
        data class SmallerFile(val preset: ConversionPreset) : ShareMode
    }

    fun shareIntent(context: Context, mode: ShareMode, sourceUri: String): Intent {
        val (file, mime) = when (mode) {
            is ShareMode.OriginalQuality -> {
                // Stream the original via its content Uri
                return Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(sourceUri))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            is ShareMode.SmartCompatible ->
                existingOrNull(context, sourceUri, mode.preset) to "video/mp4"
            is ShareMode.SmallerFile ->
                existingOrNull(context, sourceUri, mode.preset) to "video/mp4"
        }
        val contentUri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider",
            file ?: throw IllegalStateException("Compatible copy not ready yet")
        )
        return Intent(Intent.ACTION_SEND).apply {
            this.type = mime
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun existingOrNull(
        context: Context, sourceUri: String, preset: ConversionPreset
    ): File? {
        // Blocking-free: caller checks via ConversionRepository first
        return conversionRepo.outputFileFor(sourceUri, preset.label)
            .takeIf { it.exists() }
    }
}
