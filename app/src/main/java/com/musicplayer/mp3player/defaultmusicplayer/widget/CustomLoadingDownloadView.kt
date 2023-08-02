package com.musicplayer.mp3player.defaultmusicplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.LayoutLoadingDownloadBinding

class CustomLoadingDownloadView : RelativeLayout {
    var binding: LayoutLoadingDownloadBinding = LayoutLoadingDownloadBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    var listener: OnDownloadButtonClickListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
    }


    fun showLoading() {
        binding.successDownloadView.isVisible = false
        binding.loadingDownload.isVisible = true
        binding.btnDownload.isVisible = false
        binding.loadingDownload.playAnimation()
    }

    fun cancelLoading() {
        binding.loadingDownload.cancelAnimation()
        binding.successDownloadView.isVisible = false
        binding.loadingDownload.isVisible = false
        binding.btnDownload.isVisible = true
    }

    fun successDownload() {
        binding.btnDownload.isVisible = false
        binding.loadingDownload.isVisible = false
        binding.successDownloadView.isVisible = true
        binding.successDownloadView.playAnimation()
    }

    fun setOnClickDownloadListener(mListener: OnDownloadButtonClickListener) {
        listener = mListener
        binding.btnDownload.setOnClickListener {
            listener?.onClickDownload()
        }
    }

    interface OnDownloadButtonClickListener {
        fun onClickDownload()
    }
}