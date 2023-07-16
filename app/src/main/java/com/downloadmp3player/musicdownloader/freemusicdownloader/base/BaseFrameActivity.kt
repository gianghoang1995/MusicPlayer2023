package com.downloadmp3player.musicdownloader.freemusicdownloader.base

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseFrameActivity<VB : ViewBinding> : AppCompatActivity(),
    FrameView<VB> {

    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingProvider(layoutInflater)
        setContentView(binding.root)
        binding.initView()
    }

    override fun getResources(): Resources {
        return super.getResources()
    }

    abstract fun bindingProvider(inflater: LayoutInflater): VB
}