package com.musicplayer.mp3player.playermusic.ui.activity.ringtone.child

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.mp3player.playermusic.adapter.AdapterRingtoneRV
import com.musicplayer.mp3player.playermusic.base.BaseFragment
import com.musicplayer.mp3player.playermusic.equalizer.databinding.FragmentNewRingtoneBinding
import com.musicplayer.mp3player.playermusic.eventbus.BusChangeTabRingtone
import com.musicplayer.mp3player.playermusic.eventbus.BusDeleteSong
import com.musicplayer.mp3player.playermusic.eventbus.BusRingtoneCreate
import com.musicplayer.mp3player.playermusic.callback.SongLoaderCallback
import com.musicplayer.mp3player.playermusic.loader.FolderLoader
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.ui.dialog.DialogMoreRingtoneBottomSheet
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.FileProvider
import com.musicplayer.mp3player.playermusic.utils.SortOrder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class RingtoneManagerFrg : BaseFragment<FragmentNewRingtoneBinding>(), SongLoaderCallback,
    AdapterRingtoneRV.OnRingtoneItemClickListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener {
    var ringtoneAdapter: AdapterRingtoneRV? = null
    lateinit var songLoader: FolderLoader
    var lstAudio = ArrayList<MusicItem>()
    var audioManager: AudioManager? = null
    private var pathFolder = ""
    var mediaPlayer: MediaPlayer? = null
    var isAudioFocus = false
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (isAudioFocus) {
                    if (!isPlaying()) {
                        isAudioFocus = false
                        mediaPlayer?.start()
                    }
                }
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                /** App sẽ mất audio focus một lúc. Chúng ta nên pause lại, cho đến khi app nhận được AudioManager.AUDIOFOCUS_GAIN. */
                if (isPlaying()) {
                    isAudioFocus = true
                    mediaPlayer?.stop()
                    ringtoneAdapter?.setIndexNowPlaying(-1)
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (isPlaying()) {
                    isAudioFocus = true
                    mediaPlayer?.stop()
                    ringtoneAdapter?.setIndexNowPlaying(-1)
                }
            }

            else -> {
                mediaPlayer?.stop()
                ringtoneAdapter?.setIndexNowPlaying(-1)
            }
        }
    }

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewRingtoneBinding {
        return FragmentNewRingtoneBinding.inflate(inflater)
    }

    override fun FragmentNewRingtoneBinding.initView() {
        init()
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
    }

    fun init() {
        pathFolder = AppUtils.getRingtoneStorageDir().path
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        ringtoneAdapter = AdapterRingtoneRV(requireContext(), this)
        binding.rvNewRingtone.setHasFixedSize(true)
        binding.rvNewRingtone.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNewRingtone.adapter = ringtoneAdapter
        loader()
    }

    fun setDataSource(song: MusicItem?) {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener(this)
        mediaPlayer?.setOnErrorListener(this)
        mediaPlayer?.setOnPreparedListener(this)
        if (song != null) {
            val path = song.songPath
            val file = File(path)
            if (file.exists()) {
                try {
                    if (reqAudioFocus()) {
                        mediaPlayer?.setDataSource(path)
                        mediaPlayer?.prepareAsync()
                    }
                } catch (ex: java.lang.Exception) {
                }
            }
        }
    }

    private fun isPlaying(): Boolean {
        return if (mediaPlayer != null) {
            mediaPlayer!!.isPlaying
        } else {
            false
        }
    }

    private fun reqAudioFocus(): Boolean {
        val gotFocus: Boolean
        val audioFocus = audioManager!!.requestAudioFocus(
            this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )
        gotFocus = audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return gotFocus
    }

    private fun releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ringtoneAdapter?.setIndexNowPlaying(-1)
    }

    fun loader() {
        songLoader = FolderLoader(requireContext(), pathFolder, this)
        songLoader.setSortOder(SortOrder.SongSortOrder.SONG_A_Z)
        songLoader.loadInBackground()
    }

    override fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>) {
        lstAudio.clear()
        lstAudio.addAll(songList)
        ringtoneAdapter?.setIsShowMore(true)
        ringtoneAdapter?.setData(lstAudio)

        if (lstAudio.isNotEmpty()) {
            binding.layoutEmpty.tvEmpty.visibility = View.GONE
        } else {
            binding.layoutEmpty.tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onItemRingtoneClick(pos: Int, song: MusicItem) {
        setDataSource(song)
        ringtoneAdapter?.setIndexNowPlaying(pos)
    }

    override fun onItemMoreRingtoneClick(song: MusicItem, pos: Int) {
        val bottomSheet = DialogMoreRingtoneBottomSheet(object :
            DialogMoreRingtoneBottomSheet.OnDialogMoreRingtoneListener {
            override fun onSetRingTone() {
                setRingtoneHasCut(song)
            }

            override fun onDeleteRingtone() {
                onRequestDeleteFile(song)
            }

            override fun onShareRingtone() {
                song.songPath?.let { it1 ->
                    val fileProvider = FileProvider()
                    fileProvider.share(requireContext(), it1)
                }
            }
        })
        bottomSheet.show(parentFragmentManager, "more_ringtone")
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer = mp
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnCompletionListener(this)
        mediaPlayer?.setOnErrorListener(this)
        togglePlay()
    }

    private fun togglePlay() {
        if (isPlaying()) {
            mediaPlayer?.pause()
        } else {
            if (reqAudioFocus()) {
                mediaPlayer?.start()
            }
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        releaseMediaPlayer()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: BusDeleteSong) {
        ringtoneAdapter?.removeItem(item.song)
        lstAudio.remove(item.song)
        if (lstAudio.isNotEmpty()) {
            binding.layoutEmpty.tvEmpty.visibility = View.GONE
        } else {
            binding.layoutEmpty.tvEmpty.visibility = View.VISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNewRingtoneCreate(item: BusRingtoneCreate) {
        if (item.isCreate) {
            loader()
            EventBus.getDefault().postSticky(BusRingtoneCreate(false))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onPageChange(event: BusChangeTabRingtone) {
        if (event.isChange) {
            releaseMediaPlayer()
        }
    }
}