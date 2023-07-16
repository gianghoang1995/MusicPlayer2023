package com.downloadmp3player.musicdownloader.freemusicdownloader.database.block;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem;

import java.util.ArrayList;

public class AppBlockDaoBD {
    private AppBlockHelperDB database;
    private SQLiteDatabase sqLiteDatabase;

    public AppBlockDaoBD(AppBlockHelperDB database) {
        this.database = database;
    }

    @SuppressLint("Range")
    public ArrayList<AlbumItem> getListBlockAlbum() {
        ArrayList<AlbumItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.rawQuery(AppBlockHelperDB.SQL_QUERY_BLOCK_ALBUM, null)) {
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long album_id = cursor.getLong(cursor.getColumnIndex(AppBlockHelperDB.ALBUM_ID));
                        String album_name = cursor.getString(cursor.getColumnIndex(AppBlockHelperDB.ALBUM_NAME));
                        String artist_name = cursor.getString(cursor.getColumnIndex(AppBlockHelperDB.ALBUM_ARTIST_NAME));
                        int year = cursor.getInt(cursor.getColumnIndex(AppBlockHelperDB.ALBUM_YEAR));
                        int track_count = cursor.getInt(cursor.getColumnIndex(AppBlockHelperDB.ALBUM_TRACK_COUNT));
                        String album_thumb = cursor.getString(cursor.getColumnIndex(AppBlockHelperDB.ALBUM_THUMB));
                        Uri uri = Uri.parse(album_thumb);
                        AlbumItem album = new AlbumItem(album_id, album_name, artist_name, year, track_count, uri);
                        list.add(album);
                    }
                }
                cursor.close();
            }
        }
        return list;
    }

    public boolean insertBlockAlbum(AlbumItem album) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppBlockHelperDB.ALBUM_ID, album.getId());
        contentValues.put(AppBlockHelperDB.ALBUM_NAME, album.getAlbumName());
        contentValues.put(AppBlockHelperDB.ALBUM_ARTIST_NAME, album.getArtistName());
        contentValues.put(AppBlockHelperDB.ALBUM_YEAR, album.getYear());
        contentValues.put(AppBlockHelperDB.ALBUM_TRACK_COUNT, album.getTrackCount());
        contentValues.put(AppBlockHelperDB.ALBUM_THUMB, album.getAlbumThumb().toString());
        sqLiteDatabase.insert(AppBlockHelperDB.TABLE_ALBUM, null, contentValues);
        return true;
    }

    public long deleteBlockAlbum(ArrayList<AlbumItem> listAlbum) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        for (int i = 0; i < listAlbum.size(); i++) {
            AlbumItem item = listAlbum.get(i);
            result = sqLiteDatabase.delete(AppBlockHelperDB.TABLE_ALBUM, AppBlockHelperDB.ALBUM_ID + " = ?", new String[]{String.valueOf(item.getId())});
        }
        return result;
    }

    @SuppressLint("Range")
    public ArrayList<ArtistItem> getListBlockArtist() {
        ArrayList<ArtistItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(AppBlockHelperDB.SQL_QUERY_BLOCK_ARTIST, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long artist_id = cursor.getLong(cursor.getColumnIndex(AppBlockHelperDB.ARTIST_ID));
                        String artist_name = cursor.getString(cursor.getColumnIndex(AppBlockHelperDB.ARTIST_NAME));
                        int album_count = cursor.getInt(cursor.getColumnIndex(AppBlockHelperDB.ARTIST_ALBUM_COUNT));
                        int track_count = cursor.getInt(cursor.getColumnIndex(AppBlockHelperDB.ARTIST_TRACK_COUNT));
                        ArtistItem album = new ArtistItem(artist_id, artist_name, album_count, track_count);
                        list.add(album);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public boolean insertBlockArtist(ArtistItem artist) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppBlockHelperDB.ARTIST_ID, artist.getId());
        contentValues.put(AppBlockHelperDB.ARTIST_NAME, artist.getName());
        contentValues.put(AppBlockHelperDB.ARTIST_ALBUM_COUNT, artist.getAlbumCount());
        contentValues.put(AppBlockHelperDB.ARTIST_TRACK_COUNT, artist.getTrackCount());
        sqLiteDatabase.insert(AppBlockHelperDB.TABLE_ARTIST, null, contentValues);
        return true;
    }

    public long deleteBlockArtist(ArrayList<ArtistItem> listArtist) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        for (int i = 0; i < listArtist.size(); i++) {
            ArtistItem item = listArtist.get(i);
            result = sqLiteDatabase.delete(AppBlockHelperDB.TABLE_ARTIST, AppBlockHelperDB.ARTIST_ID + " = ?", new String[]{String.valueOf(item.getId())});
        }
        return result;
    }

    public boolean isBlockAlbum(AlbumItem album) {
        boolean isBlock = false;
        String[] whereArgs = {String.valueOf(album.getId())};
        sqLiteDatabase = database.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.query(
                AppBlockHelperDB.TABLE_ALBUM,
                null,
                AppBlockHelperDB.ALBUM_ID + " = ?",
                whereArgs,
                null,
                null,
                null)) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
        }
        return false;
    }
}


