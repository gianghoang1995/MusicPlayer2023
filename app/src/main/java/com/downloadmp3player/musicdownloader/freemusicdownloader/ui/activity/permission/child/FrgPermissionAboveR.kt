package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission.child

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentPermission1Binding
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission.PermissionActivity


class FrgPermissionAboveR : BaseFragment<FragmentPermission1Binding>() {
    var activity: PermissionActivity? = null
    val APP_STORAGE_ACCESS_REQUEST_CODE = 9990
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
        binding.tvContent.text = getString(R.string.per_manage_all_file)
        binding.btnCheckPermision.setOnClickListener {
            check()
        }
    }

    fun check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                activity?.nextPage(1)
            } else { //request for the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    activity?.nextPage(1)
                }
            }
        }
    }



}