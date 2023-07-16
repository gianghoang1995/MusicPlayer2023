package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder.loader

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.SortOrder
import java.io.File
import java.io.FileFilter
import java.util.*

class AudioFileFilter @JvmOverloads constructor(
    /**
     * allows Directories
     */
    private val allowDirectories: Boolean = true,
    private var context: Context
) :
    FileFilter {

    val datacol = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.COMPOSER
    )


    override fun accept(f: File): Boolean {
        if (f.isHidden || !f.canRead()) {
            return false
        }
        return if (f.isDirectory) {
            checkDirectory(f)
        } else checkFileExtension(f)
    }

    @SuppressLint("Recycle")
    fun checkMedia(file: File): Boolean {
        val path: String = file.getCanonicalPath()
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            datacol,
            MediaStore.Audio.Media.DATA + " = ?",
            arrayOf(path),
            SortOrder.SongSortOrder.SONG_A_Z
        )

        if (cursor != null) {
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

            while (cursor.moveToNext()) {
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
                return true
                cursor.close()
            }
        }
        return false
    }

    private fun checkFileExtension(f: File): Boolean {
        val ext = getFileExtension(f) ?: return false
        try {
            if (SupportedFileFormat.valueOf(ext.toUpperCase()) != null) {
                return true
            }
        } catch (e: IllegalArgumentException) { //Not known enum value
            return false
        }
        return false
    }

    private fun checkDirectory(dir: File): Boolean {
        return if (!allowDirectories) {
            false
        } else {
            val subDirs = ArrayList<File>()
            val songNumb = dir.listFiles { file ->
                if (file.isFile) {
                    if (file.name.equals(".nomedia")) false else checkFileExtension(file)
                } else if (file.isDirectory) {
                    subDirs.add(file)
                    false
                } else false
            }.size
            if (songNumb > 0) {
                Log.e(
                    "AudioFileFilter",
                    "checkDirectory: dir $dir return true con songNumb -> $songNumb"
                )
                return true
            }
            for (subDir in subDirs) {
                if (checkDirectory(subDir)) {
                    Log.e(
                        "AudioFileFilter",
                        "checkDirectory [for]: subDir $subDir return true"
                    )
                    return true
                }
            }
            false
        }
    }

    private fun checkFileExtension(fileName: String): Boolean {
        val ext = getFileExtension(fileName) ?: return false
        try {
            if (SupportedFileFormat.valueOf(ext.toUpperCase()) != null) {
                return true
            }
        } catch (e: IllegalArgumentException) { //Not known enum value
            return false
        }
        return false
    }

    fun getFileExtension(f: File): String? {
        return getFileExtension(f.name)
    }

    fun getFileExtension(fileName: String): String? {
        val i = fileName.lastIndexOf('.')
        return if (i > 0) {
            fileName.substring(i + 1)
        } else null
    }

    /**
     * Files formats currently supported by Library
     */
    enum class SupportedFileFormat(val filesuffix: String) {
        M4A("m4a"), AAC("aac"), FLAC("flac"), MP3("mp3"), MID("mid"), MIDI("midi"), IMY("imy"), OGG(
            "ogg"
        ),
        AMR("amr"), WAV("wav");

    }

    companion object {
        protected const val TAG = "AudioFileFilter"
    }

}