package com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem

data class EventRenameEvent(val oldSong: MusicItem, val newSong: MusicItem) {}