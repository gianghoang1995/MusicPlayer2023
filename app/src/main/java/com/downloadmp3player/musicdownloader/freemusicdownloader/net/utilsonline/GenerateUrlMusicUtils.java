package com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;


import com.downloadmp3player.musicdownloader.freemusicdownloader.BuildConfig;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ConfigApp;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.newpipe.ExtractorHelper;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GenerateUrlMusicUtils {
    public Context context;
    public GenerateUrlCallback listener;
    public ItemMusicOnline audioExtract;

    public GenerateUrlMusicUtils(Context context, GenerateUrlCallback listener) {
        this.context = context;
        this.listener = listener;
    }

    public int getTypeSearch() {
        return new ConfigApp(context).getDataType();
    }

    @SuppressLint({"CheckResult"})
    public void getUrlAudio(ItemMusicOnline videoFromSearch) {
        audioExtract = videoFromSearch;
        ExtractorHelper.getStreamInfo(getTypeSearch(), videoFromSearch.urlVideo, false).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnEvent((streamInfo, throwable) -> {
        }).subscribe(this::pareRecommend, this::handleError);
    }

    private void pareRecommend(StreamInfo streamInfo) {
        if (streamInfo.getAudioStreams() != null) {
            if (!streamInfo.getAudioStreams().isEmpty()) {
                if (BuildConfig.DEBUG) Log.e("Listen", "pare");
                List<InfoItem> lstStream = streamInfo.getRelatedItems();

                int bitrate = 0;
                int posStream = 0;
                for (int i = 0; i < streamInfo.getAudioStreams().size() - 1; i++) {
                    int bit = streamInfo.getAudioStreams().get(i).getBitrate();
                    if (bit > bitrate) {
                        bitrate = bit;
                        posStream = i;
                    }
                }
                String urlAudioLossless = streamInfo.getAudioStreams().get(posStream).getContent();
                String urlAudioPlay = streamInfo.getAudioStreams().get(0).getContent();
                ArrayList<ItemMusicOnline> lstRecomend = new ArrayList<>();
                for (int i = 0; i < lstStream.size(); i++) {
                    if (lstStream.get(i) instanceof StreamInfoItem) {
                        StreamInfoItem infoItem = (StreamInfoItem) lstStream.get(i);
                        String url = infoItem.getUrl();
                        String name = infoItem.getName();
                        long duration = infoItem.getDuration() * 1000;
                        int thumb = AppConstants.INSTANCE.randomThumb();
                        ItemMusicOnline videoFromSearch = new ItemMusicOnline(0, AppUtils.INSTANCE.getVideoID(url), url, name, duration, thumb, infoItem.getThumbnailUrl());
                        if (duration > 0) {
                            lstRecomend.add(videoFromSearch);
                        }
                    }
                }
                listener.onGenerateUrlSuccess(urlAudioPlay, urlAudioLossless, lstRecomend);
            } else {
                handleError(new Throwable());
            }
        } else {
            handleError(new Throwable());
        }
    }

    protected boolean handleError(final Throwable exception) {
        listener.onGenerateUrlError();
        return true;
    }

}
