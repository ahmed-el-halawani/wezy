package com.demo.data.api

import com.newcore.wezy.api.WeatherApi
import com.demo.core.utils.Constants.WEATHER_BASE_URL
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
                .build()

            Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }


        val weatherApi: WeatherApi by lazy {
            retrofit.create(WeatherApi::class.java)
        }
    }
}