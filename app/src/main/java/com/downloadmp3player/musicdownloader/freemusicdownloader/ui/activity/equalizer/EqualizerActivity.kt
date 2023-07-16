package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.equalizer

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.AdapterEqualizer
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.SpinnerAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.equalizer.EqualizerDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.equalizer.EqualizerHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityEqualizerBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CustomPresetItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.widget.SeekBarEqualizer

class EqualizerActivity : BaseActivity<ActivityEqualizerBinding>() {
    var oldBass = 0
    var oldVirt = 0
    lateinit var equalizerDao: EqualizerDaoDB
    lateinit var equalizerHelper: EqualizerHelperDB
    lateinit var spinnerAdapter: SpinnerAdapter
    var listEqualizer: ArrayList<CustomPresetItem> = ArrayList()
    var listEqualizerCustom: ArrayList<CustomPresetItem> = ArrayList()
    var dialogEqualizer: Dialog? = null
    var dialogEditText: Dialog? = null
    var customPreset: CustomPresetItem? = null

    override fun bindingProvider(inflater: LayoutInflater): ActivityEqualizerBinding {
        return ActivityEqualizerBinding.inflate(inflater)
    }

    override fun ActivityEqualizerBinding.initView() {
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        updatePresetCustom()
    }

    override fun onResume() {
        super.onResume()
    }

