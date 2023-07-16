package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity

import android.view.LayoutInflater
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityTotuorialBinding
import com.utils.adsloader.AdaptiveBannerManager

class TutorialActivity : BaseActivity<ActivityTotuorialBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivityTotuorialBinding {
        return ActivityTotuorialBinding.inflate(inflater)
    }

    override fun ActivityTotuorialBinding.initView() {
        initBannerAds(binding.frameBannerAds)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        binding.toolbarTip.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
