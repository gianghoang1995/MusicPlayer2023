package com.musicplayer.mp3player.defaultmusicplayer.loader

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseAsyncTaskLoader
import java.util.*
import kotlin.collections.ArrayList

class GetAllFileLoaderTask(
    context: Context?,
    val listener: OnLoadFileListener
) :
    BaseAsyncTaskLoader<ArrayList<Void>>(context) {
    override fun loadInBackground(): ArrayList<Void>? {
        val folderMap: MutableMap<Int, MutableList<String>?> = LinkedHashMap()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.contentResolver?.query(
                    MediaStore.Files.getContentUri("external"),
                    null,
                    MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?",
                    arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT.toString()),
                    null
                ).use { cursor ->
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val data: String =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                            val parentId: Int =
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                            val parentPath = data.substring(0, data.lastIndexOf("/"))
                            Log.e("Data", data.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    interface OnLoadFileListener {
        fun onLoadSuccess()
    }
}