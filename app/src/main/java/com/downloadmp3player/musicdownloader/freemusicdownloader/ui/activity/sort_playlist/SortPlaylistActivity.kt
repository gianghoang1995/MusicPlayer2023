package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.sort_playlist

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.DragSortRecycler
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.SortPlaylistAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshDataPlaylist
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivitySortPlaylistBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.PlaylistITem
import org.greenrobot.eventbus.EventBus
import java.util.*

class SortPlaylistActivity : BaseActivity<ActivitySortPlaylistBinding>() {
    private var listPlaylist: ArrayList<PlaylistITem> = ArrayList()
    lateinit var favoriteDao: FavoriteDaoDB
    lateinit var favoriteSqliteHelper: FavoriteSqliteHelperDB
    lateinit var playlistAdapter: SortPlaylistAdapter

    override fun bindingProvider(inflater: LayoutInflater): ActivitySortPlaylistBinding {
        return ActivitySortPlaylistBinding.inflate(inflater)
    }

    override fun ActivitySortPlaylistBinding.initView() {
        initBannerAds(binding.frameBannerAds)
        init()
    }

    fun init() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        favoriteSqliteHelper =
            FavoriteSqliteHelperDB(
                this
            )
        favoriteDao =
            FavoriteDaoDB(
                this,
                favoriteSqliteHelper
            )
        playlistAdapter = SortPlaylistAdapter(this)
        binding.rvSortPlaylist.setHasFixedSize(true)
        binding.rvSortPlaylist.layoutManager = LinearLayoutManager(this)
        binding.rvSortPlaylist.adapter = playlistAdapter

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        listPlaylist = favoriteDao.allFavorite
        playlistAdapter.setDataPlaylist(listPlaylist)
        val dragSortRecycler = DragSortRecycler()
        dragSortRecycler.setViewHandleId(R.id.btnDrag) //View you wish to use as the handle
        dragSortRecycler.setOnItemMovedListener { from, to ->
            moveSongItem(from, to)
        }

        binding.rvSortPlaylist.addItemDecoration(dragSortRecycler)
        binding.rvSortPlaylist.addOnItemTouchListener(dragSortRecycler)
        binding.rvSortPlaylist.setOnScrollListener(dragSortRecycler.scrollListener)
        onClick()
    }

    fun onClick() {
        binding.btnSortDone.setOnClickListener {
            for (i in 0 until listPlaylist.size) {
                var item = listPlaylist[i]
                item.id = i
                favoriteDao.updateFavoriteID(item)
            }
            EventBus.getDefault().postSticky(EventRefreshDataPlaylist(true))
            finish()
        }
    }

    private fun moveSongItem(lastPos: Int, moveToPos: Int) {
        val moveItem: PlaylistITem = listPlaylist[lastPos]
        listPlaylist.removeAt(lastPos)
        listPlaylist.add(moveToPos, moveItem)
        playlistAdapter.setDataPlaylist(listPlaylist)
    }
}