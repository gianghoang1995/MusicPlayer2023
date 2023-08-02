package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.ringtone

import android.view.LayoutInflater
import androidx.viewpager.widget.ViewPager
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.adapter.PagerAdapter
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseActivity
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.ActivityRingtoneMakerBinding
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventChangeTabRingtone
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.ringtone.child.NewRingtoneFrg
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.ringtone.child.RingtoneManagerFrg
import org.greenrobot.eventbus.EventBus

class RingtoneMakerActivity : BaseActivity<ActivityRingtoneMakerBinding>() {
    private var mPagerAdapter: PagerAdapter? = null

    override fun bindingProvider(inflater: LayoutInflater): ActivityRingtoneMakerBinding {
        return ActivityRingtoneMakerBinding.inflate(inflater)
    }

    override fun ActivityRingtoneMakerBinding.initView() {
        init()
    }


    override fun onResume() {
        super.onResume()
    }

    fun init() {
        initBannerAds(binding.frameBannerAds, true)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        mPagerAdapter = PagerAdapter(supportFragmentManager)
        mPagerAdapter?.addFragment(NewRingtoneFrg(), getString(R.string.cut_new_ringtone))
        mPagerAdapter?.addFragment(RingtoneManagerFrg(), getString(R.string.previous_ringtones))
        binding.viewPagerRingtoneCutter.adapter = mPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPagerRingtoneCutter)

        binding.viewPagerRingtoneCutter.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                EventBus.getDefault().postSticky(EventChangeTabRingtone(true))
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}