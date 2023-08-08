package com.musicplayer.mp3player.playermusic.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.musicplayer.mp3player.playermusic.equalizer.BuildConfig
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.eventbus.BusChangeNetwork
import com.musicplayer.mp3player.playermusic.eventbus.BusDeleteSong
import com.musicplayer.mp3player.playermusic.eventbus.BusRefreshDataWhenDelete
import com.musicplayer.mp3player.playermusic.callback.OnBinderServiceConnection
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.service.MusicPlayerService
import com.musicplayer.mp3player.playermusic.ui.activity.playing.PlayerMusicActivity
import com.musicplayer.mp3player.playermusic.ui.activity.soundeditor.RingdroidActivity
import com.musicplayer.mp3player.playermusic.ui.dialog.moresong.DialogMoreSong
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.LocaleUtils
import com.musicplayer.mp3player.playermusic.utils.PreferenceUtils
import com.musicplayer.mp3player.playermusic.widget.PlayerView
import com.google.android.material.snackbar.Snackbar
import com.musicplayer.mp3player.playermusic.BaseApplication
import com.utils.adsloader.AdaptiveBannerManager
import com.utils.adsloader.utils.DialogLoadingAds
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList

abstract class BaseActivity<VB : ViewBinding> : BaseFrameActivity<VB>(),
    DialogMoreSong.OnDeleteFileRequestListener,
    PlayerView.OnClickControlListener {

    private var dialogLoading: Dialog? = null
    private lateinit var snackbarInternet: Snackbar
    private var toast: Toast? = null
    private var ringtone: MusicItem? = null
    var toastMssg: Toast? = null
    private var removeItemSong: MusicItem? = null
    private var listSongRemove: ArrayList<MusicItem> = ArrayList()
    private val REQUEST_PERM_DELETE = 111
    private val REQUEST_PERM_DELETE_MULTIPLE_FILE = 112
    private var listenerBindService: OnBinderServiceConnection? = null
    var mBoundService = false
    var musicPlayerService: MusicPlayerService? = null
    var lastFrameAds: FrameLayout? = null
    private var streamServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder: MusicPlayerService.MusicServiceBinder =
                service as MusicPlayerService.MusicServiceBinder
            musicPlayerService = binder.getService()
            listenerBindService?.onBindServiceMusicSuccess()
            mBoundService = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            listenerBindService?.onServiceDisconnection()
            mBoundService = false
        }
    }
    var dialogNoInternet: Dialog? = null
    var dialogLoadingAds: DialogLoadingAds? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.applyLocale(this)
        fullScreenActivity()
        dialogLoadingAds = DialogLoadingAds(this)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun initBannerAds(frameLayout: FrameLayout, isColapse: Boolean) {
        val singleAdaptiveBannerManager =
            AdaptiveBannerManager(
                this,
                BuildConfig.banner_01,
                BuildConfig.banner_02,
                isColapse
            )
        singleAdaptiveBannerManager.loadBanner(frameLayout, onAdLoader = {
        }, onAdLoadFail = {
        })
    }

    private fun fullScreenActivity() {
        val window: Window = window
        val winParams: WindowManager.LayoutParams = window.attributes
        winParams.flags =
            winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        window.attributes = winParams
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }


    fun setBindListener(mListener: OnBinderServiceConnection) {
        listenerBindService = mListener
    }

    override fun onResume() {
        super.onResume()
        if (ringtone != null) {
            ringtone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    startRingdroidEditor(this, ringtone)
                }
                null
            } else {
                startRingdroidEditor(this, ringtone)
                null
            }
        }
        bindService(streamServiceConnection)
    }

    override fun onPause() {
        super.onPause()
        unbindServicePlayMusic(streamServiceConnection)
    }

    fun showAlertDialog(mess: String, listener: OnAlertDialogListener) {
        AlertDialog.Builder(this).setMessage(mess)
            .setPositiveButton(getString(R.string.accept)) { dialog, which ->
                listener.onAccept()
            }.setNegativeButton(getString(R.string.no)) { dialog, which ->
                listener.onCancel()
            }.show()
    }

    fun initThemeStyle(imgTheme: ImageView, blackTspView: View) {
        imgTheme.setImageBitmap(BaseApplication.getAppInstance().bmImg)
        val overlayProgress = (PreferenceUtils.getThemeOverlayProgress().toFloat()) / 100f
        blackTspView.setBackgroundColor(
            AppUtils.getColorWithAlpha(
                Color.BLACK, overlayProgress
            )
        )
    }

    /**
     * Delete file android 11
     */
    override fun onRequestDeleteFile(song: MusicItem) {
        removeItemSong = song
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val file = File(song.songPath)
            val mediaID: Long = AppUtils.convertFilePathToMediaID(file.absolutePath, this)
            val Uri_one = ContentUris.withAppendedId(
                MediaStore.Audio.Media.getContentUri("external"), mediaID
            )
            val listUri: MutableList<Uri> = ArrayList()
            listUri.add(Uri_one)
            MediaStore.createDeleteRequest(
                contentResolver, listUri
            ).intentSender
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, listUri)
            try {
                startIntentSenderForResult(
                    pendingIntent.intentSender, REQUEST_PERM_DELETE, null, 0, 0, 0, null
                )
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        } else {
            AppUtils.deleteFile(this, song)
        }
    }

    fun onRequestDeleteFile(listSong: ArrayList<MusicItem>) {
        listSongRemove.clear()
        listSongRemove.addAll(listSong)
        AsyncGetUri(listSongRemove).execute()
    }

    private inner class AsyncGetUri(private var listSong: ArrayList<MusicItem>) :
        AsyncTask<Void, Void, MutableList<Uri>>() {
        override fun doInBackground(vararg params: Void?): MutableList<Uri> {
            runOnUiThread {
                showLoading()
            }
            val listUri: MutableList<Uri> = ArrayList()
            for (song in listSong) {
                val file = File(song.songPath)
                val mediaID: Long =
                    AppUtils.convertFilePathToMediaID(file.absolutePath, applicationContext)
                val Uri_one = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.getContentUri("external"), mediaID
                )
                listUri.add(Uri_one)
            }
            return listUri
        }

        override fun onPostExecute(result: MutableList<Uri>?) {
            super.onPostExecute(result)
            runOnUiThread {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    result?.let {
                        MediaStore.createDeleteRequest(
                            contentResolver, it
                        ).intentSender
                    }
                    val pendingIntent =
                        result?.let { MediaStore.createDeleteRequest(contentResolver, it) }
                    try {
                        startIntentSenderForResult(
                            pendingIntent?.intentSender,
                            REQUEST_PERM_DELETE_MULTIPLE_FILE,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }

                }
                hideLoading()
            }
        }

    }

    /**
     * Delete file android 11 Activity result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERM_DELETE && resultCode == RESULT_OK) {
            AppUtils.deleteSongFromPlaylist(this, removeItemSong)
            showMessage(getString(R.string.delete_success))
            EventBus.getDefault().postSticky(removeItemSong?.let { BusDeleteSong(it) })
            removeItemSong = null
        }
        if (requestCode == REQUEST_PERM_DELETE_MULTIPLE_FILE && resultCode == RESULT_OK) {
            showMessage(getString(R.string.delete_success))
            for (item in listSongRemove) {
                AppUtils.deleteSongFromPlaylist(this, item)
            }
            EventBus.getDefault().postSticky(BusRefreshDataWhenDelete(true))
        }
    }

    fun openUrl(url: String?) {
        try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW, Uri.parse(url)
            )
            startActivity(browserIntent)
        } catch (ex: Exception) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show()
        }
    }

    protected open fun sendEmail(content: String) {
        val to = arrayOf(AppConstants.DEF_EMAIL_FEEDBACK)
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Music Player")
        emailIntent.putExtra(Intent.EXTRA_TEXT, content)
        try {
            startActivity(Intent.createChooser(emailIntent, "Feedback Music Player"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_client_email), Toast.LENGTH_SHORT).show()
        }
    }

    fun showLoading() {
        dialogLoading = Dialog(this)
        dialogLoading?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogLoading?.setContentView(R.layout.dialog_loading)
        dialogLoading?.setCancelable(false)
        dialogLoading?.setCanceledOnTouchOutside(false)
        dialogLoading?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogLoading?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val window = dialogLoading?.window
        val wlp = window?.attributes
        wlp?.gravity = Gravity.CENTER
        window?.attributes = wlp
        dialogLoading?.show()
    }

    fun hideLoading() {
        if (dialogLoading != null) {
            if (dialogLoading?.isShowing == true) {
                dialogLoading?.dismiss()
                dialogLoading = null
            }
        }
    }

    fun rateInStore() {
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    fun openPubGGPlay(pub: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:$pub")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:$pub")
                )
            )
        }
    }

    fun postIntAction(action: String, value: Int) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = action
        intent.putExtra(action, value)
        startService(intent)
    }

    fun postSliderChangeAction(band: Int, progress: Int) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = AppConstants.EQUALIZER_SLIDER_CHANGE
        intent.putExtra(AppConstants.EQUALIZER_SLIDER_CHANGE, band)
        intent.putExtra(AppConstants.EQUALIZER_SLIDER_VALUE, progress)
        startService(intent)
    }

    fun postBooleanAction(action: String, value: Boolean) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = action
        intent.putExtra(action, value)
        startService(intent)
    }

    open fun setRingtoneRingDroid(mringtone: MusicItem?) {
        ringtone = mringtone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                startRingdroidEditor(this, ringtone)
                ringtone = null
            } else {
                getString(R.string.need_permission)
                needPermision(ringtone)
            }
        } else {
            startRingdroidEditor(this, ringtone)
        }
    }

    open fun setRingoneFunction(song: MusicItem) {
        val file = File(song.songPath)
        if (Build.VERSION.SDK_INT >= 29) {
            val path = AppUtils.getRingtoneStorageDir()
            if (!path.exists()) {
                path.mkdirs()
            }
            val newFile = File(path, file.name)
            try {
                copy(file, newFile)
            } catch (ignored: IOException) {
            }
            afterSavingRingtone(song, newFile.path)
        } else {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            contentValues.put(MediaStore.MediaColumns.TITLE, song.title)
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, song.title)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            contentValues.put(MediaStore.MediaColumns.SIZE, file.length())
            contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true)
            val uri = song?.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) }
            val cursor = contentResolver.query(
                uri!!, null, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath), null
            )
            if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
                val id = cursor.getString(0)
                contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true)
                contentResolver.update(
                    uri!!,
                    contentValues,
                    MediaStore.MediaColumns.DATA + "=?",
                    arrayOf(song.songPath)
                )
                val newuri = ContentUris.withAppendedId(uri!!, java.lang.Long.valueOf(id))
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                        this, RingtoneManager.TYPE_RINGTONE, newuri
                    )
                    showMessage("${getString(R.string.set_ringtone_success)}\n" + "${getString(R.string.title_songs)}: ${song.title}")
                } catch (t: Throwable) {
                    t.printStackTrace()
                    showMessage(getString(R.string.error))
                }
                cursor.close()
            }
        }
    }

    @Throws(IOException::class)
    open fun copy(src: File?, dst: File?) {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    fun afterSavingRingtone(song: MusicItem, outPath: String) {
        val outFile = File(outPath)
        val fileSize = outFile.length()
        if (fileSize <= 512) {
            outFile.delete()
            return
        }
        // Create the database record, pointing to the existing file path
        val mimeType: String = when {
            outPath.endsWith(".m4a") -> {
                "audio/mp4a-latm"
            }

            outPath.endsWith(".wav") -> {
                "audio/wav"
            }

            else -> {
                // This should never happen.
                "audio/mpeg"
            }
        }
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, outPath)
        values.put(MediaStore.MediaColumns.TITLE, song.title + "-ringtone")
        values.put(MediaStore.MediaColumns.SIZE, fileSize)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        values.put(MediaStore.Audio.Media.ARTIST, "Ringtones")
        values.put(MediaStore.Audio.Media.DURATION, song.duration)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, song.title + "-ringtone")
        values.put(MediaStore.Audio.Media.IS_ALARM, false)
        values.put(MediaStore.Audio.Media.IS_MUSIC, false)
        try {
            val uri = MediaStore.Audio.Media.getContentUriForPath(outPath)
            val newUri = contentResolver.insert(uri!!, values)
            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    contentResolver.openOutputStream(newUri!!).use { os ->
                        val size = outFile.length().toInt()
                        val bytes = ByteArray(size)
                        try {
                            val buf = BufferedInputStream(FileInputStream(outFile))
                            buf.read(bytes, 0, bytes.size)
                            buf.close()
                            os!!.write(bytes)
                            os!!.close()
                            os!!.flush()
                        } catch (e: IOException) {
                        }
                    }
                } catch (ignored: java.lang.Exception) {
                }

                MediaScannerConnection.scanFile(
                    this, arrayOf(outFile.path), null
                ) { _: String?, uri1: Uri? ->
                    RingtoneManager.setActualDefaultRingtoneUri(
                        this, RingtoneManager.TYPE_RINGTONE, uri1
                    )
                    showMessage("${getString(R.string.set_ringtone_success)}\n" + "${getString(R.string.title_songs)}: ${song.title + "-ringtone"}")
                }
            } else {
                RingtoneManager.setActualDefaultRingtoneUri(
                    this, RingtoneManager.TYPE_RINGTONE, newUri
                )
                showMessage("${getString(R.string.set_ringtone_success)}\n" + "${getString(R.string.title_songs)}: ${song.title + "-ringtone"}")
            }
        } catch (ex: java.lang.Exception) {
        }
    }

    private fun startRingdroidEditor(context: Context, song: MusicItem?) {
        val intent = Intent(
            context, RingdroidActivity::class.java
        ).putExtra(RingdroidActivity.KEY_SOUND_COLUMN_path, song?.songPath)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_title, song?.title)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_artist, song?.artist)
        context.startActivity(intent)
    }

    private fun needPermision(song: MusicItem?) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        ringtone = song
    }

    fun showMessage(message: String?) {
        if (toastMssg != null) {
            toastMssg?.cancel()
        }
        toastMssg = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toastMssg?.show()
    }

    fun showMessage(resID: Int) {
        if (toastMssg != null) {
            toastMssg?.cancel()
        }
        toastMssg = Toast.makeText(this, getString(resID), Toast.LENGTH_SHORT)
        toastMssg?.show()
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    open fun changeStatusbarColor(color: String) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(color)
        }
    }

    open fun changeStatusbarColor(color: Int) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }

    fun changeStatusBarColor(color: Int, lightStatusBar: Boolean) {
        window?.let { win ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val oldFlags = win.decorView.systemUiVisibility
                win.statusBarColor = Color.parseColor("#141a32")
                var flags = oldFlags
                flags = if (lightStatusBar) {
                    flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                win.decorView.systemUiVisibility = flags
            }
        }
    }

    fun showCustomToast(message: String?) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(applicationContext)
        val inflater: LayoutInflater = layoutInflater
        val layout: View = inflater.inflate(
            R.layout.custom_toast_layout, findViewById(R.id.custom_toast_container)
        )

        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 8, 8, 8)

        val text: TextView = layout.findViewById(R.id.text)
        val custom_toast_container: LinearLayout = layout.findViewById(R.id.custom_toast_container)
        text.text = message
        toast?.view?.layoutParams = layoutParams
        toast?.setGravity(Gravity.TOP, 0, 0)
        toast?.duration = Toast.LENGTH_SHORT
        toast?.view = layout
        toast?.show()
    }

    fun startService(action: String) {
        var intent = Intent(this, MusicPlayerService::class.java)
        intent.action = action
        startService(intent)
    }

    fun startService(action: String, bool: Boolean) {
        var intent = Intent(this, MusicPlayerService::class.java)
        intent.action = action
        intent.putExtra(action, bool)
        startService(intent)
    }

    fun bindService(connection: ServiceConnection) {
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindServicePlayMusic(connection: ServiceConnection) {
        if (mBoundService) {
            try {
                unbindService(connection)
            } catch (ex: Exception) {
            }
        }
    }

    override fun onClickControl() {
        if (musicPlayerService?.exoPlayer != null)
            startActivity(Intent(this, PlayerMusicActivity::class.java))
        else {
            showMessage(getString(R.string.no_song_play))
        }
    }

    override fun onNextClick() {
        startService(AppConstants.ACTION_NEXT)
    }

    override fun onPriveClick() {
        startService(AppConstants.ACTION_PRIVE)
    }

    override fun onToggleClick() {
        startService(AppConstants.ACTION_TOGGLE_PLAY)
    }

    override fun onRestartClick() {
        startService(AppConstants.ACTION_RESTART)
    }

    override fun onStopMusic() {
        startService(AppConstants.ACTION_STOP)
    }


    open fun showSnackBarSuccess(message: String?) {
        snackbarInternet = Snackbar.make(
            findViewById(android.R.id.content), message!!, 2000
        )
        val sbView: View = snackbarInternet.getView()
        val textView =
            sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 1
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.baloo_bold)
        textView.typeface = typeface
        textView.textSize = 16f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else {
            textView.gravity = Gravity.CENTER_HORIZONTAL
        }
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        snackbarInternet.show()
    }

    open fun showSnackBarError(message: String?) {
        snackbarInternet = Snackbar.make(
            findViewById(android.R.id.content), message!!, 2000
        )
        val sbView: View = snackbarInternet.getView()
        val textView =
            sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 1
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.baloo_bold)
        textView.typeface = typeface
        textView.textSize = 16f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else {
            textView.gravity = Gravity.CENTER_HORIZONTAL
        }
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.red_noti))
        snackbarInternet.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun connectionChange(event: BusChangeNetwork) {
        if (event.isChange) {
            showSnackBarSuccess(getString(R.string.wellcome_back))
        } else {
            showSnackBarError(getString(R.string.internet_disconected))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun message(bus: BusDeleteSong) {

    }

    interface OnAlertDialogListener {
        fun onAccept()
        fun onCancel()
    }

}