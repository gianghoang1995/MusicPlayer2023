package com.musicplayer.mp3player.playermusic.ui.activity.permission.child

import android.Manifest.permission.*
import android.view.LayoutInflater
import android.view.ViewGroup
import com.gun0912.tedpermission.PermissionListener
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.base.BaseFragment
import com.musicplayer.mp3player.playermusic.equalizer.databinding.FragmentPermission1Binding
import com.musicplayer.mp3player.playermusic.ui.activity.permission.PermissionActivity
import com.gun0912.tedpermission.normal.TedPermission

class FrgPermissionMedia : BaseFragment<FragmentPermission1Binding>() {
    var activity: PermissionActivity? = null
    var permissionlistener: PermissionListener? = null

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPermission1Binding {
        return FragmentPermission1Binding.inflate(inflater)
    }

    override fun FragmentPermission1Binding.initView() {
        activity = getActivity() as PermissionActivity?
        init()
    }


    fun init() {
        binding.tvContent.text = getString(R.string.per_1)
        binding.btnCheckPermision.setOnClickListener {
            checkPermission()
        }
    }

    fun checkPermission() {
        permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                activity?.nextPage(1)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {}
        }
        check()
    }

    fun check() {
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             TedPermission.create()
                 .setPermissionListener(permissionlistener)
                 .setDeniedMessage(getString(R.string.need_permission))
                 .setPermissions(
                     READ_MEDIA_AUDIO,
                     READ_MEDIA_IMAGES,
                     READ_MEDIA_VIDEO
                 )
                 .check()
         } else {*/
        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage(getString(R.string.need_permission))
            .setPermissions(
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE
            )
            .check()
//        }
    }
}