package com.flexplayer.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConvertedVideoDao {
    @Query("SELECT * FROM converted_videos ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ConvertedVideoEntity>>

    @Query("SELECT * FROM converted_videos WHERE sourceUri = :sourceUri AND presetName = :preset LIMIT 1")
    suspend fun findBySource(sourceUri: String, preset: String): ConvertedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConvertedVideoEntity)

    @Delete
    suspend fun delete(entity: ConvertedVideoEntity)
}
