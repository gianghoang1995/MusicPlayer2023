package com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline;

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline;

import java.util.ArrayList;

public interface GenerateUrlCallback {
    void onGenerateUrlSuccess(String urlPlay, String urlLossless, ArrayList<ItemMusicOnline> lstRecomend);

    void onGenerateUrlError();
}
