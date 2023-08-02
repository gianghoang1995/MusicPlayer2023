package com.musicplayer.mp3player.defaultmusicplayer.net;

public interface OnFindLyricCallback {
    void onFindLyricSuccess(String lyrics);
    void onFindLyricFailed();
}
