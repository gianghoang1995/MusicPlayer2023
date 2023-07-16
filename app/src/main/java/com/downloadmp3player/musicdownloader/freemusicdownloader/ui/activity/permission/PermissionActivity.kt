package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.PagerAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityPermissionBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.main.MainActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission.child.FrgPermissionMedia
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {
    private var adapter: PagerAdapter? = null


    override fun bindingProvider(inflater: LayoutInflater): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(inflater)
    }

    override fun ActivityPermissionBinding.initView() {
        checkPermission()
    }

    private fun checkPermission() {
        adapter = PagerAdapter(supportFragmentManager)
        val grant = PackageManager.PERMISSION_GRANTED
        val permissionCheck1 = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissionCheck1 != grant) {
            adapter?.addFragment(FrgPermissionMedia(), "")
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = adapter!!.count - 1
        binding.viewPager.setSwipeEnable(false)
    }

    private fun postActionFinish() {
        var intent = Intent(this, MusicPlayerService::class.java)
        intent.action = AppConstants.ACTION_STOP
        startService(intent)
        startActivity(
            Intent(this, MainActivity::class.java).putExtra(
                AppConstants.SHOW_ADS,
                true
            )
        )
        finish()
    }

    fun nextPage(page: Int) {
        binding.viewPager.currentItem = page
        if (AppUtils.isGrantPermission(this)) {
            postActionFinish()
        }
    }
}
