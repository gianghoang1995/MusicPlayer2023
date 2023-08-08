package com.musicplayer.mp3player.playermusic.ui.activity.addsong

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.ButterKnife
import butterknife.OnClick
import com.musicplayer.mp3player.playermusic.adapter.AdapterSelectSongRV
import com.musicplayer.mp3player.playermusic.adapter.AdapterSelectSongRV.OnClickSelectSongListener
import com.musicplayer.mp3player.playermusic.adapter.AdapterSelectSongFromSearch
import com.musicplayer.mp3player.playermusic.adapter.AdapterSelectSongFromSearch.OnSelectSearchSongListener
import com.musicplayer.mp3player.playermusic.base.BaseActivity
import com.musicplayer.mp3player.playermusic.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongDaoDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongSqLiteHelperDB
import com.musicplayer.mp3player.playermusic.equalizer.databinding.ActivityAddSongFavoriteBinding
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.eventbus.BusRefreshData
import com.musicplayer.mp3player.playermusic.eventbus.BusRefreshDataWhenDelete
import com.musicplayer.mp3player.playermusic.callback.SongLoaderCallback
import com.musicplayer.mp3player.playermusic.loader.FolderLoader
import com.musicplayer.mp3player.playermusic.loader.SongLoaderAsync
import com.musicplayer.mp3player.playermusic.model.AlbumItem
import com.musicplayer.mp3player.playermusic.model.ArtistItem
import com.musicplayer.mp3player.playermusic.model.FolderItem
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.model.PlaylistITem
import com.musicplayer.mp3player.playermusic.ui.dialog.DialogSelectSong.OnDeleteFileSuccess
import com.musicplayer.mp3player.playermusic.ui.dialog.DialogSelectSong.OnDialogSelectSongListener
import com.musicplayer.mp3player.playermusic.ui.dialog.DialogSelectSong.getDialogAddToPlaylist
import com.musicplayer.mp3player.playermusic.ui.dialog.DialogSelectSong.showDialogDeleteSong
import com.musicplayer.mp3player.playermusic.utils.Keyboard
import com.musicplayer.mp3player.playermusic.utils.SortOrder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AddSongFavoriteActivity : BaseActivity<ActivityAddSongFavoriteBinding>(), SongLoaderCallback, OnClickSelectSongListener,
    OnSelectSearchSongListener {
    private var mAdapterSelectSongRV: AdapterSelectSongRV? = null
    private var adapterSearchSong: AdapterSelectSongFromSearch? = null
    private var mFolder: FolderItem? = null
    private var mAlbum: AlbumItem? = null
    private var mArtist: ArtistItem? = null
    private var isNonData = false
    private var mFolderLoader: FolderLoader? = null
    private var isSelectAll = false
    private var popupSort: PopupMenu? = null
    private var lstOldList = ArrayList<MusicItem>()
    private val listSongRoot = ArrayList<MusicItem>()
    private var mDialogSelectSong: Dialog? = null
    private var mFavorite: PlaylistITem? = null
    private var mSongListSqliteHelper: PlaylistSongSqLiteHelperDB? = null
    private var mSongListDao: PlaylistSongDaoDB? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onPause() {
        super.onPause()
        if (mDialogSelectSong != null) {
            if (mDialogSelectSong?.isShowing == true) {
                mDialogSelectSong?.dismiss()
            }
        }
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityAddSongFavoriteBinding {
        return ActivityAddSongFavoriteBinding.inflate(inflater)
    }

    override fun ActivityAddSongFavoriteBinding.initView() {
        init()
    }

    @SuppressLint("SetTextI18n")
    fun init() {
        ButterKnife.bind(this)
        mFavorite = intent.getParcelableExtra(FAVORITE_DATA)
        mFolder = intent.getParcelableExtra(FOLDER_DATA)
        mAlbum = intent.getParcelableExtra(ALBUM_DATA)
        mArtist = intent.getParcelableExtra(ARTIST_DATA)
        isNonData = intent.getBooleanExtra(NONE_DATA, false)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        mAdapterSelectSongRV =
            AdapterSelectSongRV(
                this,
                this
            )
        binding.rvAddSongToFavorite.setHasFixedSize(true)
        binding.rvAddSongToFavorite.layoutManager = LinearLayoutManager(this)
        binding.rvAddSongToFavorite.adapter = mAdapterSelectSongRV
        adapterSearchSong = AdapterSelectSongFromSearch(this, this)
        binding.rvSearchSongFavorite.setHasFixedSize(true)
        binding.rvSearchSongFavorite.layoutManager = LinearLayoutManager(this)
        binding.rvSearchSongFavorite.adapter = adapterSearchSong
        loadData()
        if (isNonData) {
            binding.btnDeleteSong.visibility = View.VISIBLE
        }
        onClick()
    }


    private fun loadData() {
        if (mFolder != null) {
            loadTrackFolder(mFolder!!)
        } else if (mAlbum != null) {
            loadTrackAlbum(mAlbum!!)
        } else if (mArtist != null) {
            loadTrackArtist(mArtist!!)
        } else if (mFavorite != null) {
            mSongListSqliteHelper = PlaylistSongSqLiteHelperDB(this, mFavorite?.favorite_id)
            mSongListDao = PlaylistSongDaoDB(mSongListSqliteHelper)
            lstOldList = mSongListDao?.oldSongLocal!!
            val name =
                if (mFavorite?.name == FavoriteSqliteHelperDB.DEFAULT_FAVORITE) getString(R.string.favorite_song)
                else mFavorite?.name
            binding.btnContinue.text =
                getString(R.string.add_to) + " " + name
            loadTrack()
        } else {
            loadTrack()
        }
    }

    private fun loadTrack() {
        val songLoader = SongLoaderAsync(this, this)
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        songLoader.setNotUseFilter()
        songLoader.loadInBackground()
    }

    private fun loadTrackArtist(artist: ArtistItem) {
        val songLoader = SongLoaderAsync(this, this)
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        songLoader.setFilter(filterArtist(artist.id!!))
        songLoader.loadInBackground()
    }

    private fun loadTrackAlbum(album: AlbumItem) {
        val songLoader = SongLoaderAsync(this, this)
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        songLoader.setFilter(filterAlbum(album.id))
        songLoader.loadInBackground()
    }

    private fun filterAlbum(id: Long): String {
        return " AND " + MediaStore.Audio.Media.ALBUM_ID + " = " + id
    }

    private fun filterArtist(id: Long): String {
        return " AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + id
    }

    fun onClick() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString().trim { it <= ' ' })
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun search(s: String) {
        val listSong = mAdapterSelectSongRV!!.listSong
        val lstSearch = ArrayList<MusicItem>()
        lstSearch.clear()
        if (s.isNotEmpty()) {
            binding.rvSearchSongFavorite.visibility = View.VISIBLE
            binding.rvAddSongToFavorite.visibility = View.INVISIBLE
            binding.viewSelectAll.visibility = View.GONE
            for (i in listSong.indices) {
                val song = listSong[i]
                assert(song.title != null)
                if (song.title?.uppercase()?.contains(s.uppercase()) == true) {
                    lstSearch.add(song)
                }
            }
            adapterSearchSong!!.setData(lstSearch, lstOldList)
        } else {
            binding.rvSearchSongFavorite.visibility = View.GONE
            binding.viewSelectAll.visibility = View.VISIBLE
            binding. rvAddSongToFavorite.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (binding.edtSearch.visibility == View.GONE) {
            super.onBackPressed()
        } else if (binding.edtSearch.text.toString().trim { it <= ' ' }.isNotEmpty()) {
            binding.edtSearch.setText("")
        } else if (binding.edtSearch.text.toString().trim { it <= ' ' }.isEmpty()) {
            Keyboard.closeKeyboard(binding.edtSearch)
            binding.edtSearch.visibility = View.GONE
            binding.toolbarOption.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NonConstantResourceId")
    fun showPopupSort() {
        if (popupSort == null) {
            popupSort = PopupMenu(this, binding.btnSort)
            popupSort?.menuInflater?.inflate(R.menu.popup_sort_song, popupSort!!.menu)
            popupSort?.setOnMenuItemClickListener { item: MenuItem ->
                item.isChecked = true
                listSongRoot.clear()
                listSongRoot.addAll(mAdapterSelectSongRV!!.listSong)
                when (item.itemId) {
                    R.id.sortByName -> {
                        if (popupSort?.menu?.findItem(R.id.orderAsc)?.isChecked == true) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                lhs.title!!.compareTo(
                                    rhs.title!!
                                )
                            })
                        } else {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                rhs.title!!.compareTo(
                                    lhs.title!!
                                )
                            })
                        }
                        mAdapterSelectSongRV!!.setData(listSongRoot, lstOldList)
                    }

                    R.id.sortByAlbum -> {
                        if (popupSort!!.menu.findItem(R.id.orderAsc).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                lhs.album!!.compareTo(
                                    rhs.album!!
                                )
                            })
                        } else {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                rhs.album!!.compareTo(
                                    lhs.album!!
                                )
                            })
                        }
                        mAdapterSelectSongRV!!.setData(listSongRoot, lstOldList)
                    }

                    R.id.sortByArtist -> {
                        if (popupSort!!.menu.findItem(R.id.orderAsc).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                lhs.artist!!.compareTo(
                                    rhs.artist!!
                                )
                            })
                        } else {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                rhs.artist!!.compareTo(
                                    lhs.artist!!
                                )
                            })
                        }
                        mAdapterSelectSongRV!!.setData(listSongRoot, lstOldList)
                    }

                    R.id.orderAsc -> {
                        if (popupSort!!.menu.findItem(R.id.sortByName).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                lhs.title!!.compareTo(
                                    rhs.title!!
                                )
                            })
                        } else if (popupSort!!.menu.findItem(R.id.sortByAlbum).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                lhs.album!!.compareTo(
                                    rhs.album!!
                                )
                            })
                        } else if (popupSort!!.menu.findItem(R.id.sortByArtist).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                lhs.artist!!.compareTo(
                                    rhs.artist!!
                                )
                            })
                        }
                        mAdapterSelectSongRV!!.setData(listSongRoot, lstOldList)
                    }

                    R.id.orderDesc -> {
                        if (popupSort!!.menu.findItem(R.id.sortByName).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                rhs.title!!.compareTo(
                                    lhs.title!!
                                )
                            })
                        } else if (popupSort!!.menu.findItem(R.id.sortByAlbum).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                rhs.album!!.compareTo(
                                    lhs.album!!
                                )
                            })
                        } else if (popupSort!!.menu.findItem(R.id.sortByArtist).isChecked) {
                            listSongRoot.sortWith(Comparator { lhs: MusicItem, rhs: MusicItem ->
                                rhs.artist!!.compareTo(
                                    lhs.artist!!
                                )
                            })
                        }
                        mAdapterSelectSongRV!!.setData(listSongRoot, lstOldList)
                    }
                }
                true
            }
        }
        popupSort!!.show()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @OnClick(
        R.id.btnDeleteSong,
        R.id.btnContinue,
        R.id.edtSearch,
        R.id.btnSort,
        R.id.ckbSelectAll,
        R.id.btnSearch,
        R.id.btnBack
    )
    fun onClickView(view: View) {
        val listSong = mAdapterSelectSongRV!!.listSongSelect
        when (view.id) {
            R.id.btnDeleteSong -> if (listSong.isEmpty()) {
                getString(R.string.no_song_selected)
            } else {
                deleteSong(listSong)
            }

            R.id.btnContinue -> {
                Log.e("Size", listSong.size.toString() + " - " + lstOldList.size)
                if (mFavorite != null) {
                    if (listSong.size > 0) {
                        mSongListDao!!.insertListSongLocal(listSong)
                        EventBus.getDefault().postSticky(BusRefreshData(true))
                        showMessage(getString(R.string.success))
                        finish()
                    } else {
                        getString(R.string.no_song_selected)
                    }
                } else {
                    if (listSong.size > 0) {
                        showDialogAddSong(listSong)
                    } else {
                        getString(R.string.no_song_selected)
                    }
                }
            }

            R.id.ckbSelectAll -> {
                isSelectAll = !isSelectAll
                if (isSelectAll) {
                    mAdapterSelectSongRV!!.setSelectAll(true)
                    binding.ckbSelectAll.setImageResource(R.drawable.checkbox_on)
                } else {
                    mAdapterSelectSongRV!!.setSelectAll(false)
                    binding.ckbSelectAll.setImageResource(R.drawable.checkbox_off)
                }
            }

            R.id.btnSearch -> showSearchView()
            R.id.btnBack -> onBackPressed()
            R.id.btnSort -> showPopupSort()
        }
    }

    private fun deleteSong(lstSong: ArrayList<MusicItem>) {
        if (Build.VERSION.SDK_INT >= 30) {
            onRequestDeleteFile(lstSong)
        } else {
            showDialogDeleteSong(this, lstSong, object : OnDeleteFileSuccess {
                override fun onDeleteSuccess() {
                    EventBus.getDefault().postSticky(BusRefreshData(true))
                    finish()
                }

                override fun onDeleteFileInAndroidQ() {
                    onRequestDeleteFile(lstSong)
                }
            }).show()
        }
    }

    private fun showDialogAddSong(lstSong: ArrayList<MusicItem>) {
        mDialogSelectSong = getDialogAddToPlaylist(this, lstSong,
            object : OnDialogSelectSongListener {
                override fun onAddToPlaylistDone() {
                    EventBus.getDefault().postSticky(BusRefreshData(true))
                    finish()
                }
            })
        mDialogSelectSong!!.show()
    }

    private fun showSearchView() {
        binding.edtSearch.visibility = View.VISIBLE
        binding.toolbarOption.visibility = View.GONE
        Keyboard.showKeyboard(binding.edtSearch)
        binding.edtSearch.requestFocus()
    }

    private fun loadTrackFolder(folder: FolderItem) {
        mFolderLoader = FolderLoader(
            this,
            folder.path,
            object : SongLoaderCallback {
                override fun onAudioLoadedSuccessful(songList: java.util.ArrayList<MusicItem>) {
                    initData(songList)
                }
            })
        mFolderLoader!!.loadInBackground()
    }

    private fun initData(lstSong: ArrayList<MusicItem>) {
        mAdapterSelectSongRV!!.setData(lstSong, lstOldList)
    }

    override fun onSelectAllSong(bool: Boolean) {
        isSelectAll = bool
        if (isSelectAll) {
            binding.ckbSelectAll.setImageResource(R.drawable.checkbox_on)
        } else {
            binding.ckbSelectAll.setImageResource(R.drawable.checkbox_off)
        }
    }

    override fun onSelectSearchSong(song: MusicItem) {
        mAdapterSelectSongRV!!.selectSong(song)
    }

    override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
        if (songList.isEmpty()) {
            binding. tvEmpty.visibility = View.VISIBLE
            binding.rvAddSongToFavorite.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvAddSongToFavorite.visibility = View.VISIBLE
        }
        mAdapterSelectSongRV?.setData(songList, lstOldList)
        binding.ckbSelectAll.isEnabled = songList.size != lstOldList.size
        if (songList.size != lstOldList.size) {
            binding. ckbSelectAll.alpha = 1f
        } else {
            binding. ckbSelectAll.alpha = 0.5f
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun OnRefreshData(event: BusRefreshData?) {
        loadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun OnRefreshData(event: BusRefreshDataWhenDelete?) {
        finish()
    }

    companion object {
        const val FOLDER_DATA = "FOLDER_DATA"
        const val ALBUM_DATA = "ALBUM_DATA"
        const val ARTIST_DATA = "ARTIST_DATA"
        const val FAVORITE_DATA = "FAVORITE_DATA"
        const val NONE_DATA = "NONE_DATA"
    }
}