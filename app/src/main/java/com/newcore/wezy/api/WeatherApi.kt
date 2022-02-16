package com.newcore.wezy.api

import com.newcore.wezy.models.weatherentities.WeatherResponse
import com.newcore.wezy.utils.Constants.WEATHER_API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/onecall")
    suspend fun getWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("lang") lang:CallLanguage=CallLanguage.En,
        @Query("units") units:Units=Units.Standard,
        @Query("exclude") exclude:String = "minutely",
        @Query("appid") appId:String = WEATHER_API_KEY,
    ): Response<WeatherResponse>

    @GET("data/2.5/onecall")
    suspend fun getAlerts(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("lang") lang:CallLanguage=CallLanguage.En,
        @Query("units") units:Units=Units.Standard,
        @Query("exclude") exclude:String = "minutely,hourly,daily,current",
        @Query("appid") appId:String = WEATHER_API_KEY,
    ): Response<WeatherResponse>
}

enum class CallLanguage{
    Ar,En
}

enum class Units{
    Standard, Metric , Imperial
}