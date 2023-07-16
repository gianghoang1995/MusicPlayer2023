package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.DragSortRecycler
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterNowPlaying
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickSongListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityNowPlayingBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventStopService
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NowPlayingActivity : BaseActivity<ActivityNowPlayingBinding>(), OnClickSongListener,
    OnBinderServiceConnection {
    lateinit var nowPlayingAdapter: AdapterNowPlaying
    var listSong: ArrayList<MusicItem>? = ArrayList()

    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityNowPlayingBinding {
        return ActivityNowPlayingBinding.inflate(inflater)
    }

    override fun ActivityNowPlayingBinding.initView() {
        init()
    }


    fun init() {
        setBindListener(this)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        initDataNowPlaying()
        initDragRecycleView()
        onClick()
    }

    fun onClick() {
        binding.tvNowPlaying.setOnClickListener {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullNowPlaying?.showAds(this,
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    super.onBackPressed()
                }, onAdLoadFail = {
                    MaxIntertitial.ShowIntertitialApplovinMax(
                        this, getString(R.string.appvolin_full)
                    ) {
                        dialogLoadingAds?.dismissDialog()
                        super.onBackPressed()
                    }
                })
        }
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?.getListData?.observe(this) {
            if (it != null) {
                nowPlayingAdapter.setData(it)
                binding.nowPlayingView.isVisible = true
                binding.loadingView.root.isVisible = false
            } else {
                binding.nowPlayingView.isVisible = false
                binding.loadingView.root.isVisible = true
            }
        }

        musicPlayerService?.obverseMusicUtils?._isServiceRunning?.observe(this) {
            binding.guideOnline.isVisible = musicPlayerService?.isPlayingOnline == true
        }

        musicPlayerService?.obverseMusicUtils?.getCurrentItemAudio?.observe(this) {
            nowPlayingAdapter.setIndexSongPlayer(it, musicPlayerService?.songPos ?: 0)
        }
    }

    private fun initDataNowPlaying() {
        nowPlayingAdapter = AdapterNowPlaying(this, this, songIndexListener = {
            binding.rvNowPlaying.scrollToPosition(it)
        })
        binding.rvNowPlaying.setHasFixedSize(true)
        binding.rvNowPlaying.layoutManager = LinearLayoutManager(this)
        binding.rvNowPlaying.adapter = nowPlayingAdapter
    }

    private fun initDragRecycleView() {
        val dragSortRecycler = DragSortRecycler()
        dragSortRecycler.setViewHandleId(R.id.btnDrag)
        dragSortRecycler.setOnItemMovedListener { from, to ->
            musicPlayerService?.moveSongItem(
                from, to
            )
            nowPlayingAdapter.moveItem(from, to)
        }
        binding.rvNowPlaying.addItemDecoration(dragSortRecycler)
        binding.rvNowPlaying.addOnItemTouchListener(dragSortRecycler)
        binding.rvNowPlaying.setOnScrollListener(dragSortRecycler.scrollListener)
        dragSortRecycler.setOnDragStateChangedListener(object :
            DragSortRecycler.OnDragStateChangedListener {
            override fun onDragStart() {
            }

            override fun onDragStop() {
            }
        })
    }

    override fun onSongClick(song: MusicItem, i: Int) {
        musicPlayerService?.songPos = i
        musicPlayerService?.setDataSource()
        nowPlayingAdapter.setIndexSongPlayer(song, i)
    }

    override fun onSongMoreClick(item: Any, i: Int, view: View) {
        musicPlayerService?.deleteSongNowPlaying(item, i)
        nowPlayingAdapter.removeItem(item)
    }

    override fun onSongOnLongClick() {

    }

    override fun onSizeSelectChange(size: Int) {

    }

    override fun onClickItemOnline(itemAudioOnline: ItemMusicOnline) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.putExtra(AppConstants.ACTION_SET_DATA_ONLINE, itemAudioOnline)
        intent.action = AppConstants.ACTION_SET_DATA_ONLINE
        startService(intent)

        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun stopServiceMusic(event: EventStopService) {
        if (event.isStop) {
            if (!isFinishing)
                finish()
        }
    }

}