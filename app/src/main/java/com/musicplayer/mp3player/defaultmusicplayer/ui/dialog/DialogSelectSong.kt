package com.musicplayer.mp3player.defaultmusicplayer.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.adapter.PlaylistAdapter
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickPlaylistListener
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventDeleteSong
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventPutLoveSong
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventRefreshDataPlaylist
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteDaoDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.PlaylistSongDaoDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.PlaylistSongSqLiteHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.model.PlaylistITem
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.service.MusicPlayerService
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

object DialogSelectSong {
    interface OnDialogSelectSongListener {
        fun onAddToPlaylistDone()
    }

    @SuppressLint("SetTextI18n")
    fun getDialogAddToPlaylist(
        context: Context,
        listSong: ArrayList<MusicItem>,
        listener: OnDialogSelectSongListener
    ): Dialog {
        val favoriteSqliteHelper =
            FavoriteSqliteHelperDB(
                context
            )
        val favoriteDao =
            FavoriteDaoDB(
                context,
                favoriteSqliteHelper
            )
        val dialogAddToPlaylist = Dialog(context, R.style.DialogSlideAnim)
        dialogAddToPlaylist.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAddToPlaylist.setContentView(R.layout.dialog_add_to_playlist)
        val window = dialogAddToPlaylist.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.BOTTOM
        window.attributes = wlp
        dialogAddToPlaylist.setCancelable(true)
        dialogAddToPlaylist.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogAddToPlaylist.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCreatePlaylist =
            dialogAddToPlaylist.findViewById<View>(R.id.btnCreatePlaylist) as ConstraintLayout

        btnCreatePlaylist.setOnClickListener {
            dialogAddToPlaylist.dismiss()
            showDialogEditText(context, listSong, listener)
        }

        val imgTheme = dialogAddToPlaylist.findViewById<ImageView>(R.id.imgTheme)
        imgTheme.setImageBitmap(BaseApplication.getAppInstance().bmImg)
        val rv_favorite = dialogAddToPlaylist.findViewById<View>(R.id.rv_favorite) as RecyclerView
        val favoriteAdapter = PlaylistAdapter(context, true, object : OnClickPlaylistListener {
            override fun onPlaylistClick(favorite: PlaylistITem, i: Int) {
                val table_name: String? = favorite.favorite_id
                val sqliteHelper =
                    PlaylistSongSqLiteHelperDB(
                        context,
                        table_name
                    )
                val songListDao =
                    PlaylistSongDaoDB(
                        sqliteHelper
                    )
                if (listSong.size > 1) {
                    for (i in listSong) {
                        songListDao.insertToLocalPlaylist(i)
                    }
                    listener.onAddToPlaylistDone()
                    Toast.makeText(context, context.getString(R.string.txt_done), Toast.LENGTH_LONG)
                        .show()
                } else {
                    if (songListDao.insertToLocalPlaylist(listSong[0]) != (-1).toLong()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.txt_done),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.song_exits_playlist),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                dialogAddToPlaylist.dismiss()
                if (favorite.name.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE)) {
                    EventBus.getDefault().postSticky(EventPutLoveSong(true))
                }
                EventBus.getDefault().postSticky(EventRefreshDataPlaylist(true))
            }

            override fun onPlaylistMoreClick(favorite: PlaylistITem, view: View) {

            }
        })
        val listFavorite = favoriteDao.customFavorite
        favoriteAdapter.hideDotView()
        favoriteAdapter.setDataPlaylist(listFavorite)
        rv_favorite.setHasFixedSize(true)
        rv_favorite.layoutManager = LinearLayoutManager(context)
        rv_favorite.adapter = favoriteAdapter
        return dialogAddToPlaylist
    }

    private fun showDialogEditText(
        context: Context,
        listSong: ArrayList<MusicItem>,
        listener: OnDialogSelectSongListener
    ) {
        val favoriteSqliteHelper =
            FavoriteSqliteHelperDB(
                context
            )
        val favoriteDao =
            FavoriteDaoDB(
                context,
                favoriteSqliteHelper
            )
        val dialogEditText = Dialog(context)
        dialogEditText.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEditText.setContentView(R.layout.dialog_editext)
        dialogEditText.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEditText.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val create_Cancel = dialogEditText.findViewById<View>(R.id.tv_cancel) as Button
        val create_Create = dialogEditText.findViewById<View>(R.id.tv_create) as Button
        val create_Edittext = dialogEditText.findViewById<View>(R.id.edt_name) as EditText

        create_Create.setOnClickListener {
            val name = create_Edittext.text.toString()
            if (name.isNotEmpty()) {
                if (favoriteDao.insertFavorite(name)) {
                    dialogEditText?.dismiss()
                    getDialogAddToPlaylist(context, listSong, object : OnDialogSelectSongListener {
                        override fun onAddToPlaylistDone() {
                            listener.onAddToPlaylistDone()
                        }
                    }).show()
                    EventBus.getDefault().postSticky(EventRefreshDataPlaylist(true))
                } else {
                    Toast.makeText(
                        context, context.getString(R.string.duplicate_playlist), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context, context.getString(R.string.empty_playlist), Toast.LENGTH_SHORT
                ).show()
            }
        }

        create_Cancel.setOnClickListener {
            dialogEditText.dismiss()
        }
        create_Edittext.requestFocus()
        dialogEditText.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogEditText.show()
    }

    fun showDialogDeleteSong(
        context: Context,
        listSong: ArrayList<MusicItem>,
        listener: OnDeleteFileSuccess
    ): Dialog {
        val dialogDeleteSong = Dialog(context)
        dialogDeleteSong.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogDeleteSong.setContentView(R.layout.dialog_delete_song)
        dialogDeleteSong.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogDeleteSong.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogDeleteSong.findViewById<View>(R.id.tv_cancel) as Button
        val btnDelete = dialogDeleteSong.findViewById<View>(R.id.tv_create) as Button

        btnDelete.setOnClickListener {
            var intent = Intent(context, MusicPlayerService::class.java)
            intent.action = AppConstants.ACTION_STOP
            context.startService(intent)
            if (Build.VERSION.SDK_INT >= 30) {
                dialogDeleteSong.dismiss()
                listener.onDeleteFileInAndroidQ()
            } else {
                for (item in listSong) {
                    deleteFile(context, item)
                }
                dialogDeleteSong.dismiss()
                Toast.makeText(context, context.getString(R.string.txt_done), Toast.LENGTH_SHORT)
                    .show()
                listener.onDeleteSuccess()
            }
        }

        btnCancel.setOnClickListener {
            dialogDeleteSong.dismiss()
        }
        return dialogDeleteSong
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
                EventBus.getDefault().postSticky(EventDeleteSong(song))
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
                        EventBus.getDefault().postSticky(EventDeleteSong(song))
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

    interface OnDeleteFileSuccess {
        fun onDeleteSuccess()
        fun onDeleteFileInAndroidQ()
    }
}