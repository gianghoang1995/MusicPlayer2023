package com.downloadmp3player.musicdownloader.freemusicdownloader.base

import androidx.viewbinding.ViewBinding

interface FrameView<VB : ViewBinding> {
    /*Init View*/
    fun VB.initView()
}