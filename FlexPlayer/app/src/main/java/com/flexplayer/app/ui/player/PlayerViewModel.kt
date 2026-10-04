package com.flexplayer.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.flexplayer.app.media.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {
    val player: ExoPlayer get() = playerManager.getOrCreate()
    fun play(uri: String) = playerManager.play(uri)
    fun pause() = playerManager.pause()
    override fun onCleared() { playerManager.release() }
}
