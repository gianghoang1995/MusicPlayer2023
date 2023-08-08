package com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder.db.pin;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.musicplayer.mp3player.playermusic.model.FolderItem;

import java.util.ArrayList;

public class PinFolderDao {
    private PinFolderHelper database;
    private SQLiteDatabase sqLiteDatabase;

    public PinFolderDao(PinFolderHelper database) {
        this.database = database;
    }

    public ArrayList<FolderItem> getPinFolder() {
        ArrayList<FolderItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(PinFolderHelper.SQL_QUERRY, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(PinFolderHelper.FOLDER_NAME));
                        String folderPath = cursor.getString(cursor.getColumnIndexOrThrow(PinFolderHelper.FOLDER_PATH));
                        int folderID = cursor.getInt(cursor.getColumnIndexOrThrow(PinFolderHelper.FOLDER_ID));
                        FolderItem folder = new FolderItem(name, 0, folderPath, folderID);
                        list.add(folder);
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

    public boolean insertPinFolder(FolderItem folder) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PinFolderHelper.FOLDER_PATH, folder.getPath());
        contentValues.put(PinFolderHelper.FOLDER_NAME, folder.getName());
        contentValues.put(PinFolderHelper.FOLDER_ID, folder.getParentId());
        sqLiteDatabase.insert(PinFolderHelper.TABLE_NAME, null, contentValues);
        return true;
    }

    public long deletePinFolder(FolderItem folder) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        return result = sqLiteDatabase.delete(PinFolderHelper.TABLE_NAME, PinFolderHelper.FOLDER_PATH + " = ?", new String[]{folder.getPath()});
    }

    @SuppressLint("Range")
    public boolean isPinFolder(FolderItem folder) {
        boolean isPin = false;
        String[] whereArgs = {folder.getPath()};
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(
                PinFolderHelper.TABLE_NAME,
                null,
                PinFolderHelper.FOLDER_PATH + " = ?",
                whereArgs,
                null,
                null,
                null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(PinFolderHelper.FOLDER_PATH));
                    if (path.equals(folder.getPath())) {
                        isPin = true;
                    }
                }
            }
            cursor.close();
        } finally {
            cursor.close();
        }
        return isPin;
    }
}


