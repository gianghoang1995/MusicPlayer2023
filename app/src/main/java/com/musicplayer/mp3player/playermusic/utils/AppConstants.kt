package com.musicplayer.mp3player.playermusic.utils

import android.content.Context
import com.musicplayer.mp3player.playermusic.equalizer.R

object AppConstants {
    const val PUBLISHER_NAME = "DefaultMusicPlayer"
    val DEF_EMAIL_FEEDBACK = "catherinegodgiven@gmail.com"
    const val POLICY_URL = "https://sites.google.com/view/pikashotpolicy/policy"
    const val DEFAULT_FOLDER_NAME = "PikaShot"

    const val KEY_LANGUAGE_SELECTED = "KEY_LANGUAGE_SELECTED"
    const val BASE_GG_SEARCH = "https://www.google.com/search?q=lyric+"
    const val INTERVAL_UPDATE_PROGRESS: Long = 100
    const val ACTION_SET_DATA_ONLINE = "ACTION_SET_DATA_ONLINE"
    const val PREF_CONFIG_APP = "pref.config.app"
    const val CATEGORY_DATA = "CATEGORY_DATA"
    const val RINGTONE_FOLDER = "Ringtone"
    const val KEYWORD_SEARCH = "keyword"
    const val THEME_NAME = "_theme.png"
    const val TEMP_THEME = "_temp_theme.png"
    const val TEMP_THEME_EDITED = "_temp_theme_edited.png"
    const val THEME_OVERLAY_PROGRESS = "THEME_OVERLAY_PROGRESS"
    const val THEME_BLUR_PROGRESS = "THEME_BLUR_PROGRESS"
    const val DEFAULT_FOLDER_EDITTAG = "TAG MUSIC"
    const val DEFAULT_FOLDER_THUMB = "Thumbnail"
    const val IS_SHOW_LOCK = "IS_SHOW_LOCK"
    const val PREF_SHAKE_COUNT = "PREF_SHAKE_COUNT"
    const val ACTION_SHAKE = "ACTION_SHAKE"
    const val PREF_SHAKE = "PREF_SHAKE"
    const val PREF_HEADPHONE = "PREF_HEADPHONE"
    const val PREF_END_CALL = "PREF_END_CALL"
    const val PREF_LOCK_SCREEN = "PREF_LOCK_SCREEN"
    const val PREF_FONT_SIZE = "PREF_FONT_SIZE"
    const val SKIP_DURATION = "SKIP_DURATION"
    const val ALBUM_ORDER_BY = "ALBUM_ORDER_BY"
    const val ALBUM_SORT_BY = "ALBUM_SORT_BY"
    const val SONG_ORDER_BY = "SONG_ORDER_BY"
    const val SONG_SORT_BY = "SONG_SORT_BY"
    const val ARTIST_ORDER_BY = "ARTIST_ORDER_BY"
    const val ARTIST_SORT_BY = "ARTIST_SORT_BY"
    const val SHOW_ADS = "ADS"
    const val ACTION_RESTART = "action_restart_music"
    const val PREF_ID_PLAYLIST = "pref_id_playlist"
    const val MAX_VALUE = 3000
    const val MIN_VALUE = -1500
    const val CENTER_VALUE = 1500
    const val ACTION_SET_DATA_PLAYER = "action_setdata_player"
    const val ACTION_TOGGLE_PLAY = "action_toggle_play"
    const val ACTION_STOP = "action_stop"
    const val ACTION_PRIVE = "action_prive"
    const val ACTION_NEXT = "action_next"
    const val ACTION_CHANGE_PRESET_EQUALIZER = "action_change_preset_equalizer"

    const val TYPE_OBJECT = 0
    const val TYPE_ADS = 1
    fun testDevices(): List<String> {
        return listOf(
            "DE84AB3B057C90AF8FBD9446091BD425",
            "B989BCAD52A558D37AFC3C508D411920",
            "0B9F875B3EBF0F07D8943433B82D7CF5",
            "D1001C40D12FB59956A18DF8A1503981",
            "5A8167409A32C7D3AB959286717678D3",
            "CC09F07C28216F699FDED6BA5B5D1DC5",
            "29657BA4F8C7933429901148C62F5C0A",
            "708460235ADAAE8E83F4A80732AA04FC",
            "7AC3EF4B7EC73FFFC746DC27BF61F5D8"
        )
    }

    const val OPEN_FROM_NOTIFICATION = "open_from_notifycation"
    const val LOOP_MODE = "loop_mode"
    const val PRESET_NUMBER = "preset_number"
    const val EQUALIZER_STATUS = "equalizer_status"
    const val EQUALIZER_SLIDER_CHANGE = "equalizer_slider_change"
    const val EQUALIZER_SLIDER_VALUE = "equalizer_slider_values"
    const val VIRTUALIZER_STATUS = "virtualizer_status"
    const val VIRTUALIZER_STRENGTH = "virtualizer_strength"
    const val BASSBOSSTER_STATUS = "bassboster_status"
    const val BASSBOSSTER_STRENGTH = "bassboster_strength"
    const val SET_TIMER = "set_timmer"
    const val TIMER_TEXT = "timer_text"
    const val PREF_DONT_SHOW_RATE = "dont_show_rate"
    const val PREF_TIME_LATTER_30_MIN = "time_30min"
    val fileExtensions = arrayOf(
        ".aac", ".mp3", ".wav", ".ogg", ".midi", ".3gp", ".mp4", ".m4a", ".amr", ".flac"
    )
    const val DATA_ALBUM = "data_album"
    const val SHORTCUT_ALBUM_ID = "SHORTCUT_ALBUM_ID"
    const val SHORTCUT_ALBUM_NAME = "SHORTCUT_ALBUM_NAME"
    const val SHORTCUT_ARTIST_ID = "SHORTCUT_ARTIST_ID"
    const val SHORTCUT_ARTIST_NAME = "SHORTCUT_ARTIST_NAME"
    const val SHORTCUT_PLAYLIST_ID = "SHORTCUT_PLAYLIST_ID"
    const val SHORTCUT_PLAYLIST_NAME = "SHORTCUT_PLAYLIST_NAME"
    const val SHORTCUT_PLAYLIST_FAVORITE_ID = "SHORTCUT_PLAYLIST_FAVORITE_ID"
    const val DATA_FOLDER = "data_folder"
    const val SHORTCUT_FOLDER_PATH = "SHORTCUT_FOLDER_PATH"
    const val DATA_GOTO_FOLDER = "data_goto_folder"
    const val DATA = "data"
    const val DATA_ARTIST = "data_artist"
    const val DATA_PLAYLIST = "data_playlist"
    const val PREF_SORT_LIST_BY = "PREF_SORT_LIST_BY"
    const val PREF_ORDER_LIST_BY = "PREF_ORDER_LIST_BY"

    @Retention(AnnotationRetention.SOURCE)
    annotation class LOOP {
        companion object {
            var LOOP_SHUFFLE = 888
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class FONT {
        companion object {
            var SMALL = 0.85f
            var NORMAL = 1f
            var LARGE = 1.3f
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class SORT {
        companion object {
            var SORT_BY_CUSTOM = 0
            var SORT_BY_NAME = 1
            var SORT_BY_ALBUM = 2
            var SORT_BY_ARTIST = 3
            var SORT_BY_DURATION = 4

        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class ORDER {
        companion object {
            var ORDER_ASC = 0
            var ORDER_DESC = 1
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class SELECTALL {
        companion object {
            var NONE = 0
            var SELECT_ALL = 1
            var UNSELECT_ALL = 999
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class PLAYBACK_STATE {
        companion object {
            var STATE_PLAYING = 0
            var STATE_PAUSED = 1
            var STATE_BUFFER = 999
            var STATE_END = 999
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class TYPE_ONLINE {
        companion object {
            var TYPE_YT = 0
            var TYPE_SCLOUD = 1
        }
    }

    fun getListThemes(context: Context?): ArrayList<Any?> {
        val listTheme = ArrayList<Any?>()
        val customThemes = context?.let { AppUtils.getTempThemeEdited(it) }
        listTheme.add(R.drawable.temp_theme)
        if (customThemes?.isNotEmpty() == true) {
            listTheme.add(customThemes)
        }
        listTheme.add(R.drawable.theme_default)
        listTheme.add(R.drawable.theme_1)
        listTheme.add(R.drawable.theme_2)
        listTheme.add(R.drawable.theme_3)
        listTheme.add(R.drawable.theme_4)
        listTheme.add(R.drawable.theme_5)
        listTheme.add(R.drawable.theme_6)
        listTheme.add(R.drawable.theme_7)
        listTheme.add(R.drawable.theme_8)
        listTheme.add(R.drawable.theme_9)
        listTheme.add(R.drawable.theme_10)
        listTheme.add(R.drawable.theme_11)
        listTheme.add(R.drawable.theme_12)
        listTheme.add(R.drawable.theme_13)
        listTheme.add(R.drawable.theme_14)
        listTheme.add(R.drawable.theme_15)
        listTheme.add(R.drawable.theme_16)
        listTheme.add(R.drawable.theme_17)
        listTheme.add(R.drawable.theme_19)
        listTheme.add(R.drawable.theme_20)
        return listTheme
    }

    fun randomThumb(): Int {
        val listDrawer = java.util.ArrayList<Int>()
        listDrawer.add(R.drawable.thumb_1)
        listDrawer.add(R.drawable.thumb_2)
        listDrawer.add(R.drawable.thumb_3)
        listDrawer.add(R.drawable.thumb_4)
        listDrawer.add(R.drawable.thumb_5)
        listDrawer.add(R.drawable.thumb_6)
        listDrawer.add(R.drawable.thumb_7)
        listDrawer.add(R.drawable.thumb_8)
        listDrawer.add(R.drawable.thumb_9)
        listDrawer.add(R.drawable.thumb_10)
        listDrawer.add(R.drawable.thumb_11)
        listDrawer.add(R.drawable.thumb_12)
        listDrawer.add(R.drawable.thumb_13)
        listDrawer.add(R.drawable.thumb_14)
        listDrawer.add(R.drawable.thumb_15)
        return listDrawer[AppUtils.getRandomNumber(listDrawer.size - 1)]
    }
}