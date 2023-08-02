package com.musicplayer.mp3player.defaultmusicplayer.model

import android.os.Parcel
import android.os.Parcelable

data class FolderItem(val name: String?, val count: Int, var path: String?, val parentId: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(), parcel.readInt(), parcel.readString(), parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(count)
        parcel.writeString(path)
        parcel.writeInt(parentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FolderItem> {
        override fun createFromParcel(parcel: Parcel): FolderItem {
            return FolderItem(parcel)
        }
        override fun newArray(size: Int): Array<FolderItem?> {
            return arrayOfNulls(size)
        }
    }

}
