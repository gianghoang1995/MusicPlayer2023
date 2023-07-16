package com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteSqliteHelperDB extends SQLiteOpenHelper {
    public static final String DB_NAME = "FavoriteSqliteHelper";
    public static final String DEFAULT_FAVORITE = "DEFAULT_FAVORITE";
    public static final String TABLE_RECENT_ADDED = "TABLE_RECENT_ADDED";/*thêm gần đây*/
    public static final String TABLE_LAST_PLAYING = "TABLE_LAST_PLAYING";/*phát cuối cùng*/
    public static final String TABLE_MOST_PLAYING = "TABLE_MOST_PLAYING";/*phát nhiều nhất*/

    public static final String TABLE_NAME = "FAVORITE_TABLE";
    public static final String ID = "ID";
    public static final String FAVORITE_NAME = "FAVORITE_NAME";
    public static final String FAVORITE_ID = "FAVORITE_ID";
    public static final String SQL_QUERY_BY_ID = "SELECT * FROM " + TABLE_NAME + " ORDER BY ID ASC";

    public FavoriteSqliteHelperDB(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                "" + ID + " INTEGER, " +
                "" + FAVORITE_ID + " NVARCHAR PRIMARY KEY, " +
                "" + FAVORITE_NAME + " NVARCHAR) ";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