    fun init() {
        initBannerAds(binding.frameBannerAds)
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        binding.btnBack.setOnClickListener {
            super.onBackPressed()
        }
        binding.bassboster.swBass.isChecked =
            PreferenceUtils.getValueBoolean(AppConstants.BASSBOSSTER_STATUS)
        binding.bassboster.bassSlider.isEnabled = binding.bassboster.swBass.isChecked
        binding.bassboster.virtualSlider.isEnabled = binding.bassboster.swBass.isChecked
        binding.bassboster.swBass.setOnCheckedChangeListener { _, isChecked ->
            binding.bassboster.bassSlider.isEnabled = isChecked
            binding.bassboster.virtualSlider.isEnabled = isChecked
            PreferenceUtils.put(AppConstants.BASSBOSSTER_STATUS, isChecked)
            PreferenceUtils.put(AppConstants.VIRTUALIZER_STATUS, isChecked)
            postBooleanAction(AppConstants.BASSBOSSTER_STATUS, isChecked)
            postBooleanAction(AppConstants.VIRTUALIZER_STATUS, isChecked)
        }

        binding.bassboster.bassSlider.progress =
            PreferenceUtils.getValueInt(AppConstants.BASSBOSSTER_STRENGTH) / 100
        binding.bassboster.bassSlider.setOnProgressChangedListener {
            val progress = it * 100
            if (oldBass != progress) {
                PreferenceUtils.put(AppConstants.BASSBOSSTER_STRENGTH, progress)
                postIntAction(AppConstants.BASSBOSSTER_STRENGTH, progress)
                oldBass = progress
            }
        }
        binding.bassboster.virtualSlider.progress =
            PreferenceUtils.getValueInt(AppConstants.VIRTUALIZER_STRENGTH) / 100
        binding.bassboster.virtualSlider.setOnProgressChangedListener {
            val progress = it * 100
            if (oldVirt != progress) {
                PreferenceUtils.put(AppConstants.VIRTUALIZER_STRENGTH, it * 100)
                postIntAction(AppConstants.VIRTUALIZER_STRENGTH, it * 100)
                oldVirt = progress
            }
        }

        equalizerHelper =
            EqualizerHelperDB(this)
        equalizerDao =
            EqualizerDaoDB(
                equalizerHelper
            )
        customPreset = CustomPresetItem(
            EqualizerHelperDB.DEFAULT_CUSTOM,
            0,
            0,
            0,
            0,
            0
        )
        setDataSpinner()
        initSliderListener()

        binding.equalizer.slider1.setThumbOffset()
        binding.equalizer.slider2.setThumbOffset()
        binding.equalizer.slider3.setThumbOffset()
        binding.equalizer.slider4.setThumbOffset()
        binding.equalizer.slider5.setThumbOffset()
        val isEnable = PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)
        changeStatusView(isEnable)
    }

    private fun changeStatusView(isEnable: Boolean) {
        binding.equalizer.slider1.setEnable(isEnable)
        binding.equalizer.slider2.setEnable(isEnable)
        binding.equalizer.slider3.setEnable(isEnable)
        binding.equalizer.slider4.setEnable(isEnable)
        binding.equalizer.slider5.setEnable(isEnable)
        binding.equalizer.spinner.isEnabled = isEnable
        binding.equalizer.btnSavePreset.isEnabled = isEnable
    }

    fun setDefaultCustom() {
        if (PreferenceUtils.getValueInt(AppConstants.PRESET_NUMBER) != listEqualizer.size - 1) PreferenceUtils.put(
            AppConstants.PRESET_NUMBER,
            listEqualizer.size - 1
        )
        if (binding.equalizer.spinner.selectedItemPosition != listEqualizer.size - 1) {
            binding.equalizer.spinner.setSelection(listEqualizer.size - 1)
        }
        updatePresetCustom()
    }

    private fun initSliderListener() {
        binding.equalizer.slider1.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.equalizer.titleTop.setText((progress / 100).toString(), 0)
                customPreset?.setSlider1(progress)
                postSliderChangeAction(0, progress)
                setDefaultCustom()
            }
        })

        binding.equalizer.slider2.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.equalizer.titleTop.setText((progress / 100).toString(), 1)
                customPreset?.setSlider2(progress)
                postSliderChangeAction(1, progress)
                setDefaultCustom()
            }
        })

        binding.equalizer.slider3.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.equalizer.titleTop.setText((progress / 100).toString(), 2)
                customPreset?.setSlider3(progress)
                postSliderChangeAction(2, progress)
                setDefaultCustom()
            }
        })

        binding.equalizer.slider4.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.equalizer.titleTop.setText((progress / 100).toString(), 3)
                customPreset?.setSlider4(progress)
                postSliderChangeAction(4, progress)
                setDefaultCustom()
            }
        })

        binding.equalizer.slider5.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.equalizer.titleTop.setText((progress / 100).toString(), 4)
                customPreset?.setSlider5(progress)
                postSliderChangeAction(4, progress)
                setDefaultCustom()
            }
        })

        binding.equalizer.btnSavePreset.setOnClickListener {
            showDialogPreset()
        }

        binding.equalizer.spinner.setSelection(PreferenceUtils.getValueInt(AppConstants.PRESET_NUMBER))
        binding.equalizer.swEqualizer.isChecked =
            PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)

        binding.equalizer.swEqualizer.setOnCheckedChangeListener { buttonView, isChecked ->
            PreferenceUtils.put(AppConstants.EQUALIZER_STATUS, isChecked)
            postBooleanAction(AppConstants.EQUALIZER_STATUS, isChecked)
            changeStatusView(isChecked)
        }
    }

    private fun showDialogPreset() {
        var position = -1
        dialogEqualizer = Dialog(this)
        dialogEqualizer?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEqualizer?.setContentView(R.layout.dialog_list_preset)
        val window = dialogEqualizer?.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        dialogEqualizer?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEqualizer?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val rv_Playlist = dialogEqualizer?.findViewById<View>(R.id.rv_equalizer) as RecyclerView
        val tv_empty = dialogEqualizer?.findViewById<View>(R.id.tv_empty_preset) as TextView
        val btnUpdate = dialogEqualizer?.findViewById<View>(R.id.btn_update) as Button
        val btnCreate = dialogEqualizer?.findViewById<View>(R.id.btn_Create) as Button

        dialogEditText = Dialog(this)
        dialogEditText?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEditText?.setContentView(R.layout.dialog_editext)
        dialogEditText?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEditText?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val create_Cancel = dialogEditText?.findViewById<View>(R.id.tv_cancel) as Button
        val create_Create = dialogEditText?.findViewById<View>(R.id.tv_create) as Button
        val create_Edittext = dialogEditText?.findViewById<View>(R.id.edt_name) as EditText

        listEqualizerCustom = equalizerDao.allCustomPreset
        val equalizerAdapter = AdapterEqualizer(this, equalizerDelete = {
            position = -1
            var nameDelete = listEqualizerCustom.get(it).presetName
            equalizerDao.deletePreset(nameDelete)
            listEqualizerCustom.removeAt(it)
            setDataSpinnerDefault()
        }, equalizerOnClick = {
            position = it
        })

        if (listEqualizerCustom.size > 0) {
            tv_empty.visibility = View.GONE
        } else {
            tv_empty.visibility = View.VISIBLE
        }

        btnCreate.setOnClickListener {
            create_Edittext.requestFocus()
            dialogEditText?.getWindow()
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialogEditText?.show()
        }

        btnUpdate.setOnClickListener {
            if (position == -1) {
                getString(R.string.please_chose_preset_to_update)
            } else {
                updatePreset(position, listEqualizerCustom.get(position).presetName)
                dialogEqualizer?.dismiss()
            }
        }

        equalizerAdapter.setDataEqualizer(listEqualizerCustom)
        rv_Playlist.setHasFixedSize(true)
        rv_Playlist.layoutManager = LinearLayoutManager(this)
        rv_Playlist.adapter = equalizerAdapter
        /*input */

        create_Create.setOnClickListener {
            val name = create_Edittext.text.toString()
            if (name.length > 0) {
                if (checkName(name)) {
                    equalizerDao.deleteCustomPreset()
                    addNewPreset(name)
                    dialogEditText?.dismiss()
                    dialogEqualizer?.dismiss()
                } else {
                    getString(R.string.new_preset_duplicate)
                }
            } else {
                showMessage(getString(R.string.empty_name))
            }
        }

        create_Cancel.setOnClickListener {
            dialogEditText?.dismiss()
        }

        dialogEqualizer?.show()
    }

    private fun checkName(name: String): Boolean {
        for (x in 0 until listEqualizer.size) {
            if (name == listEqualizer[x].presetName) {
                return false
                break
            }
        }
        return true
    }

    private fun addNewPreset(name: String): Boolean {
        customPreset?.setPresetName(name)
        return if (!equalizerDao.insertPreset(customPreset)) {
            getString(R.string.new_preset_duplicate)
            false
        } else {
            getString(R.string.save_preset_success)
            equalizerDao.addCustomPreset()
            setDataSpinner()
            binding.equalizer.spinner.setSelection(listEqualizer.size - 2)
            true
        }
    }

    private fun updatePreset(position: Int, name: String) {
        customPreset!!.setPresetName(name)
        if (!equalizerDao.updatePreset(customPreset)) {
            getString(R.string.cant_update)
        } else {
            equalizerDao.deleteCustomPreset()
            getString(R.string.preset_update_successfully)
            equalizerDao.addCustomPreset()
            setDataSpinner()
            binding.equalizer.spinner.setSelection(position + 10)
        }
    }

    private fun updatePresetCustom() {
        if (customPreset?.presetName.equals(EqualizerHelperDB.DEFAULT_CUSTOM)) equalizerDao.updatePreset(
            customPreset
        )
    }

    private fun setDataSpinner() {
        listEqualizer = equalizerDao.allPreset
        spinnerAdapter = SpinnerAdapter(this, listEqualizer)
        binding.equalizer.spinner.adapter = spinnerAdapter
        binding.equalizer.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item = listEqualizer[position]
                    binding.equalizer.slider1.setProgress(convertCustomPreset(item.slider1))
                    binding.equalizer.titleTop.setText((item.slider1 / 100).toString(), 0)
                    binding.equalizer.slider2.setProgress(convertCustomPreset(item.slider2))
                    binding.equalizer.titleTop.setText((item.slider2 / 100).toString(), 1)
                    binding.equalizer.slider3.setProgress(convertCustomPreset(item.slider3))
                    binding.equalizer.titleTop.setText((item.slider3 / 100).toString(), 2)
                    binding.equalizer.slider4.setProgress(convertCustomPreset(item.slider4))
                    binding.equalizer.titleTop.setText((item.slider4 / 100).toString(), 3)
                    binding.equalizer.slider5.setProgress(convertCustomPreset(item.slider5))
                    binding.equalizer.titleTop.setText((item.slider5 / 100).toString(), 4)
                    customPreset = listEqualizer[position]
                    PreferenceUtils.put(AppConstants.PRESET_NUMBER, position)
                    postIntAction(AppConstants.ACTION_CHANGE_PRESET_EQUALIZER, position)
                }
            }
    }

    private fun setDataSpinnerDefault() {
        listEqualizer = equalizerDao.allPreset
        spinnerAdapter = SpinnerAdapter(this, listEqualizer)
        binding.equalizer.spinner.adapter = spinnerAdapter
        binding.equalizer.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = listEqualizer[position]
                binding.equalizer.slider1.setProgress(convertCustomPreset(item.slider1))
                binding.equalizer.titleTop.setText((item.slider1 / 100).toString(), 0)
                binding.equalizer.slider2.setProgress(convertCustomPreset(item.slider2))
                binding.equalizer.titleTop.setText((item.slider2 / 100).toString(), 1)
                binding.equalizer.slider3.setProgress(convertCustomPreset(item.slider3))
                binding.equalizer.titleTop.setText((item.slider3 / 100).toString(), 2)
                binding.equalizer. slider4.setProgress(convertCustomPreset(item.slider4))
                binding.equalizer.titleTop.setText((item.slider4 / 100).toString(), 3)
                binding.equalizer.slider5.setProgress(convertCustomPreset(item.slider5))
                binding.equalizer.  titleTop.setText((item.slider5 / 100).toString(), 4)
                PreferenceUtils.put(AppConstants.PRESET_NUMBER, position)
                postIntAction(AppConstants.ACTION_CHANGE_PRESET_EQUALIZER, position)
            }
        }
        binding.equalizer.spinner.setSelection(listEqualizer.size - 1)
    }

    private fun convertCustomPreset(progress: Int): Int {
        var level = progress
        level = if (level == 0) {
            AppConstants.CENTER_VALUE
        } else {
            level + AppConstants.CENTER_VALUE
        }
        return level
    }

    fun convertProgress(progress: Int): Int {
        var progress = progress
        progress = if (progress == AppConstants.MIN_VALUE) -1500 else {
            if (progress > AppConstants.CENTER_VALUE) {
                AppConstants.CENTER_VALUE - (AppConstants.MAX_VALUE - progress)
            } else if (progress < AppConstants.CENTER_VALUE) {
                progress - AppConstants.CENTER_VALUE
            } else {
                0
            }
        }
        return progress
    }
}