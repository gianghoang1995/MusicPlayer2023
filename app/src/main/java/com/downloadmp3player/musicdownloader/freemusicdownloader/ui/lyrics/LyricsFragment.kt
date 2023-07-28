package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.lyrics

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.*
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventChangePager
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventUpdateLyric
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshData
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventShowLyric
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.lyric.LyricsDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.lyric.LyricsHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.lyric.LyricsOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail.AppDatabase
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentLyricsBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.OnFindLyricCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
import com.google.android.exoplayer2.ExoPlayer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.net.URLEncoder

class LyricsFragment : BaseFragment<FragmentLyricsBinding>(), OnBinderServiceConnection {
    var timmer: CountDownTimer? = null
    lateinit var thumbStoreDB: AppDatabase
    private var updateLyricsAsyncTask: AsyncTask<*, *, *>? = null
    lateinit var lyricHelper: LyricsHelperDB
    lateinit var lyricDao: LyricsDaoDB
    private var exoPlayer: ExoPlayer? = null

    private var runnableUpdateDuration: Runnable = object : Runnable {
        override fun run() {
            if (exoPlayer != null) {
                binding.progressTimer.max = exoPlayer!!.duration.toInt()
                binding.tvDurationLyric.text =
                    AppUtils.convertDuration(exoPlayer?.currentPosition ?: 0)
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

    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {
    }

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLyricsBinding {
        return FragmentLyricsBinding.inflate(inflater)
    }

    override fun FragmentLyricsBinding.initView() {
        thumbStoreDB = AppDatabase(requireContext())
        init()
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?.getPlayer?.observe(this) {
            exoPlayer = it
            if (exoPlayer != null) {
                val duration = exoPlayer?.duration?.toInt() ?: 0
                val curent = exoPlayer?.currentPosition?.toInt() ?: 0
                binding.progressTimer.max = duration
                binding.progressTimer.progress = curent
                binding.tvTimeLyric.text = AppUtils.convertDuration(duration)
                binding.tvDurationLyric.text = AppUtils.convertDuration(curent)

                runnableUpdateDuration.run()
                binding.progressTimer.max = exoPlayer?.duration?.toInt() ?: 0
                binding.progressTimer.postDelayed(
                    runnableUpdateDuration, AppConstants.INTERVAL_UPDATE_PROGRESS
                )
            }
        }

        musicPlayerService?.obverseMusicUtils?.getCurrentItemAudio?.observe(this) {
            if (it is ItemMusicOnline?) {
                binding.tvTitleLyric.text = it?.title
                binding.tvArtistLyric.text = getString(R.string.app_name)
                Glide.with(this).load(it?.resourceThumb).into(binding.imgThumb)
            } else if (it is MusicItem?) {
                binding.tvTitleLyric.text = it?.title
                binding.tvArtistLyric.text = it?.artist
            }
            setImgThumbLyric()
            updateLyrics()
        }

        musicPlayerService?.obverseMusicUtils?.getPlaybackState?.observe(this) {
            when (it) {
                AppConstants.PLAYBACK_STATE.STATE_PLAYING -> {
                    binding.btnPlayPauseLyric.setImageResource(R.drawable.ic_pause_big)
                    runnableUpdateDuration.run()
                    binding.progressTimer.max = exoPlayer?.duration?.toInt() ?: 0
                    binding.progressTimer.postDelayed(
                        runnableUpdateDuration, AppConstants.INTERVAL_UPDATE_PROGRESS
                    )
                }

                AppConstants.PLAYBACK_STATE.STATE_PAUSED -> {
                    binding.btnPlayPauseLyric.setImageResource(R.drawable.ic_play_big)
                }
            }
        }
    }

    fun init() {
        setBindListener(this)
        lyricHelper =
            LyricsHelperDB(context)
        lyricDao =
            LyricsDaoDB(lyricHelper)
//        loadBanner(frameBannerAds)
        binding.btnFindLyrics.setOnClickListener {
            if (AppUtils.isOnline(requireContext())) {
                if (binding.edtNameSong.text.toString().trim().isEmpty()) {
                    showMessage(getString(R.string.input_name_song))
                } else {
                    findLyricOnline(true)
                }
            } else {
                showMessage(getString(R.string.no_internet))
            }
        }

        binding.btnCancelFindLyrics.setOnClickListener {
            EventBus.getDefault().postSticky(EventChangePager(1))
            Keyboard.closeKeyboard(binding.edtNameSong)
        }

        binding.btnFindOtherLyric.setOnClickListener {
            if (AppUtils.isOnline(requireContext())) {
                findLyricFromBrowser()
            } else {
                showMessage(getString(R.string.no_internet))
            }
        }

        binding.edtNameSong.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                findLyricOnline(true)
                true
            } else false
        }

        binding.tvLyrics.setOnClickListener {
            EventBus.getDefault().postSticky(EventShowLyric(true))
        }

        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.btnFindOtherLyric.text = Html.fromHtml(
                "<u>${getString(R.string.find_on_gg)}</u>", Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            binding.btnFindOtherLyric.text =
                Html.fromHtml("<u>${getString(R.string.find_on_gg)}</u>")
        })

        binding.btnPlayPauseLyric.setOnClickListener {
            startService(AppConstants.ACTION_TOGGLE_PLAY)
        }

    }

