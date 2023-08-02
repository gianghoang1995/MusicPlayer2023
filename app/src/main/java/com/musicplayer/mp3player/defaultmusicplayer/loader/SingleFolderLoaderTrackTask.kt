package com.musicplayer.mp3player.defaultmusicplayer.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseAsyncTaskLoader
import com.musicplayer.mp3player.defaultmusicplayer.handle.SongLoaderListener
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.SortOrder
import java.util.*

class SingleFolderLoaderTrackTask(
    context: Context?,
    private val path: String?,
    listener: SongLoaderListener
) : BaseAsyncTaskLoader<List<MusicItem?>?>(context) {
    private val songListener: SongLoaderListener = listener
    private var sortOrder = SortOrder.SongSortOrder.SONG_A_Z
    private val data = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.COMPOSER
    )

    override fun loadInBackground(): ArrayList<MusicItem>? {
        val selection = MediaStore.Audio.Media.DATA + " LIKE '" + path + "/'"
        //        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND " + MediaStore.Audio.Media.DATA + " LIKE '" + path + "/%'"
        val songList: ArrayList<MusicItem> = ArrayList()
        if (AppUtils.isGrantPermission(context)) {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, data, selection, null, sortOrder
            )
            if (cursor != null && cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val artistIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
                val bitRateCol = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
                do {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol)
                    val artist = cursor.getString(artistCol)
                    val album = cursor.getString(albumCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val track = cursor.getInt(trackCol)
                    val mSongPath = cursor.getString(data)
                    val year = cursor.getString(yearCol)
                    val artistId = cursor.getLong(artistIdCol)
                    val duration = cursor.getString(durationCol)
                    val bitrate = cursor.getInt(bitRateCol)

                    songList.add(
                        MusicItem(
                            id,
                            title,
                            bitrate,
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
                cursor.close()
            }
            songListener.onAudioLoadedSuccessful(songList)
        }
        return songList
    }

    fun setSortOder(sort: String) {
        sortOrder = sort;
    }
}
