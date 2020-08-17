package ru.skillbranch.skillarticles.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.BuildConfig
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.*

object DBManager {

    val db = Room.databaseBuilder(
        App.applicationContext(),
        AppDB::class.java,
        AppDB.DATABASE_NAME
    ).build()


}

@Database(
    entities = [Article::class,
        ArticleCounts::class,
        Category::class,
        ArticlePersonalInfo::class,
        Tag::class,
        ArticleTagXRef::class,
        ArticleContent::class
    ],
    version = AppDB.DATABASE_VERSION,
    exportSchema = true,
    views = [ArticleItem::class, ArticleFull::class]
)
@TypeConverters(DateConverter::class)
abstract class AppDB : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = BuildConfig.APPLICATION_ID + ".db"
        const val DATABASE_VERSION = 1
    }

    abstract fun articlesDao(): ArticlesDao
    abstract fun articleCountsDao(): ArticleCountsDao
    abstract fun articleContentsDao(): ArticleContentsDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun articlePersonalInfosDao(): ArticlesPersonalInfosDao
    abstract fun tagsDao(): TagsDao


}
