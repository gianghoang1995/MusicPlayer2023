//package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder
//
//import android.content.Intent
//import android.database.Cursor
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.downloadmp3player.musicdownloader.freemusicdownloader.R
//import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.FolderAdapter
//import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickFolderListener
//import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshDataWhenDelete
//import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem
//import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
//import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.list.ListSongActivity
//import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder.loader.ScaningFolderListener
//import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder.loader.ScanningFolderAsync
//import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
//import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.SortOrder
//import kotlinx.android.synthetic.main.fragment_folder_selected.*
//import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//
//class FolderSelectedFragment : BaseFragment(), ScaningFolderListener, OnClickFolderListener {
//    var adsShowed = false
//    var mLastClickTime: Long = 0
//    lateinit var folderAdapter: FolderAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_folder_selected, container, false)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        adsShowed = false
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//    }
//
//    override fun init() {
//        folderAdapter = FolderAdapter(context, this)
//        rv_folder.setHasFixedSize(true)
//        rv_folder.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        rv_folder.adapter = folderAdapter
//        OverScrollDecoratorHelper.setUpOverScroll(
//            rv_folder, OverScrollDecoratorHelper.ORIENTATION_VERTICAL
//        )
//        initHomeData()
//        scanningFile()
//    }
//
//    private fun initHomeData() {
//        scanningFile()
//    }
//
//    private fun scanningFile() {
//        context?.let { ScanningFolderAsync(it, this).execute() }
//    }
//
//    override fun onFolderClick(folder: FolderItem, i: Int) {
//        var intent = Intent(context, ListSongActivity::class.java)
//        intent.putExtra(AppConstants.DATA_FOLDER, folder)
//        startActivity(intent)
//    }
//
//    override fun onSongClick(song: MusicItem, pos: Int) {
//
//    }
//
//    override fun onFolderMoreClick(folder: FolderItem, i: Int) {
//
//    }
//
//    override fun onScanningMusicFolderSuccess(folderData: ArrayList<FolderItem>) {
//        if (folderData != null) {
//            folderAdapter.setData(folderData, null)
//        }
//        rv_folder.scrollToPosition(0)
//    }
//
//    fun getSongsByParentId(parentId: Int): ArrayList<MusicItem> {
//        val ids: List<Int>? = getSongIdsByParentId(parentId)
//        if (ids?.size == 0) {
//            return java.util.ArrayList()
//        }
//        val selection = StringBuilder(127)
//        selection.append(MediaStore.Audio.Media._ID + " in (")
//        if (ids != null) {
//            for (i in ids.indices) {
//                selection.append(ids[i]).append(if (i == ids.size - 1) ") " else ",")
//            }
//        }
//        return getSongs(
//            selection.toString() + " AND " + MediaStore.Audio.Media.IS_MUSIC + " != 0 ",
//            null,
//            SortOrder.SongSortOrder.SONG_A_Z
//        )
//    }
//
//    fun getSongs(
//        selection: String?,
//        selectionValues: Array<String?>?,
//        sortOrder: String?
//    ): ArrayList<MusicItem> {
//        val songs: ArrayList<MusicItem> = ArrayList()
//        try {
//            makeSongCursor(selection, selectionValues, sortOrder).use { cursor ->
//                if (cursor != null && cursor.getCount() > 0) {
//                    while (cursor.moveToNext()) {
//                        songs.add(getSongInfo(cursor))
//                    }
//                }
//            }
//        } catch (ignore: Exception) {
//        }
//        return songs
//    }
//
//    fun getSongInfo(cursor: Cursor): MusicItem {
//        val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
//        val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
//        val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
//        val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
//        val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
//        val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
//        val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
//        val datacol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
//        val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
//        val artistIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
//        val bitRateCol = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
//
//        val id = cursor.getLong(idCol)
//        val title = cursor.getString(titleCol)
//        val artist = cursor.getString(artistCol)
//        val album = cursor.getString(albumCol)
//        val albumId = cursor.getLong(albumIdCol)
//        val track = cursor.getInt(trackCol)
//        val mSongPath = cursor.getString(datacol)
//        val year = cursor.getString(yearCol)
//        val artistId = cursor.getLong(artistIdCol)
//        val duration = cursor.getString(durationCol)
//        val bitrate = cursor.getInt(bitRateCol)
//        return MusicItem(
//            id,
//            title,
//            bitrate,
//            artist,
//            album,
//            track,
//            albumId,
//            "",
//            mSongPath,
//            false,
//            year,
//            "",
//            artistId,
//            duration,
//            0,
//            0
//        )
//
//        cursor.close()
//    }
//
//    private fun makeSongCursor(
//        selection: String?,
//        selectionValues: Array<String?>?,
//        sortOrder: String?
//    ): Cursor? {
//        return try {
//            context?.contentResolver?.query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null,
//                selection,
//                selectionValues,
//                sortOrder
//            )
//        } catch (e: SecurityException) {
//            null
//        }
//    }
//
//    private fun getSongIdsByParentId(parentId: Int): List<Int>? {
//        val ids: MutableList<Int> = java.util.ArrayList()
//        context?.contentResolver?.query(
//            MediaStore.Files.getContentUri("external"),
//            arrayOf("_id"),
//            "parent = $parentId",
//            null,
//            null
//        ).use { cursor ->
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    ids.add(cursor.getInt(0))
//                }
//            }
//        }
//        return ids
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
//    fun onDeleteListSong(item: EventRefreshDataWhenDelete) {
//        initHomeData()
//    }
//}
