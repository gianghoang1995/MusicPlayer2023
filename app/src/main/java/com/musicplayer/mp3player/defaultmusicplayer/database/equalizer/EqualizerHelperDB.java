package com.musicplayer.mp3player.defaultmusicplayer.database.equalizer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EqualizerHelperDB extends SQLiteOpenHelper {
    public static final String DB_NAME = "EqualizerHelper";
    public static final String DEFAULT_CUSTOM = "Custom";
    public static final String TABLE_NAME = "TABLE_MAME";
    public static final String CL_NAME = "CL_NAME";
    public static final String CL_SLIDER1 = "CL_SLIDER0";
    public static final String CL_SLIDER2 = "CL_SLIDER1";
    public static final String CL_SLIDER3 = "CL_SLIDER2";
    public static final String CL_SLIDER4 = "CL_SLIDER3";
    public static final String CL_SLIDER5 = "CL_SLIDER4";
    public static final String SQL_QUERRY = "SELECT * FROM " + TABLE_NAME;

    public EqualizerHelperDB(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                "" + CL_NAME + " NVARCHAR PRIMARY KEY NOT NULL," +
                "" + CL_SLIDER1 + " INTEGER," +
                "" + CL_SLIDER2 + " INTEGER," +
                "" + CL_SLIDER3 + " INTEGER," +
                "" + CL_SLIDER4 + " INTEGER," +
                "" + CL_SLIDER5 + " INTEGER" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
