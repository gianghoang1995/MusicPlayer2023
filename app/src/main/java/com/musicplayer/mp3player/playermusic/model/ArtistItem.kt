package com.musicplayer.mp3player.playermusic.model

import android.os.Parcel
import android.os.Parcelable

data class ArtistItem(
    var id: Long?,
    var name: String?,
    var albumCount: Int?,
    var trackCount: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        id?.let { parcel.writeLong(it) }
        parcel.writeString(name)
        albumCount?.let { parcel.writeInt(it) }
        trackCount?.let { parcel.writeInt(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArtistItem> {
        override fun createFromParcel(parcel: Parcel): ArtistItem {
            return ArtistItem(parcel)
        }

        override fun newArray(size: Int): Array<ArtistItem?> {
            return arrayOfNulls(size)
        }
    }
}