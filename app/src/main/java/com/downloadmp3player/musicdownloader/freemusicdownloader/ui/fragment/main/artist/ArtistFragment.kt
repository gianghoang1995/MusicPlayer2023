package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.artist

import android.app.Dialog
import android.content.Intent
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
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
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.ArtistAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickArtistListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockDaoBD
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentArtistBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.ArtistLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.ArtistLoaderTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.SongLoaderAsyncTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.addsong.AddSongFavoriteActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer.EqualizerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.list.ListSongActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.artist.adapter.AdapterUnblockArtist
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.song.SongFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ArtistFragment : BaseFragment<FragmentArtistBinding>(), ArtistLoaderListener,
    OnClickArtistListener {
    var needLoaded = false
    var popupSort: PopupMenu? = null
    var menuID: Int? = null
    var mLastClickTime: Long = 0
    lateinit var loader: ArtistLoaderTask
    var thread: Thread? = null
    lateinit var artistAdapter: ArtistAdapter
    lateinit var appPinDao: AppPinDaoDB
    lateinit var appPinHelper: AppPinHelperDB
    lateinit var appBlockHelper: AppBlockHelperDB
    lateinit var appBlockDao: AppBlockDaoBD
    var listPin: ArrayList<ArtistItem> = ArrayList()
    var listQuery: ArrayList<ArtistItem> = ArrayList()
    lateinit var songLoader: SongLoaderAsyncTask

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentArtistBinding {
        return FragmentArtistBinding.inflate(inflater)
    }

    override fun FragmentArtistBinding.initView() {
        init()
        loader()
    }

    fun init() {
        appPinHelper = AppPinHelperDB(
            requireContext()
        )
        appPinDao =
            AppPinDaoDB(appPinHelper)
        appBlockHelper =
            AppBlockHelperDB(
                requireContext()
            )
        appBlockDao = AppBlockDaoBD(
            appBlockHelper
        )

        artistAdapter = ArtistAdapter(requireContext(), this)
         binding.rvArtist.setHasFixedSize(true)
         binding.rvArtist.layoutManager = GridLayoutManager(requireContext(), 2)
         binding.rvArtist.adapter = artistAdapter

        binding.swipeToRefreshArtist.setOnRefreshListener {
            loader()
        }
    }

    fun loader() {
        needLoaded = false
        loader = ArtistLoaderTask(requireContext(), this)
        listPin.clear()
        listPin.addAll(appPinDao.listPinArtist)
        listQuery.clear()
        loader.setSortOrder(SortOrder.ArtistSortOrder.ARTIST_A_Z)
        thread = Thread {
            loader.loadInBackground()
        }
        thread?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onLoadArtistSuccessful(listArtist: ArrayList<ArtistItem>) {
        activity?.runOnUiThread {
            thread?.interrupt()
            binding.swipeToRefreshArtist.isRefreshing = false
            listQuery.clear()
            listQuery.addAll(listArtist)
            sortDefaultAlbum()
            checkEmpty(listArtist)
        }
    }

    private fun checkEmpty(listArtist: ArrayList<ArtistItem>) {
        if (listArtist.isNotEmpty() || listPin.isNotEmpty()) {
            binding.emptyView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    override fun onArtistClick(artist: ArtistItem, i: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            val intent = Intent(requireContext(), ListSongActivity::class.java)
            intent.putExtra(AppConstants.DATA_ARTIST, artist)
            startActivity(intent)
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onArtistMoreClick(artist: ArtistItem, i: Int, view: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            showPopupMoreArtist(artist, i, view)
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    private fun showPopupMoreArtist(artist: ArtistItem, pos: Int, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.popup_more_artist, popup.menu)
        if (appPinDao.isPinArtist(artist)) {
            popup.menu.findItem(R.id.menuPinItem).title = getString(R.string.unpin)
            popup.menu.findItem(R.id.menuBlockItem).isVisible = false
        } else {
            popup.menu.findItem(R.id.menuBlockItem).isVisible = true
            popup.menu.findItem(R.id.menuPinItem).title = getString(R.string.pin_artist)
        }

        popup.setOnMenuItemClickListener { item: MenuItem ->
            menuID = item.itemId
            when (item.itemId) {
                R.id.menuPlay -> querySong(artist)
                R.id.menuPlayNext -> querySong(artist)
                R.id.menuShuffle -> querySong(artist)
                R.id.menuAddQueue -> querySong(artist)
                R.id.menuShare -> querySong(artist)
                R.id.menuAddToPlaylist -> addToPlaylist(artist)
                R.id.menuPinItem -> pinArtist(artist)
                R.id.menuBlockItem -> blockArtist(pos, artist)
                R.id.menuAddToHomeScreen -> ShortcutManagerUtils.createShortcutArtist(
                    requireContext(), artist
                )
            }
            true
        }
        popup.show()
    }

    private fun blockArtist(pos: Int, artist: ArtistItem) {
        artistAdapter.removeItem(pos)
        appBlockDao.insertBlockArtist(artist)
        listQuery.remove(artist)
        checkEmpty(listQuery)
        EventBus.getDefault().postSticky(EventReloadUnblockData(true))
    }

    private fun pinArtist(artist: ArtistItem) {
        if (appPinDao.isPinArtist(artist)) {
            appPinDao.deletePinArtist(artist)
        } else {
            appPinDao.insertPinArtist(artist)
        }
        loader()
    }

    private fun querySong(artist: ArtistItem) {
        songLoader = SongLoaderAsyncTask(requireContext(), object : SongLoaderListener {
            override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
                val listSong = ArrayList<MusicItem>()
                listSong.addAll(songList)
                Collections.sort(listSong, SongFragment.SongNameComparator())
                if (listSong.size > 0) {
                    when (menuID) {
                        R.id.menuPlay -> {
                            requireContext().let {
                                musicPlayerService?.setListSong(
                                    it,
                                    listSong,
                                    0
                                )
                            }
                        }

                        R.id.menuPlayNext -> {
                            requireContext().let { musicPlayerService?.insertListNextTrack(listSong) }
                        }

                        R.id.menuShuffle -> {
                            requireContext().let {
                                musicPlayerService?.shuffleListSong(
                                    listSong,
                                    requireContext()
                                )
                            }
                        }

                        R.id.menuAddQueue -> {
                            requireContext().let { musicPlayerService?.addToQueue(listSong) }
                        }

                        R.id.menuShare -> {
                            val fileProvider = FileProvider()
                            requireContext().let { fileProvider.shareListSong(it, listSong) }
                        }
                    }
                } else {
                    getString(R.string.empty_song)
                }
            }
        })
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        filterArtist(artist.id)?.let { songLoader.setFilter(it) }
        songLoader.loadInBackground()
    }

    private fun addToPlaylist(artist: ArtistItem) {
        val intent = Intent(requireContext(), AddSongFavoriteActivity::class.java)
        intent.putExtra(AddSongFavoriteActivity.ARTIST_DATA, artist)
        startActivity(intent)
    }

    private fun mainShowMenuPopupArtist() {
        popupSort = PopupMenu(requireContext(), binding.anchorPopup)
        popupSort?.menuInflater?.inflate(R.menu.main_popup_artist, popupSort?.menu)
        val sortBy =
            PreferenceUtils.getValueInt(AppConstants.ARTIST_SORT_BY, R.id.artistSortByArtist)
        val orderBy = PreferenceUtils.getValueInt(AppConstants.ARTIST_ORDER_BY, R.id.artistOrderAsc)

        popupSort?.menu?.findItem(sortBy)?.isChecked = true
        popupSort?.menu?.findItem(orderBy)?.isChecked = true
        popupSort?.setOnMenuItemClickListener { item: MenuItem ->
            item.isChecked = true
            when (item.itemId) {
                R.id.menuEqualizer -> {
                    dialogLoadingAds?.showDialogLoading()
                    BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                        onLoadAdSuccess = {
                            dialogLoadingAds?.dismissDialog()
                        }, onAdClose = {
                            startActivity(Intent(requireContext(), EqualizerActivity::class.java))
                        }, onAdLoadFail = {
                            MaxIntertitial.ShowIntertitialApplovinMax(
                                requireActivity(), getString(R.string.appvolin_full)
                            ) {
                                dialogLoadingAds?.dismissDialog()
                                startActivity(Intent(requireContext(), EqualizerActivity::class.java))
                            }
                        })
                }

                R.id.menuShowBlockArtist -> showDialogUnblockArtist()
                R.id.artistSortByArtist -> {
                    PreferenceUtils.put(AppConstants.ARTIST_SORT_BY, R.id.artistSortByArtist)
                    if (popupSort?.menu?.findItem(R.id.artistOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.name?.compareTo(b?.name!!)!! })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.name) && !TextUtils.isEmpty(b.name)) {
                                s = a.name?.compareTo(b?.name!!)!!
                            }
                            s
                        })
                    } else {
                        listPin.sortWith(Comparator { a, b -> b.name?.compareTo(a?.name!!)!! })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.name) && !TextUtils.isEmpty(b.name)) {
                                s = b.name?.compareTo(a?.name!!)!!
                            }
                            s
                        })
                    }
                    runLayoutAnimation()
                    artistAdapter.setDataArtist(listPin, listQuery)
                }

                R.id.artistSortByCountSong -> {
                    PreferenceUtils.put(AppConstants.ARTIST_SORT_BY, R.id.artistSortByCountSong)
                    if (popupSort?.menu?.findItem(R.id.artistOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                        listQuery.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                    } else {
                        listPin.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                        listQuery.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                    }
                    runLayoutAnimation()
                    artistAdapter.setDataArtist(listPin, listQuery)
                }

                R.id.artistSortAlbumCount -> {
                    PreferenceUtils.put(AppConstants.ARTIST_SORT_BY, R.id.artistSortAlbumCount)
                    if (popupSort?.menu?.findItem(R.id.artistOrderAsc)?.isChecked!!) {
                        listPin.sortWith(Comparator { a, b -> a.albumCount?.compareTo(b?.albumCount!!)!! })
                        listQuery.sortWith(Comparator { a, b -> a.albumCount?.compareTo(b?.albumCount!!)!! })
                    } else {
                        listPin.sortWith(Comparator { a, b -> b.albumCount?.compareTo(a?.albumCount!!)!! })
                        listQuery.sortWith(Comparator { a, b -> b.albumCount?.compareTo(a?.albumCount!!)!! })
                    }
                    runLayoutAnimation()
                    artistAdapter.setDataArtist(listPin, listQuery)
                }
                /**Oder by*/
                R.id.artistOrderAsc -> {
                    PreferenceUtils.put(AppConstants.ARTIST_ORDER_BY, R.id.artistOrderAsc)
                    when {
                        popupSort?.menu?.findItem(R.id.artistSortByArtist)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> a.name?.compareTo(b?.name!!)!! })
                            listQuery.sortWith(Comparator { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.name) && !TextUtils.isEmpty(b.name)) {
                                    s = a.name?.compareTo(b?.name!!)!!
                                }
                                s
                            })
                        }

                        popupSort?.menu?.findItem(R.id.artistSortByCountSong)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                            listQuery.sortWith(Comparator { a, b -> a.trackCount?.compareTo(b?.trackCount!!)!! })
                        }

                        popupSort?.menu?.findItem(R.id.artistSortAlbumCount)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> a.albumCount?.compareTo(b?.albumCount!!)!! })
                            listQuery.sortWith(Comparator { a, b -> a.albumCount?.compareTo(b?.albumCount!!)!! })
                        }
                    }
                    runLayoutAnimation()
                    artistAdapter.setDataArtist(listPin, listQuery)
                }

                R.id.artistOrderDesc -> {
                    PreferenceUtils.put(AppConstants.ARTIST_ORDER_BY, R.id.artistOrderDesc)
                    when {
                        popupSort?.menu?.findItem(R.id.artistSortByArtist)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> b.name?.compareTo(a?.name!!)!! })
                            listQuery.sortWith(Comparator { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.name) && !TextUtils.isEmpty(b.name)) {
                                    s = b.name?.compareTo(a?.name!!)!!
                                }
                                s
                            })
                        }

                        popupSort?.menu?.findItem(R.id.artistSortByCountSong)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                            listQuery.sortWith(Comparator { a, b -> b.trackCount?.compareTo(a?.trackCount!!)!! })
                        }

                        popupSort?.menu?.findItem(R.id.artistSortAlbumCount)?.isChecked!! -> {
                            listPin.sortWith(Comparator { a, b -> b.albumCount?.compareTo(a?.albumCount!!)!! })
                            listQuery.sortWith(Comparator { a, b -> b.albumCount?.compareTo(a?.albumCount!!)!! })
                        }
                    }
                    runLayoutAnimation()
                    artistAdapter.setDataArtist(listPin, listQuery)
                }
            }
            true
        }
        popupSort?.show()
    }

    private fun sortDefaultAlbum() {
        val sortBy =
            PreferenceUtils.getValueInt(AppConstants.ARTIST_SORT_BY, R.id.artistSortByArtist)
        Log.e("Sort By", sortBy.toString())
        when (PreferenceUtils.getValueInt(AppConstants.ARTIST_ORDER_BY, R.id.artistOrderAsc)) {
            R.id.artistOrderAsc -> {
                when (sortBy) {
                    R.id.artistSortByArtist -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.name) && !TextUtils.isEmpty(b.name)) {
                                s = a.name?.compareTo(b?.name!!)!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.name) && !TextUtils.isEmpty(b.name)) {
                                s = a.name?.compareTo(b?.name!!)!!
                            }
                            s
                        })
                    }

                    R.id.artistSortByCountSong -> {
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

                    R.id.artistSortAlbumCount -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumCount.toString()) && !TextUtils.isEmpty(b.albumCount.toString())) {
                                s = a.albumCount?.compareTo(b?.albumCount!!)!!
                            }
                            s
                        })
                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.albumCount.toString()) && !TextUtils.isEmpty(b.albumCount.toString())) {
                                s = a.albumCount?.compareTo(b?.albumCount!!)!!
                            }
                            s
                        })
                    }
                }
                runLayoutAnimation()
                artistAdapter.setDataArtist(listPin, listQuery)
            }

            R.id.artistOrderDesc -> {
                when (sortBy) {
                    R.id.artistSortByArtist -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(b.name.toString()) && !TextUtils.isEmpty(a.name.toString())) {
                                s = b.name?.compareTo(a?.name!!)!!
                            }
                            s
                        })

                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(b.name.toString()) && !TextUtils.isEmpty(a.name.toString())) {
                                s = b.name?.compareTo(a?.name!!)!!
                            }
                            s
                        })
                    }

                    R.id.artistSortByCountSong -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(b.trackCount.toString()) && !TextUtils.isEmpty(a.trackCount.toString())) {
                                s = b.trackCount?.compareTo(a?.trackCount!!)!!
                            }
                            s
                        })

                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(b.trackCount.toString()) && !TextUtils.isEmpty(a.trackCount.toString())) {
                                s = b.trackCount?.compareTo(a?.trackCount!!)!!
                            }
                            s
                        })
                    }

                    R.id.artistSortAlbumCount -> {
                        listPin.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(b.albumCount.toString()) && !TextUtils.isEmpty(a.albumCount.toString())) {
                                s = b.albumCount?.compareTo(a?.albumCount!!)!!
                            }
                            s
                        })

                        listQuery.sortWith(Comparator { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(b.albumCount.toString()) && !TextUtils.isEmpty(a.albumCount.toString())) {
                                s = b.trackCount?.compareTo(a?.albumCount!!)!!
                            }
                            s
                        })
                    }
                }
                runLayoutAnimation()
                artistAdapter.setDataArtist(listPin, listQuery)
            }
        }
    }

    private fun showDialogUnblockArtist() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_show_block_folder)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.attributes = wlp
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val btnUnBlockFolder = dialog.findViewById<Button>(R.id.btnUnBlock)
        val tvEmptyBlockFolder = dialog.findViewById<TextView>(R.id.tvEmptyBlock)
        val rvBlockFolder: RecyclerView = dialog.findViewById(R.id.rvBlock)

        tvTitle.text = getString(R.string.blocked_artist)
        btnUnBlockFolder.text = getString(R.string.unblock_artist)
        val adapterBlockFolder = AdapterUnblockArtist(requireContext()) { isEnable: Boolean ->
            btnUnBlockFolder.isEnabled = isEnable
            if (isEnable) {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_unlock_folder)
            } else {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_cancel)
            }
        }
        rvBlockFolder.setHasFixedSize(true)
        rvBlockFolder.layoutManager = LinearLayoutManager(requireContext())
        rvBlockFolder.adapter = adapterBlockFolder
        val lstBlock: ArrayList<ArtistItem> = appBlockDao.listBlockArtist
        if (lstBlock.isEmpty()) {
            tvEmptyBlockFolder.visibility = View.VISIBLE
        } else {
            adapterBlockFolder.setData(lstBlock)
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnUnBlockFolder.setOnClickListener {
            appBlockDao.deleteBlockArtist(adapterBlockFolder.listDelete)
            dialog.dismiss()
            loader()
//            BaseApplication.mInstance?.isRefreshData?.postValue(true)
            EventBus.getDefault().postSticky(EventRefreshData(true))
        }
        dialog.show()
    }

    private fun argument(id: Long): Array<String>? {
        return arrayOf(id.toString())
    }

    private fun runLayoutAnimation() {
        val controller =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_album)
         binding.rvArtist.layoutAnimation = controller
         binding.rvArtist.scheduleLayoutAnimation()
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
        if (item.pos == 1) mainShowMenuPopupArtist()
    }

    fun reloadData() {
        if (needLoaded) {
            needLoaded = false
            loader()
        }
    }

}
