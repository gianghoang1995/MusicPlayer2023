package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.player.bass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragment
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentBassbossterBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils

class BassBossterFragment : BaseFragment<FragmentBassbossterBinding>() {

    var oldBass = 0;
    var oldVirt = 0

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBassbossterBinding {
        return FragmentBassbossterBinding.inflate(inflater)
    }

    override fun FragmentBassbossterBinding.initView() {
        init()
    }

    fun init() {
        binding.swBass.isChecked = PreferenceUtils.getValueBoolean(AppConstants.BASSBOSSTER_STATUS)
        binding.bassSlider.isEnabled = binding.swBass.isChecked
        binding.virtualSlider.isEnabled = binding.swBass.isChecked
        binding.swBass.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.bassSlider.isEnabled = isChecked
            binding.virtualSlider.isEnabled = isChecked
            PreferenceUtils.put(AppConstants.BASSBOSSTER_STATUS, isChecked)
            PreferenceUtils.put(AppConstants.VIRTUALIZER_STATUS, isChecked)
            postBooleanAction(AppConstants.BASSBOSSTER_STATUS, isChecked)
            postBooleanAction(AppConstants.VIRTUALIZER_STATUS, isChecked)
        }

        binding.bassSlider.progress =
            PreferenceUtils.getValueInt(AppConstants.BASSBOSSTER_STRENGTH) / 100
        binding.bassSlider.setOnProgressChangedListener {
            val progress = it * 100
            if (oldBass != progress) {
                PreferenceUtils.put(AppConstants.BASSBOSSTER_STRENGTH, progress)
                postIntAction(AppConstants.BASSBOSSTER_STRENGTH, progress)
                oldBass = progress
            }
        }
        binding.virtualSlider.progress =
            PreferenceUtils.getValueInt(AppConstants.VIRTUALIZER_STRENGTH) / 100
        binding.virtualSlider.setOnProgressChangedListener {
            val progress = it * 100
            if (oldVirt != progress) {
                PreferenceUtils.put(AppConstants.VIRTUALIZER_STRENGTH, it * 100)
                postIntAction(AppConstants.VIRTUALIZER_STRENGTH, it * 100)
                oldVirt = progress
            }
        }
    }
}