    private fun setImgThumbLyric() {
        val thumbStore =
            thumbStoreDB.thumbDao().findThumbnailByID(musicPlayerService?.getCurrentSong()?.id)
        if (thumbStore != null) {
            Glide.with(this).load(thumbStore.thumbPath)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_song_transparent).into(binding.imgThumb)
        } else {
            val uri = musicPlayerService?.getCurrentSong()?.id?.let {
                ArtworkUtils.getArtworkFromSongID(it)
            }
            Glide.with(requireContext()).load(uri).placeholder(R.drawable.ic_song_transparent)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgThumb)
        }
    }

    fun setAvtThumbnail() {
        val thumbStore =
            thumbStoreDB.thumbDao().findThumbnailByID(musicPlayerService?.getCurrentSong()?.id)
        if (thumbStore != null) {
            Glide.with(this).load(thumbStore.thumbPath)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_song_transparent).into(binding.imgThumb)
        } else {
            val uri =
                musicPlayerService?.getCurrentSong()?.id?.let { ArtworkUtils.getArtworkFromSongID(it) }
            if (!Uri.EMPTY.equals(uri)) {
                Glide.with(this).load(uri).placeholder(R.drawable.ic_song_transparent)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgThumb)
            } else {
                Glide.with(this).load(musicPlayerService?.getCurrentSong()?.albumId?.let { it1 ->
                    ArtworkUtils.uri(
                        it1
                    )
                }).placeholder(R.drawable.ic_song_transparent).transition(
                    DrawableTransitionOptions.withCrossFade()
                ).into(binding.imgThumb)
            }
        }
    }

    fun initLyric() {
        activity?.runOnUiThread {
            if (binding.tvLyrics != null) {
                if (TextUtils.isEmpty(musicPlayerService?.lyrics)) {
                    binding.lyricsSearchView.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.GONE
                    if (musicPlayerService?.getCurrentItem() != null) {
                        binding.edtNameSong.setText(musicPlayerService?.getCurrentSong()?.title)
                        queryDBLyric()
                    } else {
                        binding.lyricsSearchView.visibility = View.VISIBLE
                        binding.edtNameSong.setText("")
                        binding.loadingView.visibility = View.GONE
                        binding.scrollView.visibility = View.GONE
                    }
                } else {
                    binding.tvLyrics.text = musicPlayerService?.lyrics
                    binding.scrollView.smoothScrollTo(0, 0, 2500)
                    binding.lyricsSearchView.visibility = View.GONE
                    binding.loadingView.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun queryDBLyric() {
        activity?.runOnUiThread {
            val song = musicPlayerService?.getCurrentSong()
            if (song != null) {
                val lyricSave = lyricDao.findLyric(song.songPath)
                if (lyricSave != null) {
                    setLyricLocalHTML(lyricSave)
                } else {
                    findLyricOnline(false)
                }
            }
        }
    }

    private fun findLyricOnline(isOpenBrowser: Boolean) {
        Keyboard.closeKeyboard(binding.edtNameSong)
        binding.lyricsSearchView.visibility = View.GONE
        binding.scrollView.visibility = View.GONE
        binding.loadingView.visibility = View.VISIBLE
        val ggFindLyrics = GGFindLyrics(context)
        var querry = binding.edtNameSong.text.toString()
        ggFindLyrics.findLyrics(querry, object :
            OnFindLyricCallback {
            override fun onFindLyricSuccess(lyric: String?) {
                if (binding.edtNameSong != null) {
                    binding.edtNameSong.setText("")
                    setLyricGGFinderHTML(lyric)
                }
            }

            override fun onFindLyricFailed() {
                musicPlayerService?.updateLyricData(null, null)
                if (binding.edtNameSong != null) {
                    if (TextUtils.isEmpty(binding.edtNameSong.text)) {
                        binding.edtNameSong.setText(musicPlayerService?.getCurrentSong()?.title)
                    }
                    binding.loadingView.visibility = View.GONE
                    binding.lyricsSearchView.visibility = View.VISIBLE
                    val url = AppConstants.BASE_GG_SEARCH + URLEncoder.encode(querry, "UTF-8")
                    if (isOpenBrowser) startActivity(
                        Intent(activity, BrowserLyricAct::class.java).putExtra(
                            BrowserLyricAct.URL_LYRIC, url
                        ).putExtra(
                            BrowserLyricAct.SONG_DATA, musicPlayerService?.getCurrentSong()
                        )
                    )
                }
            }
        })
    }

    private fun findLyricFromBrowser() {
        Keyboard.closeKeyboard(binding.edtNameSong)
        binding.scrollView.visibility = View.GONE
        if (TextUtils.isEmpty(binding.edtNameSong.text)) {
            binding.edtNameSong.setText(musicPlayerService?.getCurrentSong()?.title)
        }
        val querry = binding.edtNameSong.text.toString()
        binding.loadingView.visibility = View.GONE
        binding.lyricsSearchView.visibility = View.VISIBLE
        val url = AppConstants.BASE_GG_SEARCH + URLEncoder.encode(querry, "UTF-8")
        startActivity(
            Intent(
                activity, BrowserLyricAct::class.java
            ).putExtra(BrowserLyricAct.URL_LYRIC, url)
                .putExtra(BrowserLyricAct.SONG_DATA, musicPlayerService?.getCurrentSong())
        )
    }

    private fun setLyricLocalHTML(lyric: LyricsOnline) {
        activity?.runOnUiThread {
            if (lyric.lyricData != null) {
                musicPlayerService?.updateLyricData(null, lyric.lyricData)
                if (lyric.typeLyric == LyricsHelperDB.TYPE_HTML) {
                    musicPlayerService?.updateLyricData(null, lyric.lyricData)
                } else {
                    musicPlayerService?.updateLyricData(lyric.lyricData, null)
                }
            }
            binding.loadingView.visibility = View.GONE
            binding.scrollView.smoothScrollTo(0, 0, 2500)
            binding.lyricsSearchView.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE

            if (lyric.typeLyric == LyricsHelperDB.TYPE_HTML) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.tvLyrics.text =
                        Html.fromHtml(lyric.lyricData, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    binding.tvLyrics.text = Html.fromHtml(lyric.lyricData)
                }
            } else {
                binding.tvLyrics.text = lyric.lyricData
            }
        }
    }

    fun setLyricGGFinderHTML(lyricHTML: String?) {
        if (lyricHTML != null) {
            musicPlayerService?.updateLyricData(null, lyricHTML)
        }
        if (TextUtils.isEmpty(lyricHTML)) {
            binding.lyricsSearchView.visibility = View.VISIBLE
            binding.loadingView.visibility = View.GONE
            binding.scrollView.visibility = View.GONE
        } else {
            binding.loadingView.visibility = View.GONE
            binding.scrollView.smoothScrollTo(0, 0, 2500)
            binding.lyricsSearchView.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
            if (lyricHTML != null) {
                insertLyric(lyricHTML)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvLyrics.text = Html.fromHtml(lyricHTML, Html.FROM_HTML_MODE_COMPACT)
        } else {
            binding.tvLyrics.text = Html.fromHtml(lyricHTML)
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun updateLyrics() {
        binding.lyricsSearchView.visibility = View.GONE
        binding.loadingView.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
        binding.edtNameSong.setText(musicPlayerService?.getCurrentSong()?.title)
        if (timmer != null) {
            timmer?.cancel()
        }
        timmer = object : CountDownTimer(1500, 500) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                updateLyricsAsyncTask?.cancel(false)
                updateLyricsAsyncTask = object : AsyncTask<Void?, Void?, Lyrics?>() {
                    fun onCancelled(s: Lyrics) {
                        onPostExecute(null)
                    }

                    override fun doInBackground(vararg params: Void?): Lyrics? {
                        val song: MusicItem? = musicPlayerService?.getCurrentSong()
                        val data: String? = song?.let { MusicUtils.getLyrics(it) }
                        return if (TextUtils.isEmpty(data)) {
                            musicPlayerService?.updateLyricData(null, null)
                            queryDBLyric()
                            null
                        } else {
                            val lyricData = Lyrics.parse(song, data)
                            musicPlayerService?.updateLyricData(lyricData.text, null)
                            initLyric()
                            lyricData
                        }
                    }
                }.execute()
            }
        }.start()
    }

    private fun insertLyric(lyric: String) {
        val song = musicPlayerService?.getCurrentSong()
        if (song?.songPath?.isNotEmpty() == true) {
            val lyricItem = LyricsOnline()
            lyricItem.nameSong = song.title
            lyricItem.pathSong = song.songPath
            lyricItem.lyricData = lyric
            lyricItem.typeLyric = LyricsHelperDB.TYPE_HTML
            if (!LyricUtils.compressLyric(File(song.songPath), lyric, true)) {
                lyricDao.insertLyric(lyricItem)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: EventUpdateLyric?) {
        if (event != null) if (event.isSaveLyric) {
            updateLyrics()
            EventBus.getDefault().postSticky(EventUpdateLyric(false))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun changePage(event: EventChangePager) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun refreshThumb(event: EventRefreshData) {
        setAvtThumbnail()
    }

}
