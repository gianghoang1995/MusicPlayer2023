package com.musicplayer.mp3player.playermusic.database.lyric;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LyricsHelperDB extends SQLiteOpenHelper {
    public static final int TYPE_HTML = 0;
    public static final int TYPE_COPPY = 999;
    public static final String DB_NAME = "LyricsHelperDB";
    public static final String TABLE_NAME = "LYRICS";
    public static final String NAME_SONG = "NAME_SONG";
    public static final String PATH_SONG = "PATH_SONG";
    public static final String LYRIC_SONG = "LYRIC_SONG";
    public static final String TYPE_LYRIC = "TYPE_LYRIC"; // 0: HTML,   999: Coppy Text
    public static final String SQL_QUERRY = "SELECT * FROM " + TABLE_NAME;

    public LyricsHelperDB(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
//                "" + ID + " INTEGER PRIMARY KEY NOT NULL," +
                "" + PATH_SONG + " NVARCHAR PRIMARY KEY NOT NULL," +
                "" + NAME_SONG + " NVARCHAR," +
                "" + LYRIC_SONG + " NVARCHAR," +
                "" + TYPE_LYRIC + " INTEGER" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
