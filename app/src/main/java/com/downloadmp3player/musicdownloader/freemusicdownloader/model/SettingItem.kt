package com.downloadmp3player.musicdownloader.freemusicdownloader.model

import android.os.Parcel
import android.os.Parcelable

data class SettingItem(
    var name: String?,
    var resource: Int?
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeValue(resource)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SettingItem> {
        override fun createFromParcel(parcel: Parcel): SettingItem {
            return SettingItem(parcel)
        }

        override fun newArray(size: Int): Array<SettingItem?> {
            return arrayOfNulls(size)
        }
    }
}