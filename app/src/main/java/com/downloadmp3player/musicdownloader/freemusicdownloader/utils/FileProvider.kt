package com.downloadmp3player.musicdownloader.freemusicdownloader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.downloadmp3player.musicdownloader.freemusicdownloader.BuildConfig
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import java.io.File

class FileProvider : FileProvider() {
    val AUDIO_TYPE = "audio/mpeg"

    fun getUriForFile(context: Context, filePath: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                    context, BuildConfig.FILES_AUTHORITY, File(filePath)
            )
        } else Uri.parse("file://$filePath")
    }

    fun share(context: Context, path: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, getUriForFile(context, path))
        shareIntent.putExtra(
                Intent.EXTRA_SUBJECT, path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
        )
        shareIntent.type = AUDIO_TYPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
                Intent.createChooser(
                        shareIntent, context.getString(R.string.share)
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun share(context: Context, paths: List<String>) {
        val fileUris = ArrayList<Uri>(paths.size)
        for (path in paths) {
            fileUris.add(getUriForFile(context, path))
        }
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND_MULTIPLE
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
        shareIntent.type = AUDIO_TYPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
                Intent.createChooser(
                        shareIntent, context.getString(R.string.share)
                )
        )
    }

    fun shareListSong(context: Context, paths: ArrayList<MusicItem>) {
        val fileUris = ArrayList<Uri>(paths.size)
        for (song in paths) {
            song.songPath?.let { getUriForFile(context, it) }?.let { fileUris.add(it) }
        }
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND_MULTIPLE
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
        shareIntent.type = AUDIO_TYPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
                Intent.createChooser(
                        shareIntent, context.getString(R.string.share)
                )
        )
    }

    fun view(context: Context, path: String) {
        val viewIntent = Intent(Intent.ACTION_VIEW)
        viewIntent.setDataAndType(getUriForFile(context, path), AUDIO_TYPE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
                Intent.createChooser(
                        viewIntent, context.getString(R.string.open_with)
                )
        )
    }
}