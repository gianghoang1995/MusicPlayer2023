package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.ringtone.child

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterRingtone
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentNewRingtoneBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.SongLoaderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.loader.SongLoaderAsyncTask
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.SortOrder
import java.util.*

class NewRingtoneFrg : BaseFragment<FragmentNewRingtoneBinding>(), SongLoaderListener, AdapterRingtone.OnRingtoneItemClickListener {
    lateinit var ringtoneAdapter: AdapterRingtone
    lateinit var songLoader: SongLoaderAsyncTask

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewRingtoneBinding {
        return FragmentNewRingtoneBinding.inflate(inflater)
    }

    override fun FragmentNewRingtoneBinding.initView() {
        init()
    }

    fun init() {
        ringtoneAdapter = AdapterRingtone(requireContext(), this)
        binding.rvNewRingtone.setHasFixedSize(true)
        binding.rvNewRingtone.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNewRingtone.adapter = ringtoneAdapter
        loader()
    }

    fun loader() {
        songLoader = SongLoaderAsyncTask(requireContext(), this)
        songLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z)
        songLoader.loadInBackground()
    }

    override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
        ringtoneAdapter.setIsShowMore(false)
        ringtoneAdapter.setData(songList)

        if (songList.isNotEmpty()) {
            binding.layoutEmpty.tvEmpty.visibility = View.GONE
        } else {
            binding.layoutEmpty.tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onItemRingtoneClick(pos: Int, song: MusicItem) {
        setRingtone(song)
    }

    override fun onItemMoreRingtoneClick(song: MusicItem, pos: Int) {

    }

}