package com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail

import androidx.room.*

@Dao
interface ThumbnailStoreHelper {
    @Query("SELECT * FROM ThumbnailMusic")
    fun getAllThumbnail(): List<ThumbnailMusic>

    @Query("SELECT * FROM ThumbnailMusic WHERE id LIKE :id")
    fun findThumbnailByID(id: Long?): ThumbnailMusic?

    @Insert
    fun insertThumbnail(vararg item: ThumbnailMusic)

    @Delete
    fun deleteThumbnail(item: ThumbnailMusic)

    @Update
    fun updateThumbnail(vararg item: ThumbnailMusic)
}