package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.song

import android.app.Dialog
import android.content.Intent
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.nativead.NativeAd
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.adapter.SongAdapter
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickSongListener
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseFragment
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.FragmentSongBinding
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.*
import com.musicplayer.mp3player.defaultmusicplayer.handle.SongLoaderListener
import com.musicplayer.mp3player.defaultmusicplayer.loader.SongLoaderAsyncTask
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.addsong.AddSongFavoriteActivity
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.equalizer.EqualizerActivity
import com.musicplayer.mp3player.defaultmusicplayer.ui.dialog.moresong.DialogMoreSong
import com.musicplayer.mp3player.defaultmusicplayer.ui.dialog.moresong.DialogMoreSongCallback
import com.musicplayer.mp3player.defaultmusicplayer.utils.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class SongFragment : BaseFragment<FragmentSongBinding>(), SongLoaderListener,
    OnClickSongListener {
    private var popupSort: PopupMenu? = null
    var needLoaded = false
    var mLastClickTime: Long = 0
    lateinit var songLoader: SongLoaderAsyncTask
    var songAdapter: SongAdapter? = null
    var listSong: ArrayList<MusicItem> = ArrayList()
    var dialogMoreSongUtils: DialogMoreSong? = null
    var dialogMoreSong: Dialog? = null
    var dialogCheckBookEvent: Dialog? = null
    lateinit var thread: Thread
    var nativeAd: NativeAd? = null


    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSongBinding {
        return FragmentSongBinding.inflate(inflater)
    }

    override fun FragmentSongBinding.initView() {
        init()
        loader()
    }

    override fun onDetach() {
        if (dialogCheckBookEvent != null) {
            if (dialogCheckBookEvent?.isShowing!!) {
                dialogCheckBookEvent?.dismiss()
            }
        }
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
        if (dialogMoreSong != null) {
            if (dialogMoreSong?.isShowing!!) {
                dialogMoreSong?.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialogMoreSong != null) {
            if (dialogMoreSong?.isShowing!!) {
                dialogMoreSong?.dismiss()
            }
        }
    }

    fun loader() {
        needLoaded = false
        songLoader = SongLoaderAsyncTask(requireContext(), this)
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        thread {
            songLoader.loadInBackground()
        }.run()
    }

    fun init() {
        songAdapter = SongAdapter(requireContext(), this, songIndexListener = {
//            mBinding.rvSong.scrollToPosition(it)
        })
        binding.rvSong.setHasFixedSize(true)
        binding.rvSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvSong.adapter = songAdapter

        binding.btnShuffle.setOnClickListener {
            shuffleSong()
        }

        binding.btnSort.setOnClickListener {
            mainShowMenuPopupSong(true)
        }

        binding.btnOpenSelectSong.setOnClickListener {
            startActivity(
                Intent(requireActivity(), AddSongFavoriteActivity::class.java)
                    .putExtra(AddSongFavoriteActivity.NONE_DATA, true)
            )
        }

        binding.swipeToRefreshSong.setOnRefreshListener {
            loader()
        }
    }

    override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
        activity?.runOnUiThread {
            binding.swipeToRefreshSong.isRefreshing = false
            listSong.clear()
            listSong.addAll(songList)
            sortDefaultSong()
            if (songList.size > 0) {
                binding.emptyView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onSongClick(song: MusicItem, i: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    val list = ArrayList<MusicItem>()
                    list.addAll(listSong)
                    requireContext().let { musicPlayerService?.setListSong(it, list, i) }
                }, onAdLoadFail = {
                    dialogLoadingAds?.dismissDialog()
                    val list = ArrayList<MusicItem>()
                    list.addAll(listSong)
                    requireContext().let { musicPlayerService?.setListSong(it, list, i) }
                })
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onSongMoreClick(item: Any, i: Int, view: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            if (item is MusicItem) {
                dialogMoreSongUtils = DialogMoreSong(
                    requireContext(),
                    object : DialogMoreSongCallback {
                        override fun onNextTrack() {
                            musicPlayerService?.insertNextTrack(item)
                            dialogMoreSong?.dismiss()
                        }

                        override fun onAddToPlaylist() {
                            dialogMoreSong?.dismiss()
                        }

                        override fun onSetRingtone() {
                            dialogMoreSong?.dismiss()
                            setRingtone(item)
                        }

                        override fun onDetail() {
                            dialogMoreSong?.dismiss()
                        }

                        override fun onShare() {
                            item.songPath?.let { it1 ->
                                val fileProvider = FileProvider()
                                fileProvider.share(requireContext(), it1)
                            }
                            dialogMoreSong?.dismiss()
                        }

                        override fun onDelete() {
                            //                    songAdapter.removeItem(song)
                        }

                        override fun onDeleteSongPlaylist() {

                        }


                    },
                    false,
                    showGoTo = true,
                    curentSong = musicPlayerService?.getCurrentSong(),
                    this
                )
                dialogMoreSong = dialogMoreSongUtils?.getDialog(item)
                dialogMoreSong?.show()
            }
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    fun updateIndexSong(pos: Int?, song: MusicItem?) {
        if (binding.rvSong != null) {
            if (pos != null) {
                songAdapter?.setIndexSongPlayer(song, pos)
            }
        }
    }

    fun clearIndexSong() {
        if (activity?.isFinishing == false) {
            if (binding.rvSong != null)
                songAdapter?.clearIndexSong()
        }
    }

    override fun onSongOnLongClick() {

    }

    override fun onSizeSelectChange(size: Int) {

    }

    override fun onClickItemOnline(itemAudioOnline: ItemMusicOnline) {

    }

    private fun runLayoutAnimation() {
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_album)
        binding.rvSong.layoutAnimation = controller
        binding.rvSong.scheduleLayoutAnimation()
    }

    private fun mainShowMenuPopupSong(isSort: Boolean) {
        popupSort = PopupMenu(requireContext(), binding.btnSort)
        popupSort?.menuInflater?.inflate(R.menu.popup_sort_song, popupSort?.menu)
        val sortBy = PreferenceUtils.getValueInt(AppConstants.SONG_SORT_BY, R.id.sortByName)
        val orderBy = PreferenceUtils.getValueInt(AppConstants.SONG_ORDER_BY, R.id.orderAsc)

        popupSort?.menu?.findItem(sortBy)?.isChecked = true
        popupSort?.menu?.findItem(orderBy)?.isChecked = true

        popupSort?.setOnMenuItemClickListener { item: MenuItem ->
            item.isChecked = true
            when (item.itemId) {
                R.id.menuShuffle -> {
                    shuffleSong()
                }

                R.id.menuEqualizer -> {
                    dialogLoadingAds?.showDialogLoading()
                    BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                        onLoadAdSuccess = {
                            dialogLoadingAds?.dismissDialog()
                        }, onAdClose = {
                            startActivity(
                                Intent(
                                    requireContext(), EqualizerActivity::class.java
                                )
                            )
                        }, onAdLoadFail = {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(
                                Intent(
                                    requireContext(), EqualizerActivity::class.java
                                )
                            )
                        })
                }

                R.id.sortByName -> {
                    PreferenceUtils.put(AppConstants.SONG_SORT_BY, R.id.sortByName)
                    if (popupSort?.menu?.findItem(R.id.orderAsc)?.isChecked!!) {
                        listSong.sortWith { a, b -> a.title?.compareTo(b?.title!!)!! }
                    } else {
                        listSong.sortWith { a, b -> b.title?.compareTo(a?.title!!)!! }
                    }
                    runLayoutAnimation()
                    setListSong()
                }

                R.id.sortByAlbum -> {
                    PreferenceUtils.put(AppConstants.SONG_SORT_BY, R.id.sortByAlbum)
                    if (popupSort?.menu?.findItem(R.id.orderAsc)?.isChecked!!) {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                s = a.album?.compareTo(b?.album!!)!!
                            }
                            s
                        }
                    } else {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                s = b.album?.compareTo(a?.album!!)!!
                            }
                            s
                        }
                    }
                    runLayoutAnimation()
                    setListSong()
                }

                R.id.sortByArtist -> {
                    PreferenceUtils.put(AppConstants.SONG_SORT_BY, R.id.sortByArtist)
                    if (popupSort?.menu?.findItem(R.id.orderAsc)?.isChecked!!) {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                s = a.artist?.compareTo(b?.artist!!)!!
                            }
                            s
                        }
                    } else {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                s = b.artist?.compareTo(a?.artist!!)!!
                            }
                            s
                        }
                    }
                    runLayoutAnimation()
                    setListSong()
                }

                R.id.sortByDuration -> {
                    PreferenceUtils.put(AppConstants.SONG_SORT_BY, R.id.sortByDuration)
                    if (popupSort?.menu?.findItem(R.id.orderAsc)?.isChecked!!) {
                        listSong.sortWith { a, b ->
                            a.duration?.toInt()?.compareTo(b?.duration?.toInt()!!)!!
                        }
                    } else {
                        listSong.sortWith { a, b ->
                            b.duration?.toInt()?.compareTo(a?.duration?.toInt()!!)!!
                        }
                    }
                    runLayoutAnimation()
                    setListSong()
                }
                /**Oder by*/
                R.id.orderAsc -> {
                    PreferenceUtils.put(AppConstants.SONG_ORDER_BY, R.id.orderAsc)
                    when {
                        popupSort?.menu?.findItem(R.id.sortByName)?.isChecked!! -> {
                            listSong.sortWith { a, b -> a.title?.compareTo(b?.title!!)!! }
                        }

                        popupSort?.menu?.findItem(R.id.sortByAlbum)?.isChecked!! -> {
                            listSong.sortWith { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                    s = a.album?.compareTo(b?.album!!)!!
                                }
                                s
                            }
                        }

                        popupSort?.menu?.findItem(R.id.sortByArtist)?.isChecked!! -> {
                            listSong.sortWith { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                    s = a.artist?.compareTo(b?.artist!!)!!
                                }
                                s
                            }
                        }

                        popupSort?.menu?.findItem(R.id.sortByDuration)?.isChecked!! -> {
                            listSong.sortWith { a, b ->
                                a.duration?.toInt()?.compareTo(b?.duration?.toInt()!!)!!
                            }
                        }
                    }
                    runLayoutAnimation()
                    setListSong()
                }

                R.id.orderDesc -> {
                    PreferenceUtils.put(AppConstants.SONG_ORDER_BY, R.id.orderDesc)
                    when {
                        popupSort?.menu?.findItem(R.id.sortByName)?.isChecked!! -> {
                            listSong.sortWith { a, b -> b.title?.compareTo(a?.title!!)!! }
                        }

                        popupSort?.menu?.findItem(R.id.sortByAlbum)?.isChecked!! -> {
                            listSong.sortWith { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                    s = b.album?.compareTo(a?.album!!)!!
                                }
                                s
                            }
                        }

                        popupSort?.menu?.findItem(R.id.sortByArtist)?.isChecked!! -> {
                            listSong.sortWith { a, b ->
                                var s = 1
                                if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                    s = b.artist?.compareTo(a?.artist!!)!!
                                }
                                s
                            }
                        }

                        popupSort?.menu?.findItem(R.id.sortByDuration)?.isChecked!! -> {
                            listSong.sortWith { a, b ->
                                b.duration?.toInt()?.compareTo(a?.duration?.toInt()!!)!!
                            }
                        }
                    }
                    runLayoutAnimation()
                    setListSong()
                }
            }
            true
        }
        popupSort?.show()
    }

    private fun shuffleSong() {
        if (listSong.isNotEmpty()) {
            musicPlayerService?.shuffleListSong(listSong, requireContext())
        } else {
            showMessage(getString(R.string.no_song_to_play))
        }
    }

    private fun sortDefaultSong() {
        val sortBy = PreferenceUtils.getValueInt(AppConstants.SONG_SORT_BY, R.id.sortByName)
        when (PreferenceUtils.getValueInt(AppConstants.SONG_ORDER_BY, R.id.orderAsc)) {
            R.id.orderAsc -> {
                when (sortBy) {
                    R.id.sortByName -> {
                        listSong.sortWith { a, b -> a.title?.compareTo(b?.title!!)!! }
                    }

                    R.id.sortByAlbum -> {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                s = a.album?.compareTo(b?.album!!)!!
                            }
                            s
                        }
                    }

                    R.id.sortByArtist -> {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                s = a.artist?.compareTo(b?.artist!!)!!
                            }
                            s
                        }
                    }

                    R.id.sortByDuration -> {
                        listSong.sortWith { a, b ->
                            a.duration?.toInt()?.compareTo(b?.duration?.toInt()!!)!!
                        }
                    }
                }
            }

            R.id.orderDesc -> {
                when (sortBy) {
                    R.id.sortByName -> {
                        listSong.sortWith { a, b -> b.title?.compareTo(a?.title!!)!! }
                    }

                    R.id.sortByAlbum -> {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.album) && !TextUtils.isEmpty(b.album)) {
                                s = b.album?.compareTo(a?.album!!)!!
                            }
                            s
                        }
                    }

                    R.id.sortByArtist -> {
                        listSong.sortWith { a, b ->
                            var s = 1
                            if (!TextUtils.isEmpty(a.artist) && !TextUtils.isEmpty(b.artist)) {
                                s = b.artist?.compareTo(a?.artist!!)!!
                            }
                            s
                        }
                    }

                    R.id.sortByDuration -> {
                        listSong.sortWith { a, b ->
                            b.duration?.toInt()?.compareTo(a?.duration?.toInt()!!)!!
                        }
                    }
                }
            }
        }
        runLayoutAnimation()
        setListSong()
    }

    fun setListSong() {
        songAdapter?.setDataSong(listSong)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: EventDeleteSong) {
        if (binding.rvSong != null) {
            requireActivity().runOnUiThread {
                listSong.remove(item.song)
                songAdapter?.removeItem(item.song)
            }
        } else {
            needLoaded = true
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteChange(item: EventRefreshDataWhenDelete) {
        if (binding.rvSong != null) loader()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: EventReloadUnblockData) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: EventRefreshData) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun onShowMoreEvent(item: EventShowMoreEvent) {
        if (item.pos == 2) mainShowMenuPopupSong(false)
    }

    fun reloadData() {
        if (needLoaded) {
            needLoaded = false
            loader()
        }
    }

    class SongNameComparator : Comparator<MusicItem?> {
        override fun compare(left: MusicItem?, right: MusicItem?): Int {
            return right?.title?.let { left?.title?.compareTo(it) }!!
        }
    }
}
