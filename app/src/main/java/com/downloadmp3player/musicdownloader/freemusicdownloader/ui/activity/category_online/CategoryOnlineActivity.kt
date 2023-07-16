package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.category_online

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterOnlineAudio
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityCategoryOnlineBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventChangeNetwork
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CategoryItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.PlayerMusicActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.search.SearchOnlineActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.search.SearchViewModel
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CategoryOnlineActivity : BaseActivity<ActivityCategoryOnlineBinding>(),
    AdapterOnlineAudio.OnClickItemOnlineListener,
    AdapterOnlineAudio.OnLoadMoreListener, OnBinderServiceConnection {

    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {

    }


    private var isLoadMore = false
    lateinit var onlineAudioAdapter: AdapterOnlineAudio
    private val searchViewModel by lazy { ViewModelProvider(this)[SearchViewModel::class.java] }
    var category: CategoryItem? = null

    override fun bindingProvider(inflater: LayoutInflater): ActivityCategoryOnlineBinding {
        return ActivityCategoryOnlineBinding.inflate(inflater)
    }

    override fun ActivityCategoryOnlineBinding.initView() {
        setBindListener(this@CategoryOnlineActivity)
        category = intent.getParcelableExtra(AppConstants.CATEGORY_DATA)

        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        initLiveData()
        onlineAudioAdapter = AdapterOnlineAudio(
            this@CategoryOnlineActivity,
            this@CategoryOnlineActivity,
            this@CategoryOnlineActivity
        )
        binding.rvAudioOnline.layoutManager = LinearLayoutManager(this@CategoryOnlineActivity)
        binding.rvAudioOnline.setHasFixedSize(true)
        binding.rvAudioOnline.adapter = onlineAudioAdapter
        binding.rvAudioOnline.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnGoToSearchOnline.setOnClickListener {
            startActivity(Intent(this@CategoryOnlineActivity, SearchOnlineActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener {
            category?.let { searchViewModel.getCategory(it) }
        }

        binding.tvCategoryTitle.text = category?.cateTitle
    }


    override fun onResume() {
        super.onResume()
        initBannerAds(binding.frameBannerAds)
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?.getPlayer?.observe(this) {
            if (it != null) binding.playerView.initDataPlayerView(
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


    private fun initLiveData() {
        searchViewModel.initSearchUtil(this)
        category?.let { searchViewModel.getCategory(it) }
        searchViewModel.loading.observeForever {
            binding.tvEmpty.visibility = View.GONE
            showLoading(it)
        }

        searchViewModel.responseSearchObverse.observeForever {
            binding.swipeRefresh.isRefreshing = false
            isLoadMore = false
            binding.rvAudioOnline.smoothScrollToPosition(0)
            onlineAudioAdapter.setData(it)
        }

        searchViewModel.responseLoadMoreObverse.observeForever {
            isLoadMore = false
            onlineAudioAdapter.addItemMore(it)
        }

        searchViewModel.responseErrorLoadMore.observeForever {
            binding.swipeRefresh.isRefreshing = false
            isLoadMore = false
            onlineAudioAdapter.dismissLoading()
        }

        searchViewModel.responseErrorSearch.observeForever {
            binding.swipeRefresh.isRefreshing = false
            isLoadMore = false
            onlineAudioAdapter.setData(ArrayList())
            onlineAudioAdapter.dismissLoading()
            binding.tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isShow: Boolean) {
        if (isShow) {
            binding.loadingView.visibility = View.VISIBLE
            binding.rvAudioOnline.visibility = View.GONE
        } else {
            isLoadMore = false
            binding.loadingView.visibility = View.GONE
            binding.rvAudioOnline.visibility = View.VISIBLE
        }
    }

    override fun onClickItemOnline(item: ItemMusicOnline?, position: Int) {
        if (AppUtils.isOnline(this)) {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    postOnClickItem(item)
                }, onAdLoadFail = {
                    MaxIntertitial.ShowIntertitialApplovinMax(
                        this, getString(R.string.appvolin_full)
                    ) {
                        dialogLoadingAds?.dismissDialog()
                        postOnClickItem(item)
                    }
                })
        } else {
            showMessage(getString(R.string.please_check_internet_connection))
        }
    }

    private fun postOnClickItem(item: ItemMusicOnline?) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.putExtra(AppConstants.ACTION_SET_DATA_ONLINE, item)
        intent.action = AppConstants.ACTION_SET_DATA_ONLINE
        startService(intent)
        startActivity(Intent(this@CategoryOnlineActivity, PlayerMusicActivity::class.java))
    }

    override fun onLoadMore() {
        searchViewModel.loadMoreCategoryData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun changeConnection(event: EventChangeNetwork) {
        if (event.isChange) {
            runOnUiThread {
                if (searchViewModel.responseSearchObverse.value.isNullOrEmpty()) {
                    binding.swipeRefresh.isRefreshing = true
                    category?.let { searchViewModel.getCategory(it) }
                }
            }
        }
    }

}