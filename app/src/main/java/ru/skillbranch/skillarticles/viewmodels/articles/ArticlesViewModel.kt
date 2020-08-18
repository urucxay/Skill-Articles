package ru.skillbranch.skillarticles.viewmodels.articles

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.extensions.data.toArticleFilter
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticlesViewModel(handle: SavedStateHandle) :
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

    private val listData: LiveData<PagedList<ArticleItem>> = Transformations.switchMap(state) {
        val filter = it.toArticleFilter()
        return@switchMap buildPagedList(repository.rawQueryArticles(filter))
    }

    fun observeList(
        owner: LifecycleOwner,
        isBookmark: Boolean = false,
        onChange: (list: PagedList<ArticleItem>) -> Unit
    ) {
        updateState { it.copy(isBookmark = isBookmark) }
        listData.observe(owner, Observer { onChange(it) })
    }

    fun observeTags(
        owner: LifecycleOwner,
        onChange: (list: List<String>) -> Unit
    ) {
        repository.findTags().observe(owner, Observer(onChange))
    }

    fun observeCategories(
        owner: LifecycleOwner,
        onChange: (list: List<CategoryData>) -> Unit
    ) {
        repository.findCategoriesData().observe(owner, Observer(onChange))
    }

    private fun buildPagedList(
        dataFactory: DataSource.Factory<Int, ArticleItem>
    ): LiveData<PagedList<ArticleItem>> {
        val builder = LivePagedListBuilder(
            dataFactory,
            listConfig
        )

        if (isEmptyFilter()) {
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
        updateState {
            it.copy(searchQuery = query, isHashtagSearch = query.startsWith("#", true))
        }
    }

    fun handleToggleBookmark(articleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
        }
    }

    fun handleSuggestion(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementTagUseCount(tag)
        }
    }

    fun applyCategories(selectedCategories: List<String>) {
        updateState { it.copy(selectedCategories = selectedCategories) }
    }

    private fun isEmptyFilter() = currentState.searchQuery.isNullOrEmpty()
            && !currentState.isBookmark
            && currentState.selectedCategories.isEmpty()
            && !currentState.isHashtagSearch

    private fun zeroLoadingHandle() {
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(0, listConfig.initialLoadSizeHint)
            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }
        }
    }

    private fun itemAtEndHandle(itemAtEnd: ArticleItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val items =
                repository.loadArticlesFromNetwork(itemAtEnd.id.toInt() + 1, listConfig.pageSize)

            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }

            withContext(Dispatchers.Main) {
                notify(Notify.TextMessage("Loaded from network from ${items.firstOrNull()?.data?.id} to ${items.lastOrNull()?.data?.id}"))
            }
        }
    }

}

data class ArticlesState(
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = true,
    val isBookmark: Boolean = false,
    val selectedCategories: List<String> = emptyList(),
    val isHashtagSearch: Boolean = false
) : IViewModelState {

    override fun save(outState: SavedStateHandle) {
        outState.set("isSearch", isSearch)
        outState.set("searchQuery", searchQuery)
    }

    override fun restore(savedState: SavedStateHandle): IViewModelState {
        return copy(
            isSearch = savedState["isSearch"] ?: false,
            searchQuery = savedState["searchQuery"]
        )
    }
}

class ArticleBoundaryCallback(
    private val zeroLoadingHandle: () -> Unit,
    private val itemAtEndHandle: (ArticleItem) -> Unit
) : PagedList.BoundaryCallback<ArticleItem>() {

    override fun onZeroItemsLoaded() {
        //Storage is empty
        zeroLoadingHandle()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItem) {
        //need to load more items while user is scrolling down
        itemAtEndHandle(itemAtEnd)
    }
}