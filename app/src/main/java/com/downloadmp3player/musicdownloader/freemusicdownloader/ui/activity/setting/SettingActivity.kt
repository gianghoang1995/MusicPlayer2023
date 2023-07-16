package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.setting

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivitySettingBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.UpdateFontSize
import com.downloadmp3player.musicdownloader.freemusicdownloader.handle.OnBinderServiceConnection
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils
import org.greenrobot.eventbus.EventBus

class SettingActivity : BaseActivity<ActivitySettingBinding>(), OnBinderServiceConnection {
    override fun bindingProvider(inflater: LayoutInflater): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(inflater)
    }

    override fun ActivitySettingBinding.initView() {
        init()
        onClick()
    }


    fun init() {
        initBannerAds(binding.frameBannerAds)
        setBindListener(this)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        binding.toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onClick() {
        binding.swHeadphone.isChecked = PreferenceUtils.getValueBoolean(AppConstants.PREF_HEADPHONE)
        binding.swEndCall.isChecked = PreferenceUtils.getValueBoolean(AppConstants.PREF_END_CALL)
        binding.swLockScreen.isChecked =
            PreferenceUtils.getValueBoolean(AppConstants.PREF_LOCK_SCREEN, false)

        binding.shakeView.setOnClickListener {
            showDialogChoseShakeNumber()
        }

        binding.headphoneView.setOnClickListener {
            binding.swHeadphone.isChecked = !binding.swHeadphone.isChecked
            PreferenceUtils.put(AppConstants.PREF_HEADPHONE, binding.swHeadphone.isChecked)
            musicPlayerService?.setHandleAudioBecomeNoise()
        }

        binding.swHeadphone.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtils.put(AppConstants.PREF_HEADPHONE, isChecked)
            musicPlayerService?.setHandleAudioBecomeNoise()
        }

        binding.callEndView.setOnClickListener {
            binding.swEndCall.isChecked = !binding.swEndCall.isChecked
            PreferenceUtils.put(AppConstants.PREF_END_CALL, binding.swEndCall.isChecked)
        }

        binding.swEndCall.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtils.put(AppConstants.PREF_END_CALL, isChecked)
        }

        binding.lookScreenView.setOnClickListener {
            binding.swLockScreen.isChecked = !binding.swLockScreen.isChecked
            PreferenceUtils.put(AppConstants.PREF_LOCK_SCREEN, binding.swLockScreen.isChecked)
        }

        binding.swLockScreen.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtils.put(AppConstants.PREF_LOCK_SCREEN, isChecked)
        }

        binding.tvPolicy.setOnClickListener {
            openUrl(AppConstants.POLICY_URL)
        }
        binding.tvRate.setOnClickListener {
            rateInStore()
        }
        binding.tvFeedback.setOnClickListener {
            showDialogFeedback()
        }
        binding.tvShare.setOnClickListener {
            AppUtils.shareText(
                this,
                getString(R.string.subject_share),
                "${getString(R.string.content_share)} https://play.google.com/store/apps/details?id=${packageName}"
            )
        }
    }

    private fun showDialogFeedback() {
        val dialogFeedback = Dialog(this)
        dialogFeedback.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFeedback.setContentView(R.layout.dialog_feedback)
        dialogFeedback.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogFeedback.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogFeedback.findViewById<View>(R.id.btnCancel) as Button
        val btnSend = dialogFeedback.findViewById<View>(R.id.btnSend) as Button
        val edtContent = dialogFeedback.findViewById<View>(R.id.edtFeedBack) as EditText

        btnSend.setOnClickListener {
            sendEmail(edtContent.text.toString())
            dialogFeedback.dismiss()
        }

        btnCancel.setOnClickListener {
            dialogFeedback.dismiss()
        }
        dialogFeedback.show()
    }

    private fun showDialogChangeFontSize() {
        val dialogFontsize = Dialog(this)
        dialogFontsize.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFontsize.setContentView(R.layout.dialog_font_size)
        dialogFontsize.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogFontsize.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogFontsize.findViewById<View>(R.id.btnCancel) as Button
        val groupFontSize = dialogFontsize.findViewById<View>(R.id.groupFontSize) as RadioGroup

        btnCancel.setOnClickListener {
            dialogFontsize.dismiss()
        }
        when (PreferenceUtils.getFontSize()) {
            AppConstants.FONT.SMALL -> {
                groupFontSize.check(R.id.rdSmall)
            }

            AppConstants.FONT.NORMAL -> {
                groupFontSize.check(R.id.rdNormal)
            }

            AppConstants.FONT.LARGE -> {
                groupFontSize.check(R.id.rdLarge)
            }
        }

        groupFontSize.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rdSmall -> {
                    PreferenceUtils.putFontSize(AppConstants.FONT.SMALL)
                }

                R.id.rdNormal -> {
                    PreferenceUtils.putFontSize(AppConstants.FONT.NORMAL)
                }

                R.id.rdLarge -> {
                    PreferenceUtils.putFontSize(AppConstants.FONT.LARGE)
                }
            }
            dialogFontsize.dismiss()
            EventBus.getDefault().postSticky(UpdateFontSize(true))
        }
        dialogFontsize.show()
    }

    private fun showDialogChoseShakeNumber() {
        val dialogShakeCount = Dialog(this)
        dialogShakeCount.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogShakeCount.setContentView(R.layout.dialog_shake_cout)
        dialogShakeCount.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogShakeCount.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogShakeCount.findViewById<View>(R.id.btnCancel) as Button
        val groupShake = dialogShakeCount.findViewById<View>(R.id.groupFontSize) as RadioGroup

        btnCancel.setOnClickListener {
            dialogShakeCount.dismiss()
        }

        when (PreferenceUtils.getShakeCount()) {
            -1 -> {
                groupShake.check(R.id.rdShakeOff)
            }

            1 -> {
                groupShake.check(R.id.rdShakeOne)
            }

            2 -> {
                groupShake.check(R.id.rdShakeTwo)
            }
        }

        groupShake.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rdShakeOne -> {
                    PreferenceUtils.put(AppConstants.PREF_SHAKE, true)
                    PreferenceUtils.put(AppConstants.PREF_SHAKE_COUNT, 1)
                }

                R.id.rdShakeTwo -> {
                    PreferenceUtils.put(AppConstants.PREF_SHAKE, true)
                    PreferenceUtils.put(AppConstants.PREF_SHAKE_COUNT, 2)
                }

                R.id.rdShakeOff -> {
                    PreferenceUtils.put(AppConstants.PREF_SHAKE, false)
                    PreferenceUtils.put(AppConstants.PREF_SHAKE_COUNT, -1)
                }
            }
            startService(AppConstants.ACTION_SHAKE)
            dialogShakeCount.dismiss()
        }
        dialogShakeCount.show()
    }

    override fun onBindServiceMusicSuccess() {

    }

    override fun onServiceDisconnection() {
    }
}
