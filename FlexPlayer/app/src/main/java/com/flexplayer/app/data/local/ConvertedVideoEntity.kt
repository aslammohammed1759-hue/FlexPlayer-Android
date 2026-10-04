package com.flexplayer.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "converted_videos")
data class ConvertedVideoEntity(
    @PrimaryKey val outputPath: String,
    val sourceUri: String,
    val presetName: String,
    val createdAt: Long,
    val outputSizeBytes: Long,
    val outputWidth: Int,
    val outputHeight: Int
)
