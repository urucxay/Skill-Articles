package ru.skillbranch.skillarticles.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.ArticleContentsDao
import ru.skillbranch.skillarticles.data.local.dao.ArticleCountsDao
import ru.skillbranch.skillarticles.data.local.dao.ArticlePersonalInfosDao
import ru.skillbranch.skillarticles.data.local.dao.ArticlesDao
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.models.AppSettings
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.extensions.data.toArticleContent

interface IArticleRepository {
    fun findArticle(articleId: String): LiveData<ArticleFull>
    fun getAppSettings(): LiveData<AppSettings>
    suspend fun toggleLike(articleId: String)
    suspend fun toggleBookmark(articleId: String)
    fun isAuth(): LiveData<Boolean>
    suspend fun sendMessage(articleId: String, text: String, answerToSlug: String?)
    fun loadAllComments(articleId: String, total: Int, errHandler: (Throwable) -> Unit): CommentsDataFactory
    suspend fun decrementLike(articleId: String)
    suspend fun incrementLike(articleId: String)
    fun updateSettings(settings: AppSettings)
    suspend fun fetchArticleContent(articleId: String)
    fun findArticleCommentCount(articleId: String): LiveData<Int>
}

object ArticleRepository : IArticleRepository {
    private val network = NetworkManager.api
    private val preferences = PrefManager

    private var articlesDao = db.articlesDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()
    private var articleCountsDao = db.articleCountsDao()
    private var articleContentDao = db.articleContentsDao()

    override fun findArticle(articleId: String): LiveData<ArticleFull> {
        return articlesDao.findFullArticle(articleId)
    }

    override fun getAppSettings(): LiveData<AppSettings> {
        return preferences.appSettings
    }

    override suspend fun toggleLike(articleId: String) {
        articlePersonalDao.toggleLikeOrInsert(articleId)
    }

    override suspend fun toggleBookmark(articleId: String) {
        articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override fun isAuth(): LiveData<Boolean> {
        return preferences.isAuthLiveData
    }

    override fun updateSettings(settings: AppSettings) {
        preferences.updateSettings(settings)
    }

    override suspend fun sendMessage(articleId: String, text: String, answerToSlug: String?) {
//        network.sendMessage(
//            articleId,
//            text,
//            answerToSlug,
//            User("777", "John Doe", "https://i.ibb.co/C1n19hD/photo-2020-04-25-22-30-17.jpg")
//        )

//        articleCountsDao.incrementCommentsCount(articleId)
    }

    override fun loadAllComments(articleId: String, total: Int, errHandler:(Throwable) -> Unit): CommentsDataFactory {
        return CommentsDataFactory(
            itemProvider = network,
            articleId = articleId,
            totalCount = total,
            errHandler = errHandler
        )
    }

    override suspend fun decrementLike(articleId: String) {
        articleCountsDao.decrementLike(articleId)
    }

    override suspend fun incrementLike(articleId: String) {
        articleCountsDao.incrementLike(articleId)
    }

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId)
        articleContentDao.insert(content.toArticleContent())
    }

    override fun findArticleCommentCount(articleId: String): LiveData<Int> {
        return articleCountsDao.getCommentsCount(articleId)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setupTestDao(
        articlesDao: ArticlesDao,
        articleCountsDao: ArticleCountsDao,
        articleContentDao: ArticleContentsDao,
        articlePersonalDao: ArticlePersonalInfosDao
    ) {
        this.articlesDao = articlesDao
        this.articleContentDao = articleContentDao
        this.articleCountsDao = articleCountsDao
        this.articlePersonalDao = articlePersonalDao
    }

    suspend fun refreshCommentsCount(articleId: String) {
        val counts = network.loadArticleCounts(articleId)
        articleCountsDao.updateCommentsCount(articleId, counts.comments)
    }
}

class CommentsDataFactory(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int,
    private val errHandler: (Throwable) -> Unit
) : DataSource.Factory<String?, CommentRes>() {
    override fun create(): DataSource<String?, CommentRes> =
        CommentsDataSource(itemProvider, articleId, totalCount, errHandler)
}

class CommentsDataSource(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int,
    private val errHandler: (Throwable) -> Unit
) : ItemKeyedDataSource<String, CommentRes>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<CommentRes>
    ) {
        try {
            val result = itemProvider.loadComments(
                articleId,
                params.requestedInitialKey,
                params.requestedLoadSize
            ).execute()

            callback.onResult(
                if (totalCount > 0) result.body()!! else emptyList(),
                0,
                totalCount
            )
        } catch (t: Throwable) {
            errHandler(t)
        }

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
        try {
            val result =
                itemProvider.loadComments(articleId, params.key, params.requestedLoadSize).execute()
            callback.onResult(result.body()!!)
        } catch (t: Throwable) {
            errHandler(t)
        }

    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
        try {
            val result =
                itemProvider.loadComments(articleId, params.key, -params.requestedLoadSize).execute()
            callback.onResult(result.body()!!)
        } catch (t: Throwable) {
            errHandler(t)
        }
    }

    override fun getKey(item: CommentRes): String = item.id
}