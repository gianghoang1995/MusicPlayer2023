package com.musicplayer.mp3player.playermusic.ui.lyrics

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.base.BaseActivity
import com.musicplayer.mp3player.playermusic.BaseApplication.Companion.getAppInstance
import com.musicplayer.mp3player.playermusic.database.lyric.LyricsDaoDB
import com.musicplayer.mp3player.playermusic.database.lyric.LyricsHelperDB
import com.musicplayer.mp3player.playermusic.database.lyric.LyricsOnline
import com.musicplayer.mp3player.playermusic.equalizer.databinding.ActivityBrowserLyricBinding
import com.musicplayer.mp3player.playermusic.eventbus.BusUpdateLyric
import com.musicplayer.mp3player.playermusic.callback.OnBinderServiceConnection
import com.musicplayer.mp3player.playermusic.model.MusicItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class BrowserLyricAct : BaseActivity<ActivityBrowserLyricBinding>(), OnBinderServiceConnection {
    private var url: String? = null
    private var song: MusicItem? = null
    private var clipboard: ClipboardManager? = null
    private var mLyricsHelper: LyricsHelperDB? = null
    private var mLyricsDao: LyricsDaoDB? = null
    private var lyric = ""

    override fun bindingProvider(inflater: LayoutInflater): ActivityBrowserLyricBinding {
        return ActivityBrowserLyricBinding.inflate(inflater)
    }

    override fun ActivityBrowserLyricBinding.initView() {
        init()
    }

    fun init() {
        ButterKnife.bind(this)
        setBindListener(this)
        url = intent.getStringExtra(URL_LYRIC)
        song = intent.getParcelableExtra(SONG_DATA)
        mLyricsHelper = LyricsHelperDB(this)
        mLyricsDao = LyricsDaoDB(mLyricsHelper)
        registerClipboardManager()
        goUrl(url)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().postSticky(BusUpdateLyric(true))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterClipboardManger()
    }

    private val mOnPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (clipboard != null) {
            lyric = clipboard!!.text.toString()
            binding.askSaveLyric.visibility = View.VISIBLE
            binding.tvPreviewLyric.text = lyric
            binding.tooltipCopy.visibility = View.GONE
        }
    }

    @OnClick(R.id.btnSaveLyric, R.id.btnCancelSave, R.id.btnAcceptTooltip, R.id.btnBack)
    fun OnClick(view: View) {
        when (view.id) {
            R.id.btnSaveLyric -> saveLyric()
            R.id.btnCancelSave -> binding.askSaveLyric.visibility = View.GONE
            R.id.btnAcceptTooltip -> binding.tooltipCopy.visibility = View.GONE
            R.id.btnBack -> onBackPressed()
        }
    }

    private fun saveLyric() {
        binding.askSaveLyric.visibility = View.GONE

            val lyricsOnline = LyricsOnline()
            lyricsOnline.nameSong = song!!.title
            lyricsOnline.pathSong = song!!.songPath
            lyricsOnline.lyricData = lyric
            lyricsOnline.typeLyric = LyricsHelperDB.TYPE_COPPY
            if (!LyricUtils.compressLyric(song!!.songPath?.let { File(it) }, lyric, false)) {
                if (mLyricsDao!!.isContain(song!!.songPath)) {
                    mLyricsDao!!.updateLyric(lyricsOnline)
                } else {
                    mLyricsDao!!.insertLyric(lyricsOnline)
                }
        }
        getAppInstance().adsFullInApp?.showAds(this, {
            Toast.makeText(
                this@BrowserLyricAct,
                getString(R.string.save_lyric_done),
                Toast.LENGTH_SHORT
            ).show()
            EventBus.getDefault().postSticky(BusUpdateLyric(true))
            finish()
        }) {
            Toast.makeText(
                this@BrowserLyricAct,
                getString(R.string.save_lyric_done),
                Toast.LENGTH_SHORT
            ).show()
            EventBus.getDefault().postSticky(BusUpdateLyric(true))
            finish()
        }
    }

    private fun registerClipboardManager() {
        clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard != null) {
            clipboard!!.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener)
        }
    }

    private fun unregisterClipboardManger() {
        clipboard!!.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun goUrl(url: String?) {
        if (url!!.isEmpty()) {
            Toast.makeText(this, "Please enter url", Toast.LENGTH_SHORT).show()
            return
        }
        binding.webViewLyric.settings.loadsImagesAutomatically = true
        binding.webViewLyric.settings.javaScriptEnabled = true
        binding.webViewLyric.webViewClient = WebViewClient()
        binding.webViewLyric.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                binding.loadingView.visibility = View.VISIBLE
                binding.loadingView.progress = progress
                if (progress == 100) binding.loadingView.visibility = View.GONE
            }
        }
        binding.webViewLyric.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.webViewLyric.loadUrl(url)
    }

    override fun onBackPressed() {
        if (binding.webViewLyric.canGoBack()) {
            binding.webViewLyric.goBack()
        } else {
            super.onBackPressed()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: BusUpdateLyric?) {
    }
    override fun onBindServiceMusicSuccess() {}
    override fun onServiceDisconnection() {}
    companion object {
        const val URL_LYRIC = "URL_LYRIC"
        const val SONG_DATA = "SONG"
    }
}