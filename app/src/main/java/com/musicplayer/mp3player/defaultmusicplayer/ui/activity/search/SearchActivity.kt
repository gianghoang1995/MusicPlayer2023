package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.search

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.adapter.SongAdapter
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickSongListener
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseActivity
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.ActivitySearchBinding
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventDeleteSong
import com.musicplayer.mp3player.defaultmusicplayer.handle.OnBinderServiceConnection
import com.musicplayer.mp3player.defaultmusicplayer.handle.OnSearchAudioListener
import com.musicplayer.mp3player.defaultmusicplayer.loader.SearchLoaderTask
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline
import com.musicplayer.mp3player.defaultmusicplayer.ui.dialog.PopupMoreSongUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.Keyboard
import com.musicplayer.mp3player.defaultmusicplayer.utils.SortOrder
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchActivity : BaseActivity<ActivitySearchBinding>(), OnSearchAudioListener,
    OnClickSongListener,
    OnBinderServiceConnection {

    override fun onBindServiceMusicSuccess() {
    }

    override fun onServiceDisconnection() {
    }

    var searchLoader: SearchLoaderTask? = null
    var thread = Thread()
    var searchAdapter: SongAdapter? = null
    var listSong: ArrayList<MusicItem> = ArrayList()

    override fun bindingProvider(inflater: LayoutInflater): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(inflater)
    }

    override fun ActivitySearchBinding.initView() {
        init()
        onClick()
    }

    fun init() {
        setBindListener(this)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        searchLoader = SearchLoaderTask(this, this)
        searchLoader?.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        searchLoader?.setSearchName(binding.edtSearch.text.toString().trim { it <= ' ' })
        initAdapterRv()
        binding.btnBack.setOnClickListener {
            super.onBackPressed()
        }

        binding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (binding.edtSearch.text?.isNotEmpty() == true) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    Keyboard.closeKeyboard(binding.edtSearch)
                    true
                } else false
            } else {
                getString(R.string.input_name_song)
                true
            }
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    thread.interrupt()
                    thread = Thread {
                        searchLoader?.setSearchName(s.toString().trim())
                        searchLoader?.loadInBackground()
                    }
                    thread.start()
                } else {
                    onSearchAudioSuccessful(ArrayList())
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        binding.edtSearch.requestFocus()
    }

    private fun initAdapterRv() {
        searchAdapter = SongAdapter(this, this, songIndexListener = {})
        binding.rvSearchSongs.setHasFixedSize(true)
        binding.rvSearchSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSearchSongs.adapter = searchAdapter
    }

    fun onClick() {
        binding.btnCloseSearch.setOnClickListener {
            binding.edtSearch.setText("")
            binding.searchViewSong.visibility = View.GONE
            onSearchAudioSuccessful(ArrayList())
        }
    }

    fun checkEmpty() {
        if (listSong.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }
    }

    override fun onSearchAudioSuccessful(songList: ArrayList<MusicItem>) {
        runOnUiThread {
            listSong.clear()
            listSong.addAll(songList)
            searchAdapter?.setDataSong(songList)
            if (listSong.isNotEmpty()) {
                binding.searchViewSong.visibility = View.VISIBLE
            } else {
                binding.searchViewSong.visibility = View.GONE
            }
        }
    }

    override fun onSongClick(song: MusicItem, i: Int) {
        musicPlayerService?.setListSong(this@SearchActivity, listSong, i)
        finish()
    }

    override fun onSongMoreClick(item: Any, i: Int, view: View) {
        if (item is MusicItem) {
            Keyboard.closeKeyboard(binding.edtSearch)
            val popupMoreSongUtils = PopupMoreSongUtils(
                this,
                musicPlayerService,
                object : PopupMoreSongUtils.PopUpMoreSongListener {
                    override fun onRenameSuccess(songResult: MusicItem) {
                        searchAdapter?.updateItem(i, songResult)
                    }

                    override fun onDeleteSuccess(songResult: MusicItem) {
                        searchAdapter?.removeItem(songResult)
                    }

                    override fun onSetRingTone(song: MusicItem) {
                        setRingoneFunction(song)
                    }
                }, this
            )
            popupMoreSongUtils.showPopupMoreSong(item, i, view)
        }
    }

    override fun onSongOnLongClick() {

    }

    override fun onSizeSelectChange(size: Int) {

    }

    override fun onClickItemOnline(itemAudioOnline: ItemMusicOnline) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: EventDeleteSong) {
        listSong.remove(item.song)
        searchAdapter?.removeItem(item.song)
    }

}
