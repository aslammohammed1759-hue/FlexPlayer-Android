package com.flexplayer.app.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thin wrapper around ExoPlayer (Jetpack Media3). */
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var player: ExoPlayer? = null

    fun getOrCreate(): ExoPlayer =
        player ?: ExoPlayer.Builder(context).build().also { player = it }

    fun play(uri: String) {
        getOrCreate().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    fun pause() { player?.pause() }

    fun release() {
        player?.release()
        player = null
    }
}
