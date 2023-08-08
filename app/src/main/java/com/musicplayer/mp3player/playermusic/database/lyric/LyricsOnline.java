package com.musicplayer.mp3player.playermusic.database.lyric;

public class LyricsOnline {
    public String pathSong;
    public String nameSong;
    public String lyricData;
    public int typeLyric;

    public LyricsOnline(String pathSong, String nameSong, String lyricData, int typeLyric) {
        this.pathSong = pathSong;
        this.nameSong = nameSong;
        this.lyricData = lyricData;
        this.typeLyric = typeLyric;
    }

    public LyricsOnline() {
    }
}
