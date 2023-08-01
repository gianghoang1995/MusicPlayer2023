package com.downloadmp3player.musicdownloader.freemusicdownloader.base

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseFrameActivity<VB : ViewBinding> : AppCompatActivity(),
    FrameView<VB> {

    lateinit var binding: VB
    private var mRootView: ViewGroup? = null
    private var mIsFirstLoad = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!mIsFirstLoad) {
            mIsFirstLoad = true
            binding = bindingProvider(layoutInflater)
            setContentView(binding.root)
            binding.initView()
        }
    }

    override fun getResources(): Resources {
        return super.getResources()
    }

    abstract fun bindingProvider(inflater: LayoutInflater): VB
}