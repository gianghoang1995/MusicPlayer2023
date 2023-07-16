package com.downloadmp3player.musicdownloader.freemusicdownloader.database.lyric;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class LyricsDaoDB {
    private LyricsHelperDB database;
    private SQLiteDatabase sqLiteDatabase;

    public LyricsDaoDB(LyricsHelperDB database) {
        this.database = database;
    }

    public LyricsOnline findLyric(String songPath) {
        ArrayList<LyricsOnline> listLyric = getAllLyric();
        for (LyricsOnline lyricsOnline : listLyric) {
            if (lyricsOnline.pathSong.equals(songPath)) {
                return lyricsOnline;
            }
        }
        return null;
    }

    public boolean isContain(String songPath) {
        ArrayList<LyricsOnline> listLyric = getAllLyric();
        for (LyricsOnline lyricsOnline : listLyric) {
            if (lyricsOnline.pathSong.equals(songPath)) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("Range")
    public ArrayList<LyricsOnline> getAllLyric() {
        ArrayList<LyricsOnline> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(LyricsHelperDB.SQL_QUERRY, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String nameSong = cursor.getString(cursor.getColumnIndex(LyricsHelperDB.NAME_SONG));
                        String pathSong = cursor.getString(cursor.getColumnIndex(LyricsHelperDB.PATH_SONG));
                        String lyricData = cursor.getString(cursor.getColumnIndex(LyricsHelperDB.LYRIC_SONG));
                        int typeLyric = cursor.getInt(cursor.getColumnIndex(LyricsHelperDB.TYPE_LYRIC));
                        LyricsOnline lyric = new LyricsOnline(pathSong, nameSong, lyricData, typeLyric);
                        list.add(lyric);
                    }
                }
                cursor.close();
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    public boolean insertLyric(LyricsOnline lyric) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LyricsHelperDB.NAME_SONG, lyric.nameSong);
        contentValues.put(LyricsHelperDB.PATH_SONG, lyric.pathSong);
        contentValues.put(LyricsHelperDB.LYRIC_SONG, lyric.lyricData);
        contentValues.put(LyricsHelperDB.TYPE_LYRIC, lyric.typeLyric);
        long result = sqLiteDatabase.insert(LyricsHelperDB.TABLE_NAME, null, contentValues);
        //-1 = failed
        return result != -1;
    }

    public boolean updateLyric(LyricsOnline lyric) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LyricsHelperDB.NAME_SONG, lyric.nameSong);
        contentValues.put(LyricsHelperDB.PATH_SONG, lyric.pathSong);
        contentValues.put(LyricsHelperDB.LYRIC_SONG, lyric.lyricData);
        contentValues.put(LyricsHelperDB.TYPE_LYRIC, lyric.typeLyric);
        long result = sqLiteDatabase.update(LyricsHelperDB.TABLE_NAME, contentValues, LyricsHelperDB.PATH_SONG + " = ?", new String[]{String.valueOf(lyric.pathSong)});
        return result != -1;
    }

    public boolean deletePreset(String pathSong) {
        sqLiteDatabase = database.getWritableDatabase();
        long result = sqLiteDatabase.delete(LyricsHelperDB.TABLE_NAME, LyricsHelperDB.PATH_SONG + " = ?", new String[]{String.valueOf(pathSong)});
        return result != -1;
    }
}


