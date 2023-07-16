package com.downloadmp3player.musicdownloader.freemusicdownloader.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class AlbumItem(
    var id: Long,
    var albumName: String?,
    var artistName: String?,
    var year: Int?,
    var trackCount: Int?,
    var albumThumb: Uri? = null
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readParcelable(Uri::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(albumName)
        parcel.writeString(artistName)
        parcel.writeValue(year)
        parcel.writeValue(trackCount)
        parcel.writeParcelable(albumThumb, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlbumItem> {
        override fun createFromParcel(parcel: Parcel): AlbumItem {
            return AlbumItem(parcel)
        }

        override fun newArray(size: Int): Array<AlbumItem?> {
            return arrayOfNulls(size)
        }
    }

}