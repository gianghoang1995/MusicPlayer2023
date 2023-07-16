package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder.db.block;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem;

import java.util.ArrayList;

public class BlockFolderDao {
    private BlockFolderHelper database;
    private SQLiteDatabase sqLiteDatabase;

    public BlockFolderDao(BlockFolderHelper database) {
        this.database = database;
    }

    @SuppressLint("Range")
    public ArrayList<FolderItem> getAllFolderBlock() {
        ArrayList<FolderItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(BlockFolderHelper.SQL_QUERRY, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(BlockFolderHelper.FOLDER_NAME));
                        String folderPath = cursor.getString(cursor.getColumnIndex(BlockFolderHelper.FOLDER_PATH));
                        int folderID = cursor.getInt(cursor.getColumnIndex(BlockFolderHelper.FOLDER_ID));
                        FolderItem folder = new FolderItem(name, 0, folderPath, folderID);
                        list.add(folder);
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    public boolean insertBlockFolder(FolderItem folder) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BlockFolderHelper.FOLDER_PATH, folder.getPath());
        contentValues.put(BlockFolderHelper.FOLDER_NAME, folder.getName());
        contentValues.put(BlockFolderHelper.FOLDER_ID, folder.getParentId());
        sqLiteDatabase.insert(BlockFolderHelper.TABLE_NAME, null, contentValues);
        return true;
    }

    public long deleteBlockFolder(ArrayList<FolderItem> listFolder) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        for (int i = 0; i < listFolder.size(); i++) {
            FolderItem item = listFolder.get(i);
            result = sqLiteDatabase.delete(BlockFolderHelper.TABLE_NAME, BlockFolderHelper.FOLDER_PATH + " = ?", new String[]{item.getPath()});
        }
        return result;
    }

    @SuppressLint("Range")
    public boolean isBlockFolder(FolderItem folder) {
        boolean isBlock = false;
        String[] whereArgs = {folder.getPath()};
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(
                BlockFolderHelper.TABLE_NAME,
                null,
                BlockFolderHelper.FOLDER_PATH + " = ?",
                whereArgs,
                null,
                null,
                null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(BlockFolderHelper.FOLDER_PATH));
                    if (path.equals(folder.getPath())) {
                        isBlock = true;
                    }
                }
            }
            cursor.close();
        } finally {
            cursor.close();
        }
        return isBlock;
    }
}


