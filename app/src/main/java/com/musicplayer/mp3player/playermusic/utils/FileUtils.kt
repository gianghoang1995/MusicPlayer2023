package com.musicplayer.mp3player.playermusic.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.musicplayer.mp3player.playermusic.BaseApplication
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*

object FileUtils {
    fun getStorageSDDirectories(context: Context): String {
        return try {
            var storageDirectories: Array<String>? = null
            val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val results: MutableList<String> = ArrayList()
                val externalDirs = context.getExternalFilesDirs(null)
                for (file in externalDirs) {
                    var path: String? = null
                    path = try {
                        file.path.split("/Android").toTypedArray()[0]
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    if (path != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Environment.isExternalStorageRemovable(
                                file
                            ) || rawSecondaryStoragesStr != null && rawSecondaryStoragesStr.contains(
                                path
                            )
                        ) {
                            results.add(path)
                        }
                    }
                }
                storageDirectories = results.toTypedArray()
            }
            if (storageDirectories?.size == 0) ""
            else storageDirectories!![0]

        } catch (ex: Exception) {
            ""
        }
    }

    fun getExtSdCardPaths(context: Context): String? {
        val externalStorageFilesDirs = context.getExternalFilesDirs(null)
        val primaryStorageFilesDir = context.getExternalFilesDir(null)
        for (extFilesDir in externalStorageFilesDirs) {
            if (extFilesDir != null && extFilesDir != primaryStorageFilesDir) {
                val rootPathEndIndex = extFilesDir.absolutePath.lastIndexOf("/Android/data")
                if (rootPathEndIndex < 0) {
                } else {
                    var path = extFilesDir.absolutePath.substring(0, rootPathEndIndex)
                    try {
                        path = File(path).canonicalPath
                    } catch (e: IOException) {
                    }
                    return path
                }
            }
        }
        return null
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun renameFile(filePath: String, newName: String): String {
        val file = File(filePath)
        val oldName = file.name
        val dir = file.parentFile
        if (dir.exists()) {
            val from = File(dir, oldName)
            val to = File(dir, newName + "." + file.extension)
            if (from.exists()) from.renameTo(to)
            return to.path
        }
        return ""
    }

    fun getExtSdCardPaths(): Array<String>? {
        val paths: MutableList<String> = ArrayList()
        for (file in BaseApplication.getAppInstance().getExternalFilesDirs("external")) {
            if (file != null && file != BaseApplication.getAppInstance().getExternalFilesDir("external")) {
                val index = file.absolutePath.lastIndexOf("/Android/data")
                if (index < 0) {
                    Log.w("asd", "Unexpected external file dir: " + file.absolutePath)
                } else {
                    var path = file.absolutePath.substring(0, index)
                    try {
                        path = File(path).canonicalPath
                    } catch (e: IOException) {
                        // Keep non-canonical path.
                    }
                    paths.add(path)
                }
            }
        }
        return paths.toTypedArray()
    }

    private fun getExtSdCardFolder(file: File): String {
        val extSdPaths: Array<String>? =getExtSdCardPaths()
        try {
            for (i in extSdPaths?.indices!!) {
                if (extSdPaths[i].let { file.canonicalPath.startsWith(it) }) {
                    return extSdPaths[i]
                }
            }
        } catch (e: IOException) {
            return ""
        }
        return ""
    }
    fun getDocumentFile(file: File): DocumentFile? {
        val baseFolder: String = getExtSdCardFolder(file)
        var relativePath: String? = null
        relativePath = try {
            val fullPath = file.canonicalPath
            fullPath.substring(baseFolder.length + 1)
        } catch (e: IOException) {
            Log.e("getDocumentFile", e.message.toString())
            return null
        }
        val treeUri: Uri =
            BaseApplication.getAppInstance().contentResolver.persistedUriPermissions[0].uri
                ?: return null

        // start with root of SD card and then parse through document tree.
        var document = DocumentFile.fromTreeUri(BaseApplication.getAppInstance(), treeUri)
        val parts = relativePath?.split("\\/".toRegex())?.toTypedArray()
        if (parts != null) {
            for (part in parts) {
                val nextDocument = document!!.findFile(part)
                if (nextDocument != null) {
                    document = nextDocument
                }
            }
        }
        return document
    }

    fun copyFile(sourceFile: File?, destFile: File) {
        var destination: FileChannel? = null
        var source: FileChannel? = null
        var pfd: ParcelFileDescriptor? = null
        try {
            source = FileInputStream(sourceFile).channel
            if (isFromSdCard(destFile.absolutePath)) {
                val documentFile: DocumentFile? = getDocumentFile(destFile)
                pfd = documentFile?.uri?.let {
                    BaseApplication.getAppInstance().contentResolver
                        .openFileDescriptor(it, "w")
                }
                val outputStream = FileOutputStream(pfd?.fileDescriptor)
                destination = outputStream.channel
            } else {
            destination = FileOutputStream(destFile).channel
            }
            if (!destFile.parentFile.exists()) destFile.parentFile.mkdirs()
            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            destination.transferFrom(source, 0, source.size())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("Errror coppyfile", "MESSAGE -" + e.message + "CAUSE " + e.cause)
        } finally {
            pfd?.close()
            source?.close()
            destination?.close()
        }
    }

    @Throws(IOException::class)
    fun cutFile(sourceFile: File?, destFile: File): File {
        var destination: FileChannel? = null
        var source: FileChannel? = null
        var pfd: ParcelFileDescriptor? = null
        try {
            source = FileInputStream(sourceFile).channel
            if (isFromSdCard(destFile.absolutePath)) {
                val documentFile: DocumentFile? = getDocumentFile(destFile)
                pfd = documentFile?.uri?.let {
                    BaseApplication.getAppInstance().contentResolver
                        .openFileDescriptor(it, "w")
                }
                val outputStream = FileOutputStream(pfd?.fileDescriptor)
                destination = outputStream.channel
            } else {
                destination = FileOutputStream(destFile).channel
            }
            if (!destFile.parentFile.exists()) destFile.parentFile.mkdirs()
            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            destination.transferFrom(source, 0, source.size())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("cutFile","MESSAGE -" + e.message + "CAUSE " + e.cause)
        } finally {
            if (sourceFile != null) {
                deleteRecursive(sourceFile)
            }
            pfd?.close()
            source?.close()
            destination?.close()
        }
        return destFile
    }

    open fun deleteRecursive(fileOrDirectory: File): Boolean {
        if (fileOrDirectory.isDirectory) if (fileOrDirectory.listFiles() != null) for (child in fileOrDirectory.listFiles()) {
            return deleteRecursive(child)
        }
        if (isFromSdCard(fileOrDirectory.absolutePath)) {
            if (fileOrDirectory.delete()) {
                return deleteViaContentProvider(fileOrDirectory.absolutePath)
            }
        } else {
            val documentFile: DocumentFile? =getDocumentFile(fileOrDirectory)
            if (documentFile?.delete() == true) {
                return deleteViaContentProvider(fileOrDirectory.absolutePath)
            }
        }
        return false
    }

    fun deleteViaContentProvider(fullname: String?): Boolean {
        val uri: Uri = getFileUri(BaseApplication.getAppInstance(), fullname) ?: return false
        return try {
            val resolver: ContentResolver = BaseApplication.getAppInstance().contentResolver
            // change type to image, otherwise nothing will be deleted
            val contentValues = ContentValues()
            val media_type = 1
            contentValues.put("media_type", media_type)
            resolver.update(uri, contentValues, null, null)
            resolver.delete(uri, null, null) > 0
        } catch (e: Throwable) {
            false
        }
    }

    private fun getFileUri(context: Context, fullname: String?): Uri? {
        var uri: Uri? = null
        var cursor: Cursor? = null
        var contentResolver: ContentResolver? = null
        try {
            contentResolver = context.contentResolver
            if (contentResolver == null) return null
            uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOfNulls<String>(2)
            projection[0] = "_id"
            projection[1] = "_data"
            val selection = "_data = ? " // this avoids SQL injection
            val selectionParams = arrayOfNulls<String>(1)
            selectionParams[0] = fullname
            val sortOrder = "_id"
            cursor = contentResolver.query(uri, projection, selection, selectionParams, sortOrder)
            if (cursor != null) {
                try {
                    if (cursor.count > 0) // file present!
                    {
                        cursor.moveToFirst()
                        val dataColumn = cursor.getColumnIndex("_data")
                        val s = cursor.getString(dataColumn)
                        if (s != fullname) return null
                        val idColumn = cursor.getColumnIndex("_id")
                        val id = cursor.getLong(idColumn)
                        uri = MediaStore.Files.getContentUri("external", id)
                    } else  // file isn't in the media database!
                    {
                        val contentValues = ContentValues()
                        contentValues.put("_data", fullname)
                        uri = MediaStore.Files.getContentUri("external")
                        uri = contentResolver.insert(uri, contentValues)
                    }
                } catch (e: Throwable) {
                    uri = null
                } finally {
                    cursor.close()
                }
            }
        } catch (e: Throwable) {
            uri = null
        }
        return uri
    }

    fun isFromSdCard(filepath: String): Boolean {
        val path1 = Environment.getExternalStorageDirectory().toString()
        return !filepath.startsWith(path1)
    }


}
