package com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants.randomThumb
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ConfigApp
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.newpipe.ExtractorHelper
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.Page
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class SearchOnlineUtils(
    private var mContext: Context,
    private var onSearchResult: OnSearchResultCallback,
    private var loadMoreListener: LoadMoreCallback
) {
    var filter = "&sp=EgIQAQ%253D%253D"
    private var filter2 = "videos"
    private val filterSC = "tracks"
    private var mQuery = ""
    private val listPubBlock = ArrayList<String>()
    private fun getFilter(context: Context?): String {
        return if (context?.let { ConfigApp(it).dataType } == 0) filter2 else filterSC
    }

    private fun getTypeSearch(cx: Context?): Int {
        return cx?.let { ConfigApp(it).dataType } ?: 1
    }

    @SuppressLint("CheckResult")
    fun search(query: String) {
        mQuery = query
        ExtractorHelper.searchFor(
            getTypeSearch(mContext), query, listOf(getFilter(mContext)), getFilter(mContext)
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .doOnEvent { searchResult: SearchInfo?, throwable: Throwable? -> }
            .subscribe({ searchResult: SearchInfo -> pareSearch(searchResult) }) { exception: Throwable? ->
                onErrorSearch(
                    exception
                )
            }
    }

    private fun onErrorSearch(exception: Throwable?) {
        onSearchResult.onSearchFailed("")
    }

    private fun onErrorSearchMore(exception: Throwable?) {
        loadMoreListener.onLoadMoreFailed()
    }

    private fun isContainsBlock(name: String, pub: String, des: String): Boolean {
        for (str in listPubBlock) {
            if (name.uppercase(Locale.getDefault()).contains(str.uppercase(Locale.getDefault()))
                || pub.uppercase(Locale.getDefault()).contains(str.uppercase(Locale.getDefault()))
                || des.uppercase(Locale.getDefault()).contains(str.uppercase(Locale.getDefault()))
                || str.uppercase(Locale.getDefault()).contains(name.uppercase(Locale.getDefault()))
                || str.uppercase(Locale.getDefault()).contains(pub.uppercase(Locale.getDefault()))
                || str.uppercase(Locale.getDefault()).contains(des.uppercase(Locale.getDefault()))
            ) return false
        }
        return true
    }

    @SuppressLint("CheckResult")
    fun pareSearch(searchResult: SearchInfo) {
        val lstAudio = ArrayList<ItemMusicOnline>()
        val next = searchResult.nextPage
        val lst = searchResult.relatedItems
        for (i in lst.indices) {
            val infoItem = lst[i]
            if (infoItem is StreamInfoItem) {
                val url = infoItem.url
                val name = infoItem.name
                val duration = infoItem.duration * 1000
                val thumb = randomThumb()
                val videoFromSearch = ItemMusicOnline(
                    0, AppUtils.getVideoID(url), url, name, duration, thumb
                )
                if (isContainsBlock(name, infoItem.uploaderName?: "null", infoItem.shortDescription ?: "null")) {
                    lstAudio.add(videoFromSearch)
                }
            }
        }
        onSearchResult.onSearchSuccessful(lstAudio, next)
    }

    fun parserSearchMoreSingle(searchResult: InfoItemsPage<*>) {
        val lstAudio = ArrayList<ItemMusicOnline>()
        val next = searchResult.nextPage
        if (searchResult.items != null) {
            val lst: List<*> = searchResult.items
            for (i in lst.indices) {
                val infoItem = lst[i]
                if (infoItem is StreamInfoItem) {
                    val url = infoItem.url
                    val name = infoItem.name
                    val duration = infoItem.duration * 1000
                    val thumb = randomThumb()
                    val videoFromSearch = ItemMusicOnline(
                        0,
                        AppUtils.getVideoID(url),
                        url,
                        name,
                        duration,
                        thumb,
                        infoItem.thumbnailUrl
                    )
                    if (isContainsBlock(name, infoItem.uploaderName?: "null", infoItem.shortDescription?: "null")) {
                        lstAudio.add(videoFromSearch)
                    }
                }
            }
            loadMoreListener.onLoadMoreSuccess(lstAudio, next)
        }
    }

    @SuppressLint("CheckResult")
    fun parserSearchMore(searchResult: InfoItemsPage<*>) {
        val lstAudio = ArrayList<ItemMusicOnline>()
        val next = searchResult.nextPage
        if (searchResult.items != null) {
            val lst: List<*> = searchResult.items
            for (i in lst.indices) {
                val infoItem = lst[i]
                if (infoItem is StreamInfoItem) {
                    val url = infoItem.url
                    val name = infoItem.name
                    val duration = infoItem.duration * 1000
                    val thumb = randomThumb()
                    val videoFromSearch = ItemMusicOnline(
                        0,
                        AppUtils.getVideoID(url),
                        url,
                        name,
                        duration,
                        thumb,
                        infoItem.thumbnailUrl
                    )
                    if (isContainsBlock(name, infoItem.uploaderName?: "null", infoItem.shortDescription?: "null")) {
                        lstAudio.add(videoFromSearch)
                    }
                }
            }
            loadMoreListener.onLoadMoreSuccess(lstAudio, next)
        }
    }

    @SuppressLint("CheckResult")
    fun searchMore(querry: String?, nextPage: Page?) {
        ExtractorHelper.getMoreSearchItems(
            getTypeSearch(mContext),
            querry,
            listOf(getFilter(mContext)),
            getFilter(mContext),
            nextPage
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .doOnEvent { searchResult: InfoItemsPage<*>?, throwable: Throwable? -> }
            .subscribe({ searchResult: InfoItemsPage<*> -> parserSearchMore(searchResult) }) { exception: Throwable? ->
                onErrorSearchMore(
                    exception
                )
            }
    }

    @SuppressLint("CheckResult")
    fun searchCategory(query: String) {
        mQuery = query
        ExtractorHelper.searchFor(
            getTypeSearch(mContext), query, Arrays.asList(getFilter(mContext)), getFilter(mContext)
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .doOnEvent { searchResult: SearchInfo?, throwable: Throwable? -> }
            .subscribe({ searchResult: SearchInfo -> pareSearchCategory(searchResult) }) { exception: Throwable? ->
                onErrorSearch(
                    exception
                )
            }
    }

    private fun pareSearchCategory(searchResult: SearchInfo) {
        val lstAudio = ArrayList<ItemMusicOnline>()
        val lst = searchResult.relatedItems
        val next = searchResult.nextPage
        for (i in lst.indices) {
            val infoItem = lst[i]
            if (infoItem is StreamInfoItem) {
                val url = infoItem.url
                val name = infoItem.name
                val duration = infoItem.duration * 1000
                val thumb = randomThumb()
                val videoFromSearch = ItemMusicOnline(
                    0, AppUtils.getVideoID(url), url, name, duration, thumb, infoItem.thumbnailUrl
                )
                if (isContainsBlock(name, infoItem.uploaderName?: "null", infoItem.shortDescription?: "null")) {
                    if (duration > 0) {
                        lstAudio.add(videoFromSearch)
                    }
                }
            }
        }
        onSearchResult.onSearchSuccessful(lstAudio, next)
    }

    @SuppressLint("CheckResult")
    fun loadMoreSongCategory(query: String?, nextPage: Page?) {
        val lstAudio = ArrayList<ItemMusicOnline>()
        lstAudio.clear()
        ExtractorHelper.getMoreSearchItems(
            getTypeSearch(mContext),
            query,
            listOf(getFilter(mContext)),
            getFilter(mContext),
            nextPage
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .doOnEvent { searchResult: InfoItemsPage<*>?, throwable: Throwable? -> }
            .subscribe({ searchResult: InfoItemsPage<*> -> parserSearchMoreCategory(searchResult) }) { exception: Throwable? ->
                onErrorSearchMore(
                    exception
                )
            }
    }

    private fun parserSearchMoreCategory(searchResult: InfoItemsPage<*>) {
        val lstAudio = ArrayList<ItemMusicOnline>()
        val next = searchResult.nextPage
        if (searchResult.items != null) {
            val lst: List<*> = searchResult.items
            for (i in lst.indices) {
                val infoItem = lst[i]
                if (infoItem is StreamInfoItem) {
                    val url = infoItem.url
                    val name = infoItem.name
                    val duration = infoItem.duration * 1000
                    val thumb = randomThumb()
                    val videoFromSearch = ItemMusicOnline(
                        0,
                        AppUtils.getVideoID(url),
                        url,
                        name,
                        duration,
                        thumb,
                        infoItem.thumbnailUrl
                    )
                    if (isContainsBlock(name, infoItem.uploaderName?: "null", infoItem.shortDescription?: "null")) {
                        if (duration > 0) {
                            lstAudio.add(videoFromSearch)
                        }
                    }
                }
            }
        }
        loadMoreListener.onLoadMoreSuccess(lstAudio, next)
    }

    init {
        listPubBlock.clear()
        listPubBlock.addAll(ConfigApp(mContext).listBlock)
    }
}