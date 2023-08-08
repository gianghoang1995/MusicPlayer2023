package com.musicplayer.mp3player.playermusic.ui.fragment.main.query_folder;

import com.musicplayer.mp3player.playermusic.model.FolderItem;

import java.util.ArrayList;

public interface ScanningDirectoryListener {
    void onScanningDirectorySuccess(ArrayList<FolderItem> lstFolder);

//    void onCancel();
}
