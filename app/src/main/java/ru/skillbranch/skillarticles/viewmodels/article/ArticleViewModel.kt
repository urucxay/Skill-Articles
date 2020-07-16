package ru.skillbranch.skillarticles.viewmodels.article

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleData
import ru.skillbranch.skillarticles.data.models.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.CommentsDataFactory
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticleViewModel(
    handle: SavedStateHandle,
    private val articleId: String
) : BaseViewModel<ArticleState>(handle, ArticleState()), IArticleViewModel {

    private val repository = ArticleRepository
    private var clearContent: String? = null
    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build()
    }

    private val listData: LiveData<PagedList<CommentItemData>> =
        Transformations.switchMap(getArticleData()) {
            buildPagedList(repository.allComments(articleId, it?.commentCount ?: 0))
        }

    init {
        subscribeOnDataSource(getArticleData()) { article, currentState ->
            article ?: return@subscribeOnDataSource null
            currentState.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                author = article.author,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, currentState ->
            content ?: return@subscribeOnDataSource null
            currentState.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) { info, currentState ->
            info ?: return@subscribeOnDataSource null
            currentState.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, currentState ->
            currentState.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }

        subscribeOnDataSource(repository.isAuth()) { isAuth, state ->
            state.copy(isAuth = isAuth)
        }
    }

    override fun getArticleContent(): LiveData<List<MarkdownElement>?> =
        repository.loadArticleContent(articleId)

    override fun getArticleData(): LiveData<ArticleData?> = repository.getArticle(articleId)

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> =
        repository.loadArticlePersonalInfo(articleId)

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleBookmark() {
        val toggleBookmark = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        }
        toggleBookmark()
        val msg = if (currentState.isBookmark) Notify.TextMessage("Add to bookmarks")
        else Notify.TextMessage("Remove from bookmarks")
        notify(msg)
    }

    override fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()
        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                "Don`t like it anymore",
                "No, still like it",
                toggleLike
            )
        }
        notify(msg)
    }

    override fun handleShare() {
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState {
            it.copy(
                isSearch = isSearch, isShowMenu = false, searchPosition = 0
                , searchResults = mutableListOf()
            )
        }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        if (clearContent == null && currentState.content.isNotEmpty()) clearContent =
            currentState.content.clearContent()
        val result = clearContent
            .indexesOf(query)
            .map { it to it + query.length }
        updateState { it.copy(searchQuery = query, searchResults = result, searchPosition = 0) }
    }

    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    override fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }

    override fun handleSendComment(comment: String?) {
        if (comment.isNullOrBlank()) {
            notify(Notify.TextMessage("Comment must be not empty"))
            return
        }

        updateState { it.copy(commentText = comment) }
        if (!currentState.isAuth) {
            navigate(NavigationCommand.StartLogin())
        } else {
            viewModelScope.launch {
                repository.sendComment(articleId, currentState.commentText!!, currentState.answerToSlug)
                withContext(Dispatchers.Main) {
                    updateState { it.copy(answerTo = null, answerToSlug = null, commentText = null) }
                }
            }
        }
    }

    fun observeList(
        owner: LifecycleOwner,
        onChanged: (list: PagedList<CommentItemData>) -> Unit
    ) {
        listData.observe(owner, Observer { onChanged(it) })
    }

    private fun buildPagedList(
        dataFactory: CommentsDataFactory
    ) : LiveData<PagedList<CommentItemData>>{
        return LivePagedListBuilder<String, CommentItemData>(dataFactory, listConfig)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(showBottomBar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(answerTo = null, answerToSlug = null, commentText = null) }
    }

    fun handleReplyTo(slug: String, name: String) {
        updateState { it.copy(answerToSlug = slug, answerTo = "Reply to $name") }
    }
}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReviews: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<MarkdownElement> = emptyList(),
    val commentsCount: Int = 0,
    val answerTo: String? = null,
    val answerToSlug: String? = null,
    val showBottomBar: Boolean = true,
    val commentText: String? = null
) : IViewModelState {

    override fun save(outState: SavedStateHandle) {
        outState.set("isSearch", isSearch)
        outState.set("searchQuery", searchQuery)
        outState.set("searchResults", searchResults)
        outState.set("searchPosition", searchPosition)
//        outState.set("commentText", commentText)
        outState.set("answerTo", answerTo)
        outState.set("answerToSlug", answerToSlug)
    }

    override fun restore(savedState: SavedStateHandle): IViewModelState {
        return copy(
            isSearch = savedState["isSearch"] ?: false,
            searchQuery = savedState["searchQuery"],
            searchResults = savedState["searchResults"] ?: emptyList(),
            searchPosition = savedState["searchPosition"] ?: 0,
//            commentText = savedState["commentText"],
            answerTo = savedState["answerTo"],
            answerToSlug = savedState["answerToSlug"]
        )
    }
}