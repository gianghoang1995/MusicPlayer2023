package com.musicplayer.mp3player.playermusic.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.View
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.model.AbsSynchronizedLyrics
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

object MusicUtils {

    fun getLyrics(song: MusicItem): String? {
        var lyrics = ""
        if (song.songPath?.isNotEmpty() == true) {
            val file = File(song.songPath)
            try {
                lyrics = AudioFileIO.read(file).tagOrCreateDefault.getFirst(FieldKey.LYRICS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (lyrics == null || lyrics.trim { it <= ' ' }
                    .isEmpty() || !AbsSynchronizedLyrics.isSynchronized(lyrics)) {
                val dir = file.absoluteFile.parentFile
                if (dir != null && dir.exists() && dir.isDirectory) {
                    val format = ".*%s.*\\.(lrc|txt)"
                    val filename = Pattern.quote(FileUtil.stripExtension(file.name))
                    val songTitle = Pattern.quote(song.title)
                    val patterns = ArrayList<Pattern>()
                    patterns.add(
                        Pattern.compile(
                            String.format(format, filename),
                            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
                        )
                    )
                    patterns.add(
                        Pattern.compile(
                            String.format(format, songTitle),
                            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
                        )
                    )
                    val files = dir.listFiles { f: File ->
                        for (pattern in patterns) {
                            if (pattern.matcher(f.name).matches()) return@listFiles true
                        }
                        false
                    }
                    if (files != null && files.isNotEmpty()) {
                        for (f in files) {
                            try {
                                val newLyrics: String = FileUtil.read(f)
                                if (newLyrics != null && !newLyrics.trim { it <= ' ' }.isEmpty()) {
                                    if (AbsSynchronizedLyrics.isSynchronized(newLyrics)) {
                                        return newLyrics
                                    }
                                    lyrics = newLyrics
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        return lyrics
    }

    private const val TAG = "MusicUtils"

    fun getContentURIForPath(path: String?): Uri? {
        return Uri.fromFile(File(path))
    }

    /*  Try to use String.format() as little as possible, because it creates a
     *  new Formatter every time you call it, which is very inefficient.
     *  Reusing an existing Formatter more than tripled the speed of
     *  makeTimeString().
     *  This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private val sFormatBuilder = StringBuilder()
    private val sFormatter = Formatter(sFormatBuilder, Locale.getDefault())
    private val sTimeArgs = arrayOfNulls<Any>(5)

    // A really simple BitmapDrawable-like class, that doesn't do
    // scaling, dithering or filtering.
    private class FastBitmapDrawable(private val mBitmap: Bitmap) : Drawable() {
        override fun draw(canvas: Canvas) {
            canvas.drawBitmap(mBitmap, 0f, 0f, null)
        }

        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
    }

    private const val sArtId = -2
    private var mCachedBit: Bitmap? = null
    private val sBitmapOptionsCache = BitmapFactory.Options()
    private val sBitmapOptions = BitmapFactory.Options()
    private val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
    private val sArtCache = HashMap<Long, Drawable>()
    private const val sArtCacheId = -1

    fun clearAlbumArtCache() {
        synchronized(sArtCache) { sArtCache.clear() }
    }

    fun getCachedArtwork(
        context: Context,
        artIndex: Long,
        defaultArtwork: BitmapDrawable
    ): Drawable? {
        var d: Drawable? = null
        synchronized(sArtCache) { d = sArtCache[artIndex] }
        if (d == null) {
            d = defaultArtwork
            val icon = defaultArtwork.bitmap
            val w = icon.width
            val h = icon.height
            val b = getArtworkQuick(context, artIndex, w, h)
            if (b != null) {
                d = FastBitmapDrawable(b)
                synchronized(sArtCache) {

                    // the cache may have changed since we checked
                    val value = sArtCache[artIndex]
                    if (value == null) {
                        sArtCache.put(artIndex, d as FastBitmapDrawable)
                    } else {
                        d = value
                    }
                }
            }
        }
        return d
    }

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    private fun getArtworkQuick(context: Context, album_id: Long, w: Int, h: Int): Bitmap? {
        // NOTE: There is in fact a 1 pixel border on the right side in the ImageView
        // used to display this drawable. Take it into account now, so we don't have to
        // scale later.
        var w = w
        w -= 1
        val res = context.contentResolver
        val uri = ContentUris.withAppendedId(sArtworkUri, album_id)
        if (uri != null) {
            var fd: ParcelFileDescriptor? = null
            try {
                fd = res.openFileDescriptor(uri, "r")
                var sampleSize = 1

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true
                BitmapFactory.decodeFileDescriptor(
                    fd!!.fileDescriptor, null, sBitmapOptionsCache
                )
                var nextWidth = sBitmapOptionsCache.outWidth shr 1
                var nextHeight = sBitmapOptionsCache.outHeight shr 1
                while (nextWidth > w && nextHeight > h) {
                    sampleSize = sampleSize shl 1
                    nextWidth = nextWidth shr 1
                    nextHeight = nextHeight shr 1
                }
                sBitmapOptionsCache.inSampleSize = sampleSize
                sBitmapOptionsCache.inJustDecodeBounds = false
                var b = BitmapFactory.decodeFileDescriptor(
                    fd.fileDescriptor, null, sBitmapOptionsCache
                )
                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle()
                        b = tmp
                    }
                }
                return b
            } catch (e: FileNotFoundException) {
            } finally {
                try {
                    fd?.close()
                } catch (e: IOException) {
                }
            }
        }
        return null
    }

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     * This method always returns the default album art icon when no album art is found.
     */
    fun getArtwork(context: Context, song_id: Long, album_id: Long): Bitmap? {
        return getArtwork(context, song_id, album_id, true)
    }

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    fun getArtwork(
        context: Context, song_id: Long, album_id: Long,
        allowdefault: Boolean
    ): Bitmap? {
        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                val bm = getArtworkFromFile(context, song_id, -1)
                if (bm != null) {
                    return bm
                }
            }
            return if (allowdefault) {
                getDefaultArtwork(context)
            } else null
        }
        val res = context.contentResolver
        val uri = ContentUris.withAppendedId(sArtworkUri, album_id)
        if (uri != null) {
            var `in`: InputStream? = null
            return try {
                `in` = res.openInputStream(uri)
                BitmapFactory.decodeStream(`in`, null, sBitmapOptions)
            } catch (ex: FileNotFoundException) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                var bm = getArtworkFromFile(context, song_id, album_id)
                if (bm != null) {
                    if (bm.config == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false)
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context)
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context)
                }
                bm
            } finally {
                try {
                    `in`?.close()
                } catch (ex: IOException) {
                }
            }
        }
        return null
    }

    // get album art for specified file
    private val sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()
    fun getArtworkFromFile(context: Context, songid: Long, albumid: Long): Bitmap? {
        var bm: Bitmap? = null
        val art: ByteArray? = null
        val path: String? = null
        require(!(albumid < 0 && songid < 0)) { "Must specify an album or a song id" }
        try {
            if (albumid < 0) {
                val uri = Uri.parse("content://media/external/audio/media/$songid/albumart")
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val fd = pfd.fileDescriptor
                    bm = BitmapFactory.decodeFileDescriptor(fd)
                }
            } else {
                val uri = ContentUris.withAppendedId(sArtworkUri, albumid)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val fd = pfd.fileDescriptor
                    bm = BitmapFactory.decodeFileDescriptor(fd)
                }
            }
        } catch (ex: FileNotFoundException) {
            //
        }
        if (bm != null) {
            mCachedBit = bm
        }
        return bm
    }

    @SuppressLint("ResourceType")
    private fun getDefaultArtwork(context: Context): Bitmap? {
        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeStream(
            context.resources.openRawResource(R.drawable.ic_song_transparent), null, opts
        )
    }

    fun getIntPref(context: Context, name: String?, def: Int): Int {
        val prefs = context.getSharedPreferences("com.android.music", Context.MODE_PRIVATE)
        return prefs.getInt(name, def)
    }

    fun setIntPref(context: Context, name: String?, value: Int) {
        val prefs = context.getSharedPreferences("com.android.music", Context.MODE_PRIVATE)
        val ed = prefs.edit()
        ed.putInt(name, value)
        ed.commit()
    }

    fun setBackground(v: View, bm: Bitmap?) {
        if (bm == null) {
            v.setBackgroundResource(0)
            return
        }
        val vwidth = v.width
        val vheight = v.height
        val bwidth = bm.width
        val bheight = bm.height
        val scalex = vwidth.toFloat() / bwidth
        val scaley = vheight.toFloat() / bheight
        val scale = Math.max(scalex, scaley) * 1.3f
        val config = Bitmap.Config.ARGB_8888
        val bg = Bitmap.createBitmap(vwidth, vheight, config)
        val c = Canvas(bg)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        val greymatrix = ColorMatrix()
        greymatrix.setSaturation(0f)
        val darkmatrix = ColorMatrix()
        darkmatrix.setScale(.3f, .3f, .3f, 1.0f)
        greymatrix.postConcat(darkmatrix)
        val filter: ColorFilter = ColorMatrixColorFilter(greymatrix)
        paint.colorFilter = filter
        val matrix = Matrix()
        matrix.setTranslate(
            (-bwidth / 2).toFloat(),
            (-bheight / 2).toFloat()
        ) // move bitmap center to origin
        matrix.postRotate(10f)
        matrix.postScale(scale, scale)
        matrix.postTranslate(
            (vwidth / 2).toFloat(),
            (vheight / 2).toFloat()
        ) // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint)
        v.setBackgroundDrawable(BitmapDrawable(bg))
    }
}