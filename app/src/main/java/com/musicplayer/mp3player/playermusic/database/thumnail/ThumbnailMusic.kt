package com.musicplayer.mp3player.playermusic.database.thumnail

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