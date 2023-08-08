package com.musicplayer.mp3player.playermusic.base

import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.ServiceConnection
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.eventbus.BusDeleteSong
import com.musicplayer.mp3player.playermusic.eventbus.BusPutLoveSong
import com.musicplayer.mp3player.playermusic.eventbus.BusRefreshDataWhenDelete
import com.musicplayer.mp3player.playermusic.callback.OnBinderServiceConnection
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.service.MusicPlayerService
import com.musicplayer.mp3player.playermusic.ui.activity.soundeditor.RingdroidActivity
import com.musicplayer.mp3player.playermusic.ui.dialog.moresong.DialogMoreSong
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.utils.adsloader.utils.DialogLoadingAds
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

abstract class BaseFragment<VB : ViewBinding> : Fragment(), FrameView<VB>,
    DialogMoreSong.OnDeleteFileRequestListener {
    private lateinit var _binding: VB
    protected val binding get() = _binding
    private val REQUEST_PERM_DELETE = 111
    private val REQUEST_PERM_DELETE_MULTIPLE_FILE = 112
    var mringtone: MusicItem? = null
    var mRingtoneOld: MusicItem? = null
    private var removeItemSong: MusicItem? = null
    private var toast: Toast? = null
    var dialogLoadingAds: DialogLoadingAds? = null
    private var listenerBindService: OnBinderServiceConnection? = null
    var mBoundService = false
    var musicPlayerService: MusicPlayerService? = null
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
    private var mRootView: ViewGroup? = null
    private var mIsFirstLoad = false

    open fun setRingtoneRingDroid(ringtone: MusicItem?) {
        mringtone = ringtone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(requireContext())) {
                startRingdroidEditor(requireContext(), ringtone)
                mringtone = null
            } else {
                getString(R.string.need_permission)
                needPermision(ringtone)
            }
        } else {
            startRingdroidEditor(requireContext(), ringtone)
        }
    }

    private fun needPermision(song: MusicItem?) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:${requireContext().packageName}")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        mringtone = song
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mRootView == null) {
            _binding = bindingProvider(inflater, container)
            mRootView = _binding.root as ViewGroup?
            mIsFirstLoad = true
        } else {
            mIsFirstLoad = false
        }
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mIsFirstLoad) {
            _binding.initView()
            EventBus.getDefault().register(this)
            dialogLoadingAds = DialogLoadingAds(requireActivity())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        bindService(streamServiceConnection)
        if (mringtone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    context?.let { startRingdroidEditor(it, mringtone) }
                    mringtone = null
                }
            } else {
                context?.let { startRingdroidEditor(it, mringtone) }
                mringtone = null
            }
        }

        if (mRingtoneOld != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    setRingToneFunction(mRingtoneOld!!)
                    mringtone = null
                }
            } else {
                setRingToneFunction(mRingtoneOld!!)
                mringtone = null
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unbindServicePlayMusic(streamServiceConnection, mBoundService)
    }

    fun setBindListener(mListener: OnBinderServiceConnection) {
        listenerBindService = mListener
    }

    fun startService(action: String) {
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        intent.action = action
        requireContext().startService(intent)
    }

    fun startService(action: String, bool: Boolean) {
        var intent = Intent(requireContext(), MusicPlayerService::class.java)
        intent.action = action
        intent.putExtra(action, bool)
        requireContext().startService(intent)
    }

    /**
     * Delete file android 11
     */
    override fun onRequestDeleteFile(song: MusicItem) {
        removeItemSong = song
        AppUtils.deleteSongFromPlaylist(requireActivity(), song)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val file = File(song.songPath)
            val mediaID: Long =
                AppUtils.convertFilePathToMediaID(file.absolutePath, requireActivity())
            val Uri_one = ContentUris.withAppendedId(
                MediaStore.Audio.Media.getContentUri("external"),
                mediaID
            )
            val listUri: MutableList<Uri> = ArrayList()
            listUri.add(Uri_one)
            MediaStore.createDeleteRequest(
                requireActivity().contentResolver, listUri
            ).intentSender
            val pendingIntent =
                MediaStore.createDeleteRequest(requireActivity().contentResolver, listUri)
            try {
                startIntentSenderForResult(
                    pendingIntent.intentSender,
                    REQUEST_PERM_DELETE,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        } else {
            AppUtils.deleteFile(requireActivity(), song)
        }
    }


    fun onRequestDeleteFile(listSong: ArrayList<MusicItem>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val listUri: MutableList<Uri> = ArrayList()
            for (song in listSong) {
                AppUtils.deleteSongFromPlaylist(requireActivity(), song)
                val file = File(song.songPath)
                val mediaID: Long =
                    AppUtils.convertFilePathToMediaID(file.absolutePath, requireActivity())
                val Uri_one = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.getContentUri("external"),
                    mediaID
                )
                listUri.add(Uri_one)
            }
            MediaStore.createDeleteRequest(
                requireActivity().contentResolver, listUri
            ).intentSender
            val pendingIntent =
                MediaStore.createDeleteRequest(requireActivity().contentResolver, listUri)
            try {
                startIntentSenderForResult(
                    pendingIntent.intentSender,
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
    }

    /**
     * Delete file android 11 Activity result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERM_DELETE && resultCode == AppCompatActivity.RESULT_OK) {
            showMessage(getString(R.string.delete_success))
            EventBus.getDefault().postSticky(removeItemSong?.let { BusDeleteSong(it) })
            removeItemSong = null
        }
        if (requestCode == REQUEST_PERM_DELETE_MULTIPLE_FILE && resultCode == AppCompatActivity.RESULT_OK) {
            showMessage(getString(R.string.delete_success))
            EventBus.getDefault().postSticky(BusRefreshDataWhenDelete(true))
        }
    }

    fun showCustomToast(message: String?) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(requireContext())
        val inflater: LayoutInflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast_layout, null)

        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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

    private fun startRingdroidEditor(context: Context, song: MusicItem?) {
        val intent = Intent(
            context,
            RingdroidActivity::class.java
        ).putExtra(RingdroidActivity.KEY_SOUND_COLUMN_path, song?.songPath)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_title, song?.title)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_artist, song?.artist)
        context.startActivity(intent)
        mringtone = null
    }

    fun filterArtist(id: Long?): String? {
        return "AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + id
    }

    fun filterAlbum(id: Long?): String? {
        return "AND " + MediaStore.Audio.Media.ALBUM_ID + " = " + id
    }

    open fun setRingtone(ringtone: MusicItem?) {
        mringtone = ringtone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                context?.let { startRingdroidEditor(it, mringtone) }
                mringtone = null
            } else {
                getString(R.string.need_permission)
                needPermission(ringtone)
            }
        } else {
            context?.let { startRingdroidEditor(it, mringtone) }
            mringtone = null
        }
    }

    open fun setRingtoneHasCut(ringtone: MusicItem?) {
        mRingtoneOld = ringtone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                mRingtoneOld?.let { setRingToneFunction(it) }
                mRingtoneOld = null
            } else {
                getString(R.string.need_permission)
                needPermission(ringtone)
            }
        } else {
            mRingtoneOld?.let { setRingToneFunction(it) }
            mringtone = null
        }
    }

    private fun needPermission(song: MusicItem?) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + requireContext().packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireActivity().startActivity(intent)
        mringtone = song
    }

    open fun setRingToneFunction(song: MusicItem) {
        val file = File(song.songPath)
        if (Build.VERSION.SDK_INT >= 29) {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
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
            val uri = song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) }
            val cursor = requireContext().contentResolver.query(
                uri!!, null, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath), null
            )
            if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
                val id = cursor.getString(0)
                contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true)
                requireContext().contentResolver.update(
                    uri!!,
                    contentValues,
                    MediaStore.MediaColumns.DATA + "=?",
                    arrayOf(song.songPath)
                )
                val newuri = ContentUris.withAppendedId(uri!!, java.lang.Long.valueOf(id))
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                        requireContext(), RingtoneManager.TYPE_RINGTONE, newuri
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
            FileOutputStream(dst).use { out -> // Transfer bytes from in to out
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
        } // Create the database record, pointing to the existing file path
        val mimeType: String = when {
            outPath.endsWith(".m4a") -> {
                "audio/mp4a-latm"
            }

            outPath.endsWith(".wav") -> {
                "audio/wav"
            }

            else -> { // This should never happen.
                "audio/mpeg"
            }
        }
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, outPath)
        values.put(MediaStore.MediaColumns.TITLE, song.title)
        values.put(MediaStore.MediaColumns.SIZE, fileSize)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        values.put(MediaStore.Audio.Media.ARTIST, "Ringtone")
        values.put(MediaStore.Audio.Media.DURATION, song.duration)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        values.put(MediaStore.Audio.Media.IS_ALARM, false)
        values.put(MediaStore.Audio.Media.IS_MUSIC, false)
        try {
            val uri = MediaStore.Audio.Media.getContentUriForPath(outPath)
            val newUri = requireContext().contentResolver.insert(uri!!, values)
            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    requireContext().contentResolver.openOutputStream(newUri!!).use { os ->
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
                    requireContext(), arrayOf(outFile.path), null
                ) { _: String?, uri1: Uri? ->
                    RingtoneManager.setActualDefaultRingtoneUri(
                        requireContext(), RingtoneManager.TYPE_RINGTONE, uri1
                    )
                    showMessage("${getString(R.string.set_ringtone_success)}\n" + "${getString(R.string.title_songs)}: ${song.title}")
                }
            } else {
                RingtoneManager.setActualDefaultRingtoneUri(
                    requireContext(), RingtoneManager.TYPE_RINGTONE, newUri
                )
            }
        } catch (ex: java.lang.Exception) {
        }
        showMessage("${getString(R.string.set_ringtone_success)}\n" + "${getString(R.string.title_songs)}: ${song.title}")
    }

    fun showMessage(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    fun showMessage(resID: Int) {
        Toast.makeText(requireActivity(), getString(resID), Toast.LENGTH_SHORT).show()
    }

    open fun bindService(connection: ServiceConnection) {
        val intent = Intent(context, MusicPlayerService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    open fun unbindServicePlayMusic(connection: ServiceConnection, mBound: Boolean) {
        if (mBound) {
            try {
                requireContext().unbindService(connection)
            } catch (ex: Exception) {
            }
        }
    }

    fun postIntAction(action: String, value: Int) {
        val intent = Intent(context, MusicPlayerService::class.java)
        intent.setAction(action)
        intent.putExtra(action, value)
        context?.startService(intent)
    }

    fun postSliderChangeAction(band: Int, progress: Int) {
        val intent = Intent(context, MusicPlayerService::class.java)
        intent.setAction(AppConstants.EQUALIZER_SLIDER_CHANGE)
        intent.putExtra(AppConstants.EQUALIZER_SLIDER_CHANGE, band)
        intent.putExtra(AppConstants.EQUALIZER_SLIDER_VALUE, progress)
        context?.startService(intent)
    }

    fun postBooleanAction(action: String, value: Boolean) {
        val intent = Intent(context, MusicPlayerService::class.java)
        intent.setAction(action)
        intent.putExtra(action, value)
        context?.startService(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun event(event: BusPutLoveSong) {

    }

    abstract fun bindingProvider(inflater: LayoutInflater, container: ViewGroup?): VB
}