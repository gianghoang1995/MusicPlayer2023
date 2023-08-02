package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.query_folder

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.utils.SortOrder

class GetSongFolder(private var context: Context) {
    var sort = SortOrder.SongSortOrder.SONG_A_Z;

    @SuppressLint("Range")
    fun getParentID(path: String): Int {
        val query = if (Build.VERSION.SDK_INT > 29) {
            "media_type = 2"
        } else {
            "${MediaStore.Audio.Media.IS_MUSIC + " != 0"} AND media_type = 2"
        }
        val selection = "$query AND " + MediaStore.Files.FileColumns.DATA + " LIKE '" + path + "/%'"

        context.contentResolver?.query(
            MediaStore.Files.getContentUri("external"), null, selection, null, null
        ).use { cursor ->
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val data: String =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                    val parentId: Int =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                    val parentPath = data.substring(0, data.lastIndexOf("/"))
                    if (parentPath == path) {
                        return parentId
                    }
                }
            }
        }
        return -1
    }


    fun getSongsByParentId(parentId: Int): ArrayList<MusicItem> {
        val ids: List<Int>? = getSongIdsByParentId(parentId)
        if (ids?.size == 0) {
            return java.util.ArrayList()
        }
        val selection = StringBuilder(127)
        selection.append(MediaStore.Audio.Media._ID + " in (")
        if (ids != null) {
            for (i in ids.indices) {
                selection.append(ids[i]).append(if (i == ids.size - 1) ") " else ",")
            }
        }
        return getSongs(
            selection.toString() + " AND " + MediaStore.Audio.Media.IS_MUSIC + " != 0 ",
            null,
            SortOrder.SongSortOrder.SONG_A_Z
        )
    }

    private fun getSongIdsByParentId(parentId: Int): List<Int>? {
        val ids: MutableList<Int> = java.util.ArrayList()
        context?.contentResolver?.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf("_id"),
            "parent = $parentId",
            null,
            null
        ).use { cursor ->
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ids.add(cursor.getInt(0))
                }
            }
        }
        return ids
    }

    private fun getSongs(
        selection: String?,
        selectionValues: Array<String?>?,
        sortOrder: String?
    ): ArrayList<MusicItem> {
        val musicItems: ArrayList<MusicItem> = ArrayList()
        try {
            val cursor = makeSongCursor(selection, selectionValues, sortOrder)
            cursor?.moveToFirst()
            if (cursor != null) {
                do {
                    val song = getSongInfo(cursor)
                    musicItems.add(song)
                } while (cursor.moveToNext())
//                while (cursor.moveToNext()) {
//                    val song = getSongInfo(cursor)
//                    musicItems.add(song)
//                    Log.e("Data", song.title.toString())
//                }
            }
            Log.e("GetSong", cursor?.count.toString())
        } catch (ignore: Exception) {
            Log.e("Error getSongs", ignore.toString())
        }
        return musicItems
    }

    private fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        return try {
            context.contentResolver?.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionValues,
                sortOrder
            )
        } catch (e: SecurityException) {
            Log.e("Error makeSongCursor", e.toString())
            null
        }
    }

    fun getSongInfo(cursor: Cursor): MusicItem {
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

        return MusicItem(
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
        cursor?.close()
    }
}
