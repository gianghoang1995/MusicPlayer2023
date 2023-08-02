package com.musicplayer.mp3player.defaultmusicplayer.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.LayoutBottomSheetRingtoneBinding

class DialogMoreRingtoneBottomSheet(private val mListener: OnDialogMoreRingtoneListener) :
    BottomSheetDialogFragment() {
    lateinit var binding: LayoutBottomSheetRingtoneBinding
    override fun getTheme() = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutBottomSheetRingtoneBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetRingtone.setOnClickListener {
            mListener.onSetRingTone()
            dismiss()
        }

        binding.btnDeleteRingtone.setOnClickListener {
            mListener.onDeleteRingtone()
            dismiss()
        }

        binding.btnShareRingtone.setOnClickListener {
            mListener.onShareRingtone()
            dismiss()
        }
    }

    interface OnDialogMoreRingtoneListener {
        fun onSetRingTone()
        fun onDeleteRingtone()
        fun onShareRingtone()
    }
}