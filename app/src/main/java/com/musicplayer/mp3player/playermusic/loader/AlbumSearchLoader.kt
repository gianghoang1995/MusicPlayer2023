package com.musicplayer.mp3player.playermusic.loader

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.musicplayer.mp3player.playermusic.base.BaseAsyncTaskLoader
import com.musicplayer.mp3player.playermusic.model.AlbumItem
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils

class AlbumSearchLoader(context: Context?, private val searchListener: OnSearchAlbumListener?) :
    BaseAsyncTaskLoader<ArrayList<AlbumItem>>(context) {
    private var sortorder: String? = null
    private var where = MediaStore.Audio.AlbumColumns.ALBUM + " LIKE ?"
    var keyWords = ""

    @SuppressLint("Recycle")
    override fun loadInBackground(): ArrayList<AlbumItem> {
        val mCursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            null,
            where,
            arrayOf("%$keyWords%"),
            MediaStore.Audio.Media.ALBUM + " ASC"
        )

        val albums = ArrayList<AlbumItem>()
        if (AppUtils.isGrantPermission(context)) {
            if (mCursor != null && mCursor.moveToFirst()) {
                val titleColumn = mCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)
                val idColumn = mCursor.getColumnIndex(MediaStore.Audio.Albums._ID)
                val artistColumn = mCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)
                val numOfSongsColumn =
                    mCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
                val albumFirstColumn =
                    mCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR)
                do {
                    val albumName = mCursor.getString(titleColumn)
                    val albumId = mCursor.getLong(idColumn)
                    val artistName = mCursor.getString(artistColumn)
                    val year = mCursor.getInt(albumFirstColumn)
                    val countSong = mCursor.getInt(numOfSongsColumn)
                    val albumArt = ArtworkUtils.getAlbumArtUri(albumId.toInt())
                    val item = AlbumItem(albumId, albumName, artistName, year, countSong, albumArt)
                    albums.add(item)
                } while (mCursor.moveToNext())
            }
        }
        searchListener?.onSearchAlbumSuccess(albums)
        return albums
    }

    fun setKeyword(key: String) {
        keyWords = key
    }

    fun setSortOrder(orderBy: String?) {
        sortorder = orderBy
    }

    interface OnSearchAlbumListener {
        fun onSearchAlbumSuccess(listAlbum: ArrayList<AlbumItem>)
    }
}