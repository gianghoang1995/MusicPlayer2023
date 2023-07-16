package com.downloadmp3player.musicdownloader.freemusicdownloader.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import com.google.android.gms.ads.nativead.NativeAd
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkProvider
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUntils
import java.io.File
import java.io.InputStream

class MusicItem : Parcelable, ArtworkProvider {
    @JvmField
    var title: String? = null

    @JvmField
    var id: Long = 0

    @JvmField
    var artist: String? = null

    @JvmField
    var album: String? = null

    @JvmField
    var trackNumber = 0

    @JvmField
    var albumId: Long = 0

    @JvmField
    var genre: String? = null

    @JvmField
    var songPath: String? = null

    @JvmField
    var isSelected = false

    @JvmField
    var year: String? = null

    @JvmField
    var lyrics: String? = null

    @JvmField
    var artistId: Long = 0

    @JvmField
    var duration: String? = null
    var timeAdded: Long = 0

    @JvmField
    var countPlaying = 0
    var isDefaultSelected = false

    @JvmField
    var stt = 0
    var bitRate = 0
    var nativeAdsItem: NativeAd? = null
    var isNativeAds: Boolean = false

    constructor(native: NativeAd?, isNative: Boolean) {
        this.nativeAdsItem = native
        isNativeAds = isNative
    }

    constructor() {
        this.title = ""
        this.album = ""
        this.artist = ""
        this.duration = "0"
    }

    constructor(
        id: Long,
        title: String?,
        artist: String?,
        album: String?,
        trackNumber: Int,
        albumId: Long,
        genre: String?,
        songPath: String?,
        isSelected: Boolean,
        year: String?,
        lyrics: String?,
        artistId: Long,
        duration: String?,
        timeAdded: Long,
        countPlaying: Int,
        defaultSelected: Boolean
    ) {
        this.id = id
        this.title = title
        this.artist = artist
        this.album = album
        this.trackNumber = trackNumber
        this.albumId = albumId
        this.genre = genre
        this.songPath = songPath
        this.isSelected = isSelected
        this.year = year
        this.lyrics = lyrics
        this.artistId = artistId
        this.duration = duration
        this.timeAdded = timeAdded
        this.countPlaying = countPlaying
        isDefaultSelected = defaultSelected
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(title)
        dest.writeString(artist)
        dest.writeString(album)
        dest.writeInt(trackNumber)
        dest.writeLong(albumId)
        dest.writeString(genre)
        dest.writeString(songPath)
        dest.writeByte((if (isSelected) 1 else 0).toByte())
        dest.writeString(year)
        dest.writeString(lyrics)
        dest.writeLong(artistId)
        dest.writeString(duration)
        dest.writeLong(timeAdded)
        dest.writeInt(countPlaying)
        dest.writeByte((if (isDefaultSelected) 1 else 0).toByte())
        dest.writeInt(stt)
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readLong()
        title = `in`.readString()
        artist = `in`.readString()
        album = `in`.readString()
        trackNumber = `in`.readInt()
        albumId = `in`.readLong()
        genre = `in`.readString()
        songPath = `in`.readString()
        isSelected = `in`.readByte().toInt() != 0
        year = `in`.readString()
        lyrics = `in`.readString()
        artistId = `in`.readLong()
        duration = `in`.readString()
        timeAdded = `in`.readLong()
        countPlaying = `in`.readInt()
        isDefaultSelected = `in`.readByte().toInt() != 0
        stt = `in`.readInt()
    }

    constructor(
        id: Long,
        title: String?,
        artist: String?,
        album: String?,
        trackNumber: Int,
        albumId: Long,
        genre: String?,
        songPath: String?,
        isSelected: Boolean,
        year: String?,
        lyrics: String?,
        artistId: Long,
        duration: String?
    ) {
        this.id = id
        this.title = title
        this.artist = artist
        this.album = album
        this.trackNumber = trackNumber
        this.albumId = albumId
        this.genre = genre
        this.songPath = songPath
        this.isSelected = isSelected
        this.year = year
        this.lyrics = lyrics
        this.artistId = artistId
        this.duration = duration
    }

    constructor(
        id: Long,
        title: String?,
        artist: String?,
        album: String?,
        trackNumber: Int,
        albumId: Long,
        genre: String?,
        songPath: String?,
        isSelected: Boolean,
        year: String?,
        lyrics: String?,
        artistId: Long,
        duration: String?,
        defaultSelected: Boolean
    ) {
        this.id = id
        this.title = title
        this.artist = artist
        this.album = album
        this.trackNumber = trackNumber
        this.albumId = albumId
        this.genre = genre
        this.songPath = songPath
        this.isSelected = isSelected
        this.year = year
        this.lyrics = lyrics
        this.artistId = artistId
        this.duration = duration
        isDefaultSelected = defaultSelected
    }

    constructor(
        id: Long,
        title: String?,
        bitrate: Int,
        artist: String?,
        album: String?,
        trackNumber: Int,
        albumId: Long,
        genre: String?,
        songPath: String?,
        isSelected: Boolean,
        year: String?,
        lyrics: String?,
        artistId: Long,
        duration: String?,
        timeAdded: Long,
        countPlaying: Int
    ) {
        this.id = id
        bitRate = bitrate
        this.title = title
        this.artist = artist
        this.album = album
        this.trackNumber = trackNumber
        this.albumId = albumId
        this.genre = genre
        this.songPath = songPath
        this.isSelected = isSelected
        this.year = year
        this.lyrics = lyrics
        this.artistId = artistId
        this.duration = duration
        this.timeAdded = timeAdded
        this.countPlaying = countPlaying
    }

    constructor(
        id: Long,
        title: String?,
        artist: String?,
        album: String?,
        trackNumber: Int,
        albumId: Long,
        genre: String?,
        songPath: String?,
        isSelected: Boolean,
        year: String?,
        lyrics: String?,
        artistId: Long,
        duration: String?,
        timeAdded: Long,
        countPlaying: Int,
        stt: Int
    ) {
        this.id = id
        this.title = title
        this.artist = artist
        this.album = album
        this.trackNumber = trackNumber
        this.albumId = albumId
        this.genre = genre
        this.songPath = songPath
        this.isSelected = isSelected
        this.year = year
        this.lyrics = lyrics
        this.artistId = artistId
        this.duration = duration
        this.timeAdded = timeAdded
        this.countPlaying = countPlaying
        this.stt = stt
    }

    fun setSelected(bool: Boolean) {
        this.isSelected = bool
    }

    override fun getMediaStoreArtwork(context: Context): InputStream? {
        return ArtworkUntils.getMediaStoreArtwork(context, this)
    }

    override fun getFolderArtwork(): InputStream? {
        return ArtworkUntils.getFolderArtwork(songPath)
    }

    override fun getTagArtwork(): InputStream? {
        return ArtworkUntils.getTagArtwork(songPath)
    }

    override fun getFolderArtworkFiles(): List<File>? {
        return ArtworkUntils.getAllFolderArtwork(songPath)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MusicItem?> = object : Parcelable.Creator<MusicItem?> {
            override fun createFromParcel(`in`: Parcel): MusicItem? {
                return MusicItem(`in`)
            }

            override fun newArray(size: Int): Array<MusicItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Extension method for [MediaMetadataCompat.Builder] to set the fields from
     * our JSON constructed object (to make the code a bit easier to see).
     */
 }