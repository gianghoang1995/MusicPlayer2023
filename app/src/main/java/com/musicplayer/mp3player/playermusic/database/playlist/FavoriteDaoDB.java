package com.musicplayer.mp3player.playermusic.database.playlist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.musicplayer.mp3player.playermusic.equalizer.R;
import com.musicplayer.mp3player.playermusic.callback.SongLoaderCallback;
import com.musicplayer.mp3player.playermusic.loader.SongLoaderAsync;
import com.musicplayer.mp3player.playermusic.model.PlaylistITem;
import com.musicplayer.mp3player.playermusic.model.MusicItem;
import com.musicplayer.mp3player.playermusic.utils.PreferenceUtils;
import com.musicplayer.mp3player.playermusic.utils.SortOrder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FavoriteDaoDB {
    private FavoriteSqliteHelperDB database;
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    public FavoriteDaoDB(Context context, FavoriteSqliteHelperDB database) {
        this.database = database;
        this.context = context;
    }

    @SuppressLint("Range")
    public ArrayList<PlaylistITem> getAllFavorite() {
        ArrayList<PlaylistITem> listFavorite = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.rawQuery(FavoriteSqliteHelperDB.SQL_QUERY_BY_ID, null)) {
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(FavoriteSqliteHelperDB.FAVORITE_NAME));
                        String favorite_id = cursor.getString(cursor.getColumnIndex(FavoriteSqliteHelperDB.FAVORITE_ID));
                        int id = cursor.getInt(cursor.getColumnIndex(FavoriteSqliteHelperDB.ID));
                        PlaylistSongSqLiteHelperDB playlistSongSqLiteHelperDatabase = new PlaylistSongSqLiteHelperDB(context, favorite_id);
                        PlaylistSongDaoDB playlistMusicDatabase = new PlaylistSongDaoDB(playlistSongSqLiteHelperDatabase);
                        int count = playlistMusicDatabase.getCountSongLocalInPlaylist();
                        switch (name) {
                            case FavoriteSqliteHelperDB.DEFAULT_FAVORITE:
                                listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_playlist_favorite, count));
                                break;
                            case FavoriteSqliteHelperDB.TABLE_LAST_PLAYING:
                                listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_playlist_last_playing, count));
                                break;
                            case FavoriteSqliteHelperDB.TABLE_MOST_PLAYING:
                                listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_playlist_most_play, count));
                                break;
                            case FavoriteSqliteHelperDB.TABLE_RECENT_ADDED:
                                SongLoaderAsync musicAsyncLoader = new SongLoaderAsync(context, new SongLoaderCallback() {
                                    @Override
                                    public void onAudioLoadedSuccessful(@NotNull ArrayList<MusicItem> musicItemList) {

                                    }
                                });
                                musicAsyncLoader.filterDateAdded();
                                musicAsyncLoader.setSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                                int countSong = musicAsyncLoader.getCount();
                                listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_playlist_recent_added, countSong));
                                break;
                            default:
                                listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_song_transparent, count));
                                break;
                        }
                    }
                }
                cursor.close();
            }
        }
        return listFavorite;
    }

    @SuppressLint("Range")
    public ArrayList<PlaylistITem> getCustomFavorite() {
        ArrayList<PlaylistITem> listFavorite = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(FavoriteSqliteHelperDB.SQL_QUERY_BY_ID, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(FavoriteSqliteHelperDB.FAVORITE_NAME));
                        String favorite_id = cursor.getString(cursor.getColumnIndex(FavoriteSqliteHelperDB.FAVORITE_ID));
                        int id = cursor.getInt(cursor.getColumnIndex(FavoriteSqliteHelperDB.ID));
                        PlaylistSongSqLiteHelperDB playlistSongSqLiteHelperDatabase = new PlaylistSongSqLiteHelperDB(context, favorite_id);
                        PlaylistSongDaoDB playlistMusicDatabase = new PlaylistSongDaoDB(playlistSongSqLiteHelperDatabase);
                        int count = playlistMusicDatabase.getCountSongLocalInPlaylist();
                        if (name.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE)) {
                            listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_playlist_favorite, count));
                        } else if (name.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING)
                                || name.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING)
                                || name.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED)) {

                        } else {
                            listFavorite.add(new PlaylistITem(id, favorite_id, name, R.drawable.ic_song_transparent, count));
                        }
                    }
                }
                cursor.close();
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return listFavorite;
    }

    public ArrayList<String> getAllNameFavorite() {
        ArrayList<PlaylistITem> listFavorite = getAllFavorite();
        ArrayList<String> lstName = new ArrayList<>();
        for (int i = 0; i < listFavorite.size(); i++) {
            lstName.add(listFavorite.get(i).getName());
        }
        return lstName;
    }

    public boolean insertFavorite(String favoriteName) {
        String favoriteID = "PLAYLIST_112_" + PreferenceUtils.INSTANCE.getIDPlaylist();
        PreferenceUtils.INSTANCE.setIDPlaylist();
        long result;
        boolean output;
        if (getAllNameFavorite().contains(favoriteName)) {
            result = -1;
        } else {
            ContentValues contentValues = new ContentValues();
            sqLiteDatabase = database.getWritableDatabase();
            if (favoriteName.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE) || favoriteName.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING) || favoriteName.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED) || favoriteName.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING)) {
                contentValues.put(FavoriteSqliteHelperDB.FAVORITE_NAME, favoriteName);
                contentValues.put(FavoriteSqliteHelperDB.FAVORITE_ID, favoriteName);
                contentValues.put(FavoriteSqliteHelperDB.ID, PreferenceUtils.INSTANCE.getIDPlaylist());
            } else {
                contentValues.put(FavoriteSqliteHelperDB.FAVORITE_NAME, favoriteName);
                contentValues.put(FavoriteSqliteHelperDB.FAVORITE_ID, favoriteID);
                contentValues.put(FavoriteSqliteHelperDB.ID, PreferenceUtils.INSTANCE.getIDPlaylist());
            }
            result = sqLiteDatabase.insert(FavoriteSqliteHelperDB.TABLE_NAME, null, contentValues);
        }
        output = result != -1;
        return output;
    }

    public long updateFavoriteName(String oldName, String newName) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteSqliteHelperDB.FAVORITE_NAME, newName);
        return sqLiteDatabase.update(FavoriteSqliteHelperDB.TABLE_NAME, contentValues, FavoriteSqliteHelperDB.FAVORITE_NAME + " = ?", new String[]{oldName});
    }

    public long updateFavoriteID(PlaylistITem playlist) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteSqliteHelperDB.ID, playlist.getId());
        return sqLiteDatabase.update(FavoriteSqliteHelperDB.TABLE_NAME, contentValues, FavoriteSqliteHelperDB.FAVORITE_NAME + " = ?", new String[]{playlist.getName()});
    }

    public long deleteFavorite(String name) {
        sqLiteDatabase = database.getWritableDatabase();
        return sqLiteDatabase.delete(FavoriteSqliteHelperDB.TABLE_NAME, FavoriteSqliteHelperDB.FAVORITE_NAME + " = ?", new String[]{name});
    }


}


