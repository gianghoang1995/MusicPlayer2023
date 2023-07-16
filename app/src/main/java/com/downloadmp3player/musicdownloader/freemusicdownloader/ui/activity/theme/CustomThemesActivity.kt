package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.theme

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.ThemesAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ThemesActivityBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventChangeTheme
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils
import com.yalantis.ucrop.UCrop
import org.greenrobot.eventbus.EventBus
import java.io.File


class CustomThemesActivity : BaseActivity<ThemesActivityBinding>(),
    ThemesAdapter.OnItemThemesClickListener {
    var viewLoadingSaveFile: LottieAnimationView? = null
    var dialogSaveThemes: Dialog? = null
    private val SELECT_PICTURE = 990
    lateinit var adapterThemes: ThemesAdapter
    var bitmapRoot: Bitmap? = null
    var bitmapPreview: Bitmap? = null
    var bitmapBlur: Bitmap? = null
    var posThemes = 1
    private val DEFAULT_BLUR_RADIUS = 70
    private val DEFAULT_BLACK_PERCENT = 20

    override fun bindingProvider(inflater: LayoutInflater): ThemesActivityBinding {
        return ThemesActivityBinding.inflate(inflater)
    }

    override fun ThemesActivityBinding.initView() {
        init()
    }


    fun init() {
        onClick()
        adapterThemes = ThemesAdapter(this, this)
        binding.rvChoseTheme.setHasFixedSize(true)
        binding.rvChoseTheme.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvChoseTheme.adapter = adapterThemes
        onThemesSelected(1)
        val tempThemes = AppUtils.getTempThemeEdited(this)
        if (tempThemes?.isNotEmpty() == true) {
            binding.sbBlur.progress = PreferenceUtils.getThemeBlurProgress()
            changeBitmapBlurRadius(PreferenceUtils.getThemeBlurProgress())
            binding.sbBlackColor.progress = PreferenceUtils.getThemeOverlayProgress()
        }
    }

    override fun onBackPressed() {
        showAlertDialog(
            getString(R.string.ask_cancel),
            object : OnAlertDialogListener {
                override fun onAccept() {
                    finish()
                }

                override fun onCancel() {

                }
            })
    }

    override fun onStop() {
        super.onStop()
        if (dialogSaveThemes != null)
            if (dialogSaveThemes?.isShowing == true)
                dialogSaveThemes?.dismiss()
    }

    fun onClick() {
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.sbBlur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { changeBitmapBlurRadius(it) }
            }
        })

        binding.sbBlackColor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar?.progress?.let { changeBlackProgress(it) }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.btnSaveTheme.setOnClickListener {
            showDialogSavethemes()
            PreferenceUtils.put(AppConstants.THEME_OVERLAY_PROGRESS, binding.sbBlackColor.progress)
            PreferenceUtils.put(AppConstants.THEME_BLUR_PROGRESS, binding.sbBlur.progress)
            AsyncSaveBitmap(object : OnSaveThemeSuccess {
                override fun onSaveSuccessFull() {
                    runOnUiThread {
                        viewLoadingSaveFile?.setMinAndMaxProgress(0f, 1f)
                        viewLoadingSaveFile?.repeatCount = 0
                        viewLoadingSaveFile?.addAnimatorListener(object :
                            Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                dialogSaveThemes?.dismiss()

                                BaseApplication.getAppInstance().refreshBitmapBackground()
                                EventBus.getDefault().post(
                                    EventChangeTheme(
                                        true
                                    )
                                )
                                dialogLoadingAds?.showDialogLoading()
                                BaseApplication.getAppInstance().adsFullInApp?.showAds(this@CustomThemesActivity,
                                    onLoadAdSuccess = {
                                        dialogLoadingAds?.dismissDialog()
                                    }, onAdClose = {
                                        showMessage(getString(R.string.success))
                                        finish()
                                    }, onAdLoadFail = {
                                        MaxIntertitial.ShowIntertitialApplovinMax(
                                            this@CustomThemesActivity, getString(R.string.appvolin_full)
                                        ) {
                                            dialogLoadingAds?.dismissDialog()
                                            showMessage(getString(R.string.success))
                                            finish()
                                        }
                                    })
                            }

                            override fun onAnimationCancel(animation: Animator) {
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                            }
                        })

                    }

                }
            }).execute()
        }
    }

    private fun showDialogSavethemes() {
        dialogSaveThemes = Dialog(this)
        dialogSaveThemes?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogSaveThemes?.setContentView(R.layout.dialog_save_themes_progress)
        dialogSaveThemes?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogSaveThemes?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        viewLoadingSaveFile =
            dialogSaveThemes?.findViewById<View>(R.id.loadingView) as LottieAnimationView

        viewLoadingSaveFile?.setMinAndMaxProgress(0f, 0.675f)
        viewLoadingSaveFile?.playAnimation()
        dialogSaveThemes?.show()
    }

    private fun changeBlackProgress(progress: Int) {
        val alpha = progress / 100f
        binding.imgLayerTest.setBackgroundColor(AppUtils.getColorWithAlpha(Color.BLACK, alpha))
    }

    private fun changeBitmapBlurRadius(progress: Int) {
        if (progress < 1) {
            bitmapBlur = bitmapRoot
            binding.imgPreview.setImageBitmap(bitmapBlur)
        } else {
            /*val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute {
                handler.post {
                    bitmapBlur = bitmapRoot?.let { AppUtils.blurStack(it, progress, false) }
                    imgPreview.setImageBitmap(bitmapBlur)
                }
            }*/

            AsyncChangeBlur(progress).execute()
        }
    }


    @SuppressLint("StaticFieldLeak")
    private inner class AsyncChangeBlur(private var progress: Int) :
        AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg params: Void?): Bitmap? {
            runOnUiThread {
                binding.loadingView.loading.visibility = View.VISIBLE
            }
            return bitmapRoot?.let { AppUtils.blurStack(it, progress, false) }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            bitmapBlur = result
            binding.imgPreview.setImageBitmap(bitmapBlur)
            runOnUiThread {
                binding.loadingView.loading.visibility = View.INVISIBLE
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private inner class AsyncSaveBitmap(private var onSaveThemeSuccess: OnSaveThemeSuccess) :
        AsyncTask<Void, Void, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            AppUtils.saveBitmapTempThemesDefault(this@CustomThemesActivity, bitmapRoot)
            AppUtils.saveBitmapThemes(this@CustomThemesActivity, bitmapBlur)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            onSaveThemeSuccess.onSaveSuccessFull()
        }
    }

    private fun defaultSeekbarProgress() {
        binding.sbBlur.progress = DEFAULT_BLUR_RADIUS
        binding.sbBlackColor.progress = DEFAULT_BLACK_PERCENT
    }

    private fun decodeBitmapDrawable() {
        val resInt = AppConstants.getListThemes(this)[posThemes]
        bitmapRoot = BitmapFactory.decodeResource(resources, resInt as Int)
        binding.imgPreview.setImageBitmap(bitmapRoot)
        AsyncChangeBlur(DEFAULT_BLUR_RADIUS).execute()
        changeBlackProgress(DEFAULT_BLACK_PERCENT)
        setColorPrimaryBackground()
        defaultSeekbarProgress()
    }

    private fun setColorPrimaryBackground() {
        val palette: Palette? = bitmapRoot?.let { Palette.from(it).generate() }
        val darkVibrantSwatch = palette?.darkVibrantSwatch
        val lightVibrantSwatch = palette?.lightVibrantSwatch
        val vibrantSwatch = palette?.vibrantSwatch
        val darkMutedSwatch = palette?.darkMutedSwatch
        val lightMutedSwatch = palette?.lightMutedSwatch
        when {
            vibrantSwatch != null -> {
                binding.viewBackgroundPrimary.setBackgroundColor(vibrantSwatch.rgb)
                binding.appBarLayout.setBackgroundColor(vibrantSwatch.rgb)
            }

            lightVibrantSwatch != null -> {
                binding.viewBackgroundPrimary.setBackgroundColor(lightVibrantSwatch.rgb)
                binding.appBarLayout.setBackgroundColor(lightVibrantSwatch.rgb)
            }

            darkVibrantSwatch != null -> {
                binding.viewBackgroundPrimary.setBackgroundColor(darkVibrantSwatch.rgb)
                binding.appBarLayout.setBackgroundColor(darkVibrantSwatch.rgb)
            }

            lightMutedSwatch != null -> {
                binding.viewBackgroundPrimary.setBackgroundColor(lightMutedSwatch.rgb)
                binding.appBarLayout.setBackgroundColor(lightMutedSwatch.rgb)
            }

            darkMutedSwatch != null -> {
                binding.viewBackgroundPrimary.setBackgroundColor(darkMutedSwatch.rgb)
                binding.appBarLayout.setBackgroundColor(darkMutedSwatch.rgb)
            }
        }
    }

    override fun onThemesSelected(pos: Int) {
        posThemes = pos
        binding.sbBlur.progress = 0
        binding.sbBlackColor.progress = 0
        val themPath = AppUtils.getTempThemeEdited(this)
        if (themPath?.isNotEmpty() == true) {
            when (pos) {
                0 -> {
                    pickFromGallery()
                }

                1 -> {
                    adapterThemes.setIndexThemes(pos)
                    selectedThemesCustom(themPath)
                }

                else -> {
                    adapterThemes.setIndexThemes(pos)
                    decodeBitmapDrawable()
                }
            }
        } else {
            if (pos == 0) {
                pickFromGallery()
            } else {
                adapterThemes.setIndexThemes(pos)
                decodeBitmapDrawable()
            }
        }
    }

    private fun selectedThemesCustom(filePath: String) {
        Log.e("Path", filePath)
        bitmapRoot = AppUtils.convertFileToBitmap(filePath)
        val themePreview = AppUtils.getThemePath(this)
        bitmapPreview = AppUtils.convertFileToBitmap(themePreview)
        binding.imgPreview.setImageBitmap(bitmapRoot)
        AsyncChangeBlur(DEFAULT_BLUR_RADIUS).execute()
        changeBlackProgress(DEFAULT_BLACK_PERCENT)
        adapterThemes.setCustomThemesPreview(bitmapPreview)
        adapterThemes.setIndexThemes(1)
        setColorPrimaryBackground()
        defaultSeekbarProgress()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.label_select_picture)
            ), SELECT_PICTURE
        )
    }

    private fun decodeFileToBitmap(filePath: String) {
        bitmapRoot = AppUtils.convertFileToBitmap(filePath)
        bitmapBlur = AppUtils.convertFileToBitmap(filePath)
            ?.let { AppUtils.blurStack(it, DEFAULT_BLUR_RADIUS, false) }
        changeBlackProgress(DEFAULT_BLACK_PERCENT)
        binding.imgPreview.setImageBitmap(bitmapBlur)
//        adapterThemes.setCustomThemesPreview(bitmapRoot)
//        adapterThemes.setIndexThemes(1)
        setColorPrimaryBackground()
        defaultSeekbarProgress()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri: Uri? = data?.data
                if (null != selectedImageUri) {
                    val destinationUri = Uri.fromFile(File(AppUtils.makeTempThemesPath(this)))
                    UCrop.of(selectedImageUri, destinationUri)
                        .withAspectRatio(9f, 16f)
                        .start(this)
                }
            }

            if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                val resultUri: Uri? = data?.let { UCrop.getOutput(it) }
                val filePath = File(resultUri?.path).path
                decodeFileToBitmap(filePath)
            }
        }
    }

    interface OnSaveThemeSuccess {
        fun onSaveSuccessFull()
    }
}