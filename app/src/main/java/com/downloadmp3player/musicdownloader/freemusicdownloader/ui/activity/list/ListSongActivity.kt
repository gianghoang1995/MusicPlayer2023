package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.list

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliendroid.alienads.MaxIntertitial
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.DragSortRecycler
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.SongAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickSongListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityListBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.SongLoaderAsyncTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.addsong.AddSongFavoriteActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer.EqualizerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.PlayerMusicActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.moresong.DialogMoreSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.moresong.DialogMoreSongCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ListSongActivity : BaseActivity<ActivityListBinding>(),
    SongLoaderListener,
    OnClickSongListener,
    OnBinderServiceConnection {
    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {

    }

    private lateinit var popupMenu: PopupMenu
    var mLastClickTime: Long = 0
    lateinit var songLoader: SongLoaderAsyncTask
    var songAdapter: SongAdapter? = null
    lateinit var thread: Thread
    var listSong: ArrayList<MusicItem> = ArrayList()
    var songListDao: PlaylistSongDaoDB? = null
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null
    var dialogMoreSongUtils: DialogMoreSong? = null
    var dialogMoreSong: Dialog? = null
    var showDeletePlaylist = false
    var dialogCheckBookEvent: Dialog? = null
    var album: AlbumItem? = null
    var artist: ArtistItem? = null
    var playlist: PlaylistITem? = null
    var folder: FolderItem? = null
    var folderPath: String? = null
    var folderName = ""
    var shortcutFolderPath: String? = null
    var isPlaylist: Boolean = false
    var isShortcut = false

    override fun onStart() {
        super.onStart()
        AppUtils.checkPermissionFux(this)
    }


    override fun bindingProvider(inflater: LayoutInflater): ActivityListBinding {
        return ActivityListBinding.inflate(inflater)
    }

    override fun ActivityListBinding.initView() {
        setBindListener(this@ListSongActivity)
        album = intent.getParcelableExtra(AppConstants.DATA_ALBUM)
        artist = intent.getParcelableExtra(AppConstants.DATA_ARTIST)
        playlist = intent.getParcelableExtra(AppConstants.DATA_PLAYLIST)
        folder = intent.getParcelableExtra(AppConstants.DATA_FOLDER)
        folderPath = intent.getStringExtra(AppConstants.DATA_GOTO_FOLDER)

        shortcutFolderPath = intent.getStringExtra(AppConstants.SHORTCUT_FOLDER_PATH)
        val shortcutAlbumID = intent.getStringExtra(AppConstants.SHORTCUT_ALBUM_ID)
        val shortcutAlbumName = intent.getStringExtra(AppConstants.SHORTCUT_ALBUM_NAME)
        val shortcutArtistID = intent.getStringExtra(AppConstants.SHORTCUT_ARTIST_ID)
        val shortcutArtistName = intent.getStringExtra(AppConstants.SHORTCUT_ARTIST_NAME)
        val playlistName = intent.getStringExtra(AppConstants.SHORTCUT_PLAYLIST_NAME)
        val playlistID = intent.getIntExtra(AppConstants.SHORTCUT_PLAYLIST_ID, 0)
        val playlistFavoriteID = intent.getStringExtra(AppConstants.SHORTCUT_PLAYLIST_FAVORITE_ID)

        if (playlistFavoriteID != null) {
            playlist = PlaylistITem(playlistID, playlistFavoriteID, playlistName, null, 0)
            isShortcut = true
        }
        if (shortcutAlbumID != null) {
            isShortcut = true
            album = AlbumItem(shortcutAlbumID.toLong(), shortcutAlbumName, null, null, null, null)
        }

        if (shortcutArtistID != null) {
            isShortcut = true
            artist = ArtistItem(shortcutArtistID.toLong(), shortcutArtistName, null, null)
        }

        init()
    }

    override fun onResume() {
        super.onResume()
        loader()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialogMoreSong != null) {
            if (dialogMoreSong?.isShowing!!)
                dialogMoreSong?.dismiss()
        }

        if (dialogCheckBookEvent != null) {
            if (dialogCheckBookEvent?.isShowing!!) {
                dialogCheckBookEvent?.dismiss()
            }
        }
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?.getPlayer?.observe(this) {
            if (it != null)
                binding.playerView.initDataPlayerView(
                    this,
                    this,
                    it,
                    musicPlayerService?.getCurrentItem()
                )
        }

        musicPlayerService?.obverseMusicUtils?.getPlaybackState?.observe(this) {
            when (it) {
                AppConstants.PLAYBACK_STATE.STATE_PLAYING -> {
                    binding.playerView.setPlayerState(true)
                    binding.playerView.startRunnableUpdateTimer()
                }

                AppConstants.PLAYBACK_STATE.STATE_PAUSED -> {
                    binding.playerView.setPlayerState(false)
                }
            }
        }

        musicPlayerService?.obverseMusicUtils?._isServiceRunning?.observe(this) {
            if (it) {
                binding.playerView.isVisible = true
            } else {
                binding.playerView.isGone = true
            }
        }
    }


    fun init() {
//        loadBanner(frameBannerAds)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        songAdapter = SongAdapter(this, this, songIndexListener = {

        })
        binding.rvListSong.setHasFixedSize(true)
        binding.rvListSong.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvListSong.adapter = songAdapter
        initDragRecycleView()
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (appBarLayout != null) {
                val offsetAlpha: Float = appBarLayout.y / appBarLayout.totalScrollRange
                val value = (255 - (255 * offsetAlpha * -1)) / 100
                binding.rootCollapse.alpha = value
            }
        })

        onClick()
    }

    @SuppressLint("SetTextI18n")
    fun loader() {
        listSong.clear()
        if (album != null) {
            songLoader = SongLoaderAsyncTask(this, this)
            songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
            binding.tvTitle.text = album?.albumName
            filterAlbum(album?.id)?.let { songLoader.setFilter(it) }
            thread = Thread {
                songLoader.loadInBackground()
            }
            thread.start()
            Glide.with(this)
                .load(album?.id?.let { ArtworkUtils.uri(it) })
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_albums_transparent)
                .into(binding.imgHeader)
        }

        if (artist != null) {
            songLoader = SongLoaderAsyncTask(this, this)
            songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
            binding.tvTitle.text = artist?.name
            filterArtist(artist?.id)?.let { songLoader.setFilter(it) }
            thread = Thread {
                songLoader.loadInBackground()
            }
            thread.start()
            Glide.with(this)
                .load(artist?.id?.let { ArtworkUtils.uri(it) })
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_artists_transparent)
                .into(binding.imgHeader)
        }

        if (playlist != null) {
            Glide.with(this)
                .load("")
                .placeholder(R.drawable.ic_song_transparent)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgHeader)

            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    this,
                    playlist?.favorite_id
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            if (playlist?.name.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED)) {
                binding.tvTitle.text = getString(R.string.favorite_recent_added)
                songLoader = SongLoaderAsyncTask(this, this)
                songLoader.filterDateAdded()
                songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
                thread = Thread {
                    songLoader.loadInBackground()
                }
                thread.start()
                showDeletePlaylist = false
            } else {
                showDeletePlaylist = true
                when {
                    playlist?.name.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE) -> {
                        isPlaylist = true
                        binding.btnAddSong.visibility = View.VISIBLE
                        binding.tvTitle.text = getString(R.string.favorite_song)
                        songListDao?.setQueryTypeLocal()
                    }

                    playlist?.name.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING) -> {
                        songListDao?.setQueryLastPlayingLocal()
                        binding.tvTitle.text = getString(R.string.favorite_last_playing)
                    }

                    playlist?.name.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING) -> {
                        binding.tvTitle.text = getString(R.string.favorite_most_playing)
                        songListDao?.setQueryMostPlaying()
                    }

                    else -> {
                        isPlaylist = true
                        binding.btnAddSong.visibility = View.VISIBLE
                        binding.tvTitle.text = playlist?.name
                        songListDao?.setQueryTypeLocal()
                    }
                }
                songListDao?.allSongLocalFromPlaylist?.let { listSong.addAll(it) }
                sortSong()
                songAdapter?.setDataSong(listSong)
                binding.tvCountSong.text =
                    listSong.size.toString() + " " + getString(R.string.title_songs)
                checkEmpty()
            }
        }

        if (folder != null) {
            Glide.with(this).load(R.drawable.ic_folder_query)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgHeader)
            binding.tvTitle.text = folder?.name
            folder?.parentId?.let { getSongsByParentId(it) }?.let { listSong.addAll(it) }
            sortSong()
            songAdapter?.setDataSong(listSong)
            binding.tvCountSong.text =
                listSong.size.toString() + " " + getString(R.string.title_songs)
        }

        if (folderPath != null) {
            val f = File(folderPath)
            val rootFile = f.parentFile
            folderName = rootFile.name
            binding.tvTitle.text = rootFile.name
            val folderId = getParentID(rootFile.absolutePath)
            listSong.clear()
            folderId?.let { getSongsByParentId(it) }?.let { listSong.addAll(it) }
            sortSong()
            songAdapter?.setDataSong(listSong)
            binding.tvCountSong.text =
                listSong.size.toString() + " " + getString(R.string.title_songs)
            checkEmpty()
            Glide.with(this).load(artist?.id?.let { ArtworkUtils.uri(it) })
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_artists_transparent).into(binding.imgHeader)
        }

        if (shortcutFolderPath != null) {
            val f = File(shortcutFolderPath)
            binding.tvTitle.text = f.name
            val folderId = getParentID(f.absolutePath)
            listSong.clear()
            folderId?.let { getSongsByParentId(it) }?.let { listSong.addAll(it) }
            sortSong()
            songAdapter?.setDataSong(listSong)
            binding.tvCountSong.text =
                listSong.size.toString() + " " + getString(R.string.title_songs)
            checkEmpty()
            Glide.with(this).load(artist?.id?.let { ArtworkUtils.uri(it) })
                .placeholder(R.drawable.ic_folder_query)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgHeader)
        }
        val defaultSort = PreferenceUtils.getValueInt(
            AppConstants.PREF_SORT_LIST_BY,
            AppConstants.SORT.SORT_BY_NAME
        )
        if (defaultSort == AppConstants.SORT.SORT_BY_CUSTOM && isPlaylist) {
            songAdapter?.showDrag(true)
        } else {
            songAdapter?.showDrag(false)
        }
    }

    @SuppressLint("Range")
    private fun getParentID(path: String): Int? {
        val query = if (Build.VERSION.SDK_INT > 29) {
            "media_type = 2"
        } else {
            "${MediaStore.Audio.Media.IS_MUSIC + " != 0"} AND media_type = 2"
        }
        val selection = "$query AND " + MediaStore.Files.FileColumns.DATA + " LIKE '" + path + "/%'"
        contentResolver?.query(
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
        return null
    }

    private fun checkEmpty() {
        if (listSong.size > 0) {
            binding.emptyView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    private fun initDragRecycleView() {
        val dragSortRecycler = DragSortRecycler()
        dragSortRecycler.setViewHandleId(R.id.btnDrag) //View you wish to use as the handle
        dragSortRecycler.setOnItemMovedListener { from, to ->
            moveSongItem(from, to)
        }
        binding.rvListSong.addItemDecoration(dragSortRecycler)
        binding.rvListSong.addOnItemTouchListener(dragSortRecycler)
        binding.rvListSong.setOnScrollListener(dragSortRecycler.scrollListener)
        dragSortRecycler.setOnDragStateChangedListener(object :
            DragSortRecycler.OnDragStateChangedListener {
            override fun onDragStart() {
            }

            override fun onDragStop() {
            }
        })
    }

    private fun moveSongItem(lastPos: Int, moveToPos: Int) {
        val moveItem: MusicItem = listSong[lastPos]
        listSong.removeAt(lastPos)
        listSong.add(moveToPos, moveItem)
        songAdapter?.setDataSong(listSong)

        for (i in 0 until listSong.size) {
            var item = listSong[i]
            item.stt = i
            songListDao?.updateSTTSong(item)
        }
    }

    fun onClick() {
        binding.btnAddSong.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddSongFavoriteActivity::class.java
                ).putExtra(AddSongFavoriteActivity.FAVORITE_DATA, playlist)
            )
        }

        binding.btnMore.setOnClickListener {
            showPopupMenu()
        }

        binding.btnPlayAll.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                if (listSong.size > 0) musicPlayerService?.setListSong(
                    this@ListSongActivity,
                    listSong,
                    0
                )
                else showMessage(getString(R.string.no_song_to_play))
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun getSongsByParentId(parentId: Int): ArrayList<MusicItem> {
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
            filterSkipDuration() + " AND " + selection.toString() + " AND " + MediaStore.Audio.Media.IS_MUSIC + " != 0 ",
            null,
            SortOrder.SongSortOrder.SONG_A_Z
        )
    }

    fun filterSkipDuration(): String? {
        val skipDuration = PreferenceUtils.getValueInt(AppConstants.SKIP_DURATION, 0) * 1000
        return MediaStore.Audio.AudioColumns.DURATION + " > " + skipDuration
    }

    private fun getSongIdsByParentId(parentId: Int): List<Int>? {
        val ids: MutableList<Int> = java.util.ArrayList()
        contentResolver?.query(
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

    private fun runLayoutAnimation() {
        val controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_album)
        binding.rvListSong.layoutAnimation = controller
        binding.rvListSong.scheduleLayoutAnimation()
    }

    private fun getSongs(
        selection: String?,
        selectionValues: Array<String?>?,
        sortOrder: String?
    ): ArrayList<MusicItem> {
        val songs: ArrayList<MusicItem> = ArrayList()
        try {
            makeSongCursor(selection, selectionValues, sortOrder).use { cursor ->
                if (cursor != null && cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        songs.add(getSongInfo(cursor))
                    }
                }
            }
        } catch (ignore: Exception) {
        }
        return songs
    }

    private fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        return try {
            contentResolver?.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionValues,
                sortOrder
            )
        } catch (e: SecurityException) {
            null
        }
    }

    private fun getSongInfo(cursor: Cursor): MusicItem {
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

        cursor.close()
    }

    private fun filterArtist(id: Long?): String? {
        return "AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + id
    }

    private fun filterAlbum(id: Long?): String? {
        return "AND " + MediaStore.Audio.Media.ALBUM_ID + " = " + id
    }

    override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
        runOnUiThread {
            runLayoutAnimation()
            listSong.clear()
            listSong.addAll(songList)
            sortSong()
            songAdapter?.setDataSong(listSong)
            binding.tvCountSong.text =
                listSong.size.toString() + " " + getString(R.string.title_songs)
            checkEmpty()
        }
    }

    override fun onSongClick(song: MusicItem, i: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    musicPlayerService?.setListSong(this@ListSongActivity, listSong, i)
                }, onAdLoadFail = {
                    MaxIntertitial.ShowIntertitialApplovinMax(
                        this, getString(R.string.appvolin_full)
                    ) {
                        dialogLoadingAds?.dismissDialog()
                        musicPlayerService?.setListSong(this@ListSongActivity, listSong, i)
                    }
                })
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onClickControl() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            if (musicPlayerService?.exoPlayer != null) startActivity(
                Intent(
                    this, PlayerMusicActivity::class.java
                )
            )
            else {
                showMessage(getString(R.string.no_song_play))
            }
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onNextClick() {
        startService(AppConstants.ACTION_NEXT)
    }

    override fun onPriveClick() {
        startService(AppConstants.ACTION_PRIVE)
    }

    override fun onToggleClick() {
        startService(AppConstants.ACTION_TOGGLE_PLAY)
    }

    override fun onRestartClick() {
        startService(AppConstants.ACTION_RESTART)
    }

    override fun onStopMusic() {
        startService(AppConstants.ACTION_STOP)
    }

    override fun onSongMoreClick(item: Any, i: Int, view: View) {
        if (item is MusicItem) {
            dialogMoreSongUtils = DialogMoreSong(this, object : DialogMoreSongCallback {
                override fun onNextTrack() {
                    musicPlayerService?.insertNextTrack(item)
                    dialogMoreSong?.dismiss()
                }

                override fun onAddToPlaylist() {
                    dialogMoreSong?.dismiss()
                }

                override fun onSetRingtone() {
                    dialogMoreSong?.dismiss()
                    setRingtoneRingDroid(item)
                }

                override fun onDetail() {
                    dialogMoreSong?.dismiss()
                }

                override fun onShare() {
                    val fileProvider = FileProvider()
                    item.songPath?.let { fileProvider.share(baseContext, it) }
                    dialogMoreSong?.dismiss()
                }

                override fun onDelete() {

                }

                override fun onDeleteSongPlaylist() {
                    Log.e("Delete", item.title.toString())
                    songListDao?.deleteLocalSong(item)
                    listSong.clear()
                    songListDao?.allSongLocalFromPlaylist?.let { listSong.addAll(it) }
                    sortSong()
                    songAdapter?.setDataSong(listSong)
                    dialogMoreSong?.dismiss()
                    EventBus.getDefault().postSticky(EventPutLoveSong(true))
                    binding.tvCountSong.text =
                        songAdapter?.listSong?.size.toString() + " " + getString(R.string.title_songs)
                    checkEmpty()
                }
            }, showDeletePlaylist, false, musicPlayerService?.getCurrentSong(), this)

            dialogMoreSong = dialogMoreSongUtils?.getDialog(item)
            dialogMoreSong?.show()
        }
    }

    override fun onSongOnLongClick() {
    }

    override fun onSizeSelectChange(size: Int) {
    }

    override fun onClickItemOnline(itemAudioOnline: ItemMusicOnline) {

    }

    private fun showSortDialog() {
        checkIsplaylist()
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_sort)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.BOTTOM
        window.attributes = wlp
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val groupSort: RadioGroup = dialog.findViewById(R.id.groupSort)
        val groupOrder: RadioGroup = dialog.findViewById(R.id.groupOrder)
        val ckbCustom: RadioButton = dialog.findViewById(R.id.ckbCustom)
        val ckbAsc: RadioButton = dialog.findViewById(R.id.ckbOrderAsc)
        val ckbDesc: RadioButton = dialog.findViewById(R.id.ckbOrderDesc)
        val defaultSort = PreferenceUtils.getValueInt(
            AppConstants.PREF_SORT_LIST_BY,
            AppConstants.SORT.SORT_BY_NAME
        )
        val defaultOrder = PreferenceUtils.getValueInt(
            AppConstants.PREF_ORDER_LIST_BY,
            AppConstants.ORDER.ORDER_ASC
        )

        groupSort.check(groupSort[defaultSort].id)
        groupOrder.check(groupOrder[defaultOrder].id)

        enableAsc(defaultSort, defaultOrder, ckbAsc, ckbDesc)
        if (isPlaylist) {
            ckbCustom.text = getString(R.string.custom)
        } else {
            ckbCustom.text = getString(R.string.txt_default)
        }

        groupSort.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.ckbCustom -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_SORT_LIST_BY,
                        AppConstants.SORT.SORT_BY_CUSTOM
                    )
                }

                R.id.ckbSortByName -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_SORT_LIST_BY,
                        AppConstants.SORT.SORT_BY_NAME
                    )
                }

                R.id.ckbSortByAlbum -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_SORT_LIST_BY,
                        AppConstants.SORT.SORT_BY_ALBUM
                    )
                }

                R.id.ckbSortByArtist -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_SORT_LIST_BY,
                        AppConstants.SORT.SORT_BY_ARTIST
                    )
                }

                R.id.ckbSortByDuration -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_SORT_LIST_BY,
                        AppConstants.SORT.SORT_BY_DURATION
                    )
                }
            }
            enableAsc(defaultSort, defaultOrder, ckbAsc, ckbDesc)
            changeDrag(groupSort)
            sortSong()
            dialog.dismiss()
        }

        groupOrder.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.ckbOrderAsc -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_ORDER_LIST_BY,
                        AppConstants.ORDER.ORDER_ASC
                    )
                }

                R.id.ckbOrderDesc -> {
                    PreferenceUtils.put(
                        AppConstants.PREF_ORDER_LIST_BY,
                        AppConstants.ORDER.ORDER_DESC
                    )
                }
            }
            sortSong()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sortSong() {
        val sortBy = PreferenceUtils.getValueInt(
            AppConstants.PREF_SORT_LIST_BY,
            AppConstants.SORT.SORT_BY_NAME
        )
        val orderBy = PreferenceUtils.getValueInt(
            AppConstants.PREF_ORDER_LIST_BY,
            AppConstants.ORDER.ORDER_ASC
        )
        if (sortBy == AppConstants.SORT.SORT_BY_CUSTOM) {
            if (isPlaylist) listSong.sortWith(Comparator { a, b -> a.stt.compareTo(b.stt) })
            else listSong.sortWith(Comparator { a, b -> a.title?.compareTo(b?.title!!)!! })
        }
        when (orderBy) {
            AppConstants.ORDER.ORDER_ASC -> {
                when (sortBy) {
                    AppConstants.SORT.SORT_BY_NAME -> {
                        listSong.sortWith(Comparator { a, b -> a.title?.compareTo(b?.title!!)!! })
                    }

                    AppConstants.SORT.SORT_BY_ALBUM -> {
                        listSong.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                s = a.album?.compareTo(b?.album!!)!!
                            }
                            s
                        })
                    }

                    AppConstants.SORT.SORT_BY_ARTIST -> {
                        listSong.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                s = a.artist?.compareTo(b?.artist!!)!!
                            }
                            s
                        })
                    }

                    AppConstants.SORT.SORT_BY_DURATION -> {
                        listSong.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.duration) && !TextUtils.isEmpty(b.duration)) {
                                s = b?.duration?.let {
                                    a.duration?.toLong()?.compareTo(it.toLong())
                                }!!
                            }
                            s
                        })
                    }
                }
            }

            AppConstants.ORDER.ORDER_DESC -> {
                when (sortBy) {
                    AppConstants.SORT.SORT_BY_NAME -> {
                        listSong.sortWith(Comparator { a, b -> b.title?.compareTo(a?.title!!)!! })
                    }

                    AppConstants.SORT.SORT_BY_ALBUM -> {
                        listSong.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                s = a?.album?.let { b.album?.compareTo(it) }!!
                            }
                            s
                        })
                    }

                    AppConstants.SORT.SORT_BY_ARTIST -> {
                        listSong.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                s = b.artist?.compareTo(a?.artist!!)!!
                            }
                            s
                        })
                    }

                    AppConstants.SORT.SORT_BY_DURATION -> {
                        listSong.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.duration) && !TextUtils.isEmpty(b.duration)) {
                                s = a?.duration?.let {
                                    b.duration?.toLong()?.compareTo(it.toLong())
                                }!!
                            }
                            s
                        })
                    }
                }
            }
        }
        runLayoutAnimation()
        songAdapter?.setDataSong(listSong)
        val defaultSort = PreferenceUtils.getValueInt(
            AppConstants.PREF_SORT_LIST_BY,
            AppConstants.SORT.SORT_BY_NAME
        )
        if (defaultSort == AppConstants.SORT.SORT_BY_CUSTOM && isPlaylist) {
            songAdapter?.showDrag(true)
        } else {
            songAdapter?.showDrag(false)
        }
    }

    private fun enableAsc(
        defaultSort: Int,
        defaultOrder: Int,
        ckbAsc: RadioButton,
        ckbDesc: RadioButton
    ) {
        if (defaultSort == AppConstants.SORT.SORT_BY_CUSTOM) {
            ckbAsc.isEnabled = false
            ckbDesc.isEnabled = false
            ckbAsc.alpha = 0.4f
            ckbDesc.alpha = 0.4f
            ckbAsc.isChecked = false
            ckbDesc.isChecked = false
        } else {
            ckbAsc.isEnabled = true
            ckbDesc.isEnabled = true
            ckbAsc.alpha = 1f
            ckbDesc.alpha = 1f
            if (defaultOrder == AppConstants.ORDER.ORDER_ASC) {
                ckbAsc.isChecked = true
                ckbDesc.isChecked = false
            } else {
                ckbAsc.isChecked = false
                ckbDesc.isChecked = true
            }
        }
    }

    private fun changeDrag(groupSort: RadioGroup) {
        if (groupSort.checkedRadioButtonId == R.id.ckbCustom) {
            songAdapter?.showDrag(true)
        } else {
            songAdapter?.showDrag(false)
        }
    }

    private fun checkIsplaylist() {
        if (playlist != null) when {
            playlist?.name.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE) -> {
                isPlaylist = true
            }

            playlist?.name.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING) -> {
                isPlaylist = false
            }

            playlist?.name.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED) -> {
                isPlaylist = false
            }

            playlist?.name.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING) -> {
                isPlaylist = false
            }

            else -> {
                isPlaylist = true
            }
        }
        else isPlaylist = false
    }

    private fun showPopupMenu() {
        popupMenu = PopupMenu(this, binding.btnMore)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_list, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            item.isChecked = true
            when (item.itemId) {
                R.id.menuShuffle -> {
                    if (listSong.isNotEmpty()) {
                        musicPlayerService?.shuffleListSong(listSong, this)
                    } else {
                        showMessage(getString(R.string.no_song_to_play))
                    }
                }

                R.id.menuEqualizer -> {
                    BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                        onLoadAdSuccess = {
                            dialogLoadingAds?.dismissDialog()
                        }, onAdClose = {
                            startActivity(
                                Intent(
                                    this@ListSongActivity,
                                    EqualizerActivity::class.java
                                )
                            )
                        }, onAdLoadFail = {
                            MaxIntertitial.ShowIntertitialApplovinMax(
                                this, getString(R.string.appvolin_full)
                            ) {
                                dialogLoadingAds?.dismissDialog()
                                startActivity(
                                    Intent(
                                        this@ListSongActivity,
                                        EqualizerActivity::class.java
                                    )
                                )
                            }
                        })
                }

                R.id.menuAddToHomeScreen -> {
                    when {
                        playlist != null -> {
                            ShortcutManagerUtils.createShortcutPlaylist(this, playlist)
                        }

                        album != null -> {
                            ShortcutManagerUtils.createShortcutAlbum(this, album)
                        }

                        artist != null -> {
                            ShortcutManagerUtils.createShortcutArtist(this, artist)
                        }

                        folder != null -> {
                            Log.e("Shotcut folder", "true")
                            ShortcutManagerUtils.createShortcutFolder(
                                this,
                                folder,
                                R.drawable.ic_folder_query
                            )
                        }

                        folderPath != null -> {
                            val rootFile = File(folderPath).parentFile
                            val folderItem = FolderItem(folderName, 0, rootFile.path, 0)
                            ShortcutManagerUtils.createShortcutFolder(
                                this,
                                folderItem,
                                R.drawable.ic_folder_query
                            )
                        }
                    }
                }
            }
            true
        }
        popupMenu.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: EventRefreshData) {
        Log.e("Event", "RefreshData")
        loader()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: EventDeleteSong) {
        Log.e("Event", "DeleteSong")
        if (songAdapter != null) {
            songAdapter?.removeItem(item.song)
            binding.tvCountSong.text =
                songAdapter?.listSong?.size.toString() + " " + getString(R.string.title_songs)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCloseDialogSong(closeDialog: EventCloseDialog) {
        if (dialogMoreSong != null) {
            if (dialogMoreSong?.isShowing!!) {
                dialogMoreSong?.dismiss()
            }
        }
    }
}
