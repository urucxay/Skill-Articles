package ru.skillbranch.skillarticles.viewmodels.bookmarks

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleStrategy
import ru.skillbranch.skillarticles.data.repositories.ArticlesDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleBoundaryCallback
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesState
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class BookmarksViewModel(handle: SavedStateHandle) :
    BaseViewModel<ArticlesState>(handle, ArticlesState()) {

    private val repository = ArticlesRepository
    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(30)
            .setInitialLoadSizeHint(50)
            .build()
    }

    private val listData = Transformations.switchMap(state) {
        val searchFn = if (!it.isBookmark) repository::searchArticles
        else repository::searchBookmarkedArticles

        val defaultFn = if (!it.isBookmark) repository::allArticles
        else repository::allBookmarkedArticles

        when {
            it.isSearch && !it.searchQuery.isNullOrBlank() -> buildPagedList(
                searchFn(it.searchQuery)
            )
            else -> buildPagedList(defaultFn())
        }
    }

    fun observeList(
        owner: LifecycleOwner,
        isBookmark: Boolean = false,
        onChange: (list: PagedList<ArticleItemData>) -> Unit
    ) {
        updateState { it.copy(isBookmark = isBookmark) }
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPagedList(
        dataFactory: ArticlesDataFactory
    ): LiveData<PagedList<ArticleItemData>> {
        val builder = LivePagedListBuilder<Int, ArticleItemData>(
            dataFactory,
            listConfig
        )

        if (dataFactory.strategy is ArticleStrategy.AllArticles) {
            builder.setBoundaryCallback(
                ArticleBoundaryCallback(
                    ::zeroLoadingHandle,
                    ::itemAtEndHandle
                )
            )
        }

        return builder
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch) }
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState { it.copy(searchQuery = query) }
    }

    fun handleToggleBookmark(id: String, isBookmark: Boolean) {
        repository.updateBookmark(id, !isBookmark)
        listData.value?.dataSource?.invalidate()
    }

    private fun zeroLoadingHandle() {
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            val items =
                repository.loadArticlesFromNetwork(
                    start = 0,
                    size = listConfig.initialLoadSizeHint
                )
            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }
        }
    }

    private fun itemAtEndHandle(lastLoadedArticle: ArticleItemData) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(
                start = lastLoadedArticle.id.toInt().inc(),
                size = listConfig.pageSize
            )

            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }
            //Show notification about loaded articles
            withContext(Dispatchers.Main) {
                notify(
                    Notify.TextMessage(
                        "Articles loaded from network " +
                                "${items.firstOrNull()?.id} to ${items.lastOrNull()?.id}"
                    )
                )
            }
        }
    }

}