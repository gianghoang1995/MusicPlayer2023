package com.musicplayer.mp3player.defaultmusicplayer.database.thumnail

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ThumbnailMusic::class], exportSchema = false, version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun thumbDao(): ThumbnailStoreHelper

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "ThumbnailMusic.db"
        )
            .allowMainThreadQueries()
            .build()
    }
}