package com.musicplayer.mp3player.playermusic.widget.bassview;

public interface OnCrollerChangeListener {
    void onProgressChanged(Croller croller, int progress);

    void onStartTrackingTouch(Croller croller);

    void onStopTrackingTouch(Croller croller);
}
