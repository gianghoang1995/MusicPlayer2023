package com.musicplayer.mp3player.defaultmusicplayer.model

import android.os.Parcel
import android.os.Parcelable

data class DownloadProgress(val currentByte: Int, val totalByte: Int):Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(currentByte)
        parcel.writeInt(totalByte)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DownloadProgress> {
        override fun createFromParcel(parcel: Parcel): DownloadProgress {
            return DownloadProgress(parcel)
        }

        override fun newArray(size: Int): Array<DownloadProgress?> {
            return arrayOfNulls(size)
        }
    }

}