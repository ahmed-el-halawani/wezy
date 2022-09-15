package com.newcore.wezy.localDb

import androidx.room.TypeConverter
import com.demo.data.models.weatherentities.WeatherResponse
import com.google.gson.Gson
import com.newcore.wezy.localDb.utils.ConverterHelper

class Converters {


    @TypeConverter
    fun fromWeatherResponse(weatherResponse: WeatherResponse):String = ConverterHelper.toJson(weatherResponse)


    //to
    @TypeConverter
    fun toWeatherResponse(json:String): WeatherResponse {
        return Gson().fromJson(json, WeatherResponse::class.java)
    }

}