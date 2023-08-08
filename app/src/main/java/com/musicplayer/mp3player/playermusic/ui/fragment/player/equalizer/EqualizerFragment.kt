package com.musicplayer.mp3player.playermusic.ui.fragment.player.equalizer

import android.app.Dialog
import android.view.*
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.adapter.AdapterEqualizerRV
import com.musicplayer.mp3player.playermusic.adapter.SpinnerAdapterRV
import com.musicplayer.mp3player.playermusic.base.BaseFragment
import com.musicplayer.mp3player.playermusic.database.equalizer.EqualizerDaoDB
import com.musicplayer.mp3player.playermusic.database.equalizer.EqualizerHelperDB
import com.musicplayer.mp3player.playermusic.equalizer.databinding.FragmentEqualizerBinding
import com.musicplayer.mp3player.playermusic.model.CustomPresetItem
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import com.musicplayer.mp3player.playermusic.utils.PreferenceUtils
import com.musicplayer.mp3player.playermusic.widget.SeekBarEqualizer

class EqualizerFragment : BaseFragment<FragmentEqualizerBinding>() {

    lateinit var equalizerDao: EqualizerDaoDB
    lateinit var equalizerHelper: EqualizerHelperDB
    lateinit var spinnerAdapterRV: SpinnerAdapterRV
    var listEqualizer: ArrayList<CustomPresetItem> = ArrayList()
    var listEqualizerCustom: ArrayList<CustomPresetItem> = ArrayList()
    var dialogEqualizer: Dialog? = null
    var dialogEditText: Dialog? = null
    var customPreset: CustomPresetItem? = null

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEqualizerBinding {
        return FragmentEqualizerBinding.inflate(inflater)
    }

    override fun FragmentEqualizerBinding.initView() {
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        updatePresetCustom()
    }

