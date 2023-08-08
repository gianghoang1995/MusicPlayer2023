package com.musicplayer.mp3player.playermusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.callback.OnClickSettingListener
import com.musicplayer.mp3player.playermusic.adapter.viewholder.SettingViewHolder
import com.musicplayer.mp3player.playermusic.model.SettingItem

class SettingAdapterRV(private val context: Context?, private val listener: OnClickSettingListener?) :
    RecyclerView.Adapter<SettingViewHolder>() {
    var listSetting = ArrayList<SettingItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return SettingViewHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listSetting.size
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.bind(listSetting[position])
        holder.itemView.setOnClickListener {
            listener?.onItemSettingClick(position)
        }
    }

    fun initList() {
        listSetting.add(
            SettingItem(
                context?.getString(R.string.setting),
                R.drawable.ic_menu_setting
            )
        )
        listSetting.add(SettingItem(context?.getString(R.string.language), R.drawable.ic_language))
        listSetting.add(
            SettingItem(
                context?.getString(R.string.ringtone_cutter),
                R.drawable.ic_menu_ringtone
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.timer_stop_music),
                R.drawable.ic_menu_timmer
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.scan_media),
                R.drawable.ic_menu_scan_media
            )
        )
        listSetting.add(SettingItem(context?.getString(R.string.theme), R.drawable.ic_menu_themes))
        listSetting.add(
            SettingItem(
                context?.getString(R.string.title_equalizer),
                R.drawable.ic_menu_equalizer
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.hiden_folder),
                R.drawable.ic_menu_hiden_folder
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.unblock_album),
                R.drawable.ic_unblock_album
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.unblock_artist),
                R.drawable.ic_unblock_artist
            )
        )
        listSetting.add(SettingItem(context?.getString(R.string.guide), R.drawable.ic_menu_guide))
        listSetting.add(
            SettingItem(
                context?.getString(R.string.privacy),
                R.drawable.ic_menu_privacy
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.share),
                R.drawable.ic_menu_share_gd
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.more_app),
                R.drawable.ic_menu_more_app
            )
        )
        listSetting.add(
            SettingItem(
                context?.getString(R.string.text_rate),
                R.drawable.ic_menu_rate
            )
        )
        listSetting.add(SettingItem(context?.getString(R.string.exit_app), R.drawable.ic_menu_exit))
    }
}