package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.folder.loader

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem
import java.util.*
import kotlin.collections.ArrayList

class ScanningFolderAsync(private var context: Context, private var scaningFolderListener: ScaningFolderListener) : AsyncTask<Void, Int, ArrayList<FolderItem>>() {
    var sortorder: String? = null
    @SuppressLint("Range")
    override fun doInBackground(vararg params: Void?): ArrayList<FolderItem> {
        val folders: ArrayList<FolderItem> = ArrayList()
        val folderMap: MutableMap<Int, MutableList<String>?> = LinkedHashMap()
        try {
            val query = if (Build.VERSION.SDK_INT >= 29) {
                "media_type = 2"
            } else {
                "${MediaStore.Audio.Media.IS_MUSIC + " != 0"} AND media_type = 2"
            }
            context.contentResolver?.query(
                    MediaStore.Files.getContentUri("external"), null, query, null, null
            ).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val data: String = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                        val parentId: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                        var relativePath =""
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            relativePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH))
                        }
                        val parentPath = data.substring(0, data.lastIndexOf("/"))
                        if (!folderMap.containsKey(parentId)) {
                            folderMap[parentId] = ArrayList(
                                    listOf(parentPath)
                            )
                        } else {
                            folderMap[parentId]!!.add(parentPath)
                        }

                    }

                    for ((key, value) in folderMap) {
                        val parentPath = value!![0]
                        val size = folderMap[key]!!.size
                        if (size > 0) {
                            val folder = FolderItem(
                                    parentPath.substring(
                                            parentPath.lastIndexOf("/") + 1, parentPath.length
                                    ), folderMap[key]!!.size, parentPath, key
                            )
                            folders.add(folder)
                        }
                    }

                }
            }
        } catch (e: Exception) {
        }
        return folders
    }

    override fun onPostExecute(result: ArrayList<FolderItem>) {
        super.onPostExecute(result)
        scaningFolderListener.onScanningMusicFolderSuccess(result)
    }

}