package com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline;

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline;

import org.schabi.newpipe.extractor.Page;

import java.util.ArrayList;

public interface OnSearchResultCallback {
    void onSearchSuccessful(ArrayList<ItemMusicOnline> videoFromSearch, Page nextPage);

    void onSearchFailed(String message);
}
