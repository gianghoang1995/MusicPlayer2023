package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.query_folder;

import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem;

import java.util.ArrayList;

public interface ScanningDirectoryListener {
    void onScanningDirectorySuccess(ArrayList<FolderItem> lstFolder);

//    void onCancel();
}
