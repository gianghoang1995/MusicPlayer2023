package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.moresong

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.PlaylistAdapter
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickPlaylistListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventDeleteSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventPutLoveSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRefreshDataPlaylist
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventRenameEvent
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail.AppDatabase
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.PlaylistITem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.list.ListSongActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.soundeditor.RingdroidActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.DialogSelectSong
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.FileUtils
import org.greenrobot.eventbus.EventBus
import java.io.*


class DialogMoreSong(
    private var context: Context,
    private var listener: DialogMoreSongCallback,
    private var showDeletePlaylist: Boolean,
    private var showGoTo: Boolean,
    private var curentSong: MusicItem?,
    private var deleteFileRequestListener: OnDeleteFileRequestListener
) {
    var dialogMoreSong: Dialog? = null
    var favoriteSqliteHelper =
        FavoriteSqliteHelperDB(
            context
        )
    var favoriteDao =
        FavoriteDaoDB(
            context,
            favoriteSqliteHelper
        )

    @SuppressLint("CutPasteId")
    fun getDialog(song: MusicItem): Dialog? {
        dialogMoreSong = Dialog(context, R.style.DialogSlideAnim)
        dialogMoreSong?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogMoreSong?.setContentView(R.layout.dialog_more_song)
        val window = dialogMoreSong?.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.BOTTOM
        window.attributes = wlp
        dialogMoreSong?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogMoreSong?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val imgBackground = dialogMoreSong?.findViewById<View>(R.id.imgBackground) as ImageView
        val imgThumb = dialogMoreSong?.findViewById<View>(R.id.imgThumb) as ShapeableImageView
        val tv_title = dialogMoreSong?.findViewById<View>(R.id.tv_title) as TextView
        val tv_artist = dialogMoreSong?.findViewById<View>(R.id.tv_artist) as TextView
        val btn_next_track = dialogMoreSong?.findViewById<View>(R.id.btn_next_track) as Button
        val btn_add_to_playlist =
            dialogMoreSong?.findViewById<View>(R.id.btn_add_to_playlist) as Button
        val btn_set_ringtone = dialogMoreSong?.findViewById<View>(R.id.btn_set_ringtone) as Button
        val btn_detail = dialogMoreSong?.findViewById<View>(R.id.btn_detail) as Button
        val btn_share = dialogMoreSong?.findViewById<View>(R.id.btn_share) as Button
        val btndelete = dialogMoreSong?.findViewById<View>(R.id.btn_delete) as Button
        val btn_delete_playlist_song =
            dialogMoreSong?.findViewById<View>(R.id.btn_delete_playlist_song) as Button
        val btnGoArtist = dialogMoreSong?.findViewById(R.id.btnGoArtist) as TextView
        val btnGoAlbum = dialogMoreSong?.findViewById(R.id.btnGoAlbum) as TextView
        val btnGoFolder = dialogMoreSong?.findViewById(R.id.btnGoFolder) as TextView

        imgBackground.setImageBitmap(BaseApplication.getAppInstance().bmImg)

        if (showGoTo) {
            btnGoAlbum.visibility = View.VISIBLE
            btnGoArtist.visibility = View.VISIBLE
            btnGoFolder.visibility = View.VISIBLE
        } else {
            btnGoAlbum.visibility = View.GONE
            btnGoArtist.visibility = View.GONE
            btnGoFolder.visibility = View.GONE
        }

        if (showDeletePlaylist) {
            btn_delete_playlist_song.visibility = View.VISIBLE
            btndelete.visibility = View.GONE
        } else {
            btn_delete_playlist_song.visibility = View.GONE
            btndelete.visibility = View.VISIBLE
        }

        if (curentSong?.id == song.id) {
            btndelete.visibility = View.GONE
        } else {
            btndelete.visibility = View.VISIBLE
        }
        /*init*/
        setAvtThumbnail(context, song, imgThumb)
        tv_title.text = song.title
        tv_artist.text = song.artist

        btndelete.setOnClickListener {
            dialogMoreSong?.dismiss()
            if (Build.VERSION.SDK_INT >= 30) {
                deleteFileRequestListener.onRequestDeleteFile(song)
            } else {
                showDialogDeleteSong(context, song)
            }
        }

        if (curentSong != null) {
            if (curentSong?.songPath.equals(song.songPath)) {
                btndelete.visibility = View.GONE
                btn_next_track.visibility = View.GONE
            }
        }

        btn_delete_playlist_song.setOnClickListener {
            listener.onDeleteSongPlaylist()
        }

        btn_next_track.setOnClickListener {
            listener.onNextTrack()
        }

        btn_add_to_playlist.setOnClickListener {
            showDialogAddToPlaylist(song, object : DialogSelectSong.OnDialogSelectSongListener {
                override fun onAddToPlaylistDone() {

                }
            })
            listener.onAddToPlaylist()
        }

        btn_set_ringtone.setOnClickListener {
            listener.onSetRingtone()
            dialogMoreSong?.dismiss()
        }

        btn_detail.setOnClickListener {
            listener.onDetail()
            showDialogDetail(song)
        }

        btn_share.setOnClickListener {
            listener.onShare()
        }

        btnGoArtist.setOnClickListener {
            context.startActivity(
                Intent(context, ListSongActivity::class.java).putExtra(
                    AppConstants.DATA_ARTIST,
                    ArtistItem(song.artistId, song.artist, 0, 0)
                )
            )
            dialogMoreSong?.dismiss()
        }
        btnGoAlbum.setOnClickListener {
            context.startActivity(
                Intent(context, ListSongActivity::class.java).putExtra(
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
            dialogMoreSong?.dismiss()
        }
        btnGoFolder.setOnClickListener {
            var intent = Intent(context, ListSongActivity::class.java)
            intent.putExtra(AppConstants.DATA_GOTO_FOLDER, song.songPath)
            context.startActivity(intent)
            dialogMoreSong?.dismiss()
        }
        return dialogMoreSong
    }

    fun setAvtThumbnail(context: Context, song: MusicItem, imgThumb: ShapeableImageView) {
        val thumbStoreDB = AppDatabase(context)
        val thumbStore =
            thumbStoreDB.thumbDao().findThumbnailByID(song.id)
        if (thumbStore != null) {
            Glide.with(context)
                .load(thumbStore.thumbPath)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_song_transparent).into(imgThumb)
        } else {
            val uri = song.id.let { ArtworkUtils.getArtworkFromSongID(it) }
            if (!Uri.EMPTY.equals(uri)) {
                Glide.with(context).load(uri)
                    .placeholder(R.drawable.ic_song_transparent)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgThumb)
            } else {
                Glide.with(context)
                    .load(song.albumId.let { it1 ->
                        ArtworkUtils.uri(
                            it1
                        )
                    })
                    .placeholder(R.drawable.ic_song_transparent).transition(
                        DrawableTransitionOptions.withCrossFade()
                    ).into(imgThumb)
            }
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
        val btnCancel = dialogDeleteSong.findViewById<View>(R.id.tv_cancel) as Button
        val btnDelete = dialogDeleteSong.findViewById<View>(R.id.tv_create) as Button

        btnDelete.setOnClickListener {
            AppUtils.deleteSongFromPlaylist(context, song)
            deleteFile(song)
            dialogDeleteSong.dismiss()
        }

        btnCancel.setOnClickListener {
            dialogDeleteSong.dismiss()
        }
        dialogDeleteSong.show()
    }

    private fun startRingdroidEditor(context: Context, song: MusicItem) {
        val intent = Intent(
            context,
            RingdroidActivity::class.java
        ).putExtra(RingdroidActivity.KEY_SOUND_COLUMN_path, song.songPath)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_title, song.title)
            .putExtra(RingdroidActivity.KEY_SOUND_COLUMN_artist, song.artist)
        context.startActivity(intent)
    }

    fun showDialogRename(song: MusicItem) {
        val dialogEditText = Dialog(context)
        dialogEditText.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogEditText.setContentView(R.layout.dialog_rename)
        dialogEditText.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogEditText.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnCancel = dialogEditText.findViewById<View>(R.id.tv_cancel) as Button
        val tvTitle = dialogEditText.findViewById<View>(R.id.title_dialog) as TextView
        val btnRename = dialogEditText.findViewById<View>(R.id.tv_create) as Button
        val edtRename = dialogEditText.findViewById<View>(R.id.edt_name) as EditText

        tvTitle.text = context.getString(R.string.rename)
        btnRename.text = context.getString(R.string.accept)
        edtRename.setText(song.title)

        btnCancel.setOnClickListener {
            dialogEditText.dismiss()
        }

        btnRename.setOnClickListener {
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

    fun updateDatabaseSongName(song: MusicItem, name: String) {
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
            playlistSongDao.updateSong(song.id, song)
        }
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
        newSong.songPath = song.songPath
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
        EventBus.getDefault().postSticky(EventRenameEvent(song, newSong))
    }

    @SuppressLint("SetTextI18n")
    fun showDialogDetail(song: MusicItem) {
        val thumbStoreDB = AppDatabase(context)
        val dialogInfor = Dialog(context)
        dialogInfor.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogInfor.setContentView(R.layout.dialog_info)
        val window = dialogInfor.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        window.attributes = wlp
        dialogInfor.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogInfor.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        val imgThumb = dialogInfor.findViewById<View>(R.id.imgThumb) as ShapeableImageView
        val tvContent = dialogInfor.findViewById<View>(R.id.content) as TextView

        context.let {
            val thumbStore = thumbStoreDB.thumbDao().findThumbnailByID(song.id)
            if (thumbStore != null) {
                Glide.with(it)
                    .load(thumbStore.thumbPath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_song_transparent)
                    .into(imgThumb)
            } else {
                val uri = ArtworkUtils.getArtworkFromSongID(song.id)
                if (!Uri.EMPTY.equals(uri)) {
                    Glide.with(it).load(uri)
                        .placeholder(R.drawable.ic_song_transparent)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgThumb)
                } else {
                    Glide.with(it).load(ArtworkUtils.uri(song.albumId))
                        .placeholder(R.drawable.ic_song_transparent).transition(
                            DrawableTransitionOptions.withCrossFade()
                        ).into(imgThumb)
                }
            }
        }

        tvContent.text =
            "${context.getString(R.string.title_songs)}: ${song.title}\n" + "${context.getString(R.string.tit_artist)}: ${song.artist}\n" + "${
                context.getString(R.string.location)
            }: ${song.songPath}\n" + "${context.getString(R.string.duration)}: ${
                song.duration?.toLong()?.let {
                    AppUtils.convertDuration(
                        it
                    )
                }
            }\n"
        dialogInfor.show()
    }

    @SuppressLint("SetTextI18n")
    fun showDialogAddToPlaylist(song: MusicItem, listener: DialogSelectSong.OnDialogSelectSongListener) {
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
            showDialogEditText(song)
        }
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
                if (songListDao.insertToLocalPlaylist(song) != (-1).toLong()) {
                    Toast.makeText(context, context.getString(R.string.txt_done), Toast.LENGTH_LONG)
                        .show()
                    listener.onAddToPlaylistDone()

                } else {
                    Toast.makeText(
                        context, context.getString(R.string.song_exits_playlist), Toast.LENGTH_LONG
                    ).show()
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
        dialogAddToPlaylist.show()
    }

    private fun showDialogEditText(song: MusicItem) {
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
                    dialogEditText.dismiss()
                    showDialogAddToPlaylist(
                        song,
                        object : DialogSelectSong.OnDialogSelectSongListener {
                            override fun onAddToPlaylistDone() {

                            }
                        })
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
        dialogEditText.window
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogEditText.show()
    }

    fun deleteFile(song: MusicItem) {
        val fdelete = File(song.songPath)
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                listener.onDelete()
                val rootUri =
                    song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
                if (rootUri != null) {
                    context.contentResolver.delete(
                        rootUri, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath)
                    )
                }
                EventBus.getDefault().postSticky(EventDeleteSong(song))
                context.deleteFile(fdelete.name)
                Toast.makeText(context, context.getString(R.string.txt_done), Toast.LENGTH_SHORT).show()
            } else {
                try {
                    fdelete.canonicalFile.delete()
                    if (fdelete.exists()) {
                        val rootUri =
                            song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
                        if (rootUri != null) {
                            context.contentResolver.delete(
                                rootUri,
                                MediaStore.MediaColumns.DATA + "=?",
                                arrayOf(song.songPath)
                            )
                        }
                        context.deleteFile(fdelete.name)
                        listener.onDelete()
                        EventBus.getDefault().postSticky(EventDeleteSong(song))
                        Toast.makeText(
                            context, context.getString(R.string.txt_done), Toast.LENGTH_SHORT
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
            listener.onDelete()
            Toast.makeText(
                context, context.getString(R.string.file_not_found), Toast.LENGTH_SHORT
            ).show()
        }
        val rootUri =
            song.songPath?.let { MediaStore.Audio.Media.getContentUriForPath(it) } // Change file types here
        if (rootUri != null) {
            context.contentResolver.delete(
                rootUri, MediaStore.MediaColumns.DATA + "=?", arrayOf(song.songPath)
            )
        }
    }

    interface OnDeleteFileRequestListener {
        fun onRequestDeleteFile(song: MusicItem)
    }

}