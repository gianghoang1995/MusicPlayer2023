package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.soundeditor

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.aliendroid.alienads.MaxIntertitial
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityEditorBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRingtoneCreate
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.soundeditor.utils.CheapSoundFile
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.soundeditor.utils.MediaStoreHelper
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.soundeditor.widget.MarkerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.soundeditor.widget.WaveformView
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.Pixels
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ViewUtil
import org.greenrobot.eventbus.EventBus
import java.io.*

class RingdroidActivity : BaseActivity<ActivityEditorBinding>(), MarkerView.MarkerListener,
    WaveformView.WaveformListener, View.OnClickListener {
    private var thread2: Thread? = null
    private var thread1: Thread? = null
    private var mSaveSoundFileThread: Thread? = null
    private val Supported_Format = arrayOf(".aac", ".AMR", ".mp3", ".wav", ".m4a")
    private var mNewFileKind: Int = 0
    private var mMarkerLeftInset: Int = 0
    private var mMarkerRightInset: Int = 0
    private var mLoadingLastUpdateTime: Long = 0
    private var mLoadingKeepGoing: Boolean = false
    private var mProgressDialog: ProgressDialog? = null
    private var mSoundFile: CheapSoundFile? = null
    private var mFile: File? = null
    private var mFilename: String? = null
    private var mWaveformView: WaveformView? = null
    private var mStartMarker: MarkerView? = null
    private var mEndMarker: MarkerView? = null
    private var mStartText: TextView? = null
    private var mEndText: TextView? = null
    private var mKeyDown: Boolean = false
    private var mWidth: Int = 0
    private var mMaxPos: Int = 0
    private var mStartPos = -1
    private var mEndPos = -1
    private var mStartVisible: Boolean = false
    private var mEndVisible: Boolean = false
    private var mLastDisplayedStartPos: Int = 0
    private var mLastDisplayedEndPos: Int = 0
    private var mOffset: Int = 0
    private var mOffsetGoal: Int = 0
    private var mFlingVelocity: Int = 0
    private var mPlayStartMsec: Int = 0
    private var mPlayStartOffset: Int = 0
    private var mPlayEndMsec: Int = 0
    private var mHandler: Handler? = null
    private var mIsPlaying: Boolean = false
    private var mPlayer: MediaPlayer? = null
    private var mTouchDragging: Boolean = false
    private var mTouchStart: Float = 0.toFloat()
    private var mTouchInitialOffset: Int = 0
    private var mTouchInitialStartPos: Int = 0
    private var mTouchInitialEndPos: Int = 0
    private var mWaveformTouchStartMsec: Long = 0
    private var mDensity: Float = 0.toFloat()
    private var outputFile: File? = null
    private var mSound_AlbumArt_Path: String? = null
    private var marginvalue: Int = 0
    private var EdgeReached = false
    private var mSoundDuration = 0
    private var Maskhidden = true
    var audioManager: AudioManager? = null
    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (mPlayer != null) {
                try {
                    if (mPlayer!!.isPlaying) pausePlayer()
                } catch (ex: java.lang.Exception) {
                }
            }
        }
    }
    private val mTimerRunnable = object : Runnable {
        override fun run() {
            if (mStartPos != mLastDisplayedStartPos && !mStartText!!.hasFocus()) {
                mStartText!!.text = getTimeFormat(formatTime(mStartPos))
                mLastDisplayedStartPos = mStartPos
            }

            if (mEndPos != mLastDisplayedEndPos && !mEndText!!.hasFocus()) {
                mEndText!!.text = getTimeFormat(formatTime(mEndPos))
                mLastDisplayedEndPos = mEndPos
            }

            mHandler?.postDelayed(this, 100)
        }
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityEditorBinding {
        return ActivityEditorBinding.inflate(inflater)
    }

    override fun ActivityEditorBinding.initView() {
        initBannerAds(binding.frameBannerAds)
        init()
    }

    fun init() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.zoomIn.setOnClickListener(this)
        binding.zoomOut.setOnClickListener(this)
        binding.btnSetRingTone.setOnClickListener(this)
        ViewUtil.SetOntouchListener(binding.btnSetRingTone)
        binding.PlayPauseView.visibility = View.INVISIBLE
        binding.PlayPauseView.setPlaying(!mIsPlaying)
        binding.PlayPauseView.setOnClickListener(this)
        // temporary solution to fix the delay between initial pause to play animation
        binding.PlayPauseView.postDelayed({
            runOnUiThread {
                binding.PlayPauseView.visibility = View.VISIBLE
            }
        }, 400)

        marginvalue = Pixels.pxtodp(this, 12)
        mPlayer = null
        mIsPlaying = false
        mSoundFile = null
        mKeyDown = false
        mHandler = Handler()
        mHandler?.postDelayed(mTimerRunnable, 100)
        val extras = intent.extras
        val path = extras?.getString(KEY_SOUND_COLUMN_path, null)
        val title = extras?.getString(KEY_SOUND_COLUMN_title, null)
        val artist = extras?.getString(KEY_SOUND_COLUMN_artist, "")

        binding.tvArtist.text = artist
        if (path == null) {
            showMessage(getString(R.string.cant_cut_this_song))
        } else {
            // remove mp3 part
            val newtitle: String = if (title!!.contains(EXTENSION_MP3)) title.replace(
                EXTENSION_MP3,
                ""
            ) else title.toString()
            binding.EditorSongTitle.text = newtitle
            mFilename = path
            if (mSoundFile == null) loadFromFile() else mHandler?.post { this.finishOpeningSoundFile() }
        }
        loadGui()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            var mSoundTitle: String
            val dataUri = data?.data
            val proj = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Artists.ARTIST
            )
            val tempCursor = managedQuery(dataUri, proj, null, null, null)
            tempCursor.moveToFirst() //reset the cursor
            var col_index: Int
            var AlbumID_index: Int
            do {
                col_index = tempCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                mSoundTitle = tempCursor.getString(col_index)
                AlbumID_index = tempCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID)
                val albumid = tempCursor.getLong(AlbumID_index)
                val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                val uri = ContentUris.withAppendedId(sArtworkUri, albumid)
                mSound_AlbumArt_Path = uri.toString()
            } while (tempCursor.moveToNext())
            try {
                assert(dataUri != null)
                var path: String? = dataUri!!.path

                if (!path!!.startsWith("/storage/")) {
                    path = MediaStoreHelper.getRealPathFromURI(applicationContext, data.data!!)
                }
                assert(path != null)
                val file = File(path!!)
                var mNewTitle = mSoundTitle
                if (mSoundTitle.contains(EXTENSION_MP3)) {
                    mNewTitle = file.name.replace(EXTENSION_MP3, "")
                }

                binding.EditorSongTitle.text = mNewTitle
                mFilename = file.absolutePath

                if (mSoundFile == null) loadFromFile() else mHandler?.post { this.finishOpeningSoundFile() }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun pausePlayer() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
        }
        mWaveformView?.setPlayback(-1)
        mIsPlaying = false
        enableDisableButtons()
    }

    override fun onPause() {
        super.onPause()
        if (mPlayer != null) {
            if (mPlayer!!.isPlaying) {
                mPlayer!!.pause()
                mWaveformView?.setPlayback(-1)
                mIsPlaying = false
                enableDisableButtons()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.stop()
            mPlayer!!.release()
            mPlayer = null
        }
        mProgressDialog = null
        finish()

        mSoundFile = null
        mWaveformView = null
        mHandler?.removeCallbacks(mTimerRunnable)
    }

    override fun waveformDraw() {
        if (mWaveformView != null) {
            mWidth = mWaveformView?.measuredWidth ?: 0
        }
        if (mOffsetGoal != mOffset && !mKeyDown && !EdgeReached) {
            updateDisplay()
        } else if (mIsPlaying) {
            updateDisplay()
        } else if (mFlingVelocity != 0) {
            updateDisplay()
        }
    }

    override fun waveformTouchStart(x: Float) {
        mTouchDragging = true
        mTouchStart = x
        mTouchInitialOffset = mOffset
        mFlingVelocity = 0
        mWaveformTouchStartMsec = System.currentTimeMillis()
    }

    override fun waveformTouchMove(x: Float) {
        mOffset = trap((mTouchInitialOffset + (mTouchStart - x)).toInt())
        updateDisplay()
    }

    override fun waveformTouchEnd() {
        mTouchDragging = false
        mOffsetGoal = mOffset
        val elapsedMsec = System.currentTimeMillis() - mWaveformTouchStartMsec
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                val seekMsec = mWaveformView?.pixelsToMillisecs((mTouchStart + mOffset).toInt())
                if (seekMsec != null) {
                    if (seekMsec >= mPlayStartMsec && seekMsec < mPlayEndMsec) {
                        mPlayer!!.seekTo(seekMsec - mPlayStartOffset)
                    } else {
                        handlePause()
                    }
                }
            } else {
                onPlay((mTouchStart + mOffset).toInt())
            }
        }
    }

    override fun waveformFling(vx: Float) {
        mTouchDragging = false
        mOffsetGoal = mOffset
        mFlingVelocity = (-vx).toInt()
        updateDisplay()
    }

    override fun waveformZoomIn() {
        if (mWaveformView?.canZoomOut() == true) {
            marginvalue = Pixels.pxtodp(this, 12)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                Pixels.pxtodp(this@RingdroidActivity, 12),
                0,
                Pixels.pxtodp(this@RingdroidActivity, 12),
                Pixels.pxtodp(this@RingdroidActivity, 20)
            )
            mWaveformView?.layoutParams = params
        }
        mWaveformView?.zoomIn()

        mStartPos = mWaveformView?.start ?: 0
        mEndPos = mWaveformView?.end ?: 0
        mMaxPos = mWaveformView?.maxPos() ?: 0
        mOffset = mWaveformView?.offset ?: 0
        mOffsetGoal = mOffset
        updateDisplay()

    }

    override fun waveformZoomOut() {
        if (mWaveformView?.canZoomOut() == true) {
            marginvalue = Pixels.pxtodp(this, 12)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                Pixels.pxtodp(this@RingdroidActivity, 12),
                0,
                Pixels.pxtodp(this@RingdroidActivity, 12),
                Pixels.pxtodp(this@RingdroidActivity, 20)
            )
            mWaveformView?.layoutParams = params

        }
        mWaveformView?.zoomOut()
        mStartPos = mWaveformView?.start ?: 0
        mEndPos = mWaveformView?.end ?: 0
        mMaxPos = mWaveformView?.maxPos() ?: 0
        mOffset = mWaveformView?.offset ?: 0
        mOffsetGoal = mOffset
        updateDisplay()
    }

    override fun markerDraw() {}

    override fun markerTouchStart(marker: MarkerView, pos: Float) {
        mTouchDragging = true
        mTouchStart = pos
        mTouchInitialStartPos = mStartPos
        mTouchInitialEndPos = mEndPos
    }

    override fun markerTouchMove(marker: MarkerView, pos: Float) {
        val delta: Float = pos - mTouchStart

        if (marker == mStartMarker) {
            mStartPos = trap((mTouchInitialStartPos + delta).toInt())
            if (mStartPos + mStartMarker!!.width >= mEndPos) {
                mStartPos = mEndPos - mStartMarker!!.width
            }
        } else {
            mEndPos = trap((mTouchInitialEndPos + delta).toInt())
            if (mEndPos < mStartPos + mStartMarker!!.width) mEndPos =
                mStartPos + mStartMarker!!.width
        }
        updateDisplay()
    }

    override fun markerTouchEnd(marker: MarkerView) {
        mTouchDragging = false
        if (marker == mStartMarker) {
            setOffsetGoalStart()
        } else {
            setOffsetGoalEnd()
        }
    }

    override fun markerLeft(marker: MarkerView, velocity: Int) {
        mKeyDown = true
        if (marker == mStartMarker) {
            val saveStart = mStartPos
            mStartPos = trap(mStartPos - velocity)
            mEndPos = trap(mEndPos - (saveStart - mStartPos))
            setOffsetGoalStart()
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity)
                mEndPos = mStartPos
            } else {
                mEndPos = trap(mEndPos - velocity)
            }
            setOffsetGoalEnd()
        }
        updateDisplay()
    }

    override fun markerRight(marker: MarkerView, velocity: Int) {
        mKeyDown = true
        if (marker == mStartMarker) {
            val saveStart = mStartPos
            mStartPos += velocity
            if (mStartPos > mMaxPos) mStartPos = mMaxPos
            mEndPos += mStartPos - saveStart
            if (mEndPos > mMaxPos) mEndPos = mMaxPos
            setOffsetGoalStart()
        }
        if (marker == mEndMarker) {
            mEndPos += velocity
            if (mEndPos > mMaxPos) mEndPos = mMaxPos
            setOffsetGoalEnd()
        }
        updateDisplay()
    }

    override fun markerEnter(marker: MarkerView) {}

    override fun markerKeyUp() {
        mKeyDown = false
        updateDisplay()
    }

    override fun markerFocus(marker: MarkerView) {
        mKeyDown = false
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate()
        } else {
            setOffsetGoalEndNoUpdate()
        }
        mHandler?.postDelayed({ this.updateDisplay() }, 100)
    }

    private fun loadGui() {
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        mDensity = metrics.density

        mMarkerLeftInset = (13 * mDensity).toInt()
        mMarkerRightInset = (13 * mDensity).toInt()


        mStartText = findViewById(R.id.starttext)
        mEndText = findViewById(R.id.endtext)
        binding.markStart.setOnClickListener(this)
        binding.markEnd.setOnClickListener(this)

        enableDisableButtons()

        mWaveformView = findViewById(R.id.waveform)
        mWaveformView?.setListener(this)
        mMaxPos = 0
        mLastDisplayedStartPos = -1
        mLastDisplayedEndPos = -1

        if (mSoundFile != null && mWaveformView?.hasSoundFile() == true) {
            mWaveformView?.setSoundFile(mSoundFile)
            mWaveformView?.recomputeHeights(mDensity)
            mMaxPos = mWaveformView?.maxPos() ?: 0
        }

        mStartMarker = findViewById(R.id.startmarker)
        mStartMarker!!.setListener(this)
        mStartMarker!!.alpha = 1f
        mStartMarker!!.isFocusable = true
        mStartMarker!!.isFocusableInTouchMode = true
        mStartVisible = true

        mEndMarker = findViewById(R.id.endmarker)
        mEndMarker!!.setListener(this)
        mEndMarker!!.alpha = 1f
        mEndMarker!!.isFocusable = true
        mEndMarker!!.isFocusableInTouchMode = true
        mEndVisible = true
        updateDisplay()
    }

    private fun loadFromFile() {
        mFile = File(mFilename!!)
        val mFileName = mFile!!.name
        var FileSupported = false
        for (aSupported_Format in Supported_Format) if (mFileName.contains(aSupported_Format)) {
            FileSupported = true
            break
        }

        if (!FileSupported) {
            Toast.makeText(this, getString(R.string.unsupport_format), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mLoadingLastUpdateTime = System.currentTimeMillis()
        mLoadingKeepGoing = true
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.setCancelable(false)
        mProgressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mProgressDialog?.setTitle(getString(R.string.edit_loading_text))
        mProgressDialog?.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { _, _ ->
            finish() //dismiss dialog
        }
        mProgressDialog?.show()
        mProgressDialog!!.setOnDismissListener {
            runOnUiThread {
                mEndMarker!!.visibility = View.VISIBLE
                mStartMarker!!.visibility = View.VISIBLE
            }
        }
        // Create the MediaPlayer in a background thread
        thread1 = object : Thread() {
            override fun run() {
                try {
                    mPlayer = MediaPlayer()
                    mPlayer?.setDataSource(this@RingdroidActivity, Uri.fromFile(mFile))
                    mPlayer?.prepare()
                    thread1?.interrupt()
                } catch (ignored: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RingdroidActivity,
                            getString(R.string.change_file_name),
                            Toast.LENGTH_LONG
                        ).show()
                        AlertDialog.Builder(this@RingdroidActivity).setTitle(R.string.error)
                            .setMessage(R.string.error_cutter_msg.toString())
                            .setPositiveButton(android.R.string.yes) { _, _ -> finish() }.show()
                    }
                    try {
                        val filePath = mFile!!.absolutePath
                        val file = File(filePath)
                        val inputStream = FileInputStream(file)
                        mPlayer = MediaPlayer()
                        mPlayer?.setDataSource(inputStream.fd)
                        mPlayer?.prepare()
                        thread1?.interrupt()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        thread1?.start()
        // Load the sound file in a background thread
        val listener = CheapSoundFile.ProgressListener { fractionComplete ->
            val now = System.currentTimeMillis()
            if (now - mLoadingLastUpdateTime > 100) {
                mProgressDialog!!.progress = (mProgressDialog!!.max * fractionComplete).toInt()
                mLoadingLastUpdateTime = now
            }
            mLoadingKeepGoing
        }


        thread2 = object : Thread() {
            override fun run() {
                try {
                    mSoundFile = CheapSoundFile.create(mFile!!.absolutePath, listener)
                } catch (e: Exception) {
                    mProgressDialog?.dismiss()
                    return
                }

                if (mLoadingKeepGoing) {
                    mHandler?.post {
                        if (mSoundFile != null) {
                            runOnUiThread {
                                finishOpeningSoundFile()
                            }
                        } else {
                            mProgressDialog?.dismiss()
                            AlertDialog.Builder(this@RingdroidActivity).setTitle(R.string.error)
                                .setMessage(R.string.error_cutter_msg)
                                .setPositiveButton(android.R.string.yes) { _, _ -> finish() }.show()
                        }
                    }
                }
                thread2?.interrupt()
            }
        }
        thread2?.start()
    }

    private fun finishOpeningSoundFile() {
        mWaveformView?.setSoundFile(mSoundFile)
        mWaveformView?.recomputeHeights(mDensity)
        mMaxPos = mWaveformView?.maxPos()!!
        mLastDisplayedStartPos = -1
        mLastDisplayedEndPos = -1
        mTouchDragging = false
        mOffset = 0
        mOffsetGoal = 0
        mFlingVelocity = 0
        resetPositions()
        updateDisplay()
        mProgressDialog?.dismiss()
    }

    @SuppressLint("NewApi")
    @Synchronized
    private fun updateDisplay() {
        if (mPlayer != null) {
            mSoundDuration = mPlayer!!.duration / 1000
        }
        if (mIsPlaying) {
            var now = 0f
            if (mPlayer != null) {
                now = (mPlayer!!.currentPosition + mPlayStartOffset).toFloat()
            }
            val frames = mWaveformView?.millisecsToPixels(now.toInt())
            if (frames != null) {
                mWaveformView?.setPlayback(frames)
            }
            if (frames != null) {
                setOffsetGoalNoUpdate(frames - mWidth / 2)
            }
            if (now >= mPlayEndMsec) {
                handlePause()
            }
        }

        if (!mTouchDragging) {
            var offsetDelta: Int
            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80
                } else {
                    mFlingVelocity = 0
                }

                mOffset += offsetDelta

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2
                    mFlingVelocity = 0
                }
                if (mOffset < 0) {
                    mOffset = 0
                    mFlingVelocity = 0
                }
                mOffsetGoal = mOffset
            } else {
                offsetDelta = mOffsetGoal - mOffset
                if (offsetDelta > 10) offsetDelta = offsetDelta / 10
                else if (offsetDelta > 0) offsetDelta = 1
                else if (offsetDelta < -10) offsetDelta = offsetDelta / 10
                else if (offsetDelta < 0) offsetDelta = -1
                else offsetDelta = 0
                mOffset += offsetDelta
            }
        }

        if (mWaveformView != null) {
            if (mWaveformView?.getcurrentmLevel() != 0) {
                if ((mWaveformView?.measuredWidth?.plus(mOffset)
                        ?: 0) >= (mWaveformView?.getcurrentmLevel() ?: 0)
                ) {
                    mOffset =
                        mWaveformView?.getcurrentmLevel()?.minus(mWaveformView?.measuredWidth ?: 0)
                            ?: 0
                    EdgeReached = true
                } else {
                    EdgeReached = false
                }
            }
        }
        mWaveformView?.setParameters(mStartPos, mEndPos, mOffset, mSoundDuration)
        mWaveformView?.invalidate()
        var startX = mStartPos - mOffset - mMarkerLeftInset
        if (startX + mStartMarker!!.width >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler?.postDelayed({
                    mStartVisible = true
                    mStartMarker!!.alpha = 1f
                    mStartMarker!!.alpha = 1f
                }, 0)
            }
        } else {
            if (mStartVisible) {
                mStartMarker!!.alpha = 0f
                mStartVisible = false
            }
            startX = 0
        }
        var endX = mEndPos - mOffset - mEndMarker!!.width + mMarkerRightInset
        if (endX + mEndMarker!!.width >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler?.postDelayed({
                    mEndVisible = true
                    mEndMarker!!.alpha = 1f
                }, 0)
            }
        } else {
            if (mEndVisible) {
                mEndMarker!!.alpha = 0f
                mEndVisible = false
            }
            endX = 0
        }
        val layoutParamsStart = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        mWaveformView?.measuredHeight?.let {
            layoutParamsStart.setMargins(
                startX + marginvalue,
                it, 0, 0
            )
        }
        mStartMarker!!.layoutParams = layoutParamsStart
        val layoutParamsEnd = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        if (endX + marginvalue <= (mWaveformView?.measuredWidth ?: 0)) {
            layoutParamsEnd.setMargins(endX + marginvalue, mWaveformView?.measuredHeight ?: 0, 0, 0)
        } else {
            if (endX <= (mWaveformView?.measuredWidth ?: 0)) {
                layoutParamsEnd.setMargins(
                    mWaveformView?.measuredWidth ?: 0,
                    mWaveformView?.measuredHeight ?: 0,
                    0,
                    0
                )
            } else {
                mWaveformView?.measuredHeight?.let { layoutParamsEnd.setMargins(endX, it, 0, 0) }
            }
        }
        mEndMarker!!.layoutParams = layoutParamsEnd
        mEndMarker!!.alpha
    }

    private fun enableDisableButtons() {
        runOnUiThread {
            if (mIsPlaying) {
                binding.PlayPauseView.toggle()
            } else {
                binding.PlayPauseView.toggle()
            }
        }
    }

    private fun resetPositions() {
        mStartPos = 0
        mEndPos = mMaxPos
    }

    private fun trap(pos: Int): Int {
        if (pos < 0) return 0
        return if (pos > mMaxPos) mMaxPos else pos
    }

    private fun setOffsetGoalStart() = setOffsetGoal(mStartPos - mWidth / 2)
    private fun setOffsetGoalStartNoUpdate() = setOffsetGoalNoUpdate(mStartPos - mWidth / 2)
    private fun setOffsetGoalEnd() = setOffsetGoal(mEndPos - mWidth / 2)
    private fun setOffsetGoalEndNoUpdate() = setOffsetGoalNoUpdate(mEndPos - mWidth / 2)

    private fun setOffsetGoal(offset: Int) {
        setOffsetGoalNoUpdate(offset)
        updateDisplay()
    }

    private fun setOffsetGoalNoUpdate(offset: Int) {
        if (mTouchDragging) {
            return
        }

        mOffsetGoal = offset
        if (mOffsetGoal + mWidth / 2 > mMaxPos) mOffsetGoal = mMaxPos - mWidth / 2
        if (mOffsetGoal < 0) mOffsetGoal = 0
    }

    private fun formatTime(pixels: Int): String {
        return if (mWaveformView != null && mWaveformView?.isInitialized == true) {
            formatDecimal(mWaveformView?.pixelsToSeconds(pixels) ?: 0.toDouble())
        } else {
            ""
        }
    }

    private fun formatDecimal(x: Double): String {
        var xWhole = x.toInt()
        var xFrac = (100 * (x - xWhole) + 0.5).toInt()

        if (xFrac >= 100) {
            xWhole++ //Round up
            xFrac -= 100 //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10 //we need a fraction that is 2 digits long
            }
        }

        return if (xFrac < 10) "$xWhole.0$xFrac"
        else "$xWhole.$xFrac"
    }

    @Synchronized
    private fun handlePause() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
        }
        mWaveformView?.setPlayback(-1)
        mIsPlaying = false
        enableDisableButtons()

    }

    @Synchronized
    private fun onPlay(startPosition: Int) {
        if (mIsPlaying) {
            handlePause()
            return
        }

        if (mPlayer == null) {
            // Not initialized yet
            return
        }

        try {
            mPlayStartMsec = mWaveformView?.pixelsToMillisecs(startPosition) ?: 0
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView?.pixelsToMillisecs(mStartPos) ?: 0
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView?.pixelsToMillisecs(mMaxPos) ?: 0
            } else {
                mPlayEndMsec = mWaveformView?.pixelsToMillisecs(mEndPos) ?: 0
            }

            mPlayStartOffset = 0
            val startFrame = mWaveformView?.secondsToFrames(mPlayStartMsec * 0.001)
            val endFrame = mWaveformView?.secondsToFrames(mPlayEndMsec * 0.001)
            val startByte = mSoundFile!!.getSeekableFrameOffset(startFrame ?: 0)
            val endByte = mSoundFile!!.getSeekableFrameOffset(endFrame ?: 0)
            if (startByte >= 0 && endByte >= 0) {
                mPlayStartOffset = try {
                    mPlayer!!.reset()
                    val subsetInputStream = FileInputStream(mFile!!.absolutePath)
                    mPlayer!!.setDataSource(
                        subsetInputStream.fd,
                        startByte.toLong(),
                        (endByte - startByte).toLong()
                    )
                    mPlayer!!.prepare()
                    mPlayStartMsec
                } catch (e: Exception) {
                    mPlayer!!.reset()
                    mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mPlayer!!.setDataSource(mFile!!.absolutePath)
                    mPlayer!!.prepare()
                    0
                }

            }

            mPlayer!!.setOnCompletionListener { handlePause() }
            mIsPlaying = true
            if (mPlayStartOffset == 0) {
                mPlayer!!.seekTo(mPlayStartMsec)
            }
            val res = audioManager!!.requestAudioFocus(
                afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mPlayer!!.start()
            }
            updateDisplay()
            enableDisableButtons()
        } catch (e: Exception) {
        }

    }

    @SuppressLint("SetTextI18n")
    override fun CreateSelection(startpoint: Double, endpoint: Double) {
        if (mEndPos != -1 || mStartPos != -1) {
            if (mWaveformView != null) {
                val endpointbefore =
                    java.lang.Float.valueOf(mWaveformView?.pixelsToSeconds(mEndPos).toString())
                val endpointafter = java.lang.Float.valueOf(endpoint.toString())
                val propertyValuesHolder =
                    PropertyValuesHolder.ofFloat("phase", endpointbefore, endpointafter)
                val startpointBefore =
                    java.lang.Float.valueOf(mWaveformView?.pixelsToSeconds(mStartPos).toString())
                val startpointAFter = java.lang.Float.valueOf(startpoint.toString())
                val propertyValuesHolder2 =
                    PropertyValuesHolder.ofFloat("phase2", startpointBefore, startpointAFter)
                val mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    this,
                    propertyValuesHolder,
                    propertyValuesHolder2
                )

                mObjectAnimator.addUpdateListener { valueAnimator ->
                    val newEndpos = java.lang.Float.valueOf(
                        valueAnimator.getAnimatedValue(propertyValuesHolder.propertyName).toString()
                    )
                    if (mWaveformView != null) {
                        mEndPos = mWaveformView?.secondsToPixels(newEndpos.toDouble()) ?: 0
                        val NewStartpos = java.lang.Float.valueOf(
                            valueAnimator.getAnimatedValue(propertyValuesHolder2.propertyName)
                                .toString()
                        )

                        mStartPos = mWaveformView?.secondsToPixels(NewStartpos.toDouble()) ?: 0
                        mStartText!!.text =
                            (newEndpos % 3600 / 60).toInt()
                                .toString() + ":" + (newEndpos % 60).toInt()
                                .toString()
                        mEndText!!.text =
                            (NewStartpos % 3600 / 60).toInt()
                                .toString() + ":" + (NewStartpos % 60).toInt()
                                .toString()
                        updateDisplay()
                    }
                }
                mObjectAnimator.start()
                mStartText!!.text =
                    (startpoint % 3600 / 60).toInt().toString() + ":" + (startpoint % 60).toInt()
                        .toString()
                mEndText!!.text =
                    (endpoint % 3600 / 60).toInt().toString() + ":" + (endpoint % 60).toInt()
                        .toString()
                mEndPos = mWaveformView?.secondsToPixels(endpoint) ?: 0
                mStartPos = mWaveformView?.secondsToPixels(startpoint) ?: 0
                updateDisplay()
            }
        }

    }

    fun setPhase(phase: Float) {}
    fun setPhase2(phase2: Float) {}

    override fun onClick(view: View) {
        when {
            view === binding.zoomIn -> waveformZoomIn()
            view === binding.zoomOut -> waveformZoomOut()
            view == binding.PlayPauseView -> onPlay(mStartPos)
            view == binding.markStart -> if (mIsPlaying) {
                mStartPos =
                    mWaveformView?.millisecsToPixels(mPlayer!!.currentPosition + mPlayStartOffset)
                        ?: 0
                updateDisplay()
            }

            view == binding.markEnd -> if (mIsPlaying) {
                mEndPos =
                    mWaveformView?.millisecsToPixels(mPlayer!!.currentPosition + mPlayStartOffset)
                        ?: 0
                updateDisplay()
                handlePause()
            }

            else -> Cutselection(view.id)
        }
    }

    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + this@RingdroidActivity.packageName)
        startActivity(intent)
    }

    private fun Cutselection(which: Int) {
        when (which) {
            R.id.btnSetRingTone -> {
                SaveRingTone()
                mNewFileKind = FILE_KIND_RINGTONE
            }
        }
    }

    private fun SaveRingTone() {
        val startTime = mWaveformView?.pixelsToSeconds(mStartPos)
        val endTime = mWaveformView?.pixelsToSeconds(mEndPos)
        val mStartPosMilliS = mWaveformView?.pixelsToMillisecs(mStartPos)
        val mEndPosMilliS = mWaveformView?.pixelsToMillisecs(mEndPos)
        val startFrame =
            mWaveformView?.secondsToFrames(mStartPosMilliS?.times(0.001) ?: 0.toDouble())
        val numFrames =
            mWaveformView?.secondsToFrames(mEndPosMilliS?.times(0.001) ?: 0.toDouble())
                ?.minus(startFrame ?: 0)
        val fadeTime = mWaveformView?.secondsToFrames(5.0)
        val duration = ((startTime?.let { endTime?.minus(it) } ?: 0.toDouble()) + 0.5).toInt()
        // Create an indeterminate progress dialog
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog?.setTitle(getString(R.string.dialog_saving))
        mProgressDialog?.isIndeterminate = true
        mProgressDialog?.setCancelable(false)
        mProgressDialog?.show()

        mSaveSoundFileThread = object : Thread() {
            override fun run() {
                var outPath: String? =
                    makeRingtoneFilename(binding.EditorSongTitle.text.toString(), ".mp3") ?: return
                outputFile = File(outPath)
                var fallbackToWAV: Boolean? = false
                try {
                    // Write the new file
                    if (startFrame != null) {
                        if (fadeTime != null) {
                            if (numFrames != null) {
                                mSoundFile!!.WriteFile(
                                    outputFile,
                                    startFrame,
                                    numFrames,
                                    false,
                                    false,
                                    fadeTime
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // log the error and try to create a .wav file instead
                    if (outputFile!!.exists()) {
                        outputFile!!.delete()
                    }
                    val writer = StringWriter()
                    e.printStackTrace(PrintWriter(writer))
                    fallbackToWAV = true
                }

                if (fallbackToWAV!!) {
                    outPath = makeRingtoneFilename(binding.EditorSongTitle.text.toString(), ".wav")
                    if (outPath == null) {
                        val runnable = Runnable {}
                        mHandler?.post(runnable)
                        return
                    }
                    outputFile = File(outPath)
                    try {
                        if (startFrame != null) {
                            if (fadeTime != null) {
                                if (numFrames != null) {
                                    mSoundFile!!.writewavfile(
                                        outputFile,
                                        startFrame,
                                        numFrames,
                                        false,
                                        false,
                                        fadeTime
                                    )
                                }
                            }
                        }

                    } catch (e: Exception) {
                        mProgressDialog?.dismiss()
                        if (outputFile!!.exists()) {
                            outputFile!!.delete()
                        }
                        return
                    }
                }
                val finalOutPath = outPath
                val runnable = Runnable {
                    if (endTime != null) {
                        afterSavingRingtone(
                            binding.EditorSongTitle.text.toString(),
                            finalOutPath,
                            duration,
                            endTime
                        )
                    }
                }
                mHandler?.post(runnable)
                mProgressDialog?.dismiss()
            }
        }
        mSaveSoundFileThread!!.start()
    }

    @SuppressLint("InlinedApi")
    private fun afterSavingRingtone(
        title: CharSequence,
        outPath: String?,
        duration: Int,
        endpoint: Double
    ) {
        val outFile = File(outPath!!)
        val fileSize = outFile.length()
        if (fileSize <= 512) {
            outFile.delete()
            AlertDialog.Builder(this).setTitle(getString(R.string.error))
                .setMessage(getString(R.string.file_small))
                .setPositiveButton(getString(R.string.accept), null).setCancelable(false).show()
            return
        }
        // Create the database record, pointing to the existing file path
        val mimeType: String = when {
            outPath.endsWith(".m4a") -> "audio/mp4a-latm"
            outPath.endsWith(".wav") -> "audio/wav"
            else -> "audio/mpeg"
        }
        val artist = "Ringtones"
        val values = ContentValues()
        values.apply {
            put(MediaStore.MediaColumns.DATA, outPath)
            put(MediaStore.MediaColumns.TITLE, title.toString())
            put(MediaStore.MediaColumns.SIZE, fileSize)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.DURATION, duration)
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$title.mp3")
        }

        try {
            val resolver = applicationContext.contentResolver
            val uri = MediaStore.Audio.Media.getContentUriForPath(outPath)
            val ringtoneUri = uri?.let { resolver.insert(it, values) }

            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    ringtoneUri?.let {
                        contentResolver.openOutputStream(it).use { os ->
                            val size = outFile.length().toInt()
                            val bytes = ByteArray(size)
                            try {
                                val buf = BufferedInputStream(FileInputStream(outFile))
                                buf.read(bytes, 0, bytes.size)
                                buf.close()
                                os!!.write(bytes)
                                os.close()
                                os.flush()
                            } catch (e: IOException) {
                                Log.e("Exception1", e.toString())
                            }
                        }
                    }

                    MediaScannerConnection.scanFile(
                        this, arrayOf(outFile.path), null
                    ) { _: String?, uri1: Uri? ->
                        runOnUiThread {
                            dialogLoadingAds?.showDialogLoading()
                            BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                                onLoadAdSuccess = {
                                    dialogLoadingAds?.dismissDialog()
                                }, onAdClose = {
                                    postFinishSetRingtone(uri1)
                                }, onAdLoadFail = {
                                    MaxIntertitial.ShowIntertitialApplovinMax(
                                        this, getString(R.string.appvolin_full)
                                    ) {
                                        dialogLoadingAds?.dismissDialog()
                                        postFinishSetRingtone(uri1)
                                    }
                                })
                        }
                    }
                } catch (ignored: java.lang.Exception) {
                    Log.e("Exception2", ignored.toString())
                }
            } else {
                runOnUiThread {
                    dialogLoadingAds?.showDialogLoading()
                    BaseApplication.getAppInstance().adsFullInApp?.showAds(this,
                        onLoadAdSuccess = {
                            dialogLoadingAds?.dismissDialog()
                        }, onAdClose = {
                            postFinishSetRingtone(ringtoneUri)
                        }, onAdLoadFail = {
                            MaxIntertitial.ShowIntertitialApplovinMax(
                                this, getString(R.string.appvolin_full)
                            ) {
                                dialogLoadingAds?.dismissDialog()
                                postFinishSetRingtone(ringtoneUri)
                            }
                        })
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("Exception3", ex.toString())
        }

    }

    fun postFinishSetRingtone(uri: Uri?) {
        RingtoneManager.setActualDefaultRingtoneUri(
            this, RingtoneManager.TYPE_RINGTONE, uri
        )
        showMessage("${getString(R.string.set_ringtone_success)}\n" + "${getString(R.string.title_songs)}: ${binding.EditorSongTitle.text}")
        EventBus.getDefault().postSticky(EventRingtoneCreate(true))
        finish()
    }

    private fun makeRingtoneFilename(title: CharSequence, extension: String): String? {
        val externalRootDir = AppUtils.getRingtoneStorageDir()
        var parentdir = externalRootDir.path
        if (!externalRootDir.endsWith("/")) {
            parentdir += "/"
        }
        // Create the parent directory
        val parentDirFile = File(parentdir)
        parentDirFile.mkdirs()
        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory) {
            parentdir = externalRootDir.path
        }
        // Turn the title into a filename
        var filename = ""
        for (i in title.indices) {
            if (Character.isLetterOrDigit(title[i])) {
                filename += title[i]
            }
        }
        // Try to make the filename unique
        var path: String? = null
        for (i in 0..99) {
            val testPath: String = if (i > 0) parentdir + filename + i + extension
            else parentdir + filename + extension
            try {
                val f = RandomAccessFile(File(testPath), "r")
                f.close()
            } catch (e: Exception) {
                // Good, the file didn't exist
                path = testPath
                break
            }
        }
        return path
    }

    companion object {
        private val TAG = "ActivityEditorAudio"
        private const val EXTENSION_MP3 = ".mp3"
        const val KEY_SOUND_COLUMN_title = "title"
        const val KEY_SOUND_COLUMN_artist = "artist"
        const val KEY_SOUND_COLUMN_path = "path"
        const val FILE_KIND_RINGTONE = 1

        private fun getTimeFormat(time: String): String {
            return if (time.isNotEmpty() && !time.contains("-")) {
                val Displayedmins: String
                val DisplayedSecs: String
                val mins = java.lang.Double.parseDouble(time) % 3600 / 60
                Displayedmins =
                    if (mins < 10) "0" + mins.toInt().toString() else mins.toInt().toString()
                val secs = java.lang.Double.parseDouble(time) % 60
                DisplayedSecs =
                    if (secs < 10) "0" + secs.toInt().toString() else secs.toInt().toString()
                "$Displayedmins:$DisplayedSecs"
            } else ""
        }
    }
}