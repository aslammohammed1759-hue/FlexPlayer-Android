package com.flexplayer.app.data.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.flexplayer.app.model.VideoInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Discovers on-device videos through MediaStore. Requires READ_MEDIA_VIDEO
 *  (API 33+) or READ_EXTERNAL_STORAGE (API 26-32) to see media the app did
 *  not create itself — the caller is responsible for requesting that
 *  permission before calling [scan]. */
@Singleton
class VideoScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun scan(): List<VideoInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<VideoInfo>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.SIZE
        )
        runCatching {
            context.contentResolver.query(
                collection, projection, null, null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )
        }.getOrNull()?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val wCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val hCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                result += VideoInfo(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    displayName = cursor.getString(nameCol) ?: "video",
                    durationMs = cursor.getLong(durCol),
                    width = cursor.getInt(wCol),
                    height = cursor.getInt(hCol),
                    frameRate = 0f,           // refined later by VideoAnalyzer
                    codec = "", mimeType = "",
                    bitrate = 0, isHdr = false,
                    sizeBytes = cursor.getLong(sizeCol)
                )
            }
        }
        result
    }
}
