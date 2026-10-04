package com.flexplayer.app.ui.conversion

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flexplayer.app.data.repository.ConversionRepository
import com.flexplayer.app.media.VideoTranscoder
import com.flexplayer.app.model.ConversionPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversionUiState(
    val running: Boolean = false,
    val progress: Float = 0f,
    val preset: ConversionPreset = ConversionPreset.BALANCED,
    val message: String? = null,
    val outputFile: File? = null
)

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val transcoder: VideoTranscoder,
    private val conversionRepo: ConversionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversionUiState())
    val uiState: StateFlow<ConversionUiState> = _uiState.asStateFlow()

    fun setPreset(preset: ConversionPreset) =
        _uiState.update { it.copy(preset = preset) }

    fun start(sourceUri: String) = viewModelScope.launch {
        val preset = _uiState.value.preset
        // Reuse cached copy if already converted with this preset
        conversionRepo.alreadyConverted(sourceUri, preset.label)?.let { cached ->
            _uiState.update { it.copy(outputFile = cached, message = "Using cached copy") }
            return@launch
        }
        val output = conversionRepo.outputFileFor(sourceUri, preset.label)
        _uiState.update { it.copy(running = true, progress = 0f, message = null) }
        transcoder.transcode(Uri.parse(sourceUri), preset, output).collect { event ->
            when (event) {
                is VideoTranscoder.TranscodeEvent.Progress ->
                    _uiState.update { it.copy(progress = event.fraction) }
                is VideoTranscoder.TranscodeEvent.Completed -> {
                    _uiState.update {
                        it.copy(running = false, progress = 1f, outputFile = event.outputFile)
                    }
                    conversionRepo.recordConversion(
                        com.flexplayer.app.data.local.ConvertedVideoEntity(
                            outputPath = output.absolutePath,
                            sourceUri = sourceUri,
                            presetName = preset.label,
                            createdAt = System.currentTimeMillis(),
                            outputSizeBytes = output.length(),
                            outputWidth = 0, outputHeight = 0
                        )
                    )
                }
                is VideoTranscoder.TranscodeEvent.Failed ->
                    _uiState.update { it.copy(running = false, message = "Failed: ${event.cause.message}") }
                VideoTranscoder.TranscodeEvent.Cancelled ->
                    _uiState.update { it.copy(running = false, message = "Cancelled") }
            }
        }
    }

    fun cancel() = transcoder.cancel()
}
