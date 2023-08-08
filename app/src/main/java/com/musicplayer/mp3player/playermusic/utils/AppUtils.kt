package com.musicplayer.mp3player.playermusic.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.database.playlist.FavoriteDaoDB
import com.musicplayer.mp3player.playermusic.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongDaoDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongSqLiteHelperDB
import com.musicplayer.mp3player.playermusic.eventbus.BusDeleteSong
import com.musicplayer.mp3player.playermusic.model.ItemMusicOnline
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.model.PlaylistITem
import com.musicplayer.mp3player.playermusic.ui.activity.plashscreen.PlashScreenActivity
import com.google.android.gms.ads.AdValue
import org.greenrobot.eventbus.EventBus
import org.jaudiotagger.audio.generic.Utils.getExtension
import java.io.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt


object AppUtils {

    fun checkPermissionFux(context: Activity) {
        if (!isGrantPermission(context)) {
            context.startActivity(Intent(context, PlashScreenActivity::class.java))
            context.finish()
        }
    }

    fun isGrantPermission(context: Context): Boolean {
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (isGrantPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
                 && isGrantPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                 && isGrantPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
             ) {
                 return true
             }
         } else {*/
        if (isGrantPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return true
        }
//        }
        return false
    }

    private fun isGrantPermission(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun getVideoID(url: String): String {
        val separated = url.split("=").toTypedArray()
        return separated[separated.size - 1] // this will contain "Fruit"
    }

    fun decodeBASE64(base64: String?): String? {
        var data: ByteArray?
        if (base64 != null) return try {
            data = Base64.decode(base64, Base64.DEFAULT)
            var s = String(data, StandardCharsets.UTF_8)
            if (TextUtils.isEmpty(s)) {
                s = "Unknown"
            }
            s
        } catch (ex: Exception) {
            "Unknown"
        }
        return "Unknown"
    }

    fun getCurrentMillisecond(): String? {
        val date = Date()
        //This method returns the time in millis
        val timeMilli = date.time
        return timeMilli.toString()
    }

    fun getRandomNumber(max: Int): Int {
        val min = 0
        val r = Random()
        return r.nextInt(max - min + 1) + min
    }

    fun encodeToBASE64(base64Content: String?): String? {
        var data = ByteArray(0)
        if (base64Content != null) {
            data = base64Content.toByteArray(StandardCharsets.UTF_8)
        }
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun isSpecialCharAvailable(s: String?): Boolean {
        if (s == null || s.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        return (s.contains("\\") || s.contains("*") || s.contains("\"") || s.contains(":") || s.contains(
            "?"
        ) || s.contains("/") || s.contains("|") || s.contains("<") || s.contains(">"))
    }

    fun getHighBitrateAudio(mPath: String?): Int {
        val mex = MediaExtractor()
        try {
            if (mPath != null) {
                mex.setDataSource(mPath)
            } else {
                return 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val mf: MediaFormat = mex.getTrackFormat(0)
        val bitRate: Int = mf.getInteger(MediaFormat.KEY_BIT_RATE) / 1000
        return if (bitRate >= 256) bitRate
        else 0
    }


    fun convertDuration(dura: Int): String {
        val sec = dura / 1000 % 60
        val min = dura / 1000 / 60 % 60
        val hour = dura / 1000 / 60 / 60
        var s = ""
        var m = ""
        var h = ""
        s = if (sec < 10) {
            "0$sec"
        } else {
            sec.toString() + ""
        }
        m = if (min < 10) {
            "0$min"
        } else {
            min.toString() + ""
        }
        h = if (hour < 10) {
            "0$hour"
        } else {
            hour.toString() + ""
        }
        var duration = ""
        duration = if (hour == 0) {
            "$m:$s"
        } else {
            "$h:$m:$s"
        }
        return duration
    }

    fun removeLastChar(s: String?): String? {
        return if (s == null || s.isEmpty()) null else s.substring(0, s.length - 1)
    }

    fun convertDuration(dura: Long): String? {
        val sec = dura / 1000 % 60
        val min = dura / 1000 / 60 % 60
        val hour = dura / 1000 / 60 / 60
        var s = ""
        var m = ""
        var h = ""
        s = if (sec < 10) {
            "0$sec"
        } else {
            sec.toString() + ""
        }
        m = if (min < 10) {
            "0$min"
        } else {
            min.toString() + ""
        }
        h = if (hour < 10) {
            "0$hour"
        } else {
            hour.toString() + ""
        }
        var duration = ""
        duration = if (hour == 0L) {
            "$m:$s"
        } else {
            "$h:$m:$s"
        }
        return duration
    }


    fun getRanDom(max: Int): Int {
        val min = 0
        val random: Int = Random().nextInt(max - min + 1) + min
        return random
    }

    fun shareText(context: Context, subject: String?, text: String?) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
    }

    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }

    fun deleteFile(context: Context, song: MusicItem) {
        val path = song.songPath
        val fdelete = File(path)
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                val rootUri =
                    path?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
                context.contentResolver.delete(
                    rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(path)
                )
                context.deleteFile(fdelete.name)
                EventBus.getDefault().postSticky(BusDeleteSong(song))
                Toast.makeText(
                    context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT
                ).show()
            } else {
                try {
                    fdelete.canonicalFile.delete()
                    if (fdelete.exists()) {
                        val rootUri =
                            path?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
                        context.contentResolver.delete(
                            rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(path)
                        )
                        context.deleteFile(fdelete.name)
                        EventBus.getDefault().postSticky(BusDeleteSong(song))
                        Toast.makeText(
                            context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(
                        context, context.getString(R.string.cant_delete_file), Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(
                context, context.getString(R.string.file_not_found), Toast.LENGTH_SHORT
            ).show()
        }
        val rootUri =
            path?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
        context.contentResolver.delete(
            rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(path)
        )
    }

    fun deleteFileNoNotify(context: Context, file: File) {
        if (file.exists()) {
            if (file.delete()) {
                val rootUri =
                    MediaStore.Audio.Media.getContentUriForPath(file.path) // Change file types here
                context.contentResolver.delete(
                    rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(file.path)
                )
                context.deleteFile(file.name)
            } else {
                try {
                    file.canonicalFile.delete()
                    if (file.exists()) {
                        val rootUri =
                            MediaStore.Audio.Media.getContentUriForPath(file.path) // Change file types here
                        context.contentResolver.delete(
                            rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(file.path)
                        )
                        context.deleteFile(file.name)
                    }
                } catch (e: java.lang.Exception) {
                }
            }
        }
        val rootUri =
            MediaStore.Audio.Media.getContentUriForPath(file.path) // Change file types here
        context.contentResolver.delete(
            rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(file.path)
        )
        MediaScannerConnection.scanFile(
            context, arrayOf(file.path), null
        ) { path1: String?, uri: Uri? -> }
    }

    fun deleteFileErrorClean(context: Context, file: File) {
        val rootUri =
            MediaStore.Audio.Media.getContentUriForPath(file.path) // Change file types here
        context.contentResolver.delete(
            rootUri!!, MediaStore.MediaColumns.DATA + "=?", arrayOf(file.path)
        )
        MediaScannerConnection.scanFile(
            context, arrayOf(file.path), null
        ) { path1: String?, uri: Uri? -> }
    }

    fun convertFilePathToMediaID(songPath: String, context: Context): Long {
        var id: Long = 0
        val cr = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = cr.query(uri, projection, "$selection=?", selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        return id
    }

    fun deleteSongFromPlaylist(context: Context?, song: MusicItem?) {
        val favoriteSqliteHelper =
            FavoriteSqliteHelperDB(
                context
            )
        val favoriteDao =
            FavoriteDaoDB(
                context,
                favoriteSqliteHelper
            )
        val lstFavorite: ArrayList<PlaylistITem> = favoriteDao.allFavorite
        for (favorite in lstFavorite) {
            val songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    context,
                    favorite.favorite_id
                )
            val songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            songListDao.deleteLocalSong(song)
        }
    }

    fun getOutputEditTagFolder(): File {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + AppConstants.DEFAULT_FOLDER_EDITTAG
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    fun getOutputThumbFolder(): File {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + AppConstants.DEFAULT_FOLDER_THUMB
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    fun getMimeType(file: File): String {
        var mimeType = ""
        val extension = getExtension(file)
        if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        Log.e("Mime", mimeType + "\n" + file.path)
        return mimeType
    }

    fun getColorWithAlpha(color: Int, ratio: Float): Int {
        return Color.argb(
            (Color.alpha(color) * ratio).roundToInt(),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }

    fun getThumbnailPath(context: Context, songID: Long?): String {
        val rootPath = getThumbnailStorageDir(context).path
        val file = File(rootPath, "${songID}_${System.currentTimeMillis()}.png")
        return file.path
    }

    fun getThemePath(context: Context): String {
        val rootPath = getAppSpecificAlbumStorageDir(context).path
        val file = File(rootPath, AppConstants.THEME_NAME)
        return if (file.exists()) file.path
        else ""
    }

    fun getTempThemeEdited(context: Context): String? {
        val rootPath = getAppSpecificAlbumStorageDir(context).path
        val file = File(rootPath, AppConstants.TEMP_THEME_EDITED)
        return if (file.exists()) file.path
        else ""
    }

    fun makeTempThemesPath(context: Context): String {
        val rootPath = getAppSpecificAlbumStorageDir(context).path
        val file = File(rootPath, AppConstants.TEMP_THEME)
        if (file.exists()) file.delete()
        return file.path
    }

    fun geOldTempThemePath(context: Context): String {
        val rootPath = getAppSpecificAlbumStorageDir(context).path
        val file = File(rootPath, AppConstants.TEMP_THEME)
        return if (file.exists()) file.path
        else ""
    }

    fun convertFileToBitmap(filePath: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(filePath)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
        } catch (e: Exception) {

        }
        return bitmap
    }

    private fun getAppSpecificAlbumStorageDir(context: Context): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "THEMES")
        if (!file.mkdirs()) {
        }
        return file
    }

    private fun getThumbnailStorageDir(context: Context): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "THUMBNAILS")
        if (!file.mkdirs()) {
            Log.e("LOG_TAG", "Directory not created")
        }
        return file
    }

    fun getRingtoneStorageDir(): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
            AppConstants.RINGTONE_FOLDER
        )
        if (!file.mkdirs()) {
            Log.e("LOG_TAG", "Directory not created")
        }
        return file
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun drawableToBitmap(context: Context, resID: Int): Bitmap {
        val drawable: Drawable = context.resources.getDrawable(resID)

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun bitmapToInputStream(bitmap: Bitmap): InputStream {
        val size = bitmap.height * bitmap.rowBytes
        val buffer: ByteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(buffer)
        return ByteArrayInputStream(buffer.array())
    }

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable: Drawable = ContextCompat.getDrawable(context, drawableId)!!
        val bitmap = drawable.intrinsicWidth.let {
            Bitmap.createBitmap(
                it, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
        }
        val canvas = bitmap?.let { Canvas(it) }
        if (canvas != null) {
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }


    fun setBackground(v: ImageView, bm: Bitmap?) {
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
        val scale = scalex.coerceAtLeast(scaley) * 1.3f
        val config = Bitmap.Config.ARGB_8888
        val bg = Bitmap.createBitmap(vwidth, vheight, config)
        val c = Canvas(bg)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
//        val greymatrix = ColorMatrix()
//        greymatrix.setSaturation(0f)
//        val darkmatrix = ColorMatrix()
//        darkmatrix.setScale(.3f, .3f, .3f, 0.5f)
//        greymatrix.postConcat(darkmatrix)
//        val filter: ColorFilter = ColorMatrixColorFilter(greymatrix)
//        paint.colorFilter = filter
        val matrix = Matrix()
        matrix.setTranslate(
            (-bwidth / 2).toFloat(), (-bheight / 2).toFloat()
        ) // move bitmap center to origin
        matrix.postRotate(10f)
        matrix.postScale(scale, scale)
        matrix.postTranslate(
            (vwidth / 2).toFloat(), (vheight / 2).toFloat()
        ) // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint)
        v.setImageBitmap(bg)
    }

    fun blurStack(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
        val bitmap: Bitmap = if (canReuseInBitmap) {
            sentBitmap
        } else {
            sentBitmap.copy(sentBitmap.config, true)
        }
        if (radius < 1) {
            return null
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] =
                    -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }

    fun saveBitmapThemes(context: Context, finalBitmap: Bitmap?) {
        if (finalBitmap != null) {
            val rootPath = getAppSpecificAlbumStorageDir(context).path
            val file = File(rootPath, AppConstants.THEME_NAME)
            if (file.exists()) file.delete()
            try {
                val bos = ByteArrayOutputStream()
                finalBitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, bos
                ) // YOU can also save it in JPEG
                val bitmapdata = bos.toByteArray()

                val fos = FileOutputStream(file)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
            }
        }
    }

    fun saveBitmapTempThemesBlur(context: Context, finalBitmap: Bitmap?) {
        if (finalBitmap != null) {
            val rootPath = getAppSpecificAlbumStorageDir(context).path
            val file = File(rootPath, AppConstants.TEMP_THEME)
            if (file.exists()) file.delete()
            try {
                val bos = ByteArrayOutputStream()
                finalBitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, bos
                ) // YOU can also save it in JPEG
                val bitmapdata = bos.toByteArray()

                val fos = FileOutputStream(file)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
            }
        }
    }

    fun saveBitmapTempThemesDefault(context: Context, finalBitmap: Bitmap?) {
        if (finalBitmap != null) {
            val rootPath = getAppSpecificAlbumStorageDir(context).path
            val file = File(rootPath, AppConstants.TEMP_THEME_EDITED)
            if (file.exists()) file.delete()
            try {
                val bos = ByteArrayOutputStream()
                finalBitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, bos
                ) // YOU can also save it in JPEG
                val bitmapdata = bos.toByteArray()

                val fos = FileOutputStream(file)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
            }
        }
    }

    fun outputPath(): String? {
        val defaultPath = Environment.getExternalStorageDirectory()
            .toString() + File.separator + AppConstants.DEFAULT_FOLDER_NAME
        val dir: File
        dir = if (Build.VERSION.SDK_INT >= 30) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/" + AppConstants.DEFAULT_FOLDER_NAME
            )
        } else {
            File(defaultPath)
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.path
    }

    fun getDownloadFolderPath(): String {
        val defaultPath = Environment.getExternalStorageDirectory()
            .toString() + File.separator + AppConstants.DEFAULT_FOLDER_NAME
        val dir: File = if (Build.VERSION.SDK_INT >= 30) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    .toString() + "/" + AppConstants.DEFAULT_FOLDER_NAME
            )
        } else {
            File(defaultPath)
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.path
    }

