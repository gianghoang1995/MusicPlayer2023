package com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils;

import java.io.File;
import java.util.ArrayList;

import static com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB.*;


public class PlaylistSongDaoDB {
    private final PlaylistSongSqLiteHelperDB database;
    private SQLiteDatabase sqLiteDatabase;
    private final String TABLE_NAME;
    private String SQL_QUERRY;
    private String typeLocal = " AND " + TYPE + " = " + TYPE_LOCAL;
    private String typeLocalWhere = " WHERE " + TYPE + " = " + TYPE_LOCAL;
    private String typeOnline = " AND " + TYPE + " = " + TYPE_ONLINE;
    private String typeOnlineWhere = " WHERE " + TYPE + " = " + TYPE_ONLINE;

    public PlaylistSongDaoDB(PlaylistSongSqLiteHelperDB database) {
        this.database = database;
        this.TABLE_NAME = database.getTABLE_NAME();
        this.SQL_QUERRY = database.getSQL_QUERRY();
    }

    public void setQueryTypeOnline() {
        this.SQL_QUERRY += typeOnlineWhere;
    }

    public void setQueryTypeLocal() {
        this.SQL_QUERRY += typeLocalWhere;
    }

    public void setQueryMostPlaying() {
        this.SQL_QUERRY = "SELECT * FROM " + TABLE_NAME + typeLocalWhere + " ORDER BY " + COUNT_PLAYING + " DESC limit 30";
    }

    public void setQueryLastPlayingLocal() {
        this.SQL_QUERRY += typeLocalWhere + " ORDER BY " + TIME_PLAY + " DESC limit 30";
    }

    public void setQueryLastPlayingOnline() {
        this.SQL_QUERRY += typeOnlineWhere + " ORDER BY " + TIME_PLAY + " DESC limit 30";
    }

    public int getCountDB() {
        try {
            sqLiteDatabase = database.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(SQL_QUERRY + typeLocalWhere, null);
            if (cursor != null)
                return cursor.getCount();
        } catch (Exception ex) {
            return 0;
        }
        return 0;
    }

