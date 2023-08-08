package com.musicplayer.mp3player.playermusic.base

import androidx.viewbinding.ViewBinding

interface FrameView<VB : ViewBinding> {
    /*Init View*/
    fun VB.initView()
}