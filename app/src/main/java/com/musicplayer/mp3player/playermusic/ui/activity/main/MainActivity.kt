package com.musicplayer.mp3player.playermusic.ui.activity.main

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
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.adapter.PagerAdapter
import com.musicplayer.mp3player.playermusic.adapter.SettingAdapterRV
import com.musicplayer.mp3player.playermusic.callback.OnClickSettingListener
import com.musicplayer.mp3player.playermusic.base.BaseActivity
import com.musicplayer.mp3player.playermusic.BaseApplication
import com.musicplayer.mp3player.playermusic.callback.OnBinderServiceConnection
import com.musicplayer.mp3player.playermusic.database.block.AppBlockDaoBD
import com.musicplayer.mp3player.playermusic.database.block.AppBlockHelperDB
import com.musicplayer.mp3player.playermusic.equalizer.databinding.LayoutMainActivityBinding
import com.musicplayer.mp3player.playermusic.eventbus.*
import com.musicplayer.mp3player.playermusic.loader.AlbumLoader
import com.musicplayer.mp3player.playermusic.loader.ArtistLoader
import com.musicplayer.mp3player.playermusic.loader.SongLoaderAsync
import com.musicplayer.mp3player.playermusic.model.AlbumItem
import com.musicplayer.mp3player.playermusic.model.ArtistItem
import com.musicplayer.mp3player.playermusic.model.FolderItem
import com.musicplayer.mp3player.playermusic.ui.activity.TutorialActivity
import com.musicplayer.mp3player.playermusic.ui.activity.equalizer.EqualizerActivity
import com.musicplayer.mp3player.playermusic.ui.activity.language.SelectLanguageActivity
import com.musicplayer.mp3player.playermusic.ui.activity.ringtone.RingtoneMakerActivity
import com.musicplayer.mp3player.playermusic.ui.activity.scaningmusic.ScanMusicActivity
import com.musicplayer.mp3player.playermusic.ui.activity.search.SearchActivity
import com.musicplayer.mp3player.playermusic.ui.activity.setting.SettingActivity
import com.musicplayer.mp3player.playermusic.ui.activity.theme.CustomThemesActivity
import com.musicplayer.mp3player.playermusic.ui.dialog.timer.DialogTimePicker
import com.musicplayer.mp3player.playermusic.ui.dialog.timer.DialogTimePickerCallback
import com.musicplayer.mp3player.playermusic.ui.fragment.main.album.AlbumsFragment
import com.musicplayer.mp3player.playermusic.ui.fragment.main.album.adapter.AdapterUnblockAlbums
import com.musicplayer.mp3player.playermusic.ui.fragment.main.artist.ArtistFragment
import com.musicplayer.mp3player.playermusic.ui.fragment.main.artist.adapter.AdapterUnblockArtist
import com.musicplayer.mp3player.playermusic.ui.fragment.main.playlist.PlaylistFragment
import com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.QueryFolderFragment
import com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.adapter.AdapterUnblockFolder
import com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.db.block.BlockFolderDao
import com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.db.block.BlockFolderHelper
import com.musicplayer.mp3player.playermusic.ui.fragment.main.song.SongFragmentCallBack
import com.musicplayer.mp3player.playermusic.utils.*
import com.utils.adsloader.InterstitialPreloadAdManager
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

    var dialogRating: Dialog? = null
    lateinit var pagerAdapter: PagerAdapter
    lateinit var fragmentFolder: QueryFolderFragment
    lateinit var fragmentAlbum: AlbumsFragment
    lateinit var fragmentArtist: ArtistFragment
    lateinit var fragmentSong: SongFragmentCallBack
    lateinit var fragmentPlaylist: PlaylistFragment
    lateinit var settingAdapterRV: SettingAdapterRV
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
            BaseApplication.mInstance?.adsFullPlash?.showAds(this,
                object : InterstitialPreloadAdManager.InterstitialAdListener {
                    override fun onClose() {

                    }

                    override fun onError() {
                    }
                })
        }
        AppUtils.checkPermissionFux(this)
    }

    override fun LayoutMainActivityBinding.initView() {
        initBannerAds(binding.contentMain.frameBannerAds, true)
        setBindListener(this@MainActivity)
        settingAdapterRV = SettingAdapterRV(this@MainActivity, this@MainActivity)
        settingAdapterRV.initList()
        binding.menuView.rvMenu.setHasFixedSize(true)
        binding.menuView.rvMenu.layoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        binding.menuView.rvMenu.adapter = settingAdapterRV

        pagerAdapter = PagerAdapter(supportFragmentManager)
        val folderPath = intent.getStringExtra(AppConstants.SHORTCUT_FOLDER_PATH)
        binding.contentMain.viewPager.removeAllViews()
        fragmentFolder = if (!TextUtils.isEmpty(folderPath)) QueryFolderFragment(
            folderPath
        )
        else QueryFolderFragment()
        fragmentAlbum = AlbumsFragment()
        fragmentArtist = ArtistFragment()
        fragmentSong = SongFragmentCallBack()
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
                finishAffinity()
            }
        } else {
            finishAffinity()
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
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                    })
            }

            1 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this, SelectLanguageActivity::class.java))
                        finish()
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(this, SelectLanguageActivity::class.java))
                        finish()
                    })
            }

            2 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this, RingtoneMakerActivity::class.java))
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(this, RingtoneMakerActivity::class.java))
                    })
            }

            3 -> {
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
                        dialogLoadingAds?.dismissDialog()
                        DialogTimePicker.showDialogTimePicker(
                            this@MainActivity,
                            object : DialogTimePickerCallback {
                                override fun onPickerTime(time: String) {

                                }
                            })
                    })
            }

            4 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, ScanMusicActivity::class.java))
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(this@MainActivity, ScanMusicActivity::class.java))
                    })
            }

            5 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, CustomThemesActivity::class.java))
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(
                            Intent(
                                this@MainActivity,
                                CustomThemesActivity::class.java
                            )
                        )
                    })
            }

            6 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, EqualizerActivity::class.java))
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(this@MainActivity, EqualizerActivity::class.java))
                    })
            }

            7 -> {/*Thư mục ẩn*/
                showDialogUnblockFolder()
            }

            8 -> {/*Album ẩn*/
                showDialogUnblockAlbum()
            }

            9 -> {/*Artist ẩn*/
                showDialogUnblockArtist()
            }

            10 -> {
                dialogLoadingAds?.showDialogLoading()
                BaseApplication.getAppInstance().adsFullOptionMenu?.showAds(this,
                    onLoadAdSuccess = {
                        dialogLoadingAds?.dismissDialog()
                    }, onAdClose = {
                        startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                    }, onAdLoadFail = {
                        dialogLoadingAds?.dismissDialog()
                        startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                    })
            }

            11 -> {
                openUrl(AppConstants.POLICY_URL)
            }

            12 -> {
                AppUtils.shareText(
                    this,
                    getString(R.string.subject_share),
                    "${getString(R.string.content_share)} https://play.google.com/store/apps/details?id=${packageName}"
                )
            }

            13 -> {
                openPubGGPlay(AppConstants.PUBLISHER_NAME)
            }

            14 -> {
                rateInStore()
                PreferenceUtils.put(AppConstants.PREF_DONT_SHOW_RATE, true)
            }

            15 -> {
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
            EventBus.getDefault().postSticky(BusReloadUnblockData(true))
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
            EventBus.getDefault().postSticky(BusReloadUnblockData(true))
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
            EventBus.getDefault().postSticky(BusReloadUnblockData(true))
            reloadData()
        }
        dialog.show()
    }


    private fun loadCountData() {
        val songLoader = SongLoaderAsync(this, null)
        countSong = songLoader.getCount()
        binding.menuView.tvCountSong.text =
            countSong.toString() + "\n" + getString(R.string.title_songs)
        val albumLoader = AlbumLoader(this, null)
        countAlbum = albumLoader.getCountAlbum()
        binding.menuView.tvCountAlbum.text =
            countAlbum.toString() + "\n" + getString(R.string.title_album)
        val artistLoader = ArtistLoader(this, null)
        countArtist = artistLoader.getCountArtist()
        binding.menuView.tvCountArtist.text =
            countArtist.toString() + "\n" + getString(R.string.title_artists)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(item: BusReloadUnblockData) {
        loadCountData()
        reloadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteChange(item: BusRefreshDataWhenDelete) {
        loadCountData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: BusDeleteSong) {
        loadCountData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun changeTheme(event: BusChangeTheme) {
        initThemeStyle(
            binding.contentMain.supportThemes.imgTheme,
            binding.contentMain.supportThemes.blackTspView
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun changeTheme(event: BusRefreshData) {
        loadCountData()
        reloadData()
    }


}
