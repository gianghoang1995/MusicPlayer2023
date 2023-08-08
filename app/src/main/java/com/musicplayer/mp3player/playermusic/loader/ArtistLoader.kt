package com.musicplayer.mp3player.playermusic.loader

import android.content.Context
import android.database.Cursor
import android.database.CursorJoiner
import android.provider.BaseColumns
import android.provider.MediaStore
import android.text.TextUtils
import com.musicplayer.mp3player.playermusic.base.BaseAsyncTaskLoader
import com.musicplayer.mp3player.playermusic.database.block.AppBlockDaoBD
import com.musicplayer.mp3player.playermusic.database.block.AppBlockHelperDB
import com.musicplayer.mp3player.playermusic.database.pin.AppPinDaoDB
import com.musicplayer.mp3player.playermusic.database.pin.AppPinHelperDB
import com.musicplayer.mp3player.playermusic.callback.ArtistLoaderCallback
import com.musicplayer.mp3player.playermusic.model.ArtistItem
import com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.db.block.BlockFolderDao
import com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.db.block.BlockFolderHelper
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import com.musicplayer.mp3player.playermusic.utils.PreferenceUtils

class ArtistLoader(context: Context?, listener: ArtistLoaderCallback?) : BaseAsyncTaskLoader<List<ArtistItem?>?>(context) {
    private var sortorder: String? = null
    private val artistListener: ArtistLoaderCallback? = listener
    private var appPinHelper =
        AppPinHelperDB(context)
    private var appPinDao =
        AppPinDaoDB(appPinHelper)
    private var appBlockHelper =
        AppBlockHelperDB(context)
    private var appBlockDao =
        AppBlockDaoBD(appBlockHelper)
    private var appBlockFolderHelper = BlockFolderHelper(context)
    private var appBlockFolderDao = BlockFolderDao(appBlockFolderHelper)
    private var where = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    override fun loadInBackground(): ArrayList<ArtistItem>? {
        where += filterBlockFolder() + filterBlockArtist() + filterBlockAlbum() + filterSkipDuration()
        val mCursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, MediaStore.Audio.Media.ARTIST + " ASC"
        )
        val mCursor2: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, selectionBlockArtist(), null, MediaStore.Audio.Artists.ARTIST + " ASC"
        )
        val artistList = ArrayList<ArtistItem>()

        if (mCursor != null && mCursor.count > 0 && mCursor2 != null && mCursor2.count > 0) {
            val joiner = CursorJoiner(mCursor2, arrayOf(MediaStore.Audio.Media.ARTIST), mCursor, arrayOf(MediaStore.Audio.Albums.ARTIST))
            for (joinerResult in joiner) {
                when (joinerResult) {
                    CursorJoiner.Result.LEFT -> {

                    }
                    CursorJoiner.Result.RIGHT -> {

                    }
                    CursorJoiner.Result.BOTH -> {
                        val idCol = mCursor2.getColumnIndex(BaseColumns._ID)
                        val nameCol = mCursor2.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST)
                        val albumsNbCol = mCursor2.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
                        val tracksNbCol = mCursor2.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                        val id = mCursor2.getLong(idCol)
                        val artistName = mCursor2.getString(nameCol)
                        val albumCount = mCursor2.getInt(albumsNbCol)
                        val trackCount = mCursor2.getInt(tracksNbCol)
                        val artist = ArtistItem(id, artistName, albumCount, trackCount)
                        if (!TextUtils.isEmpty(artistName) && !appPinDao.isPinArtist(artist)) {
                            artistList.add(artist)
                        }
                    }
                }
            }
            mCursor.close()
            mCursor2.close()
            artistListener?.onLoadArtistSuccessful(artistList)
            return artistList
        } else {
            artistListener?.onLoadArtistSuccessful(artistList)
            return artistList
        }
    }

    fun getCountArtist(): Int {
        where += filterBlockFolder() + filterBlockArtist() + filterBlockAlbum() + filterSkipDuration()
        val mCursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, MediaStore.Audio.Media.ARTIST + " ASC"
        )
        val mCursor2: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, selectionBlockArtist(), null, MediaStore.Audio.Artists.ARTIST + " ASC"
        )
        val artistList = ArrayList<ArtistItem>()

        if (mCursor != null && mCursor.count > 0 && mCursor2 != null && mCursor2.count > 0) {
            val joiner = CursorJoiner(mCursor2, arrayOf(MediaStore.Audio.Media.ARTIST), mCursor, arrayOf(MediaStore.Audio.Albums.ARTIST))
            for (joinerResult in joiner) {
                when (joinerResult) {
                    CursorJoiner.Result.LEFT -> {
                    }
                    CursorJoiner.Result.RIGHT -> {
                    }
                    CursorJoiner.Result.BOTH -> {
                        val idCol = mCursor2.getColumnIndex(BaseColumns._ID)
                        val nameCol = mCursor2.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST)
                        val albumsNbCol = mCursor2.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
                        val tracksNbCol = mCursor2.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                        val id = mCursor2.getLong(idCol)
                        val artistName = mCursor2.getString(nameCol)
                        val albumCount = mCursor2.getInt(albumsNbCol)
                        val trackCount = mCursor2.getInt(tracksNbCol)
                        val artist = ArtistItem(id, artistName, albumCount, trackCount)
                        if (!TextUtils.isEmpty(artistName) && !appPinDao.isPinArtist(artist)) {
                            artistList.add(artist)
                        }
                    }
                }
            }
            mCursor.close()
            mCursor2.close()
        }
        return artistList.size
    }

    fun setSortOrder(orderBy: String?) {
        sortorder = orderBy
    }

    private fun selectionBlockArtist(): String {
        var filter = ""
        val filerArtist = BaseColumns._ID
        val listBlockArtist = appBlockDao.listBlockArtist
        if (listBlockArtist.size > 0) {
            for (i in 0 until listBlockArtist.size) {
                val item = listBlockArtist[i]
                filter += if (i < listBlockArtist.size - 1) filerArtist + " != " + item.id + " AND "
                else filerArtist + " != " + item.id
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