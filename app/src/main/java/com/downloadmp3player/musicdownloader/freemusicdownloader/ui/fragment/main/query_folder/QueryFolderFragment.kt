package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Environment
import android.os.IBinder
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterQueryFolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterQueryFolder.OnQueryFolderClickListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterQuickFolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterQuickFolder.OnQuickFolderClick
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication.Companion.getAppInstance
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentFolderBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventDeleteSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshData
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventReloadUnblockData
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventShowMoreEvent
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.FolderLoaderTrackTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService.MusicServiceBinder
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.addsong.AddSongFavoriteActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer.EqualizerActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.DialogSelectSong.OnDialogSelectSongListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.DialogSelectSong.getDialogAddToPlaylist
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder.loader.ScaningFolderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder.loader.ScanningFolderAsync
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.adapter.AdapterUnblockFolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderDao
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block.BlockFolderHelper
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.pin.PinFolderDao
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.pin.PinFolderHelper
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.FileProvider
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.FileUtils.getExtSdCardPaths
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ShortcutManagerUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

@SuppressLint("NonConstantResourceId")
class QueryFolderFragment : BaseFragment<FragmentFolderBinding>, ScaningFolderListener,
    OnQueryFolderClickListener, ScanningDirectoryListener, OnQuickFolderClick {
    private var shortcutPath: String? = ""
    private var isFirst = true
    private var mLastClickTime: Long = 0
    private val lstPathCheck = ArrayList<String?>()
    private val lstNameCheck = ArrayList<String?>()
    private val lstFolderHasMusic = ArrayList<FolderItem>()
    private val lstAudio = ArrayList<MusicItem>()
    private var adapterRoot: AdapterQueryFolder? = null
    private var mScanningMusicDirectory: ScanningMusicDirectory? = null
    private var mAdapterQuickFolder: AdapterQuickFolder? = null
    private var mFolderLoaderTrack: FolderLoaderTrackTask? = null
    private var mPinFolderDao: PinFolderDao? = null
    private var mBlockFolderDao: BlockFolderDao? = null
    private var listPinFolder = ArrayList<FolderItem>()
    private var mBound = false
    private var currentFolder: FolderItem? = null
    private var needLoaded = false
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as MusicServiceBinder
            musicPlayerService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    constructor(mShortCutPath: String?) {
        shortcutPath = mShortCutPath
        if (shortcutPath != null) Log.e("shortcutPath", shortcutPath!!)
    }

    constructor() {}

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFolderBinding {
        return FragmentFolderBinding.inflate(inflater)
    }

    override fun FragmentFolderBinding.initView() {
        init()
    }

    fun init() {
        val pinFolderHelper = PinFolderHelper(context)
        mPinFolderDao = PinFolderDao(pinFolderHelper)
        val blockFolderHelper = BlockFolderHelper(context)
        mBlockFolderDao = BlockFolderDao(blockFolderHelper)
        bindService()
        adapterRoot = AdapterQueryFolder(context, this)
        binding.rvFolder.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvFolder.setHasFixedSize(true)
        binding.rvFolder.adapter = adapterRoot
        adapterRoot!!.setShowMoreFolder(false)
        reloadDbPin()
        mAdapterQuickFolder = AdapterQuickFolder(context, this)
        binding.rvQuickFolder.setHasFixedSize(true)
        binding.rvQuickFolder.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvQuickFolder.adapter = mAdapterQuickFolder
        loadFirst()
    }

    fun loadFirst() {
        val file: File
        if (isFirst && !shortcutPath!!.isEmpty()) {
            file = File(shortcutPath)
            currentFolder = FolderItem(file.name, 0, shortcutPath, 0)
        } else {
            file = File(DEFAULT_PATH)
            currentFolder = FolderItem(file.name, 0, DEFAULT_PATH, 0)
        }
        currentFolder = FolderItem(file.name, 0, DEFAULT_PATH, 0)
        mAdapterQuickFolder!!.clearData()
        mAdapterQuickFolder!!.addData(currentFolder)
        val scanningFolderAsync = ScanningFolderAsync(requireContext(), this)
        scanningFolderAsync.execute()
    }

    fun reloadDbPin() {
        listPinFolder.clear()
        listPinFolder = mPinFolderDao!!.pinFolder
        adapterRoot!!.updatePinFolderStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindServicePlayMusic()
    }


    private fun bindService() {
        val intent = Intent(context, MusicPlayerService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindServicePlayMusic() {
        if (mBound) {
            requireActivity().unbindService(connection)
        }
    }

    fun showPopupFolder(folder: FolderItem, pos: Int, view: View?) {
        val popup = PopupMenu(requireContext(), requireView())
        popup.menuInflater
            .inflate(R.menu.popup_more_folder, popup.menu)
        if (mPinFolderDao!!.isPinFolder(folder)) {
            popup.menu.findItem(R.id.menuPinItem).title = getString(R.string.unpin)
            popup.menu.findItem(R.id.menuBlockItem).isVisible = false
        } else {
            popup.menu.findItem(R.id.menuBlockItem).isVisible = true
            popup.menu.findItem(R.id.menuPinItem).title = getString(R.string.pin_folder)
        }
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuPlay -> querrySong(folder, R.id.menuPlay)
                R.id.menuPlayNext -> querrySong(folder, R.id.menuPlayNext)
                R.id.menuAddQueue -> querrySong(folder, R.id.menuAddQueue)
                R.id.menuShuffle -> shuffleListSong(folder)
                R.id.menuAddToPlaylist -> querrySong(folder, R.id.menuAddToPlaylist)
                R.id.menuPinItem -> {
                    if (mPinFolderDao!!.isPinFolder(folder)) {
                        mPinFolderDao!!.deletePinFolder(folder)
                    } else {
                        mPinFolderDao!!.insertPinFolder(folder)
                    }
                    reloadDbPin()
                    if (mAdapterQuickFolder!!.itemCount == 1) {
                        mAdapterQuickFolder!!.removeLastItem()
                        loadFirst()
                    }
                }

                R.id.menuBlockItem -> {
                    mBlockFolderDao!!.insertBlockFolder(folder)
                    adapterRoot!!.removeItem(pos)
                    showMessage(getString(R.string.hide_all_song_in_folder))
                    EventBus.getDefault().postSticky(EventReloadUnblockData(true))
                }

                R.id.menuAddToHomeScreen -> ShortcutManagerUtils.createShortcutFolder(
                    context, folder, R.drawable.ic_folder_query
                )
            }
            true
        }
        popup.show()
    }

    private fun addToPlaylist(folder: FolderItem) {
        val i = Intent(context, AddSongFavoriteActivity::class.java)
        i.putExtra(AddSongFavoriteActivity.FOLDER_DATA, folder)
        startActivity(i)
    }

    private fun querrySong(folder: FolderItem, id: Int) {
        mFolderLoaderTrack = FolderLoaderTrackTask(
            context,
            folder.path,
            object : SongLoaderListener {
                override fun onAudioLoadedSuccessful(songList: java.util.ArrayList<MusicItem>) {
                    if (songList.size > 0) {
                        when (id) {
                            R.id.menuPlay -> musicPlayerService!!.setListSong(
                                requireContext(), songList, 0
                            )

                            R.id.menuPlayNext -> musicPlayerService!!.insertListNextTrack(songList)
                            R.id.menuAddQueue -> musicPlayerService!!.addToQueue(songList)
                            R.id.menuAddToPlaylist -> addToPlaylist(folder)
                        }
                    } else {
                        showMessage(getString(R.string.empty_song))
                    }
                }
            })
        mFolderLoaderTrack!!.loadInBackground()
    }

    private fun shuffleListSong(folder: FolderItem) {
        mFolderLoaderTrack = FolderLoaderTrackTask(
            context,
            folder.path,
            object : SongLoaderListener {
                override fun onAudioLoadedSuccessful(songList: java.util.ArrayList<MusicItem>) {
                    val listSong = ArrayList(songList)
                    if (listSong.size > 0) {
                        musicPlayerService!!.shuffleListSong(listSong, requireContext())
                    } else {
                        showMessage(getString(R.string.empty_song))
                    }
                }
            })
        mFolderLoaderTrack!!.loadInBackground()
    }

    @SuppressLint("NonConstantResourceId")
    fun showPopupSongFolder(song: MusicItem, view: View?) {
        val popup = PopupMenu(requireContext(), requireView())
        popup.menuInflater
            .inflate(R.menu.popup_more_song_folder, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuPlayNext -> musicPlayerService!!.insertNextTrack(song)
                R.id.menuAddToPlaylist -> {
                    val lstSong = ArrayList<MusicItem>()
                    lstSong.add(song)
                    showDialogAddSong(lstSong)
                }

                R.id.menuShareSong -> {
                    val fileProvider = FileProvider()
                    if (!TextUtils.isEmpty(song.songPath)) {
                        fileProvider.share(requireContext(), song.songPath!!)
                    } else {
                        showMessage(getString(R.string.song_path_not_exist))
                    }
                }

                R.id.menuSetRingtone -> setRingtone(song)
            }
            true
        }
        popup.show()
    }

    private fun showDialogAddSong(lstSong: ArrayList<MusicItem>) {
        val mDialogSelectSong =
            getDialogAddToPlaylist(requireContext(), lstSong, object : OnDialogSelectSongListener {
                override fun onAddToPlaylistDone() {

                }
            })
        mDialogSelectSong.show()
    }

    override fun onScanningMusicFolderSuccess(result: ArrayList<FolderItem>) {
        if (result.size > 0) {
            adapterRoot!!.setShowMoreFolder(false)
            lstPathCheck.clear()
            lstNameCheck.clear()
            lstFolderHasMusic.clear()
            lstFolderHasMusic.addAll(result)
            for ((name, _, path) in lstFolderHasMusic) {
                lstPathCheck.add(path)
                lstNameCheck.add(name)
            }
            val lstObject = ArrayList<Any>()
            val folder = FolderItem(getString(R.string.internal), 0, ROOT_PATH, 0)
            lstObject.add(folder)
            val sdPath = getExtSdCardPaths(
                requireContext()
            )
            if (!TextUtils.isEmpty(sdPath)) {
                val folderSD = FolderItem(getString(R.string.external), 0, sdPath, 0)
                lstObject.add(folderSD)
            }
            if (mAdapterQuickFolder!!.itemCount == 1) {
                if (listPinFolder.size > 0) {
                    lstObject.addAll(listPinFolder)
                }
            }
            runLayoutAnimation()
            adapterRoot!!.addData(lstObject)
            binding.rvFolder.scrollToPosition(0)
            binding.rvQuickFolder.smoothScrollToPosition(mAdapterQuickFolder!!.itemCount - 1)
            if (isFirst) {
                if (!TextUtils.isEmpty(shortcutPath)) {
                    val folder2 = FolderItem(null, 0, shortcutPath, 0)
                    openShortcutFolder(folder2)
                    isFirst = false
                }
            }
        }
    }

    override fun onClickQuickFolder(position: Int, folder: FolderItem) {
        if (position == 0) {
            loadFirst()
        } else {
            currentFolder = folder
            adapterRoot!!.setShowMoreFolder(position != 0)
            mAdapterQuickFolder!!.removeQuickItem(position)
            if (mScanningMusicDirectory != null) {
                mScanningMusicDirectory!!.cancel(false)
            }
            mScanningMusicDirectory = ScanningMusicDirectory(context, this)
            mScanningMusicDirectory!!.path = folder.path
            mScanningMusicDirectory!!.setListCheck(lstPathCheck, lstNameCheck)
            mScanningMusicDirectory!!.execute()
        }
    }

    override fun onScanningDirectorySuccess(lstFolder: ArrayList<FolderItem>) {
        /** Duyệt folder
         * Check pin Folder ở đây
         * Check list block
         */
        val lstObject = ArrayList<Any>()
        if (mScanningMusicDirectory != null) {
            mScanningMusicDirectory!!.cancel(false)
        }
        if (lstFolder.size > 0) for (i in lstFolder.indices) {
            val folder = lstFolder[i]
            if (!mBlockFolderDao!!.isBlockFolder(folder)) {
                lstObject.add(folder)
            }
        }
        currentFolder!!.path = File(currentFolder!!.path).absolutePath
        if (lstPathCheck.contains(currentFolder!!.path)) {
            for ((_, _, path, parentId) in lstFolderHasMusic) {
                if (path == currentFolder!!.path) {
                    lstAudio.clear()
                    val getSongFolder = GetSongFolder(requireContext())
                    getSongFolder.sort = sortOrder()
                    val id = getSongFolder.getParentID(path!!)
                    Log.e("Query ParentID", "$parentId - $path")
                    Log.e("ParentId", "$id - $path")
                    lstAudio.addAll(getSongFolder.getSongsByParentId(id))
                    lstObject.addAll(lstAudio)
                    break
                }
            }
        }
        if (mAdapterQuickFolder!!.itemCount == 1) {
            if (listPinFolder.size > 0) {
                lstObject.addAll(listPinFolder)
            }
        }
        runLayoutAnimation()
        adapterRoot!!.addData(lstObject)
        binding.rvFolder.scrollToPosition(0)
        binding.rvQuickFolder.smoothScrollToPosition(mAdapterQuickFolder!!.itemCount - 1)
    }

    override fun onClickSubFolder(folder: FolderItem) {
        clickFolder(folder, mPinFolderDao!!.isPinFolder(folder))
    }

    override fun onClickMoreSubFolder(folder: FolderItem, position: Int, view: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            showPopupFolder(folder, position, view)
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onClickMoreSong(song: MusicItem, view: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        } else {
            showPopupSongFolder(song, view)
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onClickSong(song: MusicItem) {
        val index = lstAudio.indexOf(song)
        musicPlayerService!!.setListSong(requireContext(), lstAudio, index)
    }

    fun clickFolder(folder: FolderItem?, isPin: Boolean) {
        currentFolder = folder
        adapterRoot!!.setShowMoreFolder(true)
        if (isPin) mAdapterQuickFolder!!.addFolderPin(folder) else mAdapterQuickFolder!!.addData(
            folder
        )
        scanFolder(folder)
    }

    fun openShortcutFolder(folder: FolderItem) {
        val currentString = folder.path
        val separated =
            currentString!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val listQuick = ArrayList<FolderItem>()
        for (i in 1 until separated.size) {
            val item = separated[i]
            if (!item.contains("emulated")) {
                val path = currentString.split("$item/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0] + item + "/"
                listQuick.add(FolderItem(item, 0, path, 0))
            }
        }
        mAdapterQuickFolder!!.addListData(listQuick)
        currentFolder = folder
        adapterRoot!!.setShowMoreFolder(true)
        scanFolder(folder)
    }

    fun scanFolder(folder: FolderItem?) {
        if (mScanningMusicDirectory != null) {
            mScanningMusicDirectory!!.cancel(false)
        }
        mScanningMusicDirectory = ScanningMusicDirectory(context, this)
        mScanningMusicDirectory!!.path = folder!!.path
        mScanningMusicDirectory!!.setListCheck(lstPathCheck, lstNameCheck)
        mScanningMusicDirectory!!.execute()
    }

    private fun sortOrder(): String {
        return MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC"
    }

    private fun runLayoutAnimation() {
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_album)
        binding.rvFolder.layoutAnimation = controller
        binding.rvFolder.scheduleLayoutAnimation()
    }

    fun canNotExit(): Boolean {
        return if (mAdapterQuickFolder != null) mAdapterQuickFolder!!.itemCount != 1 else {
            true
        }
    }

    fun onBackClick(): Boolean {
        return if (mAdapterQuickFolder != null) {
            if (mAdapterQuickFolder!!.itemCount == 2) {
                mAdapterQuickFolder!!.removeLastItem()
                loadFirst()
            } else {
                val lstQuickFolder = mAdapterQuickFolder!!.removeLastItem()
                currentFolder = lstQuickFolder[lstQuickFolder.size - 1]
                adapterRoot!!.setShowMoreFolder(mAdapterQuickFolder!!.itemCount != 1)
                if (mScanningMusicDirectory != null) {
                    mScanningMusicDirectory!!.cancel(false)
                }
                mScanningMusicDirectory = ScanningMusicDirectory(context, this)
                mScanningMusicDirectory!!.path = currentFolder!!.path
                mScanningMusicDirectory!!.setListCheck(lstPathCheck, lstNameCheck)
                mScanningMusicDirectory!!.execute()
            }
            true
        } else {
            false
        }
    }

    private fun mainShowMenuPopupFolder() {
        val popup = PopupMenu(requireContext(), binding.anchorPopup)
        popup.menuInflater.inflate(R.menu.main_popup_folder, popup.menu)
        popup.menu.findItem(R.id.menuAddToHomeScreen).isVisible =
            mAdapterQuickFolder!!.itemCount > 1
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEqualizer -> getAppInstance().adsFullInApp?.showAds(requireActivity(), {
                    startActivity(Intent(context, EqualizerActivity::class.java))
                }) {
                    startActivity(Intent(context, EqualizerActivity::class.java))
                }

                R.id.menuShowBlockFolder -> showDialogUnblockFolder()
                R.id.menuAddToHomeScreen -> ShortcutManagerUtils.createShortcutFolder(
                    requireContext(),
                    currentFolder,
                    R.drawable.ic_folder_query
                )
            }
            true
        }
        popup.show()
    }

    private fun showDialogUnblockFolder() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_show_block_folder)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.attributes = wlp
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnUnBlockFolder = dialog.findViewById<Button>(R.id.btnUnBlock)
        val tvEmptyBlockFolder = dialog.findViewById<TextView>(R.id.tvEmptyBlock)
        val rvBlockFolder = dialog.findViewById<RecyclerView>(R.id.rvBlock)
        val adapterBlockFolder = AdapterUnblockFolder(context) { isEnable: Boolean ->
            btnUnBlockFolder.isEnabled = isEnable
            if (isEnable) {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_unlock_folder)
            } else {
                btnUnBlockFolder.setBackgroundResource(R.drawable.bg_cancel)
            }
        }
        rvBlockFolder.setHasFixedSize(true)
        rvBlockFolder.layoutManager = LinearLayoutManager(context)
        rvBlockFolder.adapter = adapterBlockFolder
        val lstBlock = mBlockFolderDao!!.allFolderBlock
        if (lstBlock.isEmpty()) {
            tvEmptyBlockFolder.visibility = View.VISIBLE
        } else {
            adapterBlockFolder.setData(lstBlock)
        }
        btnCancel.setOnClickListener { v: View? -> dialog.dismiss() }
        btnUnBlockFolder.setOnClickListener { v: View? ->
            mBlockFolderDao!!.deleteBlockFolder(adapterBlockFolder.listDelete)
            dialog.dismiss()
            if (mAdapterQuickFolder!!.itemCount == 2) {
                scanFolder(currentFolder)
            } else {
                loadFirst()
            }
            EventBus.getDefault().postSticky(EventRefreshData(true))
        }
        dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun refreshBlockData(event: EventReloadUnblockData?) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshData(event: EventRefreshData?) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(event: EventDeleteSong?) {
        needLoaded = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun onDeleteSong(event: EventShowMoreEvent) {
        if (event.pos == 3) {
            mainShowMenuPopupFolder()
        }
    }

    fun reloadDataFolder() {
        if (needLoaded) {
            loadFirst()
            needLoaded = false
        }
    }

    companion object {
        const val DEFAULT_PATH = "/storage/"
        val ROOT_PATH = Environment.getExternalStorageDirectory().absolutePath
    }
}