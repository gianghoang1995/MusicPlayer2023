package com.musicplayer.mp3player.defaultmusicplayer.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.database.thumnail.AppDatabase
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.PlayerviewBottomBinding
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import com.musicplayer.mp3player.defaultmusicplayer.utils.ArtworkUtils
import com.google.android.exoplayer2.ExoPlayer
import de.hdodenhof.circleimageview.CircleImageView

class PlayerView : RelativeLayout {
    var binding: PlayerviewBottomBinding = PlayerviewBottomBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(attrs)
    }

    private var isOnline = false
    private var exoPlayer: ExoPlayer? = null
    private var runnableUpdateDuration: Runnable = object : Runnable {
        override fun run() {
            if (exoPlayer != null) {
                binding.progressTimer.max = exoPlayer!!.duration.toInt()
                exoPlayer?.currentPosition.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.progressTimer.setProgress(it?.toInt() ?: 0, true)
                    } else {
                        binding.progressTimer.progress = it?.toInt() ?: 0
                    }
                }
                binding.progressTimer.postDelayed(this, AppConstants.INTERVAL_UPDATE_PROGRESS)
            }
        }
    }

    var activity: Activity? = null
    var listener: OnClickControlListener? = null
    lateinit var thumbStoreDB: AppDatabase

    private fun init(attrs: AttributeSet?) {
        thumbStoreDB = AppDatabase(context)
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.PlayerViewSmallDetail, 0, 0)
        val drawableResId =
            typedArray.getResourceId(R.styleable.PlayerViewSmallDetail_thumbnail, -1);
        var isHideControl =
            typedArray.getBoolean(R.styleable.PlayerViewSmallDetail_hideControl, false)

        if (isHideControl) {
            binding.btnNext.visibility = View.GONE
            binding.btnPlayPause.visibility = View.GONE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnPlayPause.visibility = View.VISIBLE
        }
        binding.btnNext.setOnClickListener {
            listener?.onNextClick()
        }

        binding.btnPlayPause.setOnClickListener {
            listener?.onToggleClick()
        }

        binding.root.setOnClickListener {
            listener?.onClickControl()
        }

        binding.btnPrive.setOnClickListener { listener?.onPriveClick() }
    }

    fun startRunnableUpdateTimer() {
        runnableUpdateDuration.run()
        binding.progressTimer.max = exoPlayer?.duration?.toInt() ?: 0
        binding.progressTimer.postDelayed(
            runnableUpdateDuration, AppConstants.INTERVAL_UPDATE_PROGRESS
        )
    }

    fun seekBar(): ProgressBar {
        return binding.progressTimer
    }

    fun setPlayerState(bool: Boolean) {
        if (bool) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause_small)
            startRunnableUpdateTimer()
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play_small)
        }
    }

    fun initDataPlayerView(
        mActivity: Activity, mListener: OnClickControlListener, mExoPlayer: ExoPlayer?, item: Any?
    ) {
        listener = mListener
        activity = mActivity
        if (item != null) if (item is MusicItem) {
            isOnline = false
            initOfflineData(item)
        } else if (item is ItemMusicOnline) {
            isOnline = true
            initOnlineData(item)
        }
        exoPlayer = mExoPlayer
        binding.progressTimer.max = exoPlayer?.duration?.toInt() ?: 0
        binding.progressTimer.progress = exoPlayer?.currentPosition?.toInt() ?: 0
        if (exoPlayer?.isPlaying == true) {
            startRunnableUpdateTimer()
        }
    }

    fun initOfflineData(song: MusicItem?) {
        if (song != null) {
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist
            binding.tvTitle.isSelected = true
            initThumb(song)
        } else {
            binding.tvTitle.text = context.getString(R.string.app_name)
            binding.tvArtist.text = context.getString(R.string.app_name)
            binding.tvTitle.isSelected = true
            setPlayerState(false)
        }
    }

    fun initOnlineData(item: ItemMusicOnline?) {
        if (item != null) {
            binding.tvTitle.text = item.title
            binding.tvArtist.text = context.getString(R.string.app_name)
            binding.tvTitle.isSelected = true
            BaseApplication.mInstance?.applicationContext?.let {
                Glide.with(it).load(item.resourceThumb)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_song_transparent).into(binding.imgThumb)
            }
        } else {
            binding.tvTitle.text = context.getString(R.string.app_name)
            binding.tvArtist.text = context.getString(R.string.app_name)
            binding.tvTitle.isSelected = true
            setPlayerState(false)
        }
    }

    private fun initThumb(song: MusicItem?) {
        val thumbStore = thumbStoreDB.thumbDao().findThumbnailByID(song?.id)
        if (thumbStore != null) {
            BaseApplication.mInstance?.applicationContext?.let {
                Glide.with(it).load(thumbStore.thumbPath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_song_transparent).into(binding.imgThumb)
            }
        } else {
            val uri = song?.id?.let { ArtworkUtils.getArtworkFromSongID(it) }
            if (!Uri.EMPTY.equals(uri)) {
                BaseApplication.mInstance?.applicationContext?.let {
                    Glide.with(it).load(uri).placeholder(R.drawable.ic_song_transparent)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgThumb)
                }
            } else {
                BaseApplication.mInstance?.applicationContext?.let {
                    Glide.with(it).load(song?.albumId?.let { ArtworkUtils.uri(it) })
                        .placeholder(R.drawable.ic_song_transparent).transition(
                            DrawableTransitionOptions.withCrossFade()
                        ).into(binding.imgThumb)
                }
            }
        }
    }

    fun rotateThumb(imgThumb: CircleImageView) {
        try {
            val rotate = RotateAnimation(
                0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotate.duration = 15000
            rotate.repeatCount = Animation.INFINITE
            imgThumb.startAnimation(rotate)
        } catch (ex: java.lang.Exception) {
        }
    }

    fun makeTransparent(src: Bitmap, value: Int): Bitmap? {
        val width = src.width
        val height = src.height
        val transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(transBitmap)
        canvas.drawARGB(0, 0, 0, 0)
        val paint = Paint()
        paint.alpha = value
        canvas.drawBitmap(src, 0f, 0f, paint)
        return transBitmap
    }

    fun clearAnimationThumb(imgThumb: CircleImageView) {
        try {
            imgThumb.clearAnimation()
        } catch (ex: java.lang.Exception) {
        }
    }

    interface OnClickControlListener {
        fun onClickControl()
        fun onNextClick()
        fun onPriveClick()
        fun onToggleClick()
        fun onRestartClick()
        fun onStopMusic()
    }
}



