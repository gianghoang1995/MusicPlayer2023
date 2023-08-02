package com.musicplayer.mp3player.defaultmusicplayer.base

import androidx.viewbinding.ViewBinding

interface FrameView<VB : ViewBinding> {
    /*Init View*/
    fun VB.initView()
}