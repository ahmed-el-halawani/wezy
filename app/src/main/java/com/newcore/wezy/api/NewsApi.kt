package com.androiddevs.mvvmnewsapp.api

import com.newcore.wezy.models.NewsResponse
import com.newcore.wezy.utils.Constants.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("everything")
    suspend fun searchForNews(
        @Query("q") q:String,
        @Query("page") page:Int =1,
        @Query("from") from:String,
        @Query("sortBy") sortBy:String,
        @Query("apiKey") apiKey:String = API_KEY
    ): Response<NewsResponse>

    @GET("top-headlines")
    suspend fun getBreakingNews(
        @Query("country") q:String="us",
        @Query("page") page:Int =1,
        @Query("apiKey") apiKey:String = API_KEY
    ): Response<NewsResponse>
}