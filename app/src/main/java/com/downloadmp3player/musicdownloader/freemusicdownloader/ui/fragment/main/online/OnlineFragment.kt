package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.online

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliendroid.alienads.MaxIntertitial
import com.aliendroid.alienads.MaxNativeAds
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterCategory
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterOnlineAudio
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentOnlineBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.category_online.CategoryOnlineActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.PlayerMusicActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.search.SearchOnlineActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.utils.adsloader.NativeAdsManager

class OnlineFragment : BaseFragment<FragmentOnlineBinding>(),
    AdapterOnlineAudio.OnClickItemOnlineListener,
    OnBinderServiceConnection {
    private var mNativeAdManager: NativeAdsManager? = null
    lateinit var adapterCategory: AdapterCategory
    lateinit var recentPlayAdapter: AdapterOnlineAudio
    lateinit var favoriteStreamAdapter: AdapterOnlineAudio
    var songListDao: PlaylistSongDaoDB? = null
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null

    var loveSongDao: PlaylistSongDaoDB? = null
    var loveSongSqliteHelper: PlaylistSongSqLiteHelperDB? = null

    var lstRecentOnline: ArrayList<ItemMusicOnline?> = ArrayList()
    var lstLoveSongOnline: ArrayList<ItemMusicOnline?> = ArrayList()

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnlineBinding {
        return FragmentOnlineBinding.inflate(inflater)
    }

    override fun FragmentOnlineBinding.initView() {
        init()
    }

    fun init() {
        onClick()
        initNativeAds()
        initCategoryAdapter()
        initOnlineAdapter()
        setBindListener(this)
    }

    private fun initNativeAds() {
        mNativeAdManager = NativeAdsManager(
            requireActivity(),
            getString(R.string.native_ads_home_01),
            getString(R.string.native_ads_home_02)
        )
        mNativeAdManager?.loadAds(onLoadSuccess = {
            try {
                binding.nativeAdsView.showShimmer(false)
                binding.nativeAdsView.setNativeAd(it)
            } catch (e: java.lang.Exception) {
            }
        }, onLoadFail = {
            try {
                binding.nativeAdsView.hideAdsAndShimmer()
                MaxNativeAds.MediumNativeMax(
                    requireContext(),
                    binding.appLovinNative,
                    getString(R.string.appvolin_native)
                )
            } catch (e: java.lang.Exception) {
            }
        })
    }


    private fun initCategoryAdapter() {
        adapterCategory =
            AdapterCategory(
                requireContext(),
                onClickItemCategory = {
                    dialogLoadingAds?.showDialogLoading()
                    BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                        onLoadAdSuccess = {
                            dialogLoadingAds?.dismissDialog()
                        }, onAdClose = {
                            val intentCate =
                                Intent(requireContext(), CategoryOnlineActivity::class.java)
                            intentCate.putExtra(AppConstants.CATEGORY_DATA, it)
                            startActivity(intentCate)
                        }, onAdLoadFail = {
                            MaxIntertitial.ShowIntertitialApplovinMax(
                                requireActivity(), getString(R.string.appvolin_full)
                            ) {
                                dialogLoadingAds?.dismissDialog()
                                val intentCate =
                                    Intent(requireContext(), CategoryOnlineActivity::class.java)
                                intentCate.putExtra(AppConstants.CATEGORY_DATA, it)
                                startActivity(intentCate)
                            }
                        })
                })
        binding.rvCategory.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategory.setHasFixedSize(true)
        binding.rvCategory.isNestedScrollingEnabled = true
        binding.rvCategory.adapter = adapterCategory
    }


    fun initOnlineAdapter() {
        recentPlayAdapter = AdapterOnlineAudio(requireContext(), this, null)
        binding.rvRecentlyPlay.setHasFixedSize(true)
        binding.rvRecentlyPlay.adapter = recentPlayAdapter
        songListSqliteHelper =
            PlaylistSongSqLiteHelperDB(
                requireContext(),
                FavoriteSqliteHelperDB.TABLE_LAST_PLAYING
            )
        songListDao =
            PlaylistSongDaoDB(
                songListSqliteHelper
            )
        songListDao?.setQueryLastPlayingOnline()

        favoriteStreamAdapter = AdapterOnlineAudio(requireContext(), this, null)
        binding.rvFavoriteSong.setHasFixedSize(true)
        binding.rvFavoriteSong.adapter = favoriteStreamAdapter
        loveSongSqliteHelper =
            PlaylistSongSqLiteHelperDB(
                requireContext(),
                FavoriteSqliteHelperDB.DEFAULT_FAVORITE
            )
        loveSongDao =
            PlaylistSongDaoDB(
                loveSongSqliteHelper
            )
        loveSongDao?.setQueryLastPlayingOnline()
    }

    fun onClick() {
        binding.edtSearchOnline.setOnClickListener {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    startActivity(Intent(requireContext(), SearchOnlineActivity::class.java))
                }, onAdLoadFail = {
                    MaxIntertitial.ShowIntertitialApplovinMax(
                        requireActivity(), getString(R.string.appvolin_full)
                    ) {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(requireContext(), SearchOnlineActivity::class.java))
                    }
                })
        }
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?._insertRecentlyPlayed?.observe(this) {
            lstRecentOnline.clear()
            songListDao?.allItemOnlineFromPlaylist?.let { lstRecentOnline.addAll(it) }
            binding.rvRecentlyPlay.layoutManager =
                getLayoutManagerCenterHorizontal(lstRecentOnline.size)
            recentPlayAdapter.setData(lstRecentOnline)
            binding.recentPlayView.isVisible = lstRecentOnline.isNotEmpty()
        }

        musicPlayerService?.obverseMusicUtils?.getInsertFavoriteStream?.observe(this) {
            lstLoveSongOnline.clear()
            loveSongDao?.allItemOnlineFromPlaylist?.let { lstLoveSongOnline.addAll(it) }
            binding.rvFavoriteSong.layoutManager =
                getLayoutManagerCenterHorizontal(lstLoveSongOnline.size)
            favoriteStreamAdapter.setData(lstLoveSongOnline)
            binding.loveSongOnlineView.isVisible = lstLoveSongOnline.isNotEmpty()
        }
    }

    private fun getLayoutManagerCenterHorizontal(size: Int): RecyclerView.LayoutManager {
        val layoutManager: RecyclerView.LayoutManager
        if (size > 0) {
            var spanCount = 0
            when {
                size >= 3 -> {
                    spanCount = 3
                }

                size == 2 -> {
                    spanCount = 2
                }

                size == 1 -> {
                    spanCount = 1
                }
            }
            layoutManager =
                object :
                    GridLayoutManager(
                        requireContext(),
                        spanCount,
                        HORIZONTAL,
                        false
                    ) {
                    override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                        lp.width = (width / 6) * 5
                        return true
                    }
                }
        } else {
            layoutManager = LinearLayoutManager(requireContext())
        }
        return layoutManager
    }


    override fun onClickItemOnline(item: ItemMusicOnline?, position: Int) {
        if (AppUtils.isOnline(requireContext())) {
            dialogLoadingAds?.showDialogLoading()
            BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                onLoadAdSuccess = {
                    dialogLoadingAds?.dismissDialog()
                }, onAdClose = {
                    postClickItem(item)
                }, onAdLoadFail = {
                    MaxIntertitial.ShowIntertitialApplovinMax(
                        requireActivity(), getString(R.string.appvolin_full)
                    ) {
                        dialogLoadingAds?.dismissDialog()
                        postClickItem(item)
                    }
                })
        } else {
            showMessage(getString(R.string.please_check_internet_connection))
        }
    }

    private fun postClickItem(item: ItemMusicOnline?) {
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        intent.putExtra(AppConstants.ACTION_SET_DATA_ONLINE, item)
        intent.action = AppConstants.ACTION_SET_DATA_ONLINE
        requireContext().startService(intent)
        startActivity(Intent(requireContext(), PlayerMusicActivity::class.java))
    }

    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {

    }
}