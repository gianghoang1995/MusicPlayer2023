package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.download

import android.annotation.SuppressLint
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseFragmentDialog
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.FragmentDownloadingAudioBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshData
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadSampleListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.util.FileDownloadUtils
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class DownloadingFragmentDialog(private val itemDownload: ItemMusicOnline?) :
    BaseFragmentDialog<FragmentDownloadingAudioBinding>() {
    private var downloadID = 0
    private var baseDownloadTask: BaseDownloadTask? = null
    private var isDownloadSuccess = true
    private var mFileOutput: File? = null

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadingAudioBinding {
        return FragmentDownloadingAudioBinding.inflate(inflater)
    }

    override fun FragmentDownloadingAudioBinding.initView() {
        isCancelable = false

        mBinding.btnCancelDownload.setOnClickListener {
            mFileOutput?.path?.let { it1 -> File(it1).delete() }
            File(FileDownloadUtils.getTempPath(mFileOutput?.path)).delete()
            FileDownloader.getImpl().clearAllTaskData()
            dismiss()
        }

        download(itemDownload?.downloadURL)

    }

    private fun download(url: String?) {
        if (itemDownload != null) {
            isDownloadSuccess = false
            val dirpath = AppUtils.outputPath()
            mFileOutput =
                File("$dirpath/${itemDownload.title?.let { AppUtils.createTitleFile(it) }}")
            val urlStream = url?.replace("\\\\".toRegex(), "")
            baseDownloadTask =
                FileDownloader.getImpl().create(urlStream).setPath(mFileOutput?.path, false)
                    .setCallbackProgressTimes(1000).setMinIntervalUpdateSpeed(1000)
                    .setCallbackProgressMinInterval(1500).setTag(urlStream)
                    .setListener(object : FileDownloadSampleListener() {
                        override fun pending(
                            task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int
                        ) {
                            super.pending(task, soFarBytes, totalBytes)
                        }

                        override fun progress(
                            task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int
                        ) {
                            super.progress(task, soFarBytes, totalBytes)
                            val progr = soFarBytes.toDouble() / 1024 / 1024
                            val total = totalBytes.toDouble() / 1024 / 1024
                            val contentDownload =
                                getString(R.string.downloading) + ": " + progr.toInt() + "/" + total.toInt() + " MB"
                            mBinding.tvProgressDownload.text = contentDownload
                        }

                        override fun error(task: BaseDownloadTask, e: Throwable) {
                            super.error(task, e)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.error_please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        }

                        override fun connected(
                            task: BaseDownloadTask,
                            etag: String?,
                            isContinue: Boolean,
                            soFarBytes: Int,
                            totalBytes: Int
                        ) {
                            super.connected(task, etag, isContinue, soFarBytes, totalBytes)

                        }

                        override fun paused(
                            task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int
                        ) {
                            super.paused(task, soFarBytes, totalBytes)
                        }

                        override fun completed(task: BaseDownloadTask) {
                            super.completed(task)

                            object : CountDownTimer(5000, 1000) {
                                @SuppressLint("SetTextI18n")
                                override fun onTick(millisUntilFinished: Long) {
                                    mBinding.btnCancelDownload.visibility = View.GONE
                                    mBinding.tvProgressDownload.text =
                                        getString(R.string.download_success) + ": " + millisUntilFinished / 1000 + "s"
                                }

                                override fun onFinish() {
                                    BaseApplication.mInstance?.obverseDownloadServiceUtils?.downloadingState?.postValue(
                                        999
                                    )
                                    addSongToMediaStore()
                                    dismiss()
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.download_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.start()
                        }

                        override fun warn(task: BaseDownloadTask) {
                            super.warn(task)
                        }
                    })
            downloadID = baseDownloadTask?.start() ?: 0
        }
    }


    fun addSongToMediaStore() {
        if (mFileOutput?.exists() == true) {
            if (Build.VERSION.SDK_INT >= 30) {
                val file = File(mFileOutput!!.path)
                val path = File(AppUtils.getDownloadFolderPath())
                if (!path.exists()) {
                    path.mkdirs()
                }
                val newFile = File(path, file.name)
                try {
                    copy(file, newFile)
                    AppUtils.deleteFileNoNotify(requireContext(), file)
                    mFileOutput = null
                } catch (ignored: IOException) {
                }
                MediaScannerConnection.scanFile(
                    requireContext(), arrayOf(newFile.absolutePath), null
                ) { path1: String?, uri: Uri? ->
                    EventBus.getDefault().postSticky(EventRefreshData(true))
                }
            } else {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Audio.AudioColumns.TITLE, itemDownload?.title)
                contentValues.put(
                    MediaStore.Audio.AudioColumns.DISPLAY_NAME, itemDownload?.title
                )
                contentValues.put(MediaStore.Audio.AudioColumns.IS_MUSIC, true)
                contentValues.put(
                    MediaStore.Audio.AudioColumns.ARTIST, AppConstants.DEFAULT_FOLDER_NAME
                )
                contentValues.put(
                    MediaStore.Audio.AudioColumns.ALBUM, AppConstants.DEFAULT_FOLDER_NAME
                )
                contentValues.put(
                    MediaStore.MediaColumns.DATE_ADDED, AppUtils.getCurrentMillisecond()
                )
                contentValues.put(MediaStore.Audio.Media.DATA, mFileOutput!!.path)
                contentValues.put(
                    MediaStore.Audio.AudioColumns.DURATION, itemDownload?.duration
                )
                try {
                    val uri = requireContext().contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues
                    )
                    EventBus.getDefault().postSticky(EventRefreshData(true))
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun copy(src: File?, dst: File?) {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    fun convertDuration(): Long {
        val duration = itemDownload?.duration ?: 0
        return if (!TextUtils.isEmpty(duration.toString())) {
            duration
        } else {
            600000
        }
    }

}