package com.musicplayer.mp3player.playermusic.ui.dialog.moresong

interface DialogMoreSongCallback {
    fun onNextTrack()
    fun onAddToPlaylist()
    fun onSetRingtone()
    fun onDetail()
    fun onShare()
    fun onDelete()
    fun onDeleteSongPlaylist()
}