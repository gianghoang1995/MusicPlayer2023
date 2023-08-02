package com.musicplayer.mp3player.defaultmusicplayer.database.equalizer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.musicplayer.mp3player.defaultmusicplayer.model.CustomPresetItem;

import java.util.ArrayList;

public class EqualizerDaoDB {
    private EqualizerHelperDB database;
    private SQLiteDatabase sqLiteDatabase;

    public EqualizerDaoDB(EqualizerHelperDB database) {
        this.database = database;
    }

    @SuppressLint("Range")
    public ArrayList<CustomPresetItem> getAllPreset() {
        ArrayList<CustomPresetItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(EqualizerHelperDB.SQL_QUERRY, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(EqualizerHelperDB.CL_NAME));
                        int slide0 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER1));
                        int slide1 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER2));
                        int slide2 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER3));
                        int slide3 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER4));
                        int slide4 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER5));
                        CustomPresetItem customPreset = new CustomPresetItem(name, slide0, slide1, slide2, slide3, slide4);
                        list.add(customPreset);
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

    @SuppressLint("Range")
    public ArrayList<CustomPresetItem> getAllCustomPreset() {
        ArrayList<CustomPresetItem> list = new ArrayList<>();
        sqLiteDatabase = database.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(EqualizerHelperDB.SQL_QUERRY, null);
            if (cursor != null) {
                int i = 0;
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        if (i > 9) {
                            String name = cursor.getString(cursor.getColumnIndex(EqualizerHelperDB.CL_NAME));
                            if (!name.equals(EqualizerHelperDB.DEFAULT_CUSTOM)) {
                                int slide0 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER1));
                                int slide1 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER2));
                                int slide2 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER3));
                                int slide3 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER4));
                                int slide4 = cursor.getInt(cursor.getColumnIndex(EqualizerHelperDB.CL_SLIDER5));
                                CustomPresetItem customPreset = new CustomPresetItem(name, slide0, slide1, slide2, slide3, slide4);
                                list.add(customPreset);
                            }
                        }
                        i++;
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

    public ArrayList<String> getAllNamePreset() {
        ArrayList<CustomPresetItem> list = getAllPreset();
        ArrayList<String> lst_Name = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            lst_Name.add(list.get(i).getPresetName());
        }
        return lst_Name;
    }

    public ArrayList<String> getCustomPreset() {
        ArrayList<CustomPresetItem> list = getAllPreset();
        ArrayList<String> lstCustom = new ArrayList<>();
        if (list.size() > 10) {
            for (int i = 10; i < list.size() - 1; i++) {
                lstCustom.add(list.get(i).getPresetName());
            }
        }
        return lstCustom;
    }

    public boolean insertPreset(CustomPresetItem customPreset) {
        ArrayList<String> lst = getAllNamePreset();
        if (lst.contains(customPreset.getPresetName())) {
            return false;
        } else {
            sqLiteDatabase = database.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(EqualizerHelperDB.CL_NAME, customPreset.presetName);
            contentValues.put(EqualizerHelperDB.CL_SLIDER1, customPreset.slider1);
            contentValues.put(EqualizerHelperDB.CL_SLIDER2, customPreset.slider2);
            contentValues.put(EqualizerHelperDB.CL_SLIDER3, customPreset.slider3);
            contentValues.put(EqualizerHelperDB.CL_SLIDER4, customPreset.slider4);
            contentValues.put(EqualizerHelperDB.CL_SLIDER5, customPreset.slider5);
            sqLiteDatabase.insert(EqualizerHelperDB.TABLE_NAME, null, contentValues);
            return true;
        }
    }

    public boolean updatePreset(CustomPresetItem customPreset) {
        sqLiteDatabase = database.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EqualizerHelperDB.CL_NAME, customPreset.presetName);
        contentValues.put(EqualizerHelperDB.CL_SLIDER1, customPreset.slider1);
        contentValues.put(EqualizerHelperDB.CL_SLIDER2, customPreset.slider2);
        contentValues.put(EqualizerHelperDB.CL_SLIDER3, customPreset.slider3);
        contentValues.put(EqualizerHelperDB.CL_SLIDER4, customPreset.slider4);
        contentValues.put(EqualizerHelperDB.CL_SLIDER5, customPreset.slider5);
        long result = sqLiteDatabase.update(EqualizerHelperDB.TABLE_NAME, contentValues, EqualizerHelperDB.CL_NAME + " = ?", new String[]{customPreset.presetName});
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public long deletePreset(String name) {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        return result = sqLiteDatabase.delete(EqualizerHelperDB.TABLE_NAME, EqualizerHelperDB.CL_NAME + " = ?", new String[]{name});
    }

    public long deleteCustomPreset() {
        long result = 0;
        sqLiteDatabase = database.getWritableDatabase();
        return result = sqLiteDatabase.delete(EqualizerHelperDB.TABLE_NAME, EqualizerHelperDB.CL_NAME + " = ?", new String[]{EqualizerHelperDB.DEFAULT_CUSTOM});
    }

    public boolean addCustomPreset() {
        return insertPreset(new CustomPresetItem(EqualizerHelperDB.DEFAULT_CUSTOM, 0, 0, 0, 0, 0));
    }

}


