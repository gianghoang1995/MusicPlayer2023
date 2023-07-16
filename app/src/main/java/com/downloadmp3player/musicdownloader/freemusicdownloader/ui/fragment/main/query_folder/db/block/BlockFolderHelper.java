package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlockFolderHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "BLOCK_FOLDER_DB";
    public static final String TABLE_NAME = "BLOCK_FOLDER";
    public static final String FOLDER_NAME = "FOLDER_NAME";
    public static final String FOLDER_PATH = "FOLDER_PATH";
    public static final String FOLDER_ID = "FOLDER_ID";
    public static final String SQL_QUERRY = "SELECT * FROM " + TABLE_NAME;

    public BlockFolderHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                "" + FOLDER_PATH + " NVARCHAR PRIMARY KEY NOT NULL," +
                "" + FOLDER_NAME + " NVARCHAR," +
                "" + FOLDER_ID + " INTEGER" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
