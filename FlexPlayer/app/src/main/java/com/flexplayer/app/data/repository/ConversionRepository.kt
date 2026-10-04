package com.flexplayer.app.data.repository

import android.content.Context
import androidx.room.Room
import com.flexplayer.app.data.local.AppDatabase
import com.flexplayer.app.data.local.ConvertedVideoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Caches conversion records in Room; converted files live in app-private
 *  storage so they never pollute the user's media library. */
@Singleton
class ConversionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val db: AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "flexplayer.db"
    ).build()
    private val dao = db.convertedVideoDao()

    val convertedVideos: Flow<List<ConvertedVideoEntity>> = dao.observeAll()

    fun outputFileFor(sourceUri: String, presetName: String): File {
        val dir = File(context.filesDir, "converted").apply { mkdirs() }
        val safeName = sourceUri.substringAfterLast('/').substringBeforeLast('.')
        return File(dir, "${safeName}_${presetName.lowercase()}.mp4")
    }

    suspend fun alreadyConverted(sourceUri: String, presetName: String): File? {
        val rec = dao.findBySource(sourceUri, presetName) ?: return null
        val f = File(rec.outputPath)
        return if (f.exists()) f else null
    }

    suspend fun recordConversion(entity: ConvertedVideoEntity) = dao.upsert(entity)

    suspend fun deleteConversion(entity: ConvertedVideoEntity) {
        File(entity.outputPath).delete()
        dao.delete(entity)
    }
}
