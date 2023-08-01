package com.downloadmp3player.musicdownloader.freemusicdownloader.loader

import android.Manifest
import android.content.Context
import android.provider.MediaStore
import androidx.core.content.PermissionChecker
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseAsyncTaskLoader
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnSearchAudioListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils

class SearchLoaderTask(private val songListenner: OnSearchAudioListener, context: Context?) :
    BaseAsyncTaskLoader<ArrayList<MusicItem>>(context) {
    private var sortorder: String? = null
    var filter: String? = null
    private var name: String? = null
    var params = arrayOf<String>()

    override fun loadInBackground(): ArrayList<MusicItem> {
        val songList: ArrayList<MusicItem> = ArrayList()
        return   if (AppUtils.isGrantPermission(context)) {
            val where =
                MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND " + MediaStore.Audio.Media.TITLE + " LIKE ?"
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                where,
                arrayOf("%$name%"),
                sortorder
            )
            if (cursor != null && cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val datacol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val artistIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
                do {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol)
                    val artist = cursor.getString(artistCol)
                    val album = cursor.getString(albumCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val track = cursor.getInt(trackCol)
                    val mSongPath = cursor.getString(datacol)
                    val year = cursor.getString(yearCol)
                    val artistId = cursor.getLong(artistIdCol)
                    val duration = cursor.getString(durationCol)
                    songList.add(
                        MusicItem(
                            id,
                            title,
                            0,
                            artist,
                            album,
                            track,
                            albumId,
                            "",
                            mSongPath,
                            false,
                            year,
                            "",
                            artistId,
                            duration,
                            0,
                            0
                        )
                    )
                } while (cursor.moveToNext())
            }
            songListenner.onSearchAudioSuccessful(songList)
            songList
        } else {
            songListenner.onSearchAudioSuccessful(songList)
            songList
        }
    }

    fun setSearchName(mname: String?) {
        name = mname
    }

    fun setSortOrder(orderBy: String?) {
        sortorder = orderBy
    }

}