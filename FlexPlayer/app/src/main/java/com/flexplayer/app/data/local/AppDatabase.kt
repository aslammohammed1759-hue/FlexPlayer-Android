package com.flexplayer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ConvertedVideoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun convertedVideoDao(): ConvertedVideoDao
}
