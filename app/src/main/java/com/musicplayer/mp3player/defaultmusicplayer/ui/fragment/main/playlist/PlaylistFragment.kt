package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.playlist

import android.app.Dialog
import android.content.Intent
import android.os.SystemClock
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.adapter.PlaylistAdapter
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickPlaylistListener
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseFragment
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventDeleteSong
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventRefreshDataPlaylist
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventRefreshDataWhenDelete
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventShowMoreEvent
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteDaoDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.PlaylistSongDaoDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.PlaylistSongSqLiteHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.FragmentPlaylistBinding
import com.musicplayer.mp3player.defaultmusicplayer.handle.SongLoaderListener
import com.musicplayer.mp3player.defaultmusicplayer.loader.SongLoaderAsyncTask
import com.musicplayer.mp3player.defaultmusicplayer.model.PlaylistITem
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.equalizer.EqualizerActivity
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.list.ListSongActivity
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.sort_playlist.SortPlaylistActivity
import com.musicplayer.mp3player.defaultmusicplayer.utils.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PlaylistFragment : BaseFragment<FragmentPlaylistBinding>() {
    var mLastClickTime: Long = 0
    lateinit var favoriteDao: FavoriteDaoDB
    lateinit var favoriteSqliteHelper: FavoriteSqliteHelperDB
    var dialogEditText: Dialog? = null
    lateinit var playlistAdapter: PlaylistAdapter
    var listPlaylist = ArrayList<PlaylistITem>()
    var songListDao: PlaylistSongDaoDB? = null
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistBinding {
        return FragmentPlaylistBinding.inflate(inflater)
    }

    override fun FragmentPlaylistBinding.initView() {
        init()
        updateDataList()
    }

    override fun onResume() {
        updateDataList()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialogEditText != null) {
            if (dialogEditText?.isShowing!!) {
                dialogEditText?.dismiss()
            }
        }
    }

    fun init() {
        favoriteSqliteHelper =
            FavoriteSqliteHelperDB(
                requireContext()
            )
        favoriteDao =
            FavoriteDaoDB(
                requireContext(),
                favoriteSqliteHelper
            )
        playlistAdapter =
            PlaylistAdapter(requireContext(), false, object : OnClickPlaylistListener {
                override fun onPlaylistClick(favorite: PlaylistITem, i: Int) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    } else {
                        val intent = Intent(requireContext(), ListSongActivity::class.java)
                        intent.putExtra(AppConstants.DATA_PLAYLIST, favorite)
                        startActivity(intent)
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                }

                override fun onPlaylistMoreClick(favorite: PlaylistITem, view: View) {
                    showPopupMenu(favorite, view)
                }

            })

        binding.rvFavorite.setHasFixedSize(true)
        binding.rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorite.adapter = playlistAdapter

        binding.btnCreatePlaylist.setOnClickListener {
            showDialogEditText(null)
        }

        binding.swipeToRefreshPlaylist.setOnRefreshListener {
            updateDataList()
        }

        binding.btnSortPlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), SortPlaylistActivity::class.java))
        }
    }

    private fun showDialogEditText(favorite: PlaylistITem?) {
        dialogEditText = Dialog(requireContext())
        dialogEditText?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEditText?.setContentView(R.layout.dialog_editext)
        dialogEditText?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEditText?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val create_Cancel = dialogEditText?.findViewById<View>(R.id.tv_cancel) as Button
        val create_Create = dialogEditText?.findViewById<View>(R.id.tv_create) as Button
        val create_Edittext = dialogEditText?.findViewById<View>(R.id.edt_name) as EditText


        if (favorite != null) {
            create_Create.text = getString(R.string.update)
            create_Edittext.setText(favorite.name)
        }

        create_Create.setOnClickListener {
            val name = create_Edittext.text.trim().toString()
            if (name.isNotEmpty()) {
                if (favorite != null) {
                    var isCont = false
                    for (index in 0 until listPlaylist.size) {
                        if (listPlaylist[index].name.equals(name)) {
                            isCont = true
                            break
                        }
                    }
                    if (!isCont) {
                        if (favoriteDao.updateFavoriteName(favorite.name, name).toInt() == 1) {
                            showMessage(getString(R.string.success))
                            updateDataList()
                            dialogEditText?.dismiss()
                        } else {
                            getString(R.string.error)
                        }
                    } else {
                        showMessage(getString(R.string.duplicate_playlist))
                    }
                } else {
                    if (favoriteDao.insertFavorite(name)) {
                        dialogLoadingAds?.showDialogLoading()
                        BaseApplication.getAppInstance().adsFullInApp?.showAds(requireActivity(),
                            onLoadAdSuccess = {
                                dialogLoadingAds?.dismissDialog()
                            }, onAdClose = {
                                showMessage(getString(R.string.success))
                                updateDataList()
                                dialogEditText?.dismiss()
                            }, onAdLoadFail = {
                                dialogLoadingAds?.dismissDialog()
                                showMessage(getString(R.string.success))
                                updateDataList()
                                dialogEditText?.dismiss()
                            })
                    } else {
                        getString(R.string.duplicate_playlist)
                    }
                }
            } else {
                showMessage(getString(R.string.empty_playlist))
            }
        }

        create_Cancel.setOnClickListener {
            dialogEditText?.dismiss()
        }
        create_Edittext.requestFocus()
        dialogEditText?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogEditText?.show()
    }

    private fun updateDataList() {
        listPlaylist = favoriteDao.allFavorite
        playlistAdapter.setDataPlaylist(listPlaylist)
        binding.swipeToRefreshPlaylist.isRefreshing = false
    }

    fun showPopupMenu(favorite: PlaylistITem, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.popup_more_playlist, popup.menu)
        if (favorite.name.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING) || favorite.name.equals(
                FavoriteSqliteHelperDB.DEFAULT_FAVORITE
            ) || favorite.name.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING) || favorite.name.equals(
                FavoriteSqliteHelperDB.TABLE_RECENT_ADDED
            )
        ) {
            popup.menu.findItem(R.id.menuDelete).isVisible = false
            popup.menu.findItem(R.id.menuRename).isVisible = false
        }
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuPlay -> {
                    queryMusic(favorite, R.id.menuPlay)
                }

                R.id.menuPlayNext -> {
                    queryMusic(favorite, R.id.menuPlayNext)
                }

                R.id.menuAddToQueue -> {
                    queryMusic(favorite, R.id.menuAddToQueue)
                }

                R.id.menuShuffle -> {
                    queryMusic(favorite, R.id.menuShuffle)
                }

                R.id.menuRename -> {
                    showDialogEditText(favorite)
                }

                R.id.menuDelete -> {
                    ShortcutManagerUtils.delShortcut(requireContext(), favorite)
                    if (favoriteDao.deleteFavorite(favorite.name).toInt() != -1) {
                        updateDataList()
                        getString(R.string.success)
                    } else {
                        getString(R.string.error)
                    }
                }

                R.id.menuAddToHomeScreen -> {
                    ShortcutManagerUtils.createShortcutPlaylist(requireContext(), favorite)
                }
            }
            true
        }
        popup.show()
    }

    private fun queryMusic(playlist: PlaylistITem, id: Int) {
        val listSong = ArrayList<MusicItem>()
        songListSqliteHelper =
            PlaylistSongSqLiteHelperDB(
                requireContext(),
                playlist.favorite_id
            )
        songListDao =
            PlaylistSongDaoDB(
                songListSqliteHelper
            )
        if (playlist.name.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED)) {
            val songLoader = SongLoaderAsyncTask(requireContext(), object : SongLoaderListener {
                override fun onAudioLoadedSuccessful(songList: java.util.ArrayList<MusicItem>) {
                    onLoadDataDone(songList, id)
                }
            })
            songLoader.filterDateAdded()
            songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
            songLoader.loadInBackground()
        } else {
            if (playlist.name.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING)) {
                songListDao?.setQueryMostPlaying()
            }
            songListDao?.allSongLocalFromPlaylist?.let { listSong.addAll(it) }
            onLoadDataDone(listSong, id)
        }
    }

    fun onLoadDataDone(listSong: ArrayList<MusicItem>, id: Int) {
        if (listSong.size > 0) {
            when (id) {
                R.id.menuPlay -> {
                    requireContext().let { musicPlayerService?.setListSong(it, listSong, 0) }
                }

                R.id.menuPlayNext -> {
                    requireContext().let { musicPlayerService?.insertListNextTrack(listSong) }
                }

                R.id.menuAddToQueue -> {
                    requireContext().let { musicPlayerService?.addToQueue(listSong) }
                }

                R.id.menuShuffle -> {
                    requireContext().let {
                        musicPlayerService?.shuffleListSong(
                            listSong,
                            requireContext()
                        )
                    }
                }
            }
        } else {
            showMessage(getString(R.string.empty_song))
        }
    }


    private fun showPopupMainPlaylist() {
        val popup = PopupMenu(requireContext(), requireView())
        popup.menuInflater.inflate(R.menu.popup_playlist_main, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEqualizer -> {
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

                R.id.menuSort -> {
                    startActivity(Intent(requireContext(), SortPlaylistActivity::class.java))
                }
            }
            true
        }
        popup.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun onShowMoreEvent(item: EventShowMoreEvent) {
        if (item.pos == 4) showPopupMainPlaylist()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshDataPlaylist(event: EventRefreshDataPlaylist) {
        if (event.isNewPlaylist) {
            updateDataList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(event: EventDeleteSong) {
        for (item in 0 until listPlaylist.size) {
            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    requireContext(),
                    listPlaylist[item].favorite_id
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            songListDao?.deleteLocalSong(event.song)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteListSong(item: EventRefreshDataWhenDelete) {
        updateDataList()
    }
}
