package com.musicplayer.mp3player.playermusic.ui.dialog

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.eventbus.BusDeleteSong
import com.musicplayer.mp3player.playermusic.eventbus.BusRefreshDataWhenDelete
import com.musicplayer.mp3player.playermusic.eventbus.BusRenameEvent
import com.musicplayer.mp3player.playermusic.database.playlist.FavoriteDaoDB
import com.musicplayer.mp3player.playermusic.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongDaoDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongSqLiteHelperDB
import com.musicplayer.mp3player.playermusic.model.AlbumItem
import com.musicplayer.mp3player.playermusic.model.ArtistItem
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.service.MusicPlayerService
import com.musicplayer.mp3player.playermusic.ui.activity.list.ListSongActivityCallBack
import com.musicplayer.mp3player.playermusic.ui.activity.soundeditor.RingdroidActivity
import com.musicplayer.mp3player.playermusic.ui.dialog.moresong.DialogMoreSong
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils
import com.musicplayer.mp3player.playermusic.utils.FileUtils
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

open class PopupMoreSongUtils(
    val context: Context,
    val musicPlayerService: MusicPlayerService?,
    val listener: PopUpMoreSongListener,
    private var deleteFileRequestListener: DialogMoreSong.OnDeleteFileRequestListener
) {
    fun showPopupMoreSong(song: MusicItem, pos: Int, view: View?) {
        val listSong = ArrayList<MusicItem>()
        listSong.add(song)
        val popup = PopupMenu(context, view!!)

        popup.menuInflater.inflate(R.menu.popup_search_item, popup.menu)
        if (Build.VERSION.SDK_INT >= 29) {
            popup.menu.findItem(R.id.menuRename).isVisible = false
        }

        if (musicPlayerService?.getCurrentSong()?.id == song.id) {
            popup.menu.findItem(R.id.menuDelete).isVisible = false
        }

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuPlayNext -> {
                    musicPlayerService?.insertNextTrack(song)
                }
                R.id.menuAddQueue -> {
                    musicPlayerService?.addToQueue(listSong)
                }
                R.id.menuAddToPlaylist -> {
                    DialogSelectSong.getDialogAddToPlaylist(
                        context,
                        listSong,
                        object : DialogSelectSong.OnDialogSelectSongListener {
                            override fun onAddToPlaylistDone() {

                            }
                        }).show()
                }
                R.id.menuSetRingtone -> {
                    listener.onSetRingTone(song)
                }
                R.id.menuGoAlbum -> {
                    context.startActivity(
                        Intent(
                            context,
                            ListSongActivityCallBack::class.java
                        ).putExtra(
                            AppConstants.DATA_ALBUM,
                            AlbumItem(
                                song.albumId,
                                song.album,
                                null,
                                song.year?.toInt(),
                                0,
                                ArtworkUtils.getAlbumArtUri(song.albumId.toInt())
                            )
                        )
                    )
                }
                R.id.menuGoArtist -> {
                    context.startActivity(
                        Intent(
                            context,
                            ListSongActivityCallBack::class.java
                        ).putExtra(
                            AppConstants.DATA_ARTIST,
                            ArtistItem(song.artistId, song.artist, 0, 0)
                        )
                    )
                }
                R.id.menuGoFolder -> {
                    val intent = Intent(context, ListSongActivityCallBack::class.java)
                    intent.putExtra(AppConstants.DATA_GOTO_FOLDER, song.songPath)
                    context.startActivity(intent)
                }
                R.id.menuRename -> {
                    showDialogRename(song)
                }
                R.id.menuDelete -> {
                    if (Build.VERSION.SDK_INT >= 30) {
                        deleteFileRequestListener.onRequestDeleteFile(song)
                    } else {
                        showDialogDeleteSong(context, song)
                    }
                }
            }
            true
        }
        popup.show()
    }

    fun setRingtone(ringtone: MusicItem?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                startRingdroidEditor(context, ringtone)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.need_permission),
                    Toast.LENGTH_SHORT
                ).show()
                needPermision(ringtone)
            }
        } else {
            startRingdroidEditor(context, ringtone)
        }
    }

    private fun needPermision(song: MusicItem?) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:${context.packageName}")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startRingdroidEditor(context: Context, song: MusicItem?) {
        val intent = Intent(
            context,
            RingdroidActivity::class.java
        ).putExtra(RingdroidActivity.KEY_SOUND_COLUMN_path, song?.songPath)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_title, song?.title)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_artist, song?.artist)
        context.startActivity(intent)
    }

    private fun showDialogRename(song: MusicItem) {
        val dialogEditText = Dialog(context)
        dialogEditText.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEditText.setContentView(R.layout.dialog_rename)
        dialogEditText.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEditText.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogEditText.findViewById<View>(R.id.tv_cancel) as Button
        val tvTitle = dialogEditText.findViewById<View>(R.id.title_dialog) as TextView
        val btnCreate = dialogEditText.findViewById<View>(R.id.tv_create) as Button
        val edtRename = dialogEditText.findViewById<View>(R.id.edt_name) as EditText

        tvTitle.text = context.getString(R.string.rename)
        btnCreate.text = context.getString(R.string.accept)
        edtRename.setText(song.title)

        btnCancel.setOnClickListener {
            dialogEditText.dismiss()
        }

        btnCreate.setOnClickListener {
            val newTitle = edtRename.text.toString()
            if (newTitle.isEmpty()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.blank_title_song),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (AppUtils.isSpecialCharAvailable(newTitle)) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.name_contain_symbol),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    updateMetadata(newTitle, song)
                    dialogEditText.dismiss()
                    Toast.makeText(
                        context,
                        context.getString(R.string.rename_successfull),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        edtRename.requestFocus()
        dialogEditText.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogEditText.show()
    }

    private fun updateMetadata(newTitle: String, song: MusicItem) {
        var newSong = MusicItem(
            song.id,
            song.title,
            song.artist,
            song.album,
            song.trackNumber,
            song.albumId,
            song.genre,
            song.songPath,
            song.isSelected,
            song.year,
            song.lyrics,
            song.artistId,
            song.duration,
            song.timeAdded,
            song.countPlaying,
            false
        )
        newSong.title = newTitle
        val newPath = song.songPath?.let { FileUtils.renameFile(it, newTitle) }
        val values = ContentValues()
        values.put(MediaStore.Audio.Media.TITLE, newTitle)
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, newTitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, newPath)
        }
        values.put(MediaStore.Audio.Media.DATA, newPath)
        val result = context.contentResolver.update(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            values,
            MediaStore.Audio.Media._ID + "=?",
            arrayOf(song.id.toString())
        )
        if (result != 0) {
            Toast.makeText(context, context.getString(R.string.txt_done), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, context.getString(R.string.cant_rename), Toast.LENGTH_SHORT)
                .show()
        }
        newSong.songPath = newPath
        updateDatabaseSongName(song, newTitle)
        EventBus.getDefault().postSticky(BusRenameEvent(song, newSong))
        listener.onRenameSuccess(newSong)
    }

    private fun updateDatabaseSongName(song: MusicItem, name: String) {
        val favoriteSqliteHelper =
            FavoriteSqliteHelperDB(
                context
            )
        val favoriteDao =
            FavoriteDaoDB(
                context,
                favoriteSqliteHelper
            )
        val listPlaylist = favoriteDao.allFavorite
        for (playlist in listPlaylist) {
            val playlistSongSqLiteHelper =
                PlaylistSongSqLiteHelperDB(
                    context,
                    playlist.favorite_id
                )
            val playlistSongDao =
                PlaylistSongDaoDB(
                    playlistSongSqLiteHelper
                )
            playlistSongDao.updateSong(song.id!!, song)
        }
    }

    private fun showDialogDeleteSong(context: Context, song: MusicItem) {
        val dialogDeleteSong = Dialog(context)
        dialogDeleteSong.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogDeleteSong.setContentView(R.layout.dialog_delete_song)
        dialogDeleteSong.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogDeleteSong.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogDeleteSong?.findViewById<View>(R.id.tv_cancel) as Button
        val btnDelete = dialogDeleteSong?.findViewById<View>(R.id.tv_create) as Button

        btnDelete.setOnClickListener {
            deleteFile(context, song)
            dialogDeleteSong.dismiss()
            EventBus.getDefault().postSticky(BusRefreshDataWhenDelete(true))
            Toast.makeText(context, context.getString(R.string.txt_done), Toast.LENGTH_SHORT).show()
            listener.onDeleteSuccess(song)
        }

        btnCancel.setOnClickListener {
            dialogDeleteSong?.dismiss()
        }
        dialogDeleteSong.show()
    }

    private fun deleteFile(context: Context, song: MusicItem) {
        val fdelete = File(song.songPath)
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                val rootUri =
                    song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
                rootUri?.let {
                    context.contentResolver.delete(
                        it, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath)
                    )
                }
                EventBus.getDefault().postSticky(BusDeleteSong(song))
                context.deleteFile(fdelete.name)
            } else {
                try {
                    fdelete.canonicalFile.delete()
                    if (fdelete.exists()) {
                        val rootUri =
                            song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
                        if (rootUri != null) {
                            context.contentResolver.delete(
                                rootUri, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath)
                            )
                        }
                        context.deleteFile(fdelete.name)
                        EventBus.getDefault().postSticky(BusDeleteSong(song))
                        Toast.makeText(
                            context, context.getString(R.string.txt_done), Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        val rootUri =
            song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
        rootUri?.let {
            context.contentResolver.delete(
                it, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath)
            )
        }
    }

    interface PopUpMoreSongListener {
        fun onRenameSuccess(songResult: MusicItem)
        fun onDeleteSuccess(songResult: MusicItem)
        fun onSetRingTone(song: MusicItem)
    }
}