package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.search

import android.content.Intent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterOnlineAudio
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.SuggestionsAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivitySearchOnlineBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventChangeNetwork
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.RequestSuggestionUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.SearchSuggestionCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.PlayerMusicActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.Keyboard
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchOnlineActivity : BaseActivity<ActivitySearchOnlineBinding>(),
    SearchSuggestionCallback,
    AdapterOnlineAudio.OnClickItemOnlineListener,
    AdapterOnlineAudio.OnLoadMoreListener,
    OnBinderServiceConnection {

    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {

    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?.getPlayer?.observe(this) {
            if (it != null)
                binding.playerView.initDataPlayerView(
                    this,
                    this,
                    it,
                    musicPlayerService?.getCurrentItem()
                )
        }

        musicPlayerService?.obverseMusicUtils?.getPlaybackState?.observe(this) {
            when (it) {
                AppConstants.PLAYBACK_STATE.STATE_PLAYING -> {
                    binding.playerView.setPlayerState(true)
                    binding.playerView.startRunnableUpdateTimer()
                }

                AppConstants.PLAYBACK_STATE.STATE_PAUSED -> {
                    binding.playerView.setPlayerState(false)
                }
            }
        }

        musicPlayerService?.obverseMusicUtils?._isServiceRunning?.observe(this) {
            if (it) {
                binding.playerView.isVisible = true
            } else {
                binding.playerView.isGone = true
            }
        }
    }

    private var isLoadMore = false
    lateinit var requestSuggestion: RequestSuggestionUtils
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    lateinit var onlineAudioAdapter: AdapterOnlineAudio
    var listSuggestion: ArrayList<String> = ArrayList()
    private val searchViewModel by lazy { ViewModelProvider(this)[SearchViewModel::class.java] }

    override fun bindingProvider(inflater: LayoutInflater): ActivitySearchOnlineBinding {
        return ActivitySearchOnlineBinding.inflate(inflater)
    }

    override fun ActivitySearchOnlineBinding.initView() {
        init()
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
    }

    fun init() {
        setBindListener(this)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        initLiveData()
        onlineAudioAdapter = AdapterOnlineAudio(this, this, this)
        binding.rvSearch.layoutManager = LinearLayoutManager(this)
        binding.rvSearch.setHasFixedSize(true)
        binding.rvSearch.adapter = onlineAudioAdapter
        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val llManager = (recyclerView.layoutManager as LinearLayoutManager?)!!
                if (dy > 0 && llManager.findLastCompletelyVisibleItemPosition() == onlineAudioAdapter.itemCount - 2) {
                    if (!isLoadMore) {
                        onlineAudioAdapter.showLoading()
                        isLoadMore = true
                    }
                }
            }
        })

        requestSuggestion =
            RequestSuggestionUtils(
                this,
                this
            )
        suggestionsAdapter = SuggestionsAdapter(this, onClickToItem = {
            isLoadMore = false
            binding.edtSearchOnline.setText(it.toString())
            binding.edtSearchOnline.setSelection(it.toString().length)
            Keyboard.closeKeyboard(binding.edtSearchOnline)
            searchViewModel.search(it.toString())
        })

        binding.rvSuggestion.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvSuggestion.setHasFixedSize(true)
        binding.rvSuggestion.adapter = suggestionsAdapter

        binding.edtSearchOnline.addTextChangedListener { text ->
            run {
                if (text.toString().isNotEmpty()) {
                    requestSuggestion.querySearch(text.toString())
                    binding.rvSuggestion.visibility = View.VISIBLE
                    binding.rvSearch.visibility = View.GONE
                } else {
                    binding.rvSuggestion.visibility = View.GONE
                    binding.rvSearch.visibility = View.VISIBLE
                }
            }
        }

        binding.edtSearchOnline.setOnEditorActionListener { v, actionId, event ->
            if (binding.edtSearchOnline.text?.isNotEmpty() == true) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    Keyboard.closeKeyboard(binding.edtSearchOnline)
                    searchViewModel.search(binding.edtSearchOnline.text.toString())
                    true
                } else false
            } else {
                getString(R.string.input_name_song)
                true
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.swipeRefresh.setOnRefreshListener {
            Keyboard.closeKeyboard(binding.edtSearchOnline)
            searchViewModel.search(binding.edtSearchOnline.text.toString())
        }

        binding.btnClearText.setOnClickListener {
            binding.edtSearchOnline.setText("")
            searchViewModel._responseSearch.value = arrayListOf()
        }
    }

    private fun initLiveData() {
        searchViewModel.initSearchUtil(this)
        searchViewModel.loading.observe(this) {
            showLoading(it)
        }

        searchViewModel.responseSearchObverse.observe(this) {
            binding.tvEmpty.isVisible = it.isEmpty()
            isLoadMore = false
            binding.swipeRefresh.isRefreshing = false
            binding.rvSearch.smoothScrollToPosition(0)
            onlineAudioAdapter.setData(it)
        }

        searchViewModel.responseLoadMoreObverse.observe(this) {
            isLoadMore = false
            onlineAudioAdapter.addItemMore(it)
        }

        searchViewModel.responseErrorLoadMore.observe(this) {
            isLoadMore = false
            onlineAudioAdapter.dismissLoading()
        }

        searchViewModel.responseErrorSearch.observe(this) {
            isLoadMore = false
            onlineAudioAdapter.dismissLoading()
            onlineAudioAdapter.setData(ArrayList())
            binding.tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isShow: Boolean) {
        binding.tvEmpty.visibility = View.GONE
        if (isShow) {
            binding.rvSuggestion.visibility = View.GONE
            binding.loadingView.visibility = View.VISIBLE
            binding.rvSearch.visibility = View.GONE
        } else {
            isLoadMore = false
            binding.swipeRefresh.isRefreshing = false
            binding.loadingView.visibility = View.GONE
            binding.rvSearch.visibility = View.VISIBLE
        }
    }

    override fun onSearchSuggestionSuccess(list: ArrayList<String>) {
        listSuggestion.clear()
        listSuggestion.addAll(list)
        suggestionsAdapter.setListKeyword(list)
    }

    override fun onClickItemOnline(item: ItemMusicOnline?, position: Int) {
        if (AppUtils.isOnline(this)) {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    val intent = Intent(this, MusicPlayerService::class.java)
                    intent.putExtra(AppConstants.ACTION_SET_DATA_ONLINE, item)
                    intent.action = AppConstants.ACTION_SET_DATA_ONLINE
                    startService(intent)
                    startActivity(Intent(this@SearchOnlineActivity, PlayerMusicActivity::class.java))
                }, onAdLoadFail = {
                    MaxIntertitial.ShowIntertitialApplovinMax(
                        this, getString(R.string.appvolin_full)
                    ) {
                        dialogLoadingAds?.dismissDialog()
                        val intent = Intent(this, MusicPlayerService::class.java)
                        intent.putExtra(AppConstants.ACTION_SET_DATA_ONLINE, item)
                        intent.action = AppConstants.ACTION_SET_DATA_ONLINE
                        startService(intent)
                        startActivity(Intent(this@SearchOnlineActivity, PlayerMusicActivity::class.java))
                    }
                })
        } else {
            showMessage(getString(R.string.please_check_internet_connection))
        }
    }

    override fun onLoadMore() {
        searchViewModel.loadMoreData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun changeConnection(event: EventChangeNetwork) {
        if (event.isChange) {
            runOnUiThread {
                if (searchViewModel.responseSearchObverse.value.isNullOrEmpty()) {
                    binding.swipeRefresh.isRefreshing = true
                    Keyboard.closeKeyboard(binding.edtSearchOnline)
                    searchViewModel.search(binding.edtSearchOnline.text.toString())
                }
            }
        }
    }
}