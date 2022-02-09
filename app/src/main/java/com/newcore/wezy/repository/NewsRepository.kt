package com.newcore.wezy.repository

import androidx.lifecycle.LiveData
import com.androiddevs.mvvmnewsapp.api.RetrofitInstance
import com.newcore.wezy.localDb.WeatherDao
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.models.NewsResponse
import com.newcore.wezy.models.weatherentities.WeatherLang
import retrofit2.Response

class NewsRepository (
    val db: WeatherDatabase
): WeatherDao {
    //
    suspend fun searchForNews(
        q:String,
        page:Int =1,
        from:String="2022-02-03",
        sortBy:String="popularity"
    ) : Response<NewsResponse> {
        return RetrofitInstance.newsApi.searchForNews(q,page,from,sortBy)
    }
//

    suspend fun getBreakingNews(
        country:String="us",
        page:Int =1
    ) : Response<NewsResponse> {
        return RetrofitInstance.newsApi.getBreakingNews(country,page)
    }
//
//    override suspend fun upsert(article: Article): Long = db.articleDao().upsert(article)
//
//    override fun getAll(): LiveData<List<Article>> = db.articleDao().getAll()
//
//    override suspend fun deleteArticle(article: Article) =  db.articleDao().deleteArticle(article)

    override suspend fun upsert(weatherResponse: WeatherLang): Long {
        TODO("Not yet implemented")
    }

    override fun getAll(): LiveData<List<WeatherLang>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWithId(id: String): WeatherLang {
        TODO("Not yet implemented")
    }


}