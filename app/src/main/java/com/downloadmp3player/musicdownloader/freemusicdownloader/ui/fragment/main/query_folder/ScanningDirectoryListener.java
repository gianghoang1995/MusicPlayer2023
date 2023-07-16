package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.query_folder;

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem;

import java.util.ArrayList;

public interface ScanningDirectoryListener {
    void onScanningDirectorySuccess(ArrayList<FolderItem> lstFolder);

//    void onCancel();
}
