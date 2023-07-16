package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission.child

import android.Manifest.permission.*
import android.view.LayoutInflater
import android.view.ViewGroup
import com.gun0912.tedpermission.PermissionListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentPermission1Binding
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission.PermissionActivity
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

    override fun onResume() {
        super.onResume()
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
        //below android 11======
        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage(getString(R.string.need_permission))
            .setPermissions(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
            .check()
    }
}