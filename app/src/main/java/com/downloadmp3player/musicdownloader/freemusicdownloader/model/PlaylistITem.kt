package com.downloadmp3player.musicdownloader.freemusicdownloader.model

import android.os.Parcel
import android.os.Parcelable


data class PlaylistITem(
    var id: Int,
    var favorite_id: String?,
    var name: String?,
    var thumbnail: Int?,
    var countSong: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(favorite_id)
        parcel.writeString(name)
        parcel.writeValue(thumbnail)
        parcel.writeValue(countSong)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaylistITem> {
        override fun createFromParcel(parcel: Parcel): PlaylistITem {
            return PlaylistITem(parcel)
        }

        override fun newArray(size: Int): Array<PlaylistITem?> {
            return arrayOfNulls(size)
        }
    }
}
