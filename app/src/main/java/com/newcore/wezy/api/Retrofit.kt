package com.androiddevs.mvvmnewsapp.api

import android.util.Log
import com.newcore.wezy.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {

            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client = OkHttpClient
                .Builder()
                .addInterceptor(logging)
                .addInterceptor {
                    Log.i("fromMiddleWare", it.toString())
                    it.proceed(it.request())
                }
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }


        val newsApi: NewsApi by lazy {
            retrofit.create(NewsApi::class.java)
        }
    }
}