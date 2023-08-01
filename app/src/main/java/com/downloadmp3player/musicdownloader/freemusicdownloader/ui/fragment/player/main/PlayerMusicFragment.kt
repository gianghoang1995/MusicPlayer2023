package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.player.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail.AppDatabase
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail.ThumbnailMusic
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentPlayerOnlineBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventPutLoveSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshData
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer.EqualizerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.NowPlayingActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.PlayerViewModel
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.DialogSelectSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer.DialogTimePicker
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer.DialogTimePickerCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.widget.CustomLoadingDownloadView
import com.utils.adsloader.NativeAdsManager
import com.yalantis.ucrop.UCrop
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class PlayerMusicFragment : BaseFragment<FragmentPlayerOnlineBinding>(), OnBinderServiceConnection {
    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {
    }

    private var currentSong: MusicItem? = null
    var exoPlayer: ExoPlayer? = null

    private val SELECT_PICTURE = 1546
    private val CROP_PIC = 1235
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null
    var songListDao: PlaylistSongDaoDB? = null
    lateinit var thumbStoreDB: AppDatabase
    private val playerViewModel by lazy { ViewModelProvider(this)[PlayerViewModel::class.java] }
    private var runnableUpdateDuration: Runnable = object : Runnable {
        override fun run() {
            if (exoPlayer != null) {
                binding.seekPlayer.max = exoPlayer!!.duration.toInt()
                binding.tvCurrentPosition.text =
                    AppUtils.convertDuration(exoPlayer?.currentPosition ?: 0)
                exoPlayer?.currentPosition.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekPlayer.setProgress(it?.toInt() ?: 0, true)
                    } else {
                        binding.seekPlayer.progress = it?.toInt() ?: 0
                    }
                }
                binding.seekPlayer.postDelayed(this, AppConstants.INTERVAL_UPDATE_PROGRESS)
            }
        }
    }
    var stopMusicTimer: CountDownTimer? = null
    var millisUntilFinished: Long = 0

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlayerOnlineBinding {
        return FragmentPlayerOnlineBinding.inflate(inflater)
    }

    override fun FragmentPlayerOnlineBinding.initView() {
        init()
    }


    override fun onResume() {
        super.onResume()
        updateStateLoopMode()
    }

    override fun onStop() {
        super.onStop()
        cancelTimer()
    }

    fun init() {
        setBindListener(this)
        songListSqliteHelper = PlaylistSongSqLiteHelperDB(
            requireContext(), FavoriteSqliteHelperDB.DEFAULT_FAVORITE
        )
        songListDao = PlaylistSongDaoDB(
            songListSqliteHelper
        )
        binding.tvNameSong.isSelected = true
        thumbStoreDB = AppDatabase(requireContext())
        onClick()
        playerViewModel._isNewThumb.observe(this) {}
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?._isServiceRunning?.observe(this) {
            if (!it) requireActivity().finish()
        }

        musicPlayerService?.obverseMusicUtils?.getPlayer?.observe(this) {
            exoPlayer = it
            binding.tvMaxDuration.text = AppUtils.convertDuration(exoPlayer?.duration ?: 0)
            binding.tvCurrentPosition.text =
                AppUtils.convertDuration(exoPlayer?.currentPosition ?: 0)
            runnableUpdateDuration.run()
            binding.seekPlayer.max = exoPlayer?.duration?.toInt() ?: 0
            binding.seekPlayer.postDelayed(
                runnableUpdateDuration, AppConstants.INTERVAL_UPDATE_PROGRESS
            )
        }

        musicPlayerService?.obverseMusicUtils?.getCurrentItemAudio?.observe(this) {
            if (it is ItemMusicOnline?) {
                binding.tvNameSong.text = it?.title
                binding.tvArtist.text = getString(R.string.app_name)
                Glide.with(this).load(it?.resourceThumb).into(binding.imgAvt)
            } else if (it is MusicItem?) {
                binding.tvNameSong.text = it?.title
                binding.tvArtist.text = it?.artist
                setArtWork(it)
            }
        }

        musicPlayerService?.obverseMusicUtils?.getPlaybackState?.observe(this) {
            when (it) {
                AppConstants.PLAYBACK_STATE.STATE_PLAYING -> {
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause_big)
                    runnableUpdateDuration.run()
                    binding.seekPlayer.max = exoPlayer?.duration?.toInt() ?: 0
                    binding.seekPlayer.postDelayed(
                        runnableUpdateDuration, AppConstants.INTERVAL_UPDATE_PROGRESS
                    )
                }

                AppConstants.PLAYBACK_STATE.STATE_PAUSED -> {
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play_big)
                }
            }
        }

        musicPlayerService?.obverseMusicUtils?._isRunningTimer?.observe(this) {
            if (it) {
                binding.tvTimeRunning.isVisible = true
                updateTimer()
            } else {
                binding.tvTimeRunning.isVisible = false
                cancelTimer()
            }
        }

        musicPlayerService?.obverseMusicUtils?._isEndSong?.observe(this) {
            updateFavoriteStatus()
        }
    }

    private fun cancelTimer() {
        if (stopMusicTimer != null) {
            stopMusicTimer?.cancel()
            millisUntilFinished = 0
        }
    }

    private fun updateTimer() {
        cancelTimer()
        millisUntilFinished = musicPlayerService?.millisUntilFinished ?: 0
        if (millisUntilFinished > 0) {
            stopMusicTimer = object : CountDownTimer(millisUntilFinished, 1000) {
                override fun onFinish() {
                    stopMusicTimer?.cancel()
                    stopMusicTimer = null
                    binding.tvTimeRunning.isVisible = false
                }

                override fun onTick(utilFinish: Long) {
                    millisUntilFinished = utilFinish
                    binding.tvTimeRunning.text = AppUtils.convertDuration(utilFinish)
                }
            }
            stopMusicTimer?.start()
        }
    }

    private fun updateFavoriteStatus() {
        binding.btnFavorite.cancelAnimation()
        if (musicPlayerService?.checkFavorite() == true) {
            binding.btnFavorite.progress = 1F
        } else {
            binding.btnFavorite.progress = 0f
        }
    }

    override fun onDestroy() {
        binding.seekPlayer.progress = 0
        binding.seekPlayer.removeCallbacks(runnableUpdateDuration)
        super.onDestroy()
    }

    fun setArtWork(song: MusicItem?) {
        val thumbStore = thumbStoreDB.thumbDao().findThumbnailByID(song?.id)
        if (thumbStore != null) {
            Glide.with(this).load(thumbStore.thumbPath).placeholder(R.drawable.ic_song_transparent)
                .transition(DrawableTransitionOptions.withCrossFade()).into(binding.imgAvt)

        } else {
            val uri = song?.id?.let { ArtworkUtils.getArtworkFromSongID(it) }
            Glide.with(this).load(uri).placeholder(R.drawable.ic_song_transparent)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgAvt)
        }
    }

    fun onClick() {
        binding.btnChangeThumb.setOnClickListener {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    pickFromGallery()
                }, onAdLoadFail = {
                    dialogLoadingAds?.dismissDialog()
                    pickFromGallery()
                })
        }

        binding.seekPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress: Int, fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                musicPlayerService?.seek(seekBar?.progress ?: 0)
            }
        })

        binding.btnFavorite.setOnClickListener {
            binding.btnFavorite.cancelAnimation()
            if (musicPlayerService != null) {
                if (songListDao?.isContainsItemLocal(musicPlayerService?.getCurrentSong()) != null) {
                    binding.btnFavorite.progress = 0f
                    showMessage(getString(R.string.remove_song_from_favorite))
                    if (musicPlayerService?.getCurrentSong() != null) {
                        songListDao?.deleteLocalSong(musicPlayerService?.getCurrentSong())
                    }
                } else {
                    showMessage(getString(R.string.added_to_favorite))
                    binding.btnFavorite.playAnimation()
                    if (musicPlayerService?.getCurrentSong() != null) {
                        songListDao?.insertToLocalPlaylist(musicPlayerService?.getCurrentSong())
                    }
                }
            }
        }

        binding.btnPrive.setOnClickListener {
            startService(AppConstants.ACTION_PRIVE)
        }

        binding.btnNext.setOnClickListener {
            startService(AppConstants.ACTION_NEXT)
        }

        binding.btnPlayPause.setOnClickListener {
            startService(AppConstants.ACTION_TOGGLE_PLAY)
        }

        binding.btnEqualizer.setOnClickListener {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    startActivity(Intent(requireContext(), EqualizerActivity::class.java))
                }, onAdLoadFail = {
                    dialogLoadingAds?.dismissDialog()
                    startActivity(Intent(requireContext(), EqualizerActivity::class.java))
                })
        }

        binding.btnLoop.setOnClickListener {
            when (PreferenceUtils.getLoopMode()) {
                Player.REPEAT_MODE_ALL -> {
                    binding.btnLoop.setImageResource(R.drawable.ic_loop_one)
                    PreferenceUtils.putLoopMode(Player.REPEAT_MODE_ONE)
                    showCustomToast(getString(R.string.loop_one))
                }

                Player.REPEAT_MODE_ONE -> {
                    binding.btnLoop.setImageResource(R.drawable.ic_shuffle_on)
                    PreferenceUtils.putLoopMode(AppConstants.LOOP.LOOP_SHUFFLE)
                    showCustomToast(getString(R.string.shuffle_on))
                }

                AppConstants.LOOP.LOOP_SHUFFLE -> {
                    binding.btnLoop.setImageResource(R.drawable.ic_loop_all)
                    PreferenceUtils.putLoopMode(Player.REPEAT_MODE_ALL)
                    showCustomToast(getString(R.string.loop_all))
                }
            }
        }


        binding.btnAddToPlaylist.setOnClickListener {
            val listSong: ArrayList<MusicItem> = ArrayList()
            musicPlayerService?.getCurrentSong()?.let { it1 ->
                listSong.add(it1)
            }
            DialogSelectSong.getDialogAddToPlaylist(
                requireContext(),
                listSong,
                object : DialogSelectSong.OnDialogSelectSongListener {
                    override fun onAddToPlaylistDone() {

                    }
                }).show()
        }

        binding.btnTimer.setOnClickListener {
            DialogTimePicker.showDialogTimePicker(
                requireContext(),
                object : DialogTimePickerCallback {
                    override fun onPickerTime(time: String) {

                    }
                })
        }

        binding.btnNowPlaying.setOnClickListener {
            startActivity(Intent(requireActivity(), NowPlayingActivity::class.java))
        }
    }

    private fun updateStateLoopMode() {
        when (PreferenceUtils.getLoopMode()) {
            Player.REPEAT_MODE_ALL -> {
                binding.btnLoop.setImageResource(R.drawable.ic_loop_all)
            }

            Player.REPEAT_MODE_ONE -> {
                binding.btnLoop.setImageResource(R.drawable.ic_loop_one)
            }

            AppConstants.LOOP.LOOP_SHUFFLE -> {
                binding.btnLoop.setImageResource(R.drawable.ic_shuffle_on)
            }
        }
    }


    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(
            Intent.createChooser(
                intent, getString(R.string.label_select_picture)
            ), SELECT_PICTURE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Log.e("SELECT_PICTURE", "SELECT_PICTURE")
                val selectedImageUri: Uri? = data?.data
                currentSong = musicPlayerService?.getCurrentSong()
                if (null != selectedImageUri) {
                    val destinationUri = Uri.fromFile(
                        File(
                            AppUtils.getThumbnailPath(
                                requireContext(), currentSong?.id
                            )
                        )
                    )
                    UCrop.of(selectedImageUri, destinationUri).withAspectRatio(1f, 1f)
                        .start(requireContext(), this@PlayerMusicFragment, CROP_PIC)
                }
            }

            if (resultCode == AppCompatActivity.RESULT_OK && requestCode == CROP_PIC) {
                Log.e("ResultCrop", "crop done")
                val resultUri: Uri? = data?.let { UCrop.getOutput(it) }
                binding.imgAvt.setImageURI(Uri.parse(resultUri?.path))
                val itemCheck = thumbStoreDB.thumbDao().findThumbnailByID(currentSong?.id)
                if (itemCheck != null) {
                    thumbStoreDB.thumbDao()
                        .updateThumbnail(ThumbnailMusic(currentSong?.id, resultUri?.path))
                } else {
                    thumbStoreDB.thumbDao()
                        .insertThumbnail(ThumbnailMusic(currentSong?.id, resultUri?.path))
                }
                playerViewModel.isNewThumb.postValue(true)
                EventBus.getDefault().post(EventRefreshData(true))
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onLoveSong(item: EventPutLoveSong) {
        if (musicPlayerService != null) {
            if (musicPlayerService?.checkFavorite()!!) {
                binding.btnFavorite.progress = 1f
            }
        }
    }
}
