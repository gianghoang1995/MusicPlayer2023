package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.PagerAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.SettingAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickSettingListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockDaoBD
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.block.AppBlockHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.LayoutMainActivityBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.AlbumLoaderTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.ArtistLoaderTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.SongLoaderAsyncTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.TutorialActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer.EqualizerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.ringtone.RingtoneMakerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.scaningmusic.ScanMusicActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.search.SearchActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.setting.SettingActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.theme.CustomThemesActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer.DialogTimePicker
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer.DialogTimePickerCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.album.AlbumsFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.album.adapter.AdapterUnblockAlbums
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.artist.ArtistFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.artist.adapter.AdapterUnblockArtist
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.online.OnlineFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.playlist.PlaylistFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.QueryFolderFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.adapter.AdapterUnblockFolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderDao
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderHelper
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.song.SongFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.*
import com.utils.adsloader.InterstitialPreloadAdManager
import com.utils.adsloader.NativeAdGiftView
import com.utils.adsloader.NativeAdMediumView
import com.utils.adsloader.NativeAdsManager
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainActivity : BaseActivity<LayoutMainActivityBinding>(), OnClickSettingListener,
    OnBinderServiceConnection {
    override fun onBindServiceMusicSuccess() {
        initObverse()
    }

    override fun onServiceDisconnection() {

    }

    private var nativeAdsManager: NativeAdsManager? = null
    private var nativeAdsBig: NativeAdGiftView? = null
    var dialogRating: Dialog? = null
    var dialogExitApp: Dialog? = null
    lateinit var pagerAdapter: PagerAdapter
    lateinit var fragmentFolder: QueryFolderFragment
    lateinit var fragmentAlbum: AlbumsFragment
    lateinit var fragmentArtist: ArtistFragment
    lateinit var fragmentSong: SongFragment
    lateinit var fragmentPlaylist: PlaylistFragment
    lateinit var settingAdapter: SettingAdapter
    var countSong: Int = 0
    var countAlbum: Int = 0
    var countArtist: Int = 0
    override fun onStop() {
        super.onStop()
        if (dialogRating != null) {
            if (dialogRating?.isShowing!!) {
                dialogRating?.dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(AppConstants.SHOW_ADS, false)) {
            BaseApplication.isShowAdsFull = true
            BaseApplication.mInstance?.adsFullPlash?.showAds(this,object :InterstitialPreloadAdManager.InterstitialAdListener{
                override fun onClose() {

                }

                override fun onError() {
                }
            })
        }
        AppUtils.checkPermissionFux(this)
    }

    override fun LayoutMainActivityBinding.initView() {
        initBannerAds(binding.contentMain.frameBannerAds)
        setBindListener(this@MainActivity)
        settingAdapter = SettingAdapter(this@MainActivity, this@MainActivity)
        settingAdapter.initList()
        binding.menuView.rvMenu.setHasFixedSize(true)
        binding.menuView.rvMenu.layoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        binding.menuView.rvMenu.adapter = settingAdapter

        pagerAdapter = PagerAdapter(supportFragmentManager)
        val folderPath = intent.getStringExtra(AppConstants.SHORTCUT_FOLDER_PATH)
        binding.contentMain.viewPager.removeAllViews()
        fragmentFolder = if (!TextUtils.isEmpty(folderPath)) QueryFolderFragment(
            folderPath
        )
        else QueryFolderFragment()
        fragmentAlbum = AlbumsFragment()
        fragmentArtist = ArtistFragment()
        fragmentSong = SongFragment()
        fragmentPlaylist = PlaylistFragment()

        pagerAdapter.addFragment(fragmentSong, getString(R.string.title_songs))
        pagerAdapter.addFragment(fragmentPlaylist, getString(R.string.title_playlist))
        pagerAdapter.addFragment(fragmentAlbum, getString(R.string.title_album))
        pagerAdapter.addFragment(fragmentArtist, getString(R.string.title_artists))
        pagerAdapter.addFragment(fragmentFolder, getString(R.string.title_folders))


        binding.contentMain.viewPager.adapter = pagerAdapter
        binding.contentMain.viewPager.offscreenPageLimit = pagerAdapter.count - 1
        if (!TextUtils.isEmpty(folderPath)) {
            binding.contentMain.viewPager.currentItem = 5
        } else {
            binding.contentMain.viewPager.currentItem = 0
        }
        pagerAdapter.notifyDataSetChanged()
        binding.contentMain.tabLayout.setupWithViewPager(binding.contentMain.viewPager)
        OverScrollDecoratorHelper.setUpOverScroll(binding.contentMain.viewPager)
        setViewEvent()
        binding.menuView.rvMenu.isNestedScrollingEnabled = false
    }

    override fun bindingProvider(inflater: LayoutInflater): LayoutMainActivityBinding {
        return LayoutMainActivityBinding.inflate(inflater)
    }


    override fun onResume() {
        super.onResume()
        loadCountData()
        initThemeStyle(
            binding.contentMain.supportThemes.imgTheme,
            binding.contentMain.supportThemes.blackTspView
        )
        binding.menuView.navigationMain.setImageBitmap(BaseApplication.mInstance?.bmImg)
        reloadData()
    }

    override fun onBackPressed() {
        if (fragmentFolder.canNotExit()) {
            if (binding.contentMain.viewPager.currentItem == 3) {
                if (!fragmentFolder.onBackClick()) {
                    exit()
                }
            } else {
                exit()
            }
        } else {
            exit()
        }
    }

    private fun reloadData() {
        when (binding.contentMain.viewPager.currentItem) {
            1 -> {
                fragmentSong.reloadData()
            }

            3 -> {
                fragmentAlbum.reloadData()
            }

            4 -> {
                fragmentArtist.reloadData()
            }

            5 -> {
                fragmentFolder.reloadDataFolder()
            }
        }
    }

    private fun initObverse() {
        musicPlayerService?.obverseMusicUtils?.getPlayer?.observe(this) {
            if (it != null) binding.contentMain.playerView.initDataPlayerView(
                this,
                this,
                it,
                musicPlayerService?.getCurrentItem()
            )
        }

        musicPlayerService?.obverseMusicUtils?.getPlaybackState?.observe(this) {
            when (it) {
                AppConstants.PLAYBACK_STATE.STATE_PLAYING -> {
                    binding.contentMain.playerView.setPlayerState(true)
                    binding.contentMain.playerView.startRunnableUpdateTimer()
                }

                AppConstants.PLAYBACK_STATE.STATE_PAUSED -> {
                    binding.contentMain.playerView.setPlayerState(false)
                }
            }
        }

        musicPlayerService?.obverseMusicUtils?._isServiceRunning?.observe(this) {
            if (it) {
                binding.contentMain.playerView.isVisible = true
            } else {
                binding.contentMain.playerView.isGone = true
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun setViewEvent() {
        binding.contentMain.btnSetting.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(Gravity.START)) binding.drawerLayout.openDrawer(
                Gravity.START
            )
            else binding.drawerLayout.closeDrawer(Gravity.END)
        }

        binding.contentMain.viewPager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    1 -> {
                        fragmentSong.reloadData()
                    }

                    3 -> {
                        fragmentAlbum.reloadData()
                    }

                    4 -> {
                        fragmentArtist.reloadData()
                    }

                    5 -> {
                        fragmentFolder.reloadDataFolder()
                    }
                }
            }
        })

        binding.contentMain.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun exit() {
        val mills = PreferenceUtils.getValueLong(AppConstants.PREF_TIME_LATTER_30_MIN)
        if (!PreferenceUtils.getValueBoolean(AppConstants.PREF_DONT_SHOW_RATE)) {
            if (System.currentTimeMillis() - mills > 3600000 / 2) {
                showDialogRating()
            } else {
                initDialogExitApp()
                showDialogExitApp()
            }
        } else {
            initDialogExitApp()
            showDialogExitApp()
        }
    }

    private fun showDialogRating() {
        dialogRating = Dialog(this)
        dialogRating?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogRating?.setContentView(R.layout.rating_dialog)
        dialogRating?.setCanceledOnTouchOutside(true)
        dialogRating?.setCancelable(false)
        dialogRating?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogRating?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btn_rating: TextView? = dialogRating?.findViewById(R.id.btn_rating)
        val btn_cancel = dialogRating?.findViewById<TextView>(R.id.btn_cancel)

        btn_rating?.setOnClickListener { v: View? ->
            PreferenceUtils.put(AppConstants.PREF_DONT_SHOW_RATE, true)
            rateInStore()
            dialogRating?.dismiss()
            finish()
        }

        btn_cancel?.setOnClickListener { v: View? ->
            PreferenceUtils.put(AppConstants.PREF_TIME_LATTER_30_MIN, System.currentTimeMillis())
            dialogRating?.dismiss()
            finish()
        }
        dialogRating?.show()
    }


    private fun initDialogExitApp() {
        nativeAdsManager = NativeAdsManager(
            this, getString(R.string.native_ads_01),
            getString(R.string.native_ads_02)
        )
        nativeAdsManager?.loadAds(onLoadSuccess = {
            nativeAdsBig?.setNativeAd(it)
            nativeAdsBig?.showShimmer(false)
        }, onLoadFail = {})

        dialogExitApp = Dialog(this)
        dialogExitApp?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogExitApp?.setContentView(R.layout.dialog_exit_app)
        dialogExitApp?.setCancelable(false)
        dialogExitApp?.window?.setGravity(Gravity.BOTTOM)
        dialogExitApp?.setCanceledOnTouchOutside(true)
        dialogExitApp?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogExitApp?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        nativeAdsBig = dialogExitApp?.findViewById(R.id.nativeAdsBig)
        val btnExit = dialogExitApp?.findViewById<Button>(R.id.btnExit)
        btnExit?.setOnClickListener {
            finishAffinity()
        }
        dialogExitApp?.show()
    }

    private fun showDialogExitApp() {
        dialogExitApp?.show()
    }

    override fun onItemSettingClick(position: Int) {
        when (position) {
            0 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                        }
                    })
            }

            1 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this, RingtoneMakerActivity::class.java))
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(Intent(this, RingtoneMakerActivity::class.java))
                        }
                    })
            }

            2 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        DialogTimePicker.showDialogTimePicker(
                            this@MainActivity,
                            object : DialogTimePickerCallback {
                                override fun onPickerTime(time: String) {

                                }
                            })
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            DialogTimePicker.showDialogTimePicker(
                                this@MainActivity,
                                object : DialogTimePickerCallback {
                                    override fun onPickerTime(time: String) {

                                    }
                                })
                        }
                    })
            }

            3 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, ScanMusicActivity::class.java))
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(Intent(this@MainActivity, ScanMusicActivity::class.java))
                        }
                    })
            }

            4 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, CustomThemesActivity::class.java))
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(Intent(this@MainActivity, CustomThemesActivity::class.java))
                        }
                    })
            }

            5 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, EqualizerActivity::class.java))
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(Intent(this@MainActivity, EqualizerActivity::class.java))
                        }
                    })
            }

            6 -> {/*Thư mục ẩn*/
                showDialogUnblockFolder()
            }

            7 -> {/*Album ẩn*/
                showDialogUnblockAlbum()
            }

            8 -> {/*Artist ẩn*/
                showDialogUnblockArtist()
            }

            9 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                    }, onAdLoadFail = {
                        MaxIntertitial.ShowIntertitialApplovinMax(
                            this, getString(R.string.appvolin_full)
                        ) {
                            dialogLoadingAds?.dismissDialog()
                            startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                        }
                    })
            }

            10 -> {
                openUrl(AppConstants.POLICY_URL)
            }

            11 -> {
                AppUtils.shareText(
                    this,
                    getString(R.string.subject_share),
                    "${getString(R.string.content_share)} https://play.google.com/store/apps/details?id=${packageName}"
                )
            }

            12 -> {
                openPubGGPlay(AppConstants.PUBLISHER_NAME)
            }

            13 -> {
                rateInStore()
                PreferenceUtils.put(AppConstants.PREF_DONT_SHOW_RATE, true)
            }

            14 -> {
                finishAffinity()
            }
        }
    }

    private fun showDialogUnblockFolder() {
        val blockFolderHelper = BlockFolderHelper(this)
        var mBlockFolderDao = BlockFolderDao(blockFolderHelper)
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_show_block_folder)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.attributes = wlp
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnUnBlockFolder = dialog.findViewById<Button>(R.id.btnUnBlock)
        val tvEmptyBlockFolder = dialog.findViewById<TextView>(R.id.tvEmptyBlock)
        val rvBlockFolder: RecyclerView = dialog.findViewById(R.id.rvBlock)
        val adapterBlockFolder = AdapterUnblockFolder(this) { isEnable: Boolean ->
            btnUnBlockFolder.isEnabled = isEnable
            if (isEnable) {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_unlock_folder)
            } else {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_cancel)
            }
        }
        rvBlockFolder.setHasFixedSize(true)
        rvBlockFolder.layoutManager = LinearLayoutManager(this)
        rvBlockFolder.adapter = adapterBlockFolder
        val lstBlock: ArrayList<FolderItem> = mBlockFolderDao.getAllFolderBlock()
        if (lstBlock.isEmpty()) {
            tvEmptyBlockFolder.visibility = View.VISIBLE
        } else {
            adapterBlockFolder.setData(lstBlock)
        }
        btnCancel.setOnClickListener { v: View? -> dialog.dismiss() }
        btnUnBlockFolder.setOnClickListener { v: View? ->
            mBlockFolderDao.deleteBlockFolder(adapterBlockFolder.listDelete)
            dialog.dismiss()
            EventBus.getDefault().postSticky(EventReloadUnblockData(true))
            reloadData()
        }
        dialog.show()
    }

    private fun showDialogUnblockAlbum() {
        val blockAlbumHelper = AppBlockHelperDB(this)
        val blockAlbumDao = AppBlockDaoBD(
            blockAlbumHelper
        )
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_show_block_folder)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.attributes = wlp
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val btnUnBlockFolder = dialog.findViewById<Button>(R.id.btnUnBlock)
        val tvEmptyBlockFolder = dialog.findViewById<TextView>(R.id.tvEmptyBlock)
        val rvBlockFolder: RecyclerView = dialog.findViewById(R.id.rvBlock)

        tvTitle.text = getString(R.string.blocked_albums)
        btnUnBlockFolder.text = getString(R.string.unblock_album)
        val adapterBlockFolder = AdapterUnblockAlbums(this) { isEnable: Boolean ->
            btnUnBlockFolder.isEnabled = isEnable
            if (isEnable) {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_unlock_folder)
            } else {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_cancel)
            }
        }
        rvBlockFolder.setHasFixedSize(true)
        rvBlockFolder.layoutManager = LinearLayoutManager(this)
        rvBlockFolder.adapter = adapterBlockFolder
        var lstBlock: ArrayList<AlbumItem> = blockAlbumDao.listBlockAlbum
        if (lstBlock.isEmpty()) {
            tvEmptyBlockFolder.visibility = View.VISIBLE
        } else {
            adapterBlockFolder.setData(lstBlock)
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnUnBlockFolder.setOnClickListener {
            blockAlbumDao.deleteBlockAlbum(adapterBlockFolder.listDelete)
            dialog.dismiss()
            EventBus.getDefault().postSticky(EventReloadUnblockData(true))
            reloadData()
        }
        dialog.show()
    }

    private fun showDialogUnblockArtist() {
        val blockAlbumHelper = AppBlockHelperDB(this)
        val blockAlbumDao = AppBlockDaoBD(
            blockAlbumHelper
        )
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_show_block_folder)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.attributes = wlp
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val btnUnBlockFolder = dialog.findViewById<Button>(R.id.btnUnBlock)
        val tvEmptyBlockFolder = dialog.findViewById<TextView>(R.id.tvEmptyBlock)
        val rvBlockFolder: RecyclerView = dialog.findViewById(R.id.rvBlock)

        tvTitle.text = getString(R.string.blocked_artist)
        btnUnBlockFolder.text = getString(R.string.unblock_artist)
        val adapterBlockFolder = AdapterUnblockArtist(this) { isEnable: Boolean ->
            btnUnBlockFolder.isEnabled = isEnable
            if (isEnable) {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_unlock_folder)
            } else {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_cancel)
            }
        }
        rvBlockFolder.setHasFixedSize(true)
        rvBlockFolder.layoutManager = LinearLayoutManager(this)
        rvBlockFolder.adapter = adapterBlockFolder
        val lstBlock: ArrayList<ArtistItem> = blockAlbumDao.listBlockArtist
        if (lstBlock.isEmpty()) {
            tvEmptyBlockFolder.visibility = View.VISIBLE
        } else {
            adapterBlockFolder.setData(lstBlock)
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnUnBlockFolder.setOnClickListener {
            blockAlbumDao.deleteBlockArtist(adapterBlockFolder.listDelete)
            dialog.dismiss()
            EventBus.getDefault().postSticky(EventReloadUnblockData(true))
            reloadData()
        }
        dialog.show()
    }


    private fun loadCountData() {
        val songLoader = SongLoaderAsyncTask(this, null)
        countSong = songLoader.getCount()
        binding.menuView.tvCountSong.text =
            countSong.toString() + "\n" + getString(R.string.title_songs)
        val albumLoader = AlbumLoaderTask(this, null)
        countAlbum = albumLoader.getCountAlbum()
        binding.menuView.tvCountAlbum.text =
            countAlbum.toString() + "\n" + getString(R.string.title_album)
        val artistLoader = ArtistLoaderTask(this, null)
        countArtist = artistLoader.getCountArtist()
        binding.menuView.tvCountArtist.text =
            countArtist.toString() + "\n" + getString(R.string.title_artists)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: EventReloadUnblockData) {
        loadCountData()
        reloadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteChange(item: EventRefreshDataWhenDelete) {
        loadCountData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: EventDeleteSong) {
        loadCountData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun changeTheme(event: EventChangeTheme) {
        initThemeStyle(
            binding.contentMain.supportThemes.imgTheme,
            binding.contentMain.supportThemes.blackTspView
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun changeTheme(event: EventRefreshData) {
        loadCountData()
        reloadData()
    }


}
