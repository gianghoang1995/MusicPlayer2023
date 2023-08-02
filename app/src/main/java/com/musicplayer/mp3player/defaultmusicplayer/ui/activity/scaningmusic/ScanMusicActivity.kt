package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.scaningmusic

import android.annotation.SuppressLint
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseActivity
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.ActivityScanMusicBinding
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventRefreshData
import com.musicplayer.mp3player.defaultmusicplayer.handle.SongLoaderListener
import com.musicplayer.mp3player.defaultmusicplayer.loader.SongLoaderAsyncTask
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import com.musicplayer.mp3player.defaultmusicplayer.utils.PreferenceUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.SortOrder
import org.greenrobot.eventbus.EventBus
import java.util.*

class ScanMusicActivity : BaseActivity<ActivityScanMusicBinding>() {
    var isSuccess = false
    override fun bindingProvider(inflater: LayoutInflater): ActivityScanMusicBinding {
        return ActivityScanMusicBinding.inflate(inflater)
    }

    override fun ActivityScanMusicBinding.initView() {
        init()
    }

    override fun onResume() {
        super.onResume()
        initBannerAds(binding.frameBannerAds, false)
    }

    fun init() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        when (PreferenceUtils.getValueInt(AppConstants.SKIP_DURATION, 0)) {
            0 -> {
                binding.groupCheckbox.check(R.id.rd0s)
            }

            30 -> {
                binding.groupCheckbox.check(R.id.rd30s)
            }

            60 -> {
                binding.groupCheckbox.check(R.id.rd60s)
            }

            90 -> {
                binding.groupCheckbox.check(R.id.rd90s)
            }
        }

        binding.btnStartScan.setOnClickListener {
            if (!isSuccess) {
                binding.btnStartScan.text = getString(R.string.scanning)
                binding.viewScanning.visibility = View.VISIBLE
                binding.viewSuccess.visibility = View.GONE
                binding.scanning.playAnimation()
                when (binding.groupCheckbox.checkedRadioButtonId) {
                    R.id.rd0s -> {
                        PreferenceUtils.put(AppConstants.SKIP_DURATION, 0)
                    }

                    R.id.rd30s -> {
                        PreferenceUtils.put(AppConstants.SKIP_DURATION, 30)
                    }

                    R.id.rd60s -> {
                        PreferenceUtils.put(AppConstants.SKIP_DURATION, 60)
                    }

                    R.id.rd90s -> {
                        PreferenceUtils.put(AppConstants.SKIP_DURATION, 90)
                    }
                }
                loader()
            } else {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        finish()
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        finish()
                    })
            }
        }
    }

    fun loader() {
        val songLoader = SongLoaderAsyncTask(this, object : SongLoaderListener {
            @SuppressLint("SetTextI18n")
            override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
                val handler = Handler()
                handler.postDelayed({
                    isSuccess = true
                    binding.scanning.cancelAnimation()
                    binding.scanning.frame = 0
                    binding.tvScanCount.text =
                        songList.size.toString() + " " + getString(R.string.scanned_songs)
                    binding.viewScanning.visibility = View.GONE
                    binding.viewSuccess.visibility = View.VISIBLE
                    binding.successAnim.playAnimation()
                    binding.btnStartScan.text = getString(R.string.txt_done)
                }, 1500)
//                BaseApplication.mInstance?.isRefreshData?.postValue(true)
                EventBus.getDefault().postSticky(EventRefreshData(true))
            }
        })
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        songLoader.filterSkipDuration()
        songLoader.loadInBackground()
    }
}