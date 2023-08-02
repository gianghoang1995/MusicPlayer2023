package com.musicplayer.mp3player.defaultmusicplayer.ui.activity

import android.view.LayoutInflater
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseActivity
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.ActivityTotuorialBinding

class TutorialActivity : BaseActivity<ActivityTotuorialBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivityTotuorialBinding {
        return ActivityTotuorialBinding.inflate(inflater)
    }

    override fun ActivityTotuorialBinding.initView() {
        initBannerAds(binding.frameBannerAds, false)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        binding.toolbarTip.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