    @SuppressLint("Range")
    public int getCountSongLocalInPlaylist() {
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor;
        try {
            cursor = sqLiteDatabase.rawQuery(SQL_QUERRY + typeLocalWhere, null);
            if (cursor != null) {
                return cursor.getCount();
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }


    @SuppressLint("Range")
    public ArrayList<MusicItem> getAllSongLocalFromPlaylist() {
        ArrayList<MusicItem> list = new ArrayList<>();
        try {
            sqLiteDatabase = database.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(SQL_QUERRY, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String title = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(TITLE)));
                        String songPath = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(SONG_PATH)));
                        String lyrics = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(LYRICS)));
                        long id = cursor.getLong(cursor.getColumnIndex(SONG_ID));
                        String artist = cursor.getString(cursor.getColumnIndex(ARTIST));
                        String album = cursor.getString(cursor.getColumnIndex(ALBULM));
                        int trackNumber = cursor.getInt(cursor.getColumnIndex(TRACK_NUMBER));
                        long albumId = cursor.getLong(cursor.getColumnIndex(ALBUM_ID));
                        String genre = cursor.getString(cursor.getColumnIndex(GENRE));
                        String year = cursor.getString(cursor.getColumnIndex(YEAR));
                        long artistID = cursor.getLong(cursor.getColumnIndex(ARTIST_ID));
                        String duration = cursor.getString(cursor.getColumnIndex(DURATION));
                        int countPlay = cursor.getInt(cursor.getColumnIndex(COUNT_PLAYING));
                        int hourPlay = cursor.getInt(cursor.getColumnIndex(TIME_PLAY));
                        int stt = cursor.getInt(cursor.getColumnIndex(STT));
                        MusicItem song = new MusicItem(id, title, artist, album, trackNumber, albumId, genre, songPath, false, year, lyrics, artistID, duration, hourPlay, countPlay, stt);
                        if (new File(songPath).exists()) {
                            list.add(song);
                        } else {
                            deleteLocalSong(song);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception ex) {

        }
        return list;
    }

    @SuppressLint("Range")
    public ArrayList<ItemMusicOnline> getAllItemOnlineFromPlaylist() {
        ArrayList<ItemMusicOnline> list = new ArrayList<>();
        try {
            sqLiteDatabase = database.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(SQL_QUERRY, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String title = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(TITLE)));
                        String videoUrl = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(URL_VIDEO)));
                        long duration = cursor.getLong(cursor.getColumnIndex(DURATION));
                        String videoID = cursor.getString(cursor.getColumnIndex(VIDEO_ID));
                        ItemMusicOnline item = new ItemMusicOnline(0, videoID, videoUrl, title, duration, AppConstants.INSTANCE.randomThumb(), "");
                        list.add(item);
                    }
                }
                cursor.close();
            }
        } catch (Exception ex) {

        }
        return list;
    }

    @SuppressLint("Range")
    public ArrayList<MusicItem> getOldSongLocal() {
        ArrayList<MusicItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.rawQuery(SQL_QUERRY + typeLocalWhere, null)) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String title = AppUtils.INSTANCE.decodeBASE64(cursor.getString(cursor.getColumnIndex(TITLE)));
                    String songPath = AppUtils.INSTANCE.decodeBASE64(cursor.getString(cursor.getColumnIndex(SONG_PATH)));
                    String lyrics = AppUtils.INSTANCE.decodeBASE64(cursor.getString(cursor.getColumnIndex(LYRICS)));
                    long id = cursor.getLong(cursor.getColumnIndex(SONG_ID));
                    String artist = cursor.getString(cursor.getColumnIndex(ARTIST));
                    String album = cursor.getString(cursor.getColumnIndex(ALBULM));
                    int trackNumber = cursor.getInt(cursor.getColumnIndex(TRACK_NUMBER));
                    long albumId = cursor.getLong(cursor.getColumnIndex(ALBUM_ID));
                    String genre = cursor.getString(cursor.getColumnIndex(GENRE));
                    String year = cursor.getString(cursor.getColumnIndex(YEAR));
                    long artistID = cursor.getLong(cursor.getColumnIndex(ARTIST_ID));
                    String duration = cursor.getString(cursor.getColumnIndex(DURATION));
                    MusicItem song = new MusicItem(id, title, artist, album, trackNumber, albumId, genre, songPath, false, year, lyrics, artistID, duration, true);
                    list.add(song);
                }
                cursor.close();
            }
        }
        return list;
    }

    public long insertToLocalPlaylist(MusicItem song) {
        MusicItem curent = isContainsItemLocal(song);
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int count = getCountDB() + 1;
        if (curent == null) {
            contentValues.put(TITLE, AppUtils.INSTANCE.encodeToBASE64(song.title));
            contentValues.put(SONG_PATH, AppUtils.INSTANCE.encodeToBASE64(song.songPath));
            contentValues.put(LYRICS, AppUtils.INSTANCE.encodeToBASE64(song.lyrics));
            contentValues.put(SONG_ID, song.id);
            contentValues.put(ARTIST, song.artist);
            contentValues.put(ALBULM, song.album);
            contentValues.put(TRACK_NUMBER, song.trackNumber);
            contentValues.put(ALBUM_ID, song.albumId);
            contentValues.put(GENRE, song.genre);
            contentValues.put(YEAR, song.year);
            contentValues.put(ARTIST_ID, song.artistId);
            contentValues.put(DURATION, song.duration);
            contentValues.put(TIME_PLAY, System.currentTimeMillis());
            contentValues.put(STT, count);
            contentValues.put(TYPE, TYPE_LOCAL);
            long val = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
            return val;
        } else {
            if (TABLE_NAME.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING) ||
                    TABLE_NAME.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED) ||
                    TABLE_NAME.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING)) {
                contentValues.put(TIME_PLAY, System.currentTimeMillis());
                long val = sqLiteDatabase.update(TABLE_NAME, contentValues, SONG_ID + " = ?", new String[]{String.valueOf(song.id)});
                return val;
            }
            return -1;
        }
    }

    public long insertItemOnlineToPlaylist(ItemMusicOnline item) {
        ItemMusicOnline curent = isContainsItemOnline(item);
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (curent == null) {
            contentValues.put(TITLE, AppUtils.INSTANCE.encodeToBASE64(item.title));
            contentValues.put(URL_VIDEO, AppUtils.INSTANCE.encodeToBASE64(item.urlVideo));
            contentValues.put(DURATION, item.duration);
            contentValues.put(VIDEO_ID, item.videoID);
            contentValues.put(TIME_PLAY, System.currentTimeMillis());
            contentValues.put(TYPE, TYPE_ONLINE);
            return sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        } else {
            if (TABLE_NAME.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING) ||
                    TABLE_NAME.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED) ||
                    TABLE_NAME.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING)) {
                contentValues.put(TIME_PLAY, System.currentTimeMillis());
                return sqLiteDatabase.update(TABLE_NAME, contentValues, VIDEO_ID + " = ?", new String[]{String.valueOf(item.videoID)});
            }
            return -1;
        }
    }

    public void insertListSongLocal(ArrayList<MusicItem> lst) {
        sqLiteDatabase = database.getWritableDatabase();
        int count = getCountDB() + 1;
        for (int i = 0; i < lst.size(); i++) {
            MusicItem song = lst.get(i);
            ContentValues contentValues = new ContentValues();
            contentValues.put(TITLE, AppUtils.INSTANCE.encodeToBASE64(song.title.isEmpty() ? "song" : song.title));
            contentValues.put(LYRICS, AppUtils.INSTANCE.encodeToBASE64(song.lyrics));
            contentValues.put(SONG_PATH, AppUtils.INSTANCE.encodeToBASE64(song.songPath));
            contentValues.put(SONG_ID, song.id);
            contentValues.put(ALBULM, song.album);
            contentValues.put(ARTIST, song.artist);
            contentValues.put(TRACK_NUMBER, song.trackNumber);
            contentValues.put(ALBUM_ID, song.albumId);
            contentValues.put(GENRE, song.genre);
            contentValues.put(YEAR, song.year);
            contentValues.put(ARTIST_ID, song.artistId);
            contentValues.put(DURATION, song.duration);
            contentValues.put(TIME_PLAY, String.valueOf(System.currentTimeMillis()));
            contentValues.put(COUNT_PLAYING, 1);
            contentValues.put(STT, (count + i));
            contentValues.put(TYPE, TYPE_LOCAL);
            sqLiteDatabase.insert("'" + TABLE_NAME + "'", null, contentValues);
        }
    }

    public long insertSongToMostPlayingLocal(MusicItem song) {
        MusicItem curent = isContainsItemLocal(song);
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (curent == null) {
            contentValues.put(TITLE, AppUtils.INSTANCE.encodeToBASE64(song.title));
            contentValues.put(SONG_PATH, AppUtils.INSTANCE.encodeToBASE64(song.songPath));
            contentValues.put(LYRICS, AppUtils.INSTANCE.encodeToBASE64(song.lyrics));
            contentValues.put(SONG_ID, song.id);
            contentValues.put(ARTIST, song.artist);
            contentValues.put(ALBULM, song.album);
            contentValues.put(TRACK_NUMBER, song.trackNumber);
            contentValues.put(ALBUM_ID, song.albumId);
            contentValues.put(GENRE, song.genre);
            contentValues.put(YEAR, song.year);
            contentValues.put(ARTIST_ID, song.artistId);
            contentValues.put(DURATION, song.duration);
            contentValues.put(COUNT_PLAYING, 1);
            contentValues.put(TIME_PLAY, System.currentTimeMillis());
            contentValues.put(TYPE, TYPE_LOCAL);
            return sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        } else {
            contentValues.put(COUNT_PLAYING, curent.countPlaying + 1);
            return sqLiteDatabase.update(TABLE_NAME, contentValues, SONG_ID + " = ?", new String[]{String.valueOf(song.id)});
        }
    }

    public long updateSong(long id, MusicItem song) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, song.title);
        contentValues.put(SONG_PATH, song.songPath);
        return sqLiteDatabase.update(TABLE_NAME, contentValues, SONG_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public long updateSTTSong(MusicItem song) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STT, song.stt);
        return sqLiteDatabase.update(TABLE_NAME, contentValues, SONG_ID + " = ?", new String[]{String.valueOf(song.id)});
    }

    @SuppressLint("Range")
    public MusicItem isContainsItemLocal(MusicItem song) {
        if (song != null) {
            sqLiteDatabase = database.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SONG_ID + " = \"" + song.id + "\"" + typeLocal;
            try (Cursor cursor = sqLiteDatabase.rawQuery(query, null)) {
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        String title = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(TITLE)));
                        String songPath = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(SONG_PATH)));
                        String lyrics = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(LYRICS)));

                        long id = cursor.getLong(cursor.getColumnIndex(SONG_ID));
                        String artist = cursor.getString(cursor.getColumnIndex(ARTIST));
                        String album = cursor.getString(cursor.getColumnIndex(ALBULM));
                        int trackNumber = cursor.getInt(cursor.getColumnIndex(TRACK_NUMBER));
                        long albumId = cursor.getLong(cursor.getColumnIndex(ALBUM_ID));
                        String genre = cursor.getString(cursor.getColumnIndex(GENRE));
                        String year = cursor.getString(cursor.getColumnIndex(YEAR));
                        long artistID = cursor.getLong(cursor.getColumnIndex(ARTIST_ID));
                        String duration = cursor.getString(cursor.getColumnIndex(DURATION));
                        int countPlay = cursor.getInt(cursor.getColumnIndex(COUNT_PLAYING));
                        int hourPlay = cursor.getInt(cursor.getColumnIndex(TIME_PLAY));
                        return new MusicItem(id, title, artist, album, trackNumber, albumId, genre, songPath, false, year, lyrics, artistID, duration, countPlay, hourPlay, true);
                    }
                    cursor.close();
                }
            }
        }
        return null;
    }

    @SuppressLint("Range")
    public ItemMusicOnline isContainsItemOnline(ItemMusicOnline item) {
        if (item != null) {
            sqLiteDatabase = database.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + VIDEO_ID + " = '" + item.videoID + "'" + typeOnline;
            try (Cursor cursor = sqLiteDatabase.rawQuery(query, null)) {
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        String title = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(TITLE)));
                        String videoUrl = AppUtils.INSTANCE.decodeBASE64(
                                cursor.getString(cursor.getColumnIndex(URL_VIDEO)));
                        long duration = cursor.getLong(cursor.getColumnIndex(DURATION));
                        String videoID = cursor.getString(cursor.getColumnIndex(VIDEO_ID));
                        return new ItemMusicOnline(
                                0,
                                videoID,
                                videoUrl,
                                title,
                                duration,
                                AppConstants.INSTANCE.randomThumb(),
                                ""
                        );
                    }
                    cursor.close();
                }
            }
        }
        return null;
    }

    public void deleteLocalSong(MusicItem song) {
        sqLiteDatabase = database.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME, SONG_ID + " = ?", new String[]{String.valueOf(song.id)});
    }

    public void deleteOnlineSong(ItemMusicOnline item) {
        sqLiteDatabase = database.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME, VIDEO_ID + " = ?", new String[]{String.valueOf(item.videoID)});
    }
}


