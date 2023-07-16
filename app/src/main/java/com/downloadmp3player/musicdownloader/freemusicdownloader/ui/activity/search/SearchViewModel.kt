package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.search

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CategoryItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.LoadMoreCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.OnSearchResultCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.SearchOnlineUtils
import org.schabi.newpipe.extractor.Page
import java.util.ArrayList

class SearchViewModel() : ViewModel(),
    OnSearchResultCallback,
    LoadMoreCallback {
    lateinit var searchUtils: SearchOnlineUtils
    var mNextPage: Page? = null
    var mKeyword = ""

    var _responseSearch = MutableLiveData<ArrayList<ItemMusicOnline?>>()
    val responseSearchObverse: LiveData<ArrayList<ItemMusicOnline?>>
        get() = _responseSearch

    private var _responseLoadMore = MutableLiveData<ArrayList<ItemMusicOnline?>>()
    val responseLoadMoreObverse: LiveData<ArrayList<ItemMusicOnline?>> get() = _responseLoadMore

    private var _responseErrorSearch = MutableLiveData<String?>()
    val responseErrorSearch: LiveData<String?>
        get() = _responseErrorSearch

    private var _responseErrorLoadMore = MutableLiveData<String?>()
    val responseErrorLoadMore: LiveData<String?>
        get() = _responseErrorLoadMore


    private val isLoading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = isLoading

    init {
        isLoading.value = false
    }

    fun initSearchUtil(context: Context) {
        searchUtils = SearchOnlineUtils(context, this, this)
    }

    fun search(keyword: String) {
        mKeyword = keyword
        isLoading.value = true
        searchUtils.search(keyword)
    }

    fun getCategory(category: CategoryItem) {
        isLoading.value = true
        category.cateKeyword?.let { searchUtils.searchCategory(it) }
    }

    fun loadMoreData() {
        searchUtils.searchMore(mKeyword, mNextPage)
    }

    fun loadMoreCategoryData() {
        searchUtils.loadMoreSongCategory(mKeyword, mNextPage)
    }

    override fun onLoadMoreSuccess(list: ArrayList<ItemMusicOnline?>, nextPage: Page?) {
        mNextPage = nextPage
        _responseLoadMore.value = list
    }

    override fun onLoadMoreFailed() {
        isLoading.value = false
        _responseErrorLoadMore.value = ""
    }

    override fun onSearchSuccessful(videoFromSearch: ArrayList<ItemMusicOnline?>, nextPage: Page?) {
        mNextPage = nextPage
        isLoading.value = false
        _responseSearch.value = videoFromSearch
    }

    override fun onSearchFailed(message: String?) {
        isLoading.value = false
        _responseErrorSearch.value = message
    }
}