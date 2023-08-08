package com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.musicplayer.mp3player.playermusic.model.FolderItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScanningMusicDirectory extends AsyncTask<Void, Integer, ArrayList<FolderItem>> {
    public Context mContext;
    public String path;
    public ScanningDirectoryListener listener;
    public ArrayList<String> lstPathCheckRoot = new ArrayList<>();
    public ArrayList<String> lstNameCheck = new ArrayList<>();
    public ArrayList<String> lstPathAdded = new ArrayList<>();

    public ScanningMusicDirectory(Context context, ScanningDirectoryListener listener) {
        mContext = context;
        this.listener = listener;
    }

    public void setPath(String mPath) {
        path = mPath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<FolderItem> doInBackground(Void... voids) {
        String internalPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
        lstPathAdded.clear();
        ArrayList<FolderItem> lstFolder = new ArrayList<>();
        lstFolder.clear();
        File directory = new File(path);
        if (directory.isDirectory() && directory.canRead()) {
            File[] files = directory.listFiles();
            if (files.length > 0)
                for (File file : files) {
                    String name = file.getName();
                    String pathDirectory = file.getAbsolutePath();
                    for (int i = 0; i < lstPathCheckRoot.size(); i++) {
                        if ((lstPathCheckRoot.get(i) + "/").contains(pathDirectory + "/")) {
                            if (!lstPathAdded.contains(pathDirectory)) {
                                lstPathAdded.add(pathDirectory);
                                FolderItem folder;
                                if (internalPath.contains(pathDirectory)) {
                                    folder = new FolderItem(new File(internalPath).getName(), 0, internalPath, 0);
                                } else {
                                    folder = new FolderItem(name, 0, pathDirectory, 0);
                                }
                                lstFolder.add(folder);
                                break;
                            }
                        }
                    }
                }
        }
        Collections.sort(lstFolder, new FolderNameComparator());
        return lstFolder;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(ArrayList<FolderItem> folders) {
        super.onPostExecute(folders);
        listener.onScanningDirectorySuccess(folders);
    }

    public void setListCheck(ArrayList<String> lstPath, ArrayList<String> lstName) {
        lstPathCheckRoot.clear();
        lstNameCheck.clear();
        lstNameCheck.addAll(lstName);
        lstPathCheckRoot.addAll(lstPath);
    }

    public class FolderNameComparator implements Comparator<FolderItem> {
        public int compare(FolderItem left, FolderItem right) {
            return left.getName().compareTo(right.getName());
        }
    }
}
