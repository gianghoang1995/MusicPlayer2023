package com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ThumbnailMusic")
data class ThumbnailMusic(
    @PrimaryKey
    var id: Long?,
    @ColumnInfo(name = "thumbPath") var thumbPath: String?,
) {
}