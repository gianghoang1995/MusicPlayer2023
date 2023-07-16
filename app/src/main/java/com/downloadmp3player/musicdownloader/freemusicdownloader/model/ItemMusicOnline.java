package com.downloadmp3player.musicdownloader.freemusicdownloader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemMusicOnline implements Parcelable {
    public int dbID;
    public String videoID;
    public String urlVideo;
    public String title;
    public long duration;
    public int resourceThumb;
    public String downloadURL;

    public ItemMusicOnline(int dbID, String videoID, String urlVideo, String title, long duration, int resourceThumb) {
        this.dbID = dbID;
        this.videoID = videoID;
        this.urlVideo = urlVideo;
        this.title = title;
        this.duration = duration;
        this.resourceThumb = resourceThumb;
    }

    public ItemMusicOnline(int dbID, String videoID, String urlVideo, String title, long duration, int resourceThumb, String downloadURL) {
        this.dbID = dbID;
        this.videoID = videoID;
        this.urlVideo = urlVideo;
        this.title = title;
        this.duration = duration;
        this.resourceThumb = resourceThumb;
        this.downloadURL = downloadURL;
    }

    protected ItemMusicOnline(Parcel in) {
        dbID = in.readInt();
        videoID = in.readString();
        urlVideo = in.readString();
        title = in.readString();
        duration = in.readLong();
        resourceThumb = in.readInt();
        downloadURL = in.readString();
    }

    public static final Creator<ItemMusicOnline> CREATOR = new Creator<ItemMusicOnline>() {
        @Override
        public ItemMusicOnline createFromParcel(Parcel in) {
            return new ItemMusicOnline(in);
        }

        @Override
        public ItemMusicOnline[] newArray(int size) {
            return new ItemMusicOnline[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dbID);
        dest.writeString(videoID);
        dest.writeString(urlVideo);
        dest.writeString(title);
        dest.writeLong(duration);
        dest.writeInt(resourceThumb);
        dest.writeString(downloadURL);
    }
}
