//package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.lookscreen
//
//import android.animation.Animator
//import android.animation.ValueAnimator
//import android.annotation.SuppressLint
//import android.app.Dialog
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.Window
//import android.view.WindowManager
//import android.widget.RadioGroup
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.widget.PopupMenu
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
//import com.google.android.exoplayer2.Player
//import com.downloadmp3player.musicdownloader.freemusicdownloader.R
//import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
//import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
//import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventPutLoveSong
//import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventStopService
//import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
//import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
//import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
//import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityLookscreenBinding
//import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
//import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
//import com.downloadmp3player.musicdownloader.freemusicdownloader.widget.CustomDigitalClock
//import me.tankery.lib.circularseekbar.CircularSeekBar
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//import org.schabi.newpipe.extractor.timeago.patterns.it
//import java.text.SimpleDateFormat
//import java.util.*
//
//class CarPlayerActivity : BaseActivity<ActivityLookscreenBinding>(), OnBinderServiceConnection {
//    private var mTranX: Float = 0f
//    private var mSlideTouchX: Float = 0f
//    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null
//    var songListDao: PlaylistSongDaoDB? = null
//
//    override fun bindingProvider(inflater: LayoutInflater): ActivityLookscreenBinding {
//        return ActivityLookscreenBinding.inflate(inflater)
//    }
//
//    override fun ActivityLookscreenBinding.initView() {
//        val mWindow = window
//        mWindow.decorView.systemUiVisibility =
//
//            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        window.addFlags(
//            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
//                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//        )
//        setBindListener(this@CarPlayerActivity)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//        }
//        init()
//        setupSlideToUnlock()
//    }
//
//    override fun onResume() {
//        if (musicPlayerService != null)
//            if (!musicPlayerService!!.isServiceRunning) {
//                finish()
//            }
//        super.onResume()
//    }
//
//    fun init() {
//        songListSqliteHelper =
//            PlaylistSongSqLiteHelperDB(
//                this,
//                FavoriteSqliteHelperDB.DEFAULT_FAVORITE
//            )
//        songListDao =
//            PlaylistSongDaoDB(
//                songListSqliteHelper
//            )
//
//        binding.seekBarLock.setOnSeekBarChangeListener(object :
//            CircularSeekBar.OnCircularSeekBarChangeListener {
//            override fun onProgressChanged(
//                circularSeekBar: CircularSeekBar?,
//                progress: Float,
//                fromUser: Boolean
//            ) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
//                musicPlayerService?.seek(seekBar?.progress?.toInt() ?: 0)
//            }
//
//            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
//
//            }
//
//        })
//
//        when (PreferenceUtils.getLoopMode()) {
//            Player.REPEAT_MODE_ALL -> {
//                binding.btnShuffle.setImageResource(R.drawable.ic_loop_all)
//            }
//
//            Player.REPEAT_MODE_ONE -> {
//                binding.btnShuffle.setImageResource(R.drawable.ic_loop_one)
//            }
//
//            AppConstants.LOOP.LOOP_SHUFFLE -> {
//                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
//            }
//        }
//
//        setTimeFormat()
//        onClick()
//    }
//
//    @SuppressLint("SetTextI18n")
//    fun initDataActivityLockScreen() {
//        binding.btnFavorite.cancelAnimation()
//        val currentDuration = AppUtils.convertDuration(musicPlayerService?.exoPlayer?.currentPosition ?: 0)
//        val duration = AppUtils.convertDuration(exoPlayer?.duration ?: 0)
//        binding.tvDuration.text = "$currentDuration : $duration"
//
//        binding.tvTitleSong?.text = getCurrent()?.title
//        if (isPlaying()) {
//            binding.btnPlayPause?.setImageResource(R.drawable.ic_pause_lockscreen)
//        } else {
//            binding.btnPlayPause?.setImageResource(R.drawable.ic_play_lockscreen)
//        }
//        if (binding.imgThumbLock != null) {
//            val thumbStore = thumbStoreDB.thumbDao().findThumbnailByID(getCurrent()?.id)
//            if (thumbStore != null) {
//                Glide.with(this)
//                    .load(thumbStore.thumbPath)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .placeholder(R.drawable.ic_song_transparent).into(binding.imgThumbLock)
//            } else {
//                val uri = getCurrent()?.id?.let { ArtworkUtils.getArtworkFromSongID(it) }
//                if (!Uri.EMPTY.equals(uri)) {
//                    Glide.with(this).load(uri)
//                        .placeholder(R.drawable.ic_song_transparent)
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(binding.imgThumbLock)
//                } else {
//                    Glide.with(this)
//                        .load(getCurrent()?.albumId?.let { it1 -> ArtworkUtils.uri(it1) })
//                        .placeholder(R.drawable.ic_song_transparent).transition(
//                            DrawableTransitionOptions.withCrossFade()
//                        ).into(binding.imgThumbLock)
//                }
//            }
//        }
//        binding.progressLockScreen.run()
//        binding.seekBarLockScreen?.postDelayed(progressLockScreen, mInterval)
//        changeStatusPlayer()
//        setBackgroundLockScreen()
//        if (checkFavorite()) {
//            btnFavoriteLoockScreen?.progress = 1F
//        } else {
//            btnFavoriteLoockScreen?.progress = 0f
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupSlideToUnlock() {
//        binding.rootLock.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    mSlideTouchX = event.rawX
//                    mTranX = 0f
//                }
//
//                MotionEvent.ACTION_MOVE -> performSlide(event.rawX)
//                MotionEvent.ACTION_UP -> checkEndTouch()
//            }
//            true
//        }
//    }
//
//    private fun performSlide(touchX: Float) {
//        val delta: Float = touchX - mSlideTouchX
//        mTranX += delta
//        performTranX(mTranX)
//        mSlideTouchX = touchX
//    }
//
//    private fun checkEndTouch() {
//        if (mTranX > Pixels.getScreenWidth(this) / 2f) {
//            translateTo(Pixels.getScreenWidth(this).toFloat(), true)
//        } else {
//            translateTo(0f, false)
//        }
//    }
//
//    private fun performTranX(tranX: Float) {
//        if (tranX < 0) {
//            binding.rootLock.translationX = 0f
//        } else {
//            binding.rootLock.translationX = tranX
//        }
//    }
//
//    private fun translateTo(endTranX: Float, isFinish: Boolean) {
//        animValue(mTranX, endTranX, 300L, isFinish)
//    }
//
//    private fun animValue(start: Float, end: Float, duration: Long, isFinish: Boolean) {
//        val delta = end - start
//        val animator: ValueAnimator = ValueAnimator.ofFloat(start, end)
//        animator.addUpdateListener { animation ->
//            val per: Float = animation.animatedFraction
//            val test = start + delta * per
//            mTranX = test
//            performTranX(test)
//        }
//        animator.duration = duration
//        animator.start()
//        animator.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationStart(animation: Animator) {}
//            override fun onAnimationEnd(animation: Animator) {
//                if (isFinish) {
//                    finish()
//                }
//            }
//
//            override fun onAnimationCancel(animation: Animator) {}
//            override fun onAnimationRepeat(animation: Animator) {}
//        })
//    }
//
//    private fun setTimeFormat() {
//        if (PreferenceUtils.getValueInt(AppConstants.FORMAT_TIME_LOCKSCREEN) == 0) {
//            binding.digitalClock.setTimeFormat(CustomDigitalClock.m12)
//        } else {
//            binding.digitalClock.setTimeFormat(CustomDigitalClock.m24)
//        }
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    fun onClick() {
//        binding.btnNext.setOnClickListener {
//            startService(AppConstants.ACTION_NEXT)
//        }
//
//        binding.btnPrive.setOnClickListener {
//            startService(AppConstants.ACTION_PRIVE)
//        }
//
//        binding.btnPlayPause.setOnClickListener {
//            startService(AppConstants.ACTION_TOGGLE_PLAY)
//        }
//
//        binding.btnMoreLockScreen.setOnClickListener {
//            showPopupMenu()
//        }
//
//        binding.btnShuffle.setOnClickListener {
//            when (PreferenceUtils.getLoopMode()) {
//                Player.REPEAT_MODE_ALL -> {
//                    binding.btnShuffle.setImageResource(R.drawable.ic_loop_one)
//                    PreferenceUtils.putLoopMode(Player.REPEAT_MODE_ONE)
//                    showCustomToast(getString(R.string.loop_one))
//                }
//
//                Player.REPEAT_MODE_ONE -> {
//                    binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
//                    PreferenceUtils.putLoopMode(AppConstants.LOOP.LOOP_SHUFFLE)
//                    showCustomToast(getString(R.string.shuffle_on))
//                }
//
//                AppConstants.LOOP.LOOP_SHUFFLE -> {
//                    binding.btnShuffle.setImageResource(R.drawable.ic_loop_all)
//                    PreferenceUtils.putLoopMode(Player.REPEAT_MODE_ALL)
//                    showCustomToast(getString(R.string.loop_all))
//                }
//            }
//        }
//
//        binding.btnFavorite.setOnClickListener {
//            binding.btnFavorite.cancelAnimation()
//            if (musicPlayerService != null) {
//                if (musicPlayerService?.checkFavorite()!!) {
//                    binding.btnFavorite.progress = 0f
//                    showMessage(getString(R.string.remove_song_from_favorite))
////                    if (musicPlayerService?.getCurrent() != null) {
////                        songListDao?.deletePlaylistSong(musicPlayerService?.getCurrent())
////                    }
//                } else {
//                    showMessage(getString(R.string.added_to_favorite))
//                    binding.btnFavorite.playAnimation()
////                    if (musicPlayerService?.getCurrent() != null) {
////                        songListDao?.insertSongToPlaylist(musicPlayerService?.getCurrent())
////                    }
//                }
//            }
//        }
//
//        binding.tvTitleSong.isSelected = true
//        val today: Date = Calendar.getInstance().time //getting date
//        val formatter = SimpleDateFormat("dd.MM.yyyy") //formating according to my need
//        val date: String = formatter.format(today)
//        binding.tvDate.text = date
//    }
//
//    override fun onDestroy() {
//        ((BaseApplication)).mInstance?.isLockScreenRunning = false
//        super.onDestroy()
//    }
//
//    private fun showPopupMenu() {
//        var popupMenu = PopupMenu(this, binding.btnMoreLockScreen)
//        popupMenu.menuInflater.inflate(R.menu.popup_lockscreen, popupMenu.menu)
//        popupMenu.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.menuOffLockScreen -> {
//                    showAlertOffLockScreen()
//                }
//
//                R.id.menuBackgroundLockScreen -> {
//                    showDialogChoseBackgroundType()
//                }
//
//                R.id.menuTimeFormat -> {
//                    showDialogTimeFormat()
//                }
//            }
//            true
//        }
//        popupMenu.show()
//    }
//
//    private fun showAlertOffLockScreen() {
//        AlertDialog.Builder(this).setTitle(getString(R.string.tittle_off_lockscreen))
//            .setMessage(getString(R.string.message_off_lockscreen))
//            .setPositiveButton(getString(R.string.off)) { dialog, which ->
//                PreferenceUtils.put(AppConstants.PREF_LOCK_SCREEN, false)
//                finish()
//            }.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
//            }.show()
//    }
//
//    private fun showDialogChoseBackgroundType() {
//        val dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.dialog_chose_theme_lockscreen)
//        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.window!!.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        val groupTheme = dialog.findViewById<View>(R.id.groupTheme) as RadioGroup
//        val typeBackgroud = PreferenceUtils.getValueInt(AppConstants.BACKGROUND_LOCKSCREEN)
//        if (typeBackgroud == 0) {
//            groupTheme.check(R.id.rdCoverAlbum)
//        } else {
//            groupTheme.check(R.id.rdTheme)
//        }
//
//        groupTheme.setOnCheckedChangeListener { group, checkedId ->
//            if (checkedId == R.id.rdCoverAlbum) {
//                PreferenceUtils.put(AppConstants.BACKGROUND_LOCKSCREEN, 0)
////                setBackgroundLockScreen()
//            } else if (checkedId == R.id.rdTheme) {
//                PreferenceUtils.put(AppConstants.BACKGROUND_LOCKSCREEN, 1)
//                binding.imgBGLockScreen.setImageBitmap(BaseApplication.mInstance?.bmImg)
//            }
//            dialog.dismiss()
//        }
//        dialog.show()
//    }
//
//    /*    fun setBackgroundLockScreen() {
//            if (PreferenceUtils.getValueInt(AppConstants.BACKGROUND_LOCKSCREEN) == 0) {
//                val thumbStore = thumbStoreDB.thumbDao().findThumbnailByID(getCurrent()?.id)
//                if (thumbStore != null) {
//                    Glide.with(this).asBitmap().centerCrop()
//                        .load(thumbStore.thumbPath)
//                        .placeholder(R.drawable.ic_song_transparent)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .signature(ObjectKey(System.currentTimeMillis() / (1000 * 60 * 60 * 24))) //refresh avatar cache every day
//                        .into(object : CustomTarget<Bitmap>() {
//                            override fun onLoadCleared(placeholder: Drawable?) {}
//                            override fun onLoadFailed(errorDrawable: Drawable?) {
//                                imgBGLockScreen?.setImageBitmap(BaseApplication.mInstance?.bmImg)
//                            }
//
//                            override fun onResourceReady(
//                                resource: Bitmap,
//                                transition: Transition<in Bitmap>?
//                            ) {
//                                Blurry.with(imgBGLockScreen?.context).radius(100).from(resource)
//                                    .into(imgBGLockScreen)
//                            }
//                        })
//                } else {
//                    val uri = getCurrent()?.id?.let { ArtworkUtils.getArtworkFromSongID(it) }
//                    Glide.with(this).asBitmap().centerCrop()
//                        .load(getCurrent()?.id?.let { ArtworkUtils.getArtworkFromSongID(it) })
//                        .placeholder(R.drawable.ic_song_transparent)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .signature(ObjectKey(System.currentTimeMillis() / (1000 * 60 * 60 * 24))) //refresh avatar cache every day
//                        .into(object : CustomTarget<Bitmap>() {
//                            override fun onLoadCleared(placeholder: Drawable?) {}
//                            override fun onLoadFailed(errorDrawable: Drawable?) {
//                                imgBGLockScreen?.setImageBitmap(BaseApplication.mInstance?.bmImg)
//                            }
//
//                            override fun onResourceReady(
//                                resource: Bitmap,
//                                transition: Transition<in Bitmap>?
//                            ) {
//                                Blurry.with(imgBGLockScreen?.context).radius(100).from(resource)
//                                    .into(imgBGLockScreen)
//                            }
//                        })
//                }
//            } else {
//                imgBGLockScreen?.setImageBitmap(BaseApplication.mInstance?.bmImg)
//            }
//        }*/
//
//    private fun showDialogTimeFormat() {
//        val dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.dialog_format_time)
//        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.window!!.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        val groupFormatTime = dialog.findViewById<View>(R.id.groupFormatTime) as RadioGroup
//        val formatTime = PreferenceUtils.getValueInt(AppConstants.FORMAT_TIME_LOCKSCREEN)
//        if (formatTime == 0) {
//            groupFormatTime.check(R.id.rdFormat12h)
//        } else {
//            groupFormatTime.check(R.id.rdFormat24h)
//        }
//
//        groupFormatTime.setOnCheckedChangeListener { group, checkedId ->
//            if (checkedId == R.id.rdFormat12h) {
//                PreferenceUtils.put(AppConstants.FORMAT_TIME_LOCKSCREEN, 0)
//            } else if (checkedId == R.id.rdFormat24h) {
//                PreferenceUtils.put(AppConstants.FORMAT_TIME_LOCKSCREEN, 1)
//            }
//            setTimeFormat()
//            dialog.dismiss()
//        }
//        dialog.show()
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
//    fun onLoveSong(item: EventPutLoveSong) {
//        if (musicPlayerService != null) {
//            if (musicPlayerService?.checkFavorite()!!) {
//                binding.btnFavorite.progress = 1f
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
//    fun stopServiceMusic(event: EventStopService) {
//        if (event.isStop) {
//            if (!isFinishing) finish()
//        }
//    }
//
//    override fun onBindServiceMusicSuccess() {
//
//    }
//
//    override fun onServiceDisconnection() {
//    }
//}