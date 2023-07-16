package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.album

import android.app.Dialog
import android.content.Intent
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterAlbum
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickAlbumListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockDaoBD
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentAlbumsBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.AlbumLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.AlbumLoaderTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.SongLoaderAsyncTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.addsong.AddSongFavoriteActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer.EqualizerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.list.ListSongActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.album.adapter.AdapterUnblockAlbums
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.song.SongFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class AlbumsFragment : BaseFragment<FragmentAlbumsBinding>(), AlbumLoaderListener,
    OnClickAlbumListener {
    var needLoaded = false
    private var popupSort: PopupMenu? = null
    private lateinit var songLoader: SongLoaderAsyncTask
    var mLastClickTime: Long = 0
    lateinit var albumLoader: AlbumLoaderTask
    lateinit var thread: Thread
    lateinit var albumAdapter: AdapterAlbum
    var listPin: ArrayList<AlbumItem> = ArrayList()
    var listQuery: ArrayList<AlbumItem> = ArrayList()
    var menuID: Int? = null
    lateinit var pinAlbumDao: AppPinDaoDB
    lateinit var pinAlbumHelper: AppPinHelperDB
    lateinit var blockAlbumDao: AppBlockDaoBD
    lateinit var blockAlbumHelper: AppBlockHelperDB

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAlbumsBinding {
        return FragmentAlbumsBinding.inflate(inflater)
    }

    override fun FragmentAlbumsBinding.initView() {
        init()
        loader()
    }

    fun loader() {
        needLoaded = false
        listPin.clear()
        listQuery.clear()
        listPin.addAll(pinAlbumDao.listPinAlbum)
        albumLoader = AlbumLoaderTask(context, this)
        albumLoader.setSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z)
        thread = Thread {
            albumLoader.loadInBackground()
        }
        thread.start()
    }

    fun init() {
        blockAlbumHelper =
            AppBlockHelperDB(context)
        blockAlbumDao = AppBlockDaoBD(
            blockAlbumHelper
        )
        pinAlbumHelper =
            AppPinHelperDB(context)
        pinAlbumDao =
            AppPinDaoDB(pinAlbumHelper)

        albumAdapter = AdapterAlbum(context, this)
        binding.rvAlbums.setHasFixedSize(true)
        binding.rvAlbums.layoutManager = GridLayoutManager(context, 2)
        binding.rvAlbums.adapter = albumAdapter

        binding.swipeToRefreshAlbum.setOnRefreshListener {
            loader()
        }
    }

    override fun onLoadAlbumSuccessful(listAlbum: ArrayList<AlbumItem>) {
        activity?.runOnUiThread {
            binding.swipeToRefreshAlbum.isRefreshing = false
            thread.interrupt()
            listQuery.clear()
            listQuery.addAll(listAlbum)
            runLayoutAnimation()
            sortDefaultAlbum()
            checkEmpty(listAlbum)
        }
    }

    private fun checkEmpty(listAlbum: ArrayList<AlbumItem>) {
        if (listAlbum.isNotEmpty() || listPin.isNotEmpty()) {
            binding.emptyView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    private fun runLayoutAnimation() {
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_album)
        binding.rvAlbums.layoutAnimation = controller
        binding.rvAlbums.scheduleLayoutAnimation()
    }

    override fun onAlbumClick(album: AlbumItem, i: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            val intent = Intent(context, ListSongActivity::class.java)
            intent.putExtra(AppConstants.DATA_ALBUM, album)
            startActivity(intent)
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onAlbumMoreClick(album: AlbumItem, i: Int, view: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            showPopupAlbum(album, i, view)
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    private fun showPopupAlbum(album: AlbumItem, pos: Int, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.popup_more_album, popup.menu)
        if (pinAlbumDao.isPinAlbum(album)) {
            popup.menu.findItem(R.id.menuPinItem).title = getString(R.string.unpin)
            popup.menu.findItem(R.id.menuBlockItem).isVisible = false
        } else {
            popup.menu.findItem(R.id.menuBlockItem).isVisible = true
            popup.menu.findItem(R.id.menuPinItem).title = getString(R.string.pin_album)
        }

        popup.setOnMenuItemClickListener { item: MenuItem ->
            menuID = item.itemId
            when (item.itemId) {
                R.id.menuPlay -> querySong(album)
                R.id.menuPlayNext -> querySong(album)
                R.id.menuShuffle -> querySong(album)
                R.id.menuAddQueue -> querySong(album)
                R.id.menuShare -> querySong(album)
                R.id.menuAddToPlaylist -> addToPlaylist(album)
                R.id.menuPinItem -> pinAlbum(album)
                R.id.menuBlockItem -> blockAlbum(pos, album)
                R.id.menuAddToHomeScreen -> ShortcutManagerUtils.createShortcutAlbum(context, album)
            }
            true
        }
        popup.show()
    }

    private fun pinAlbum(album: AlbumItem) {
        if (pinAlbumDao.isPinAlbum(album)) {
            pinAlbumDao.deletePinAlbum(album)
        } else {
            pinAlbumDao.insertPinAlbum(album)
        }
        loader()
    }

    fun mainShowMenuPopupAlbum() {
        popupSort = PopupMenu(requireContext(), binding.anchorPopup)
        popupSort?.menuInflater?.inflate(R.menu.main_popup_album, popupSort?.menu)
        val sortBy = PreferenceUtils.getValueInt(AppConstants.ALBUM_SORT_BY, R.id.albumSortByAlbum)
        val orderBy = PreferenceUtils.getValueInt(AppConstants.ALBUM_ORDER_BY, R.id.albumOrderAsc)
        popupSort?.menu?.findItem(sortBy)?.isChecked = true
        popupSort?.menu?.findItem(orderBy)?.isChecked = true
        popupSort?.setOnMenuItemClickListener { item: MenuItem ->
            item.isChecked = true
            when (item.itemId) {
                R.id.menuEqualizer -> {
                    dialogLoadingAds?.showDialogLoading()
                    BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(requireActivity(),
                        onLoadAdSuccess = {
                            dialogLoadingAds?.dismissDialog()
                        }, onAdClose = {
                            startActivity(Intent(requireContext(), EqualizerActivity::class.java))
                        }, onAdLoadFail = {
                            MaxIntertitial.ShowIntertitialApplovinMax(
                                requireActivity(), getString(R.string.appvolin_full)
                            ) {
                                dialogLoadingAds?.dismissDialog()
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        EqualizerActivity::class.java
                                    )
                                )
                            }
                        })
                }

                R.id.menuShowBlockFolder -> showDialogUnblockAlbum()
                R.id.albumSortByAlbum -> {
                    PreferenceUtils.put(AppConstants.ALBUM_SORT_BY, R.id.albumSortByAlbum)
                    if (popupSort?.menu?.findItem(R.id.albumOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.albumName?.compareTo(b?.albumName!!)!! })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                s = a.albumName?.compareTo(b.albumName!!)!!
                            }
                            s
                        })
                    } else {
                        listPin.sortWith(Comparator { a, b -> b.albumName?.compareTo(a?.albumName!!)!! })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                s = b.albumName?.compareTo(a?.albumName!!)!!
                            }
                            s
                        })
                    }
                    runLayoutAnimation()
                    albumAdapter.setDataAlbum(listPin, listQuery)
                }

                R.id.albumSortByCount -> {
                    PreferenceUtils.put(AppConstants.ALBUM_SORT_BY, R.id.albumSortByCount)
                    if (popupSort?.menu?.findItem(R.id.albumOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                        listQuery.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                    } else {
                        listPin.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                        listQuery.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                    }
                    runLayoutAnimation()
                    albumAdapter.setDataAlbum(listPin, listQuery)
                }

                R.id.albumSortByArtist -> {
                    PreferenceUtils.put(AppConstants.ALBUM_SORT_BY, R.id.albumSortByArtist)
                    if (popupSort?.menu?.findItem(R.id.albumOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.artistName?.compareTo(b?.artistName!!)!! })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                s = a.artistName?.compareTo(b?.artistName!!)!!
                            }
                            s
                        })

                    } else {
                        listPin.sortWith(Comparator { a, b -> b.artistName?.compareTo(a?.artistName!!)!! })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                s = b.artistName?.compareTo(a?.artistName!!)!!
                            }
                            s
                        })
                    }
                    runLayoutAnimation()
                    albumAdapter.setDataAlbum(listPin, listQuery)
                }

                R.id.albumSortByYear -> {
                    PreferenceUtils.put(AppConstants.ALBUM_SORT_BY, R.id.albumSortByYear)
                    if (popupSort?.menu?.findItem(R.id.albumOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.year?.compareTo(b?.year!!)!! })
                        listQuery.sortWith(Comparator { a, b -> a.year?.compareTo(b?.year!!)!! })
                    } else {
                        listPin.sortWith(Comparator { a, b -> b.year?.compareTo(a?.year!!)!! })
                        listQuery.sortWith(Comparator { a, b -> b.year?.compareTo(a?.year!!)!! })
                    }
                    runLayoutAnimation()
                    albumAdapter.setDataAlbum(listPin, listQuery)
                }
                /**Oder by*/
                R.id.albumOrderAsc -> {
                    PreferenceUtils.put(AppConstants.ALBUM_ORDER_BY, R.id.albumOrderAsc)
                    when {
                        popupSort?.menu?.findItem(R.id.albumSortByAlbum)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> a.albumName?.compareTo(b?.albumName!!)!! })
                            listQuery.sortWith(Comparator { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                    s = a.albumName?.compareTo(b?.albumName!!)!!
                                }
                                s
                            })
                        }

                        popupSort?.menu?.findItem(R.id.albumSortByCount)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                            listQuery.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                        }

                        popupSort?.menu?.findItem(R.id.albumSortByArtist)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> a.artistName?.compareTo(b?.artistName!!)!! })
                            listQuery.sortWith(Comparator { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                    s = a.artistName?.compareTo(b?.artistName!!)!!
                                }
                                s
                            })
                        }

                        else -> {
                            listPin.sortWith(Comparator { a, b -> a.year?.compareTo(b?.year!!)!! })
                            listQuery.sortWith(Comparator { a, b -> a.year?.compareTo(b?.year!!)!! })
                        }
                    }
                    runLayoutAnimation()
                    albumAdapter.setDataAlbum(listPin, listQuery)
                }

                R.id.albumOrderDesc -> {
                    PreferenceUtils.put(AppConstants.ALBUM_ORDER_BY, R.id.albumOrderDesc)
                    when {
                        popupSort?.menu?.findItem(R.id.albumSortByAlbum)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> b.albumName?.compareTo(a?.albumName!!)!! })
                            listQuery.sortWith(Comparator { a, b -> b.albumName?.compareTo(a?.albumName!!)!! })
                        }

                        popupSort?.menu?.findItem(R.id.albumSortByCount)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                            listQuery.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                        }

                        popupSort?.menu?.findItem(R.id.albumSortByArtist)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> b.artistName?.compareTo(a?.artistName!!)!! })
                            listQuery.sortWith(Comparator { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                    s = b.artistName?.compareTo(a?.artistName!!)!!
                                }
                                s
                            })
                        }

                        else -> {
                            listPin.sortWith(Comparator { a, b -> b.year?.compareTo(a?.year!!)!! })
                            listQuery.sortWith(Comparator { a, b -> b.year?.compareTo(a?.year!!)!! })
                        }
                    }
                    runLayoutAnimation()
                    albumAdapter.setDataAlbum(listPin, listQuery)
                }
            }
            true
        }
        popupSort?.show()
    }

    private fun sortDefaultAlbum() {
        var sortBy = PreferenceUtils.getValueInt(AppConstants.ALBUM_SORT_BY, R.id.albumSortByAlbum)
        when (PreferenceUtils.getValueInt(AppConstants.ALBUM_ORDER_BY, R.id.albumOrderAsc)) {
            R.id.albumOrderAsc -> {
                when (sortBy) {
                    R.id.albumSortByAlbum -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                s = b.albumName?.let { a.albumName?.compareTo(it) }!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                s = b.albumName?.let { a.albumName?.compareTo(it) }!!
                            }
                            s
                        })
                    }

                    R.id.albumSortByCount -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.trackCount.toString()) && !TextUtils.isEmpty(b.trackCount.toString())) {
                                s = a.trackCount?.compareTo(b?.trackCount!!)!!
                            }
                            s
                        })

                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.trackCount.toString()) && !TextUtils.isEmpty(b.trackCount.toString())) {
                                s = a.trackCount?.compareTo(b?.trackCount!!)!!
                            }
                            s
                        })
                    }

                    R.id.albumSortByArtist -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                s = a.artistName?.compareTo(b?.artistName!!)!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                s = a.artistName?.compareTo(b?.artistName!!)!!
                            }
                            s
                        })
                    }

                    else -> {
                        listPin.sortWith(Comparator { a, b -> a.year?.compareTo(b?.year!!)!! })
                        listQuery.sortWith(Comparator { a, b -> a.year?.compareTo(b?.year!!)!! })
                    }
                }
                runLayoutAnimation()
                albumAdapter.setDataAlbum(listPin, listQuery)
            }

            R.id.albumOrderDesc -> {
                when (sortBy) {
                    R.id.albumSortByAlbum -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                s = b.albumName?.compareTo(a?.albumName!!)!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumName) && !TextUtils.isEmpty(b.albumName)) {
                                s = b.albumName?.compareTo(a?.albumName!!)!!
                            }
                            s
                        })
                    }

                    R.id.albumSortByCount -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.trackCount.toString()) && !TextUtils.isEmpty(b.trackCount.toString())) {
                                s = b.trackCount?.compareTo(a?.trackCount!!)!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.trackCount.toString()) && !TextUtils.isEmpty(b.trackCount.toString())) {
                                s = b.trackCount?.compareTo(a?.trackCount!!)!!
                            }
                            s
                        })

                    }

                    R.id.albumSortByArtist -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                s = b.artistName?.compareTo(a?.artistName!!)!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artistName) && !TextUtils.isEmpty(b.artistName)) {
                                s = b.artistName?.compareTo(a?.artistName!!)!!
                            }
                            s
                        })
                    }

                    else -> {
                        listPin.sortWith(Comparator { a, b -> b.year?.compareTo(a?.year!!)!! })
                        listQuery.sortWith(Comparator { a, b -> b.year?.compareTo(a?.year!!)!! })
                    }
                }
                runLayoutAnimation()
                albumAdapter.setDataAlbum(listPin, listQuery)
            }
        }
    }

    private fun blockAlbum(pos: Int, album: AlbumItem) {
        blockAlbumDao.insertBlockAlbum(album)
        albumAdapter.removeItem(pos)
        listQuery.remove(album)
        checkEmpty(listQuery)
        EventBus.getDefault().postSticky(EventReloadUnblockData(true))
    }

    private fun addToPlaylist(album: AlbumItem) {
        val intent = Intent(context, AddSongFavoriteActivity::class.java)
        intent.putExtra(AddSongFavoriteActivity.ALBUM_DATA, album)
        startActivity(intent)
    }

    private fun querySong(album: AlbumItem) {
        songLoader = SongLoaderAsyncTask(context, object : SongLoaderListener {
            override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
                val listSong = ArrayList<MusicItem>()
                listSong.addAll(songList)
                Collections.sort(listSong, SongFragment.SongNameComparator())
                if (listSong.size > 0) {
                    when (menuID) {
                        R.id.menuPlay -> {
                            context?.let { musicPlayerService?.setListSong(it, listSong, 0) }
                        }

                        R.id.menuPlayNext -> {
                            context?.let { musicPlayerService?.insertListNextTrack(listSong) }
                        }

                        R.id.menuShuffle -> {
                            context?.let { musicPlayerService?.shuffleListSong(listSong, context) }
                        }

                        R.id.menuAddQueue -> {
                            context?.let { musicPlayerService?.addToQueue(listSong) }
                        }

                        R.id.menuShare -> {
                            val fileProvider = FileProvider()
                            context?.let { fileProvider.shareListSong(it, listSong) }
                        }
                    }
                } else {
                    getString(R.string.empty_song)
                }
            }
        })
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        filterAlbum(album.id)?.let { it1 -> songLoader.setFilter(it1) }
        songLoader.loadInBackground()
    }

    private fun showDialogUnblockAlbum() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_show_block_folder)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.attributes = wlp
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val btnUnBlockFolder = dialog.findViewById<Button>(R.id.btnUnBlock)
        val tvEmptyBlockFolder = dialog.findViewById<TextView>(R.id.tvEmptyBlock)
        val rvBlockFolder: RecyclerView = dialog.findViewById(R.id.rvBlock)

        tvTitle.text = getString(R.string.blocked_albums)
        btnUnBlockFolder.text = getString(R.string.unblock_album)
        val adapterBlockFolder = AdapterUnblockAlbums(context) { isEnable: Boolean ->
            btnUnBlockFolder.isEnabled = isEnable
            if (isEnable) {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_unlock_folder)
            } else {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_cancel)
            }
        }
        rvBlockFolder.setHasFixedSize(true)
        rvBlockFolder.layoutManager = LinearLayoutManager(context)
        rvBlockFolder.adapter = adapterBlockFolder
        var lstBlock: ArrayList<AlbumItem> = blockAlbumDao.listBlockAlbum
        if (lstBlock.isEmpty()) {
            tvEmptyBlockFolder.visibility = View.VISIBLE
        } else {
            adapterBlockFolder.setData(lstBlock)
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnUnBlockFolder.setOnClickListener {
            blockAlbumDao.deleteBlockAlbum(adapterBlockFolder.listDelete)
            dialog.dismiss()
//            BaseApplication.mInstance?.isRefreshData?.postValue(true)
            EventBus.getDefault().postSticky(EventRefreshData(true))
            loader()
        }
        dialog.show()
    }

    private fun filterAlbum(id: Long): String? {
        return " AND " + MediaStore.Audio.Media.ALBUM_ID + " = " + id
    }

    private fun argument(id: Long): Array<String>? {
        return arrayOf(id.toString())
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteListSong(item: EventRefreshDataWhenDelete) {
        loader()
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: EventReloadUnblockData) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: EventRefreshData) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: EventDeleteSong) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun onShowMoreEvent(item: EventShowMoreEvent) {
        if (item.pos == 0) mainShowMenuPopupAlbum()
    }

    fun reloadData() {
        if (needLoaded) {
            needLoaded = false
            loader()
        }
    }

}