    fun isMusicDownloaded(context: Context?, video: ItemMusicOnline): Boolean {
        val dirpath = getDownloadFolderPath()
        val mFileOutput = File("$dirpath/${createTitleFile(video.title)}")
        return mFileOutput.exists()
    }

    fun createTitleFile(title: String): String {
        var fileNameSave = title.replace("/".toRegex(), "").replace("\\\\".toRegex(), "")
            .replace("\\*".toRegex(), "").replace("\\?".toRegex(), "").replace("\"".toRegex(), "")
            .replace("<".toRegex(), "").replace("\\(".toRegex(), "").replace("\\)".toRegex(), "")
            .replace(">".toRegex(), "").replace("\\|".toRegex(), "").replace("|".toRegex(), "")
            .replace("#".toRegex(), "").replace("#".toRegex(), "").replace("\\.".toRegex(), "")
            .replace(":".toRegex(), "")
        fileNameSave = fileNameSave.replace("[\\\\><\"|*?%:#/]".toRegex(), "") + ".mp3"
        return fileNameSave
    }

    fun round(value: Double, places: Int): Double {
        var values = value
        require(places >= 0)
        val factor = 10.0.pow(places.toDouble()).toLong()
        values *= factor
        val tmp = value.roundToInt()
        return tmp.toDouble() / factor
    }

    fun postRevenueToFirebase(ad: AdValue, adUnit: String?) {

    }
}