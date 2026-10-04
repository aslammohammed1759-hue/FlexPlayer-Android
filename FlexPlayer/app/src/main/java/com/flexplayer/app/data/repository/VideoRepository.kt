package com.flexplayer.app.data.repository

import com.flexplayer.app.data.media.VideoScanner
import com.flexplayer.app.media.VideoAnalyzer
import com.flexplayer.app.model.VideoInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class VideoRepository @Inject constructor(
    private val scanner: VideoScanner,
    private val analyzer: VideoAnalyzer
) {
    /** Scans, then deep-analyzes each video for codec/fps/HDR details. */
    fun observeVideos(): Flow<List<VideoInfo>> = flow {
        val basics = scanner.scan()
        emit(basics)
        val detailed = basics.map { v ->
            runCatching { analyzer.analyze(android.net.Uri.parse(v.uri)) }
                .getOrDefault(v)
        }
        emit(detailed)
    }
}
