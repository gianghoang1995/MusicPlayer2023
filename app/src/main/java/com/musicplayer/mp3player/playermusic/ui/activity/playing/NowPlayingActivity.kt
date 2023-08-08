package com.musicplayer.mp3player.playermusic.ui.activity.playing

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.adapter.DragSortRecycler
import com.musicplayer.mp3player.playermusic.adapter.AdapterNowPlayingRV
import com.musicplayer.mp3player.playermusic.callback.SongListenerCallBack
import com.musicplayer.mp3player.playermusic.base.BaseActivity
import com.musicplayer.mp3player.playermusic.BaseApplication
import com.musicplayer.mp3player.playermusic.equalizer.databinding.ActivityNowPlayingBinding
import com.musicplayer.mp3player.playermusic.eventbus.BusStopService
import com.musicplayer.mp3player.playermusic.callback.OnBinderServiceConnection
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.model.ItemMusicOnline
import com.musicplayer.mp3player.playermusic.service.MusicPlayerService
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NowPlayingActivity : BaseActivity<ActivityNowPlayingBinding>(), SongListenerCallBack,
    OnBinderServiceConnection {
    lateinit var nowPlayingAdapter: AdapterNowPlayingRV
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
            BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    super.onBackPressed()
                }, onAdLoadFail = {
                    dialogLoadingAds?.dismissDialog()
                    super.onBackPressed()
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

        musicPlayerService?.obverseMusicUtils?.getCurrentItemAudio?.observe(this) {
            nowPlayingAdapter.setIndexSongPlayer(it, musicPlayerService?.songPos ?: 0)
        }
    }

    private fun initDataNowPlaying() {
        nowPlayingAdapter = AdapterNowPlayingRV(this, this, songIndexListener = {
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
    fun stopServiceMusic(event: BusStopService) {
        if (event.isStop) {
            if (!isFinishing)
                finish()
        }
    }

}