package com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline;

import org.schabi.newpipe.extractor.Page;

import java.util.ArrayList;

public interface LoadMoreCallback {
    void onLoadMoreSuccess(ArrayList<ItemMusicOnline> list, Page nextPage);

    void onLoadMoreFailed();
}
