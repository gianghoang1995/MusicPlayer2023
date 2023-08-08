package com.musicplayer.mp3player.playermusic.ui.lyrics;

import android.text.Html;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class LyricUtils {
    public static boolean compressLyric(File mp3file, String lyrics, boolean isHtml) {
        boolean isCompress = false;
        if (mp3file != null)
            try {
                AudioFile af = AudioFileIO.read(mp3file);
                Tag tags = af.getTag();
                if (isHtml)
                    tags.setField(FieldKey.LYRICS, Html.fromHtml(lyrics).toString());
                else
                    tags.setField(FieldKey.LYRICS, lyrics);
                af.setTag(tags);
                AudioFileIO.write(af);
                isCompress = true;
            } catch (Exception e) {
                e.printStackTrace();
                isCompress = false;
            }
        return isCompress;
    }
}
