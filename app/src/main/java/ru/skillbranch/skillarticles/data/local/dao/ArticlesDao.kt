package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem

@Dao
interface ArticlesDao : BaseDao<Article> {

    @Transaction
    fun upsert(list: List<Article>) {
        insert(list)
            .mapIndexed { index, recordResult -> if (recordResult == -1L) list[index] else null }
            .filterNotNull()
            .also { if (it.isNotEmpty()) update(it) }
    }

    @Query(
        """
        SELECT * FROM articles
    """
    )
    fun findArticles(): List<Article>

    @Query(
        """
            SELECT * FROM articles
            WHERE id = :id
        """
    )
    fun findArticleById(id: String): Article

    @Query(
        """
        SELECT * FROM ArticleItem
    """
    )
    fun findArticleItems(): List<ArticleItem>

    @Query(
        """
            SELECT * FROM ArticleItem
            WHERE category in (:categoryIds)
        """
    )
    fun findArticleItemsByCategoryIds(categoryIds: List<String>): List<ArticleItem>

    @Query(
        """
            SELECT * FROM ArticleItem
            INNER JOIN article_tag_x_ref AS refs ON refs.a_id = id
            WHERE refs.t_id in (:tag)
        """
    )
    fun findArticlesByTagId(tag: List<String>): List<ArticleItem>

    @RawQuery(observedEntities = [ArticleItem::class])
    fun findArticlesByRaw(simpleSQLiteQuery: SimpleSQLiteQuery): DataSource.Factory<Int, ArticleItem>

    @Query(
        """
        SELECT * FROM ArticleFull
        WHERE id = :articleId
    """
    )
    fun findFullArticle(articleId: String): LiveData<ArticleFull>
}