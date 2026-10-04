package com.flexplayer.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flexplayer.app.data.repository.VideoRepository
import com.flexplayer.app.media.CompatibilityEngine
import com.flexplayer.app.media.DeviceCapabilityManager
import com.flexplayer.app.model.CompatibilityResult
import com.flexplayer.app.model.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val videos: List<VideoInfo> = emptyList(),
    val compat: Map<Long, CompatibilityResult> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val capabilityManager: DeviceCapabilityManager,
    private val compatibilityEngine: CompatibilityEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        val profile = capabilityManager.buildProfile()
        videoRepository.observeVideos().collect { videos ->
            _uiState.update { state ->
                state.copy(
                    videos = videos,
                    isLoading = false,
                    compat = videos.associate { v ->
                        // Basic-scan rows have no codec yet; skip deep check
                        if (v.mimeType.isBlank()) v.id to
                            CompatibilityResult.Compatible(v)
                        else v.id to compatibilityEngine.evaluate(v, profile)
                    }
                )
            }
        }
    }
}
