package com.musicplayer.mp3player.playermusic.ui.lyrics;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.musicplayer.mp3player.playermusic.net.OnFindLyricListener;
import com.musicplayer.mp3player.playermusic.utils.AppConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GGFindLyrics {
    private Context mContext;
    AsyncLoadLyric asyncLoadLyric;

    public GGFindLyrics(Context context) {
        mContext = context;
    }

    public void findLyrics(final String query, final OnFindLyricListener listener) {

        String urlRequest = null;
        try {
            urlRequest = AppConstants.BASE_GG_SEARCH + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (asyncLoadLyric != null) {
            asyncLoadLyric.setCallback(null);
            asyncLoadLyric.cancel(true);
        }
        asyncLoadLyric = new AsyncLoadLyric();
        asyncLoadLyric.setUrl(urlRequest);
        asyncLoadLyric.setCallback(listener);
        asyncLoadLyric.execute();
    }

    public class AsyncLoadLyric extends AsyncTask<Void, Void, Void> {
        String urlRequest = "";
        OnFindLyricListener onFindLyricListener;
        String lyricContent = "";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(urlRequest).get();
                saveFile("Lyric new", doc.toString());
                Elements listSpanLyrics = doc.select(".ujudUb");
                if (listSpanLyrics != null) {
                    for (Element e : listSpanLyrics) {
                        lyricContent += e + "\n";
                    }
                }

            } catch (Exception ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (lyricContent.length() > 0) {
                onFindLyricListener.onFindLyricSuccess(lyricContent);
            } else {
                onFindLyricListener.onFindLyricFailed();
            }
        }

        public void setUrl(String url) {
            urlRequest = url;
        }

        public void setCallback(OnFindLyricListener findLyricListener) {
            onFindLyricListener = findLyricListener;
        }
    }

    public static void saveFile(String filename, String fileContents) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
        file.mkdirs();
        try {
            FileWriter out = new FileWriter(new File(file.getPath(), filename + ".txt"));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
    }
}
