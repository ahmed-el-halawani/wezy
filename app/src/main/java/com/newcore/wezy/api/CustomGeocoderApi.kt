package com.newcore.wezy.api

import com.newcore.wezy.models.geocoder.GeocoderResponse
import com.newcore.wezy.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CustomGeocoderApi {

    @GET("geo/1.0/reverse")
    suspend fun getWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("limit") limit:Int = 1,
        @Query("appid") appId:String = Constants.WEATHER_API_KEY,
    ): Response<List<GeocoderResponse>>
}