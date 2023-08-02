package com.musicplayer.mp3player.defaultmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomPresetItem implements Parcelable {
    public int id;
    public String presetName;
    public int slider1;
    public int slider2;
    public int slider3;
    public int slider4;
    public int slider5;

    public CustomPresetItem(String presetName, int slider1, int slider2, int slider3, int slider4, int slider5) {
        this.presetName = presetName;
        this.slider1 = slider1;
        this.slider2 = slider2;
        this.slider3 = slider3;
        this.slider4 = slider4;
        this.slider5 = slider5;
    }

    protected CustomPresetItem(Parcel in) {
        id = in.readInt();
        presetName = in.readString();
        slider1 = in.readInt();
        slider2 = in.readInt();
        slider3 = in.readInt();
        slider4 = in.readInt();
        slider5 = in.readInt();
    }

    public static final Creator<CustomPresetItem> CREATOR = new Creator<CustomPresetItem>() {
        @Override
        public CustomPresetItem createFromParcel(Parcel in) {
            return new CustomPresetItem(in);
        }

        @Override
        public CustomPresetItem[] newArray(int size) {
            return new CustomPresetItem[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public int getSlider1() {
        return slider1;
    }

    public void setSlider1(int slider1) {
        this.slider1 = slider1;
    }

    public int getSlider2() {
        return slider2;
    }

    public void setSlider2(int slider2) {
        this.slider2 = slider2;
    }

    public int getSlider3() {
        return slider3;
    }

    public void setSlider3(int slider3) {
        this.slider3 = slider3;
    }

    public int getSlider4() {
        return slider4;
    }

    public void setSlider4(int slider4) {
        this.slider4 = slider4;
    }

    public int getSlider5() {
        return slider5;
    }

    public void setSlider5(int slider5) {
        this.slider5 = slider5;
    }

    @Override
    public String toString() {
        return "CustomPreset{" +
                "id=" + id +
                ", presetName='" + presetName + '\'' +
                ", slider1=" + slider1 +
                ", slider2=" + slider2 +
                ", slider3=" + slider3 +
                ", slider4=" + slider4 +
                ", slider5=" + slider5 +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(presetName);
        dest.writeInt(slider1);
        dest.writeInt(slider2);
        dest.writeInt(slider3);
        dest.writeInt(slider4);
        dest.writeInt(slider5);
    }
}
