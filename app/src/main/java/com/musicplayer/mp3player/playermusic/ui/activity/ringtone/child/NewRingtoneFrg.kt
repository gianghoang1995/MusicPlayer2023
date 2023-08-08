package com.musicplayer.mp3player.playermusic.ui.activity.ringtone.child

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.mp3player.playermusic.adapter.AdapterRingtoneRV
import com.musicplayer.mp3player.playermusic.base.BaseFragment
import com.musicplayer.mp3player.playermusic.equalizer.databinding.FragmentNewRingtoneBinding
import com.musicplayer.mp3player.playermusic.callback.SongLoaderCallback
import com.musicplayer.mp3player.playermusic.loader.SongLoaderAsync
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.utils.SortOrder
import java.util.*

class NewRingtoneFrg : BaseFragment<FragmentNewRingtoneBinding>(), SongLoaderCallback, AdapterRingtoneRV.OnRingtoneItemClickListener {
    lateinit var ringtoneAdapter: AdapterRingtoneRV
    lateinit var songLoader: SongLoaderAsync

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
        ringtoneAdapter = AdapterRingtoneRV(requireContext(), this)
        binding.rvNewRingtone.setHasFixedSize(true)
        binding.rvNewRingtone.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNewRingtone.adapter = ringtoneAdapter
        loader()
    }

    fun loader() {
        songLoader = SongLoaderAsync(requireContext(), this)
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