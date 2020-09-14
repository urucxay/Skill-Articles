package ru.skillbranch.skillarticles.data.remote

import retrofit2.Call
import retrofit2.http.*
import ru.skillbranch.skillarticles.data.remote.req.LoginReq
import ru.skillbranch.skillarticles.data.remote.res.*

interface RestService {

    @GET("articles/{article}/content")
    suspend fun loadArticleContent(@Path("article") articleId: String): ArticleContentRes

    @GET("articles/{article}/messages")
    fun loadComments(
        @Path("article") articleId: String,
        @Query("last") last: String? = null,
        @Query("limit") limit: Int = 10
    ): Call<List<CommentRes>>

    @GET("articles/{article}/counts")
    suspend fun loadArticleCounts(@Path("article") articleId: String): ArticleCountsRes

    @POST("auth/login")
    suspend fun login(@Body loginReq: LoginReq): AuthRes

    @GET("articles")
    suspend fun articles(
        @Query("last") last: String? = null,
        @Query("limit") limit: Int = 10
    ): List<ArticleRes>



    //    @POST("articles/{article}/messages")
//    suspend fun sendMessage(
//        @Path("article") articleId: String,
//        @Body message: MessageReq,
//        @Header("Authorization") token: String
//    ): MessageRes
//    @POST("articles/{article}/decrementLikes")
//    suspend fun decrementLike(
//        @Path("article") articleId: String,
//        @Header("Authorization") accessToken: String
//    ): LikeRes
//
//    @POST("articles/{article}/incrementLikes")
//    suspend fun incrementLike(
//        @Path("article") articleId: String,
//        @Header("Authorization") accessToken: String
//    ): LikeRes

//    //https://skill-articles.skill-branch.ru/api/v1/articles/{articleId}/addBookmark
//    @POST("articles/{article}/addBookmark")
//    suspend fun addBookmark(
//        @Path("article") articleId: String,
//        @Header("Authorization") accessToken: String
//    ): BookmarkRes
//
//    //https://skill-articles.skill-branch.ru/api/v1/articles/{articleId}/removeBookmark
//    @POST("articles/{article}/removeBookmark")
//    suspend fun removeBookmark(
//        @Path("article") articleId: String,
//        @Header("Authorization") accessToken: String
//    ): BookmarkRes
//
//    // https://skill-articles.skill-branch.ru/api/v1/auth/refresh
//    @POST("auth/refresh")
//    fun refreshToken(
//        @Body refreshToken: RefreshReq
//    ): Call<AuthRes>
//
//    //https://skill-articles.skill-branch.ru/api/v1/profile/avatar/upload
//    @Multipart
//    @POST("profile/avatar/upload")
//    suspend fun upload(
//        @Part file: MultipartBody.Part?,
//        @Header("Authorization") accessToken: String
//    ): UploadRes
//
//    //https://skill-articles.skill-branch.ru/api/v1/profile/avatar/remove
//    @PUT("profile/avatar/remove")
//    suspend fun remove(
//        @Header("Authorization") accessToken: String
//    ): UploadRes

}