    fun init() {
        equalizerHelper =
            EqualizerHelperDB(
                context
            )
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

        binding.slider1.setThumbOffset()
        binding.slider2.setThumbOffset()
        binding.slider3.setThumbOffset()
        binding.slider4.setThumbOffset()
        binding.slider5.setThumbOffset()

        val isEnable = PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)
        changeStatusView(isEnable)
    }

    fun changeStatusView(isEnable: Boolean) {
        binding.slider1.setEnable(isEnable)
        binding.slider2.setEnable(isEnable)
        binding.slider3.setEnable(isEnable)
        binding.slider4.setEnable(isEnable)
        binding.slider5.setEnable(isEnable)
        binding.spinner.isEnabled = isEnable
        binding.btnSavePreset.isEnabled = isEnable
    }

    fun setDefaultCustom() {
        if (PreferenceUtils.getValueInt(AppConstants.PRESET_NUMBER) != listEqualizer.size - 1)
            PreferenceUtils.put(AppConstants.PRESET_NUMBER, listEqualizer.size - 1)
        if (binding.spinner.selectedItemPosition != listEqualizer.size - 1) {
            binding.spinner.setSelection(listEqualizer.size - 1)
        }
        updatePresetCustom()
    }

    fun initSliderListener() {
        binding.slider1.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.titleTop.setText((progress / 100).toString(), 0)
                customPreset?.setSlider1(progress)
                postSliderChangeAction(0, progress)
                setDefaultCustom()
            }
        })

        binding.slider2.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.titleTop.setText((progress / 100).toString(), 1)
                customPreset?.setSlider2(progress)
                postSliderChangeAction(1, progress)
                setDefaultCustom()
            }
        })

        binding.slider3.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.titleTop.setText((progress / 100).toString(), 2)
                customPreset?.setSlider3(progress)
                postSliderChangeAction(2, progress)
                setDefaultCustom()
            }
        })

        binding.slider4.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.titleTop.setText((progress / 100).toString(), 3)
                customPreset?.setSlider4(progress)
                postSliderChangeAction(4, progress)
                setDefaultCustom()
            }
        })

        binding.slider5.onSeekChange(object : SeekBarEqualizer.OnSeekChange {
            override fun onChange(value: Int) {
                val progress = convertProgress(value)
                binding.titleTop.setText((progress / 100).toString(), 4)
                customPreset?.setSlider5(progress)
                postSliderChangeAction(4, progress)
                setDefaultCustom()
            }
        })

        binding.btnSavePreset.setOnClickListener {
            showDialogPreset()
        }

        binding.spinner.setSelection(PreferenceUtils.getValueInt(AppConstants.PRESET_NUMBER))
        binding.swEqualizer.isChecked =
            PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)

        binding.swEqualizer.setOnCheckedChangeListener { buttonView, isChecked ->
            PreferenceUtils.put(AppConstants.EQUALIZER_STATUS, isChecked)
            postBooleanAction(AppConstants.EQUALIZER_STATUS, isChecked)
            changeStatusView(isChecked)
        }
    }

    fun showDialogPreset() {
        var position = -1
        dialogEqualizer = Dialog(requireContext())
        dialogEqualizer?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEqualizer?.setContentView(R.layout.dialog_list_preset)
        val window = dialogEqualizer?.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        dialogEqualizer?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEqualizer?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val rv_Playlist = dialogEqualizer?.findViewById<View>(R.id.rv_equalizer) as RecyclerView
        val tv_empty = dialogEqualizer?.findViewById<View>(R.id.tv_empty_preset) as TextView
        val btnUpdate = dialogEqualizer?.findViewById<View>(R.id.btn_update) as Button
        val btnCreate = dialogEqualizer?.findViewById<View>(R.id.btn_Create) as Button

        dialogEditText = Dialog(requireContext())
        dialogEditText?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEditText?.setContentView(R.layout.dialog_editext)
        dialogEditText?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEditText?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val create_Cancel = dialogEditText?.findViewById<View>(R.id.tv_cancel) as Button
        val create_Create = dialogEditText?.findViewById<View>(R.id.tv_create) as Button
        val create_Edittext = dialogEditText?.findViewById<View>(R.id.edt_name) as EditText

        listEqualizerCustom = equalizerDao.allCustomPreset

        val equalizerAdapter = AdapterEqualizerRV(context,
            equalizerDelete = {
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
        rv_Playlist.layoutManager = LinearLayoutManager(context)
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

    fun checkName(name: String): Boolean {
        for (x in 0..listEqualizer.size - 1) {
            if (name.equals(listEqualizer.get(x).presetName)) {
                return false
                break
            }
        }
        return true
    }

    fun addNewPreset(name: String): Boolean {
        customPreset?.setPresetName(name)
        if (!equalizerDao.insertPreset(customPreset)) {
            getString(R.string.new_preset_duplicate)
            return false
        } else {
            getString(R.string.save_preset_success)
            equalizerDao.addCustomPreset()
            setDataSpinner()
            binding.spinner.setSelection(listEqualizer.size - 2)
            return true
        }
    }

    fun updatePreset(position: Int, name: String) {
        customPreset!!.setPresetName(name)
        if (!equalizerDao.updatePreset(customPreset)) {
            getString(R.string.cant_update)
        } else {
            equalizerDao.deleteCustomPreset()
            getString(R.string.preset_update_successfully)
            equalizerDao.addCustomPreset()
            setDataSpinner()
            binding.spinner.setSelection(position + 10)
        }
    }

    fun updatePresetCustom() {
        if (customPreset?.presetName.equals(EqualizerHelperDB.DEFAULT_CUSTOM))
            equalizerDao.updatePreset(customPreset)
    }

    fun setDataSpinner() {
        listEqualizer = equalizerDao.allPreset
        spinnerAdapterRV = context?.let {
            SpinnerAdapterRV(
                it,
                listEqualizer
            )
        }!!
        binding.spinner.adapter = spinnerAdapterRV

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = listEqualizer.get(position)
                binding.slider1.setProgress(convertCustomPreset(item.slider1))
                binding.titleTop.setText((item.slider1 / 100).toString(), 0)
                binding.slider2.setProgress(convertCustomPreset(item.slider2))
                binding.titleTop.setText((item.slider2 / 100).toString(), 1)
                binding.slider3.setProgress(convertCustomPreset(item.slider3))
                binding.titleTop.setText((item.slider3 / 100).toString(), 2)
                binding.slider4.setProgress(convertCustomPreset(item.slider4))
                binding.titleTop.setText((item.slider4 / 100).toString(), 3)
                binding.slider5.setProgress(convertCustomPreset(item.slider5))
                binding.titleTop.setText((item.slider5 / 100).toString(), 4)
                customPreset = listEqualizer.get(position)
                PreferenceUtils.put(AppConstants.PRESET_NUMBER, position)
                postIntAction(AppConstants.ACTION_CHANGE_PRESET_EQUALIZER, position)
            }
        }
    }

    fun setDataSpinnerDefault() {
        listEqualizer = equalizerDao.allPreset
        spinnerAdapterRV = context?.let {
            SpinnerAdapterRV(
                it,
                listEqualizer
            )
        }!!
        binding.spinner.adapter = spinnerAdapterRV
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = listEqualizer.get(position)
                binding.slider1.setProgress(convertCustomPreset(item.slider1))
                binding.titleTop.setText((item.slider1 / 100).toString(), 0)
                binding.slider2.setProgress(convertCustomPreset(item.slider2))
                binding.titleTop.setText((item.slider2 / 100).toString(), 1)
                binding.slider3.setProgress(convertCustomPreset(item.slider3))
                binding.titleTop.setText((item.slider3 / 100).toString(), 2)
                binding.slider4.setProgress(convertCustomPreset(item.slider4))
                binding.titleTop.setText((item.slider4 / 100).toString(), 3)
                binding.slider5.setProgress(convertCustomPreset(item.slider5))
                binding.titleTop.setText((item.slider5 / 100).toString(), 4)
                PreferenceUtils.put(AppConstants.PRESET_NUMBER, position)
                postIntAction(AppConstants.ACTION_CHANGE_PRESET_EQUALIZER, position)
            }
        }
        binding.spinner.setSelection(listEqualizer.size - 1)
    }

    private fun convertCustomPreset(progress: Int): Int {
        var level = progress
        if (level == 0) {
            level = AppConstants.CENTER_VALUE
        } else {
            level = level + AppConstants.CENTER_VALUE
        }
        return level
    }

    fun convertProgress(progress: Int): Int {
        var progress = progress
        if (progress == AppConstants.MIN_VALUE) progress = -1500 else {
            if (progress > AppConstants.CENTER_VALUE) {
                progress = AppConstants.CENTER_VALUE - (AppConstants.MAX_VALUE - progress)
            } else if (progress < AppConstants.CENTER_VALUE) {
                progress = progress - AppConstants.CENTER_VALUE
            } else {
                progress = 0
            }
        }
        return progress
    }
}
