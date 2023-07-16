package com.downloadmp3player.musicdownloader.freemusicdownloader.loader

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.core.content.PermissionChecker
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseAsyncTaskLoader
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.SortOrder
import java.util.*

class FolderLoaderTrackTask(context: Context?, private val path: String?, listener: SongLoaderListener) : BaseAsyncTaskLoader<List<MusicItem?>?>(context) {
    private val songListener: SongLoaderListener = listener
    private var sortOrder = SortOrder.SongSortOrder.SONG_A_Z

    override fun loadInBackground(): ArrayList<MusicItem>? {
        val selection = MediaStore.Audio.Media.DATA + " LIKE '" + path + "/%'"
        val songList: ArrayList<MusicItem> = ArrayList()
        if (PermissionChecker.checkCallingOrSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            val cursor: Cursor? = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, null, sortOrder
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
                    songList.add(
                            MusicItem(
                                    id, title, artist, album, track, albumId, "", mSongPath, false, year, "", artistId, duration, 0, 0, false
                            )
                    )
                } while (cursor.moveToNext())
                cursor.close()
            }
            songListener.onAudioLoadedSuccessful(songList)
        }
        return songList
    }

    private fun getDuration(path: String): String {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(path)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr ?: "0"
    }

    fun setSortOder(sort: String) {
        sortOrder = sort;
    }
}
