package com.downloadmp3player.musicdownloader.freemusicdownloader.loader

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.core.content.PermissionChecker
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseAsyncTaskLoader
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockDaoBD
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderDao
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderHelper
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils
import java.util.*


class SongLoaderAsyncTask(context: Context?, listener: SongLoaderListener?) :
    BaseAsyncTaskLoader<ArrayList<MusicItem>>(context) {
    var sortorder: String? = null
    var isFilter = true
    private val songListener: SongLoaderListener? = listener
    private var appBlockHelper =
        AppBlockHelperDB(context)
    private var appBlockDao =
        AppBlockDaoBD(appBlockHelper)
    private var appBlockFolderHelper = BlockFolderHelper(context)
    private var appBlockFolderDao = BlockFolderDao(appBlockFolderHelper)
    private var where = MediaStore.Audio.Media.IS_MUSIC + " != 0"
    private var filterDateAdded = ""
    override fun loadInBackground(): ArrayList<MusicItem> {
        if (isFilter)
            where += filterBlockFolder() + filterBlockArtist() + filterBlockAlbum() + filterSkipDuration() + filterDateAdded
        else
            where += filterSkipDuration()
        val songList: ArrayList<MusicItem> = ArrayList()
        if (AppUtils.isGrantPermission(context)) {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, sortorder
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
                cursor.close()
            }
            if (cursor == null) { //
            }
            songListener?.onAudioLoadedSuccessful(songList)
        }
        return songList
    }

    fun getBitrate(path: String): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt() ?: 0
    }

    fun getCount(): Int {
        var count = 0
        where += filterBlockFolder() + filterBlockArtist() + filterBlockAlbum() + filterSkipDuration() + filterDateAdded
        if (AppUtils.isGrantPermission(context)) {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, sortorder
            )
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.count
                cursor.close()
            }
        }
        return count
    }

    fun filterDateAdded() {
        val numWeeks = 1 * 3600 * 24 * 7    /* 1 tuần*/
        filterDateAdded =
            " AND " + MediaStore.Audio.Media.DATE_ADDED + ">=" + ((System.currentTimeMillis() / 1000) - numWeeks)
    }

    fun setSortOrder(orderBy: String?) {
        sortorder = orderBy
    }

    fun setFilter(filter: String) {
        where += " $filter"
    }

    private fun filterBlockArtist(): String {
        var filter = ""
        val filerArtist: String = MediaStore.Audio.Media.ARTIST_ID
        val listBlockArtist = appBlockDao.listBlockArtist
        if (listBlockArtist.size > 0) {
            for (item in listBlockArtist) {
                filter += " AND " + filerArtist + " != " + item.id
            }
        }
        return filter
    }

    private fun filterBlockFolder(): String {
        var filter = ""
        val filerFolder: String = MediaStore.Audio.Media.DATA
        val listBlockFolder = appBlockFolderDao.allFolderBlock
        if (listBlockFolder.size > 0) {
            for (item in listBlockFolder) {
                filter += " AND " + filerFolder + " not like " + "'${item.path}/%'"
            }
        }
        return filter
    }

    private fun filterBlockAlbum(): String {
        var filter = ""
        var filerAlbum = MediaStore.Audio.Media.ALBUM_ID
        val listBlockAlbum = appBlockDao.listBlockAlbum
        if (listBlockAlbum.size > 0) {
            for (item in listBlockAlbum) {
                filter += " AND " + filerAlbum + " != \'" + item.id + "\'"
            }
        }
        return filter
    }

    fun setNotUseFilter() {
        isFilter = false
    }

    fun filterSkipDuration(): String? {
        val skipDuration = PreferenceUtils.getValueInt(AppConstants.SKIP_DURATION, 0) * 1000
        return " AND " + MediaStore.Audio.AudioColumns.DURATION + " > " + skipDuration
    }
}
