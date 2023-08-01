package com.downloadmp3player.musicdownloader.freemusicdownloader.loader

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.database.CursorJoiner
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.content.PermissionChecker
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseAsyncTaskLoader
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockDaoBD
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.AlbumLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderDao
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderHelper
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils

class AlbumLoaderTask(context: Context?, private val albumListenner: AlbumLoaderListener?) : BaseAsyncTaskLoader<ArrayList<AlbumItem>>(context) {
    private var pinAlbumHelper =
        AppPinHelperDB(context)
    private var pinAlbumDao =
        AppPinDaoDB(pinAlbumHelper)
    private var appBlockHelper =
        AppBlockHelperDB(context)
    private var appBlockDao =
        AppBlockDaoBD(appBlockHelper)
    private var appBlockFolderHelper = BlockFolderHelper(context)
    private var appBlockFolderDao = BlockFolderDao(appBlockFolderHelper)
    private var sortorder: String? = null
    private var where = MediaStore.Audio.Media.IS_MUSIC + " != 0"
    override fun loadInBackground(): ArrayList<AlbumItem> {
        where += filterBlockFolder() + filterBlockArtist() + filterBlockAlbum() + filterSkipDuration()
        val mCursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, MediaStore.Audio.Media.ALBUM + " ASC"
        )
        val mCursor2: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, selectionBlockAlbum(), null, MediaStore.Audio.Albums.ALBUM + " ASC"
        )
        val albums = ArrayList<AlbumItem>()
        return   if (AppUtils.isGrantPermission(context)) {
            if (mCursor != null && mCursor.count > 0 && mCursor2 != null && mCursor2.count > 0) {
                val joiner = CursorJoiner(
                        mCursor2, arrayOf(MediaStore.Audio.Media.ALBUM), mCursor, arrayOf(MediaStore.Audio.Albums.ALBUM)
                )

                for (joinerResult in joiner) {
                    when (joinerResult) {
                        CursorJoiner.Result.LEFT -> {
                        }
                        CursorJoiner.Result.RIGHT -> {
                        }
                        CursorJoiner.Result.BOTH -> {
                            val titleColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)
                            val idColumn = mCursor2.getColumnIndex(MediaStore.Audio.Albums._ID)
                            val artistColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)
                            val numOfSongsColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
                            val albumFirstColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR)
                            val albumName = mCursor2.getString(titleColumn)
                            val albumId = mCursor2.getLong(idColumn)
                            val artistName = mCursor2.getString(artistColumn)
                            val year = mCursor2.getInt(albumFirstColumn)
                            val countSong = mCursor2.getInt(numOfSongsColumn)
                            val albumArt = ArtworkUtils.getAlbumArtUri(albumId.toInt())
                            val item = AlbumItem(albumId, albumName, artistName, year, countSong, albumArt)
                            if (!TextUtils.isEmpty(albumName) && countSong > 0) {
                                if (!pinAlbumDao.isPinAlbum(item)) {
                                    albums.add(item)
                                }
                            }
                        }
                    }
                }
            }
            albumListenner?.onLoadAlbumSuccessful(albums)
            return albums
        } else {
            albumListenner?.onLoadAlbumSuccessful(albums)
            return albums
        }
    }

    fun getCountAlbum(): Int {
        where += filterBlockFolder() + filterBlockArtist() + filterBlockAlbum() + filterSkipDuration()
        val mCursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, MediaStore.Audio.Media.ALBUM + " ASC"
        )
        val mCursor2: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, selectionBlockAlbum(), null, MediaStore.Audio.Albums.ALBUM + " ASC"
        )
        val albums = ArrayList<AlbumItem>()
        if (AppUtils.isGrantPermission(context)) {
            if (mCursor != null && mCursor.count > 0 && mCursor2 != null && mCursor2.count > 0) {
                val joiner = CursorJoiner(
                        mCursor2, arrayOf(MediaStore.Audio.Media.ALBUM), mCursor, arrayOf(MediaStore.Audio.Albums.ALBUM)
                )

                for (joinerResult in joiner) {
                    when (joinerResult) {
                        CursorJoiner.Result.LEFT -> {
                        }
                        CursorJoiner.Result.RIGHT -> {
                        }
                        CursorJoiner.Result.BOTH -> {
                            val titleColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)
                            val idColumn = mCursor2.getColumnIndex(MediaStore.Audio.Albums._ID)
                            val artistColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)
                            val numOfSongsColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
                            val albumFirstColumn = mCursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR)
                            val albumName = mCursor2.getString(titleColumn)
                            val albumId = mCursor2.getLong(idColumn)
                            val artistName = mCursor2.getString(artistColumn)
                            val year = mCursor2.getInt(albumFirstColumn)
                            val countSong = mCursor2.getInt(numOfSongsColumn)
                            val albumArt = ArtworkUtils.getAlbumArtUri(albumId.toInt())
                            val item = AlbumItem(albumId, albumName, artistName, year, countSong, albumArt)
                            if (!TextUtils.isEmpty(albumName) && countSong > 0) {
                                if (!pinAlbumDao.isPinAlbum(item)) {
                                    albums.add(item)
                                }
                            }
                        }
                    }
                }
            }
        }
        return albums.size
    }

    fun getCountSong(albumID: Long): Int {
        var count = 0
        var filterBlockArtist = filterBlockArtist()
        filterBlockArtist = if (filterBlockArtist().isNotEmpty()) {
            " AND $filterBlockArtist"
        } else {
            ""
        }
        var filterBlockAlbum = filterBlockAlbum()
        filterBlockAlbum = if (filterBlockAlbum().isNotEmpty()) {
            " AND $filterBlockAlbum"
        } else {
            ""
        }
        val where = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Albums.ALBUM_ID + " = $albumID " + filterBlockArtist + filterBlockAlbum + " AND " + filterSkipDuration()

        return context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, sortorder
        )!!.count
    }

    fun setSortOrder(orderBy: String?) {
        sortorder = orderBy
    }

    private fun selectionBlockAlbum(): String {
        var filter = ""
        val filerAlbum = MediaStore.Audio.Albums._ID
        val listBlockAlbum = appBlockDao.listBlockAlbum
        if (listBlockAlbum.size > 0) {
            for (i in 0 until listBlockAlbum.size) {
                val item = listBlockAlbum[i]
                filter += if (i < listBlockAlbum.size - 1) filerAlbum + " != " + item.id + " AND "
                else filerAlbum + " != " + item.id
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

    fun filterSkipDuration(): String? {
        val skipDuration = PreferenceUtils.getValueInt(AppConstants.SKIP_DURATION, 0) * 1000
        return " AND " + MediaStore.Audio.AudioColumns.DURATION + " > " + skipDuration
    }
}