package com.musicplayer.mp3player.defaultmusicplayer.database.playlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaylistSongSqLiteHelperDB extends SQLiteOpenHelper {
    public String TABLE_NAME = "";
    public static final String SONG_PATH = "SONG_PATH";
    public static final String SONG_ID = "SONG_ID";
    public static final String TITLE = "TITLE";
    public static final String ARTIST = "ARTIST";
    public static final String ALBULM = "ALBULM";
    public static final String TRACK_NUMBER = "TRACK_NUMBER";
    public static final String ALBUM_ID = "ALBUM_ID";
    public static final String GENRE = "GENRE";
    public static final String YEAR = "YEAR";
    public static final String LYRICS = "LYRICS";
    public static final String ARTIST_ID = "ARTIST_ID";
    public static final String DURATION = "DURATION";
    public static final String COUNT_PLAYING = "COUNT_PLAYING";
    public static final String TIME_PLAY = "HOUR_PLAY";
    public static final String STT = "STT"; //số thứ tự
    public static final String URL_VIDEO = "URL_VIDEO";
    public static final String VIDEO_ID = "VIDEO_ID";
    public static final String TYPE = "TYPE";
    public static final int TYPE_ONLINE = 1;
    public static final int TYPE_LOCAL = 0;
    public String SQL_QUERRY = "";

    private String CREATE_TABLE;

    public PlaylistSongSqLiteHelperDB(Context context, String playlistID) {
        super(context, playlistID, null, 1);
        this.TABLE_NAME = playlistID;
        this.SQL_QUERRY = "SELECT * FROM " + playlistID;
        setCREATE_TABLE(playlistID);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public void setCREATE_TABLE(String table) {
        this.TABLE_NAME = table;
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME
                + "(" + "" + SONG_ID + " INTEGER PRIMARY KEY,"
                + "" + TITLE + " TEXT,"
                + "" + SONG_PATH + " TEXT,"
                + "" + ARTIST + " TEXT,"
                + "" + ALBULM + " TEXT,"
                + "" + TRACK_NUMBER + " INTEGER,"
                + "" + ALBUM_ID + " INTEGER,"
                + "" + GENRE + " TEXT,"
                + "" + YEAR + " TEXT,"
                + "" + LYRICS + " TEXT,"
                + "" + ARTIST_ID + " INTEGER,"
                + "" + DURATION + " TEXT, "
                + "" + COUNT_PLAYING + " INTEGER, "
                + "" + TIME_PLAY + " INTEGER,"
                + "" + STT + " INTEGER,"
                + "" + URL_VIDEO + " TEXT,"
                + "" + TYPE + " INTEGER,"
                + "" + VIDEO_ID + " TEXT"
                + ")";
    }

    public String getTABLE_NAME() {
        return this.TABLE_NAME;
    }

    public String getSQL_QUERRY() {
        return this.SQL_QUERRY;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
