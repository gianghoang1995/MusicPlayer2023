package com.musicplayer.mp3player.playermusic.ui.activity.youtube_search

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.base.BaseActivity
import com.musicplayer.mp3player.playermusic.equalizer.databinding.ActivityWebviewSearchYoutubeBinding
import com.musicplayer.mp3player.playermusic.utils.AppConstants

class YoutubeSearchWebviewActivity : BaseActivity<ActivityWebviewSearchYoutubeBinding>() {
    private var keyword: String = ""
    private var urlLight: String = "https://m.youtube.com/results?app=m&theme=light&search_query="
    var watch = "youtube.com/watch"
    var error = false

    override fun bindingProvider(inflater: LayoutInflater): ActivityWebviewSearchYoutubeBinding {
        return ActivityWebviewSearchYoutubeBinding.inflate(inflater)
    }

    override fun ActivityWebviewSearchYoutubeBinding.initView() {
        keyword = intent.getStringExtra(AppConstants.KEYWORD_SEARCH).toString()
        init()
    }

    @SuppressLint("SetJavaScriptEnabled")

    fun init() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        binding.toolBar.title = getString(R.string.search_from_youtube) + " Youtube"
        binding.webViewSearch.settings.loadsImagesAutomatically = true
        binding.webViewSearch.settings.mediaPlaybackRequiresUserGesture = false
        binding.webViewSearch.settings.databaseEnabled = true
        binding.webViewSearch.settings.domStorageEnabled = true
        binding.webViewSearch.setBackgroundColor(Color.parseColor("#181a20"))
        binding.webViewSearch.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.webViewSearch.settings.forceDark
        }
        binding.webViewSearch.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G928X Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36"
        binding.webViewSearch.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!error) {
                    binding.searchView.visibility = View.INVISIBLE
                    binding.webViewSearch.visibility = View.VISIBLE
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                handleError()
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }
        }
        binding.webViewSearch.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                binding.progressLoading.visibility = View.VISIBLE
                binding.progressLoading.progress = progress
                if (progress == 100) binding.progressLoading.visibility = View.GONE
                else binding.progressLoading.visibility = View.VISIBLE
            }
        }

        binding.webViewSearch.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY


        goUrl(keyword)
        onClick()
    }

    fun onClick() {
        binding.btnReload.setOnClickListener {
            goUrl(keyword)
        }

        binding.toolBar.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun handleError() {
        binding.errorView.visibility = View.VISIBLE
        binding.webViewSearch.visibility = View.INVISIBLE
        binding.searchView.visibility = View.INVISIBLE
        error = true
    }

    private fun showWebView() {
        binding.webViewSearch.visibility = View.VISIBLE
        binding.errorView.visibility = View.INVISIBLE
        binding.searchView.visibility = View.INVISIBLE
        error = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun goUrl(keyword: String) {
        showWebView()
        binding.webViewSearch.loadUrl(urlLight + keyword)
    }
}