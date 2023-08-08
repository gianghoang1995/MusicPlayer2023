package com.musicplayer.mp3player.playermusic.ui.activity.playing

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.adapter.PagerAdapter
import com.musicplayer.mp3player.playermusic.base.BaseActivity
import com.musicplayer.mp3player.playermusic.eventbus.*
import com.musicplayer.mp3player.playermusic.database.thumnail.AppDatabase
import com.musicplayer.mp3player.playermusic.equalizer.databinding.ActivityPlayerBinding
import com.musicplayer.mp3player.playermusic.callback.OnBinderServiceConnection
import com.musicplayer.mp3player.playermusic.ui.activity.main.MainActivity
import com.musicplayer.mp3player.playermusic.ui.dialog.moresong.DialogMoreSong
import com.musicplayer.mp3player.playermusic.ui.dialog.moresong.DialogMoreSongCallback
import com.musicplayer.mp3player.playermusic.ui.fragment.player.main.PlayerMusicFragment
import com.musicplayer.mp3player.playermusic.ui.lyrics.LyricsFragment
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.FileProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PlayerMusicActivity : BaseActivity<ActivityPlayerBinding>(), OnBinderServiceConnection {
    override fun onBindServiceMusicSuccess() {
    }


    override fun onServiceDisconnection() {
    }

    lateinit var thumbStoreDB: AppDatabase
    private val playerViewModel by lazy { ViewModelProvider(this)[PlayerViewModel::class.java] }
    lateinit var pagerAdapter: PagerAdapter
    var dialogMoreSongUtils: DialogMoreSong? = null
    var dialogMoreSong: Dialog? = null
    var openFromMain = false
    var mLastClickTime: Long = 0

    override fun onStart() {
        super.onStart()
        AppUtils.checkPermissionFux(this)
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityPlayerBinding {
        return ActivityPlayerBinding.inflate(inflater)
    }

    override fun ActivityPlayerBinding.initView() {
        initBannerAds(binding.frameBannerAds, true)
        setBindListener(this@PlayerMusicActivity)
        initThemeStyle(binding.include.imgTheme, binding.include.blackTspView)
        init()
        playerViewModel._isNewThumb.observe(this@PlayerMusicActivity) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (musicPlayerService != null) if (!musicPlayerService!!.isServiceRunning) {
            finish()
        }
    }

    override fun onDestroy() {
        if (dialogMoreSong != null) {
            if (dialogMoreSong?.isShowing!!) {
                dialogMoreSong?.dismiss()
            }
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 1) {
            binding.viewPager.currentItem = 0
        } else if (openFromMain) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun init() {
        thumbStoreDB = AppDatabase(this)
        openFromMain = intent.getBooleanExtra(AppConstants.OPEN_FROM_NOTIFICATION, false)
        pagerAdapter = PagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(PlayerMusicFragment(), getString(R.string.now_playing))
        pagerAdapter.addFragment(LyricsFragment(), getString(R.string.lyrics))
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = pagerAdapter.count - 1

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.btnPlaying.setTextColor(Color.WHITE)
                        binding.btnLyric.setTextColor(Color.parseColor("#C3C3C3"))
                    }

                    1 -> {
                        binding.btnLyric.setTextColor(Color.WHITE)
                        binding.btnPlaying.setTextColor(Color.parseColor("#C3C3C3"))
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        onClick()
    }

//
//
//    @SuppressLint("UseCompatLoadingForDrawables")
//    private fun showLyricAnim() {
//        if (nestedLyric != null) {
//            nestedLyric.scrollTo(0, 0)
//            initLyricInfo()
//            constraintSet.apply {
//                clone(rootLyrics)
//                constrainPercentHeight(R.id.top, 0.15f)
//                val transition = TransitionSet().apply {
//                    addTransition(ChangeBounds())
//                    addTransition(Fade(Fade.MODE_IN))
//                    interpolator = AccelerateDecelerateInterpolator()
//                }
//                setVisibility(R.id.top, View.VISIBLE)
//                TransitionManager.beginDelayedTransition(rootLyrics, transition)
//                applyTo(rootLyrics)
//                val transition2 = TransitionSet().apply {
//                    addTransition(ChangeBounds())
//                    addTransition(Fade(Fade.MODE_IN))
//                    addTransition(ScaleTransition())
//                    interpolator = AccelerateDecelerateInterpolator()
//                }
//                clone(rootLyrics)
//                constrainPercentHeight(R.id.bottom, 0.85f)
//                constrainPercentWidth(R.id.bottom, 1f)
//                setVisibility(R.id.bottom, View.VISIBLE)
//                setVisibility(R.id.rootLyrics, View.VISIBLE)
//                TransitionManager.beginDelayedTransition(rootLyrics, transition2)
//                applyTo(rootLyrics)
//            }
//        }
//    }
//
//    private fun hideLyricAnim() {
//        if (top != null) {
//            rootLyrics.background = null
//            constraintSet.apply {
//                clone(rootLyrics)
//                constrainPercentHeight(R.id.top, 0f)
//                val transition = TransitionSet().apply {
//                    addTransition(ChangeBounds())
//                    //                    addTransition(Fade(Fade.OUT))
//                    //                    interpolator = AccelerateDecelerateInterpolator()
//                }
//                setVisibility(R.id.top, View.INVISIBLE)
//                TransitionManager.beginDelayedTransition(rootLyrics, transition)
//                applyTo(rootLyrics)
//                val transition2 = TransitionSet().apply {
//                    addTransition(ChangeBounds())
//                    //                    addTransition(Fade(Fade.OUT))
//                    //                    interpolator = AccelerateDecelerateInterpolator()
//                }
//                clone(rootLyrics)
//                constrainPercentHeight(R.id.bottom, 0f)
//                constrainPercentWidth(R.id.bottom, 0.7f)
//                setVisibility(R.id.bottom, View.INVISIBLE)
//                setVisibility(R.id.rootLyrics, View.INVISIBLE)
//                TransitionManager.beginDelayedTransition(rootLyrics, transition2)
//                applyTo(rootLyrics)
//            }
//        }
//    }

    fun onClick() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnMore.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {

            } else {
                dialogMoreSongUtils = DialogMoreSong(
                    this,
                    object : DialogMoreSongCallback {
                        override fun onNextTrack() {
                            musicPlayerService?.getCurrentSong()?.let { it1 ->
                                musicPlayerService?.insertNextTrack(
                                    it1
                                )
                            }
                            dialogMoreSong?.dismiss()
                        }

                        override fun onAddToPlaylist() {
                            dialogMoreSong?.dismiss()
                        }

                        override fun onSetRingtone() {
                            setRingtoneRingDroid(musicPlayerService?.getCurrentSong())
                            dialogMoreSong?.dismiss()
                        }

                        override fun onDetail() {
                            dialogMoreSong?.dismiss()
                        }

                        override fun onShare() {
                            musicPlayerService?.getCurrentSong()!!.songPath?.let { it1 ->
                                var fileProvider = FileProvider()
                                fileProvider.share(baseContext, it1)
                            }
                            dialogMoreSong?.dismiss()
                        }

                        override fun onDelete() {

                        }

                        override fun onDeleteSongPlaylist() {

                        }

                    },
                    showDeletePlaylist = false,
                    showGoTo = true,
                    curentSong = musicPlayerService?.getCurrentSong(),
                    this
                )

                dialogMoreSong = musicPlayerService?.getCurrentSong()?.let { it1 ->
                    dialogMoreSongUtils?.getDialog(
                        it1
                    )
                }
                dialogMoreSong?.show()
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        binding.btnPlaying.setOnClickListener {
            binding.viewPager.currentItem = 0
        }

        binding.btnLyric.setOnClickListener {
            binding.viewPager.currentItem = 1
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCloseDialogSong(closeDialog: BusCloseDialog) {
        if (dialogMoreSong != null) {
            if (dialogMoreSong?.isShowing!!) {
                dialogMoreSong?.dismiss()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun stopServiceMusic(event: BusStopService) {
        if (event.isStop) {
            if (!isFinishing) finish()
            EventBus.getDefault().postSticky(BusStopService(false))
        }
    }

    private fun switchThumbActivity() {
        binding.viewPager.setCurrentItem(0, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun changePage(event: BusChangePager) {
        switchThumbActivity()
    }

}
