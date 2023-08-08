package com.musicplayer.mp3player.playermusic.eventbus

import com.musicplayer.mp3player.playermusic.model.MusicItem

data class BusRenameEvent(val oldSong: MusicItem, val newSong: MusicItem) {}