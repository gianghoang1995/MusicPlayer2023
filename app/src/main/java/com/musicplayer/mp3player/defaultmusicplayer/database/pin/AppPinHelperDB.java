package com.musicplayer.mp3player.defaultmusicplayer.database.pin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppPinHelperDB extends SQLiteOpenHelper {
    public static final String DB_NAME = "AppPinHelper";
    public static final String TABLE_ALBUM = "TABLE_ALBUM";
    public static final String ALBUM_ID = "ALBUM_ID";
    public static final String ALBUM_NAME = "ALBUM_NAME";
    public static final String ALBUM_ARTIST_NAME = "ALBUM_ARTIST_NAME";
    public static final String ALBUM_YEAR = "ALBUM_YEAR";
    public static final String ALBUM_TRACK_COUNT = "ALBUM_TRACK_COUNT";
    public static final String ALBUM_THUMB = "ALBUM_THUMB";
    public static final String SQL_QUERY_PIN_ALBUM = "SELECT * FROM " + TABLE_ALBUM;

    public static final String TABLE_ARTIST = "TABLE_ARTIST";
    public static final String ARTIST_ID = "ARTIST_ID";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String ARTIST_ALBUM_COUNT = "ARTIST_ALBUM_COUNT";
    public static final String ARTIST_TRACK_COUNT = "ARTIST_TRACK_COUNT";
    public static final String SQL_QUERY_PIN_ARTIST = "SELECT * FROM " + TABLE_ARTIST;

    public AppPinHelperDB(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_PIN_ALBUM = "CREATE TABLE IF NOT EXISTS " + TABLE_ALBUM + "(" +
                "" + ALBUM_ID + " INTEGER PRIMARY KEY NOT NULL," +
                "" + ALBUM_NAME + " NVARCHAR," +
                "" + ALBUM_ARTIST_NAME + " NVARCHAR," +
                "" + ALBUM_YEAR + " INTEGER," +
                "" + ALBUM_TRACK_COUNT + " INTEGER," +
                "" + ALBUM_THUMB + " NVARCHAR" +
                ")";

        String CREATE_TABLE_PIN_ARTIST = "CREATE TABLE IF NOT EXISTS " + TABLE_ARTIST + "(" +
                "" + ARTIST_ID + " INTEGER PRIMARY KEY NOT NULL," +
                "" + ARTIST_NAME + " NVARCHAR," +
                "" + ARTIST_ALBUM_COUNT + " INTEGER," +
                "" + ARTIST_TRACK_COUNT + " INTEGER" +
                ")";

        db.execSQL(CREATE_TABLE_PIN_ALBUM);
        db.execSQL(CREATE_TABLE_PIN_ARTIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
