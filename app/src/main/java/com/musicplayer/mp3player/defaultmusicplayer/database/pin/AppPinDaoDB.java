package com.musicplayer.mp3player.defaultmusicplayer.database.pin;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.musicplayer.mp3player.defaultmusicplayer.model.AlbumItem;
import com.musicplayer.mp3player.defaultmusicplayer.model.ArtistItem;

import java.util.ArrayList;

public class AppPinDaoDB {
    private AppPinHelperDB database;
    private SQLiteDatabase sqLiteDatabase;

    public AppPinDaoDB(AppPinHelperDB database) {
        this.database = database;
    }

    @SuppressLint("Range")
    public ArrayList<AlbumItem> getListPinAlbum() {
        ArrayList<AlbumItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(AppPinHelperDB.SQL_QUERY_PIN_ALBUM, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long album_id = cursor.getLong(cursor.getColumnIndex(AppPinHelperDB.ALBUM_ID));
                        String album_name = cursor.getString(cursor.getColumnIndex(AppPinHelperDB.ALBUM_NAME));
                        String artist_name = cursor.getString(cursor.getColumnIndex(AppPinHelperDB.ALBUM_ARTIST_NAME));
                        int year = cursor.getInt(cursor.getColumnIndex(AppPinHelperDB.ALBUM_YEAR));
                        int track_count = cursor.getInt(cursor.getColumnIndex(AppPinHelperDB.ALBUM_TRACK_COUNT));
                        String album_thumb = cursor.getString(cursor.getColumnIndex(AppPinHelperDB.ALBUM_THUMB));
                        Uri uri = Uri.parse(album_thumb);
                        AlbumItem album = new AlbumItem(album_id, album_name, artist_name, year, track_count, uri);
                        list.add(album);
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

    public boolean insertPinAlbum(AlbumItem album) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppPinHelperDB.ALBUM_ID, album.getId());
        contentValues.put(AppPinHelperDB.ALBUM_NAME, album.getAlbumName());
        contentValues.put(AppPinHelperDB.ALBUM_ARTIST_NAME, album.getArtistName());
        contentValues.put(AppPinHelperDB.ALBUM_YEAR, album.getYear());
        contentValues.put(AppPinHelperDB.ALBUM_TRACK_COUNT, album.getTrackCount());
        contentValues.put(AppPinHelperDB.ALBUM_THUMB, album.getAlbumThumb().toString());
        sqLiteDatabase.insert(AppPinHelperDB.TABLE_ALBUM, null, contentValues);
        return true;
    }

    public long deletePinAlbum(AlbumItem album) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        return result = sqLiteDatabase.delete(AppPinHelperDB.TABLE_ALBUM, AppPinHelperDB.ALBUM_ID + " = ?", new String[]{String.valueOf(album.getId())});
    }

    public boolean isPinAlbum(AlbumItem album) {
        String[] whereArgs = {String.valueOf(album.getId())};
        sqLiteDatabase = database.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.query(
                AppPinHelperDB.TABLE_ALBUM,
                null,
                AppPinHelperDB.ALBUM_ID + " = ?",
                whereArgs,
                null,
                null,
                null)) {
            if (cursor.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("Range")
    public ArrayList<ArtistItem> getListPinArtist() {
        ArrayList<ArtistItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(AppPinHelperDB.SQL_QUERY_PIN_ARTIST, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long artist_ID = cursor.getLong(cursor.getColumnIndex(AppPinHelperDB.ARTIST_ID));
                        String artist_name = cursor.getString(cursor.getColumnIndex(AppPinHelperDB.ARTIST_NAME));
                        int album_count = cursor.getInt(cursor.getColumnIndex(AppPinHelperDB.ARTIST_ALBUM_COUNT));
                        int track_count = cursor.getInt(cursor.getColumnIndex(AppPinHelperDB.ARTIST_TRACK_COUNT));
                        ArtistItem artist = new ArtistItem(artist_ID, artist_name, album_count, track_count);
                        list.add(artist);
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    public boolean insertPinArtist(ArtistItem artist) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppPinHelperDB.ARTIST_ID, artist.getId());
        contentValues.put(AppPinHelperDB.ARTIST_NAME, artist.getName());
        contentValues.put(AppPinHelperDB.ARTIST_ALBUM_COUNT, artist.getAlbumCount());
        contentValues.put(AppPinHelperDB.ARTIST_TRACK_COUNT, artist.getTrackCount());
        sqLiteDatabase.insert(AppPinHelperDB.TABLE_ARTIST, null, contentValues);
        return true;
    }

    public long deletePinArtist(ArtistItem artist) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        return result = sqLiteDatabase.delete(AppPinHelperDB.TABLE_ARTIST, AppPinHelperDB.ARTIST_ID + " = ?", new String[]{String.valueOf(artist.getId())});
    }

    public boolean isPinArtist(ArtistItem artist) {
        String[] whereArgs = {String.valueOf(artist.getId())};
        sqLiteDatabase = database.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.query(
                AppPinHelperDB.TABLE_ARTIST,
                null,
                AppPinHelperDB.ARTIST_ID + " = ?",
                whereArgs,
                null,
                null,
                null)) {
            if (cursor.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

}


