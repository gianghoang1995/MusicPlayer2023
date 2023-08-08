package com.musicplayer.mp3player.playermusic.model

import android.os.Parcel
import android.os.Parcelable

class CategoryItem(var cateTitle: String?, var thumb: Int, var defColor: String?, var cateKeyword: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cateTitle)
        parcel.writeInt(thumb)
        parcel.writeString(defColor)
        parcel.writeString(cateKeyword)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CategoryItem> {
        override fun createFromParcel(parcel: Parcel): CategoryItem {
            return CategoryItem(parcel)
        }

        override fun newArray(size: Int): Array<CategoryItem?> {
            return arrayOfNulls(size)
        }
    }

}