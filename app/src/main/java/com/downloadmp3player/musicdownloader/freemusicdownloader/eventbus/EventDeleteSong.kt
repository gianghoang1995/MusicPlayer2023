package com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem

data class EventDeleteSong(var song: MusicItem) {
}