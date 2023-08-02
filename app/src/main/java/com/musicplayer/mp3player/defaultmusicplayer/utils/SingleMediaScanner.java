package com.musicplayer.mp3player.defaultmusicplayer.utils;


import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

import java.io.File;

public class SingleMediaScanner implements MediaScannerConnectionClient {

    private final MediaScannerConnection mMs;
    private final File mFile;
    private OnScanComplete onScanComplete;

    public SingleMediaScanner(Context context, File f,OnScanComplete monScanComplete) {
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
        onScanComplete = monScanComplete;
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
        onScanComplete.OnComplete(uri);
    }

   public interface OnScanComplete{
        void OnComplete(Uri uri);
    }
}