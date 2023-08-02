package com.musicplayer.mp3player.defaultmusicplayer.eventbus

import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem

data class EventDeleteSong(var song: MusicItem) {
}