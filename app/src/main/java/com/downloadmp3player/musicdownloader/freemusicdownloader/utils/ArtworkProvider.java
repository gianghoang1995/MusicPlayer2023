package com.downloadmp3player.musicdownloader.freemusicdownloader.utils;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface ArtworkProvider {

    @interface Type {
        int MEDIA_STORE = 0;
        int TAG = 1;
        int FOLDER = 2;
        int REMOTE = 3;
    }
    @Nullable
    InputStream getMediaStoreArtwork(Context context);

    @Nullable
    InputStream getFolderArtwork();

    @Nullable
    InputStream getTagArtwork();

    @Nullable
    List<File> getFolderArtworkFiles();
}