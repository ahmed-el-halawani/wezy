package com.newcore.wezy.localDb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newcore.wezy.localDb.utils.ConverterHelper
import com.newcore.wezy.models.weatherentities.*
import java.lang.reflect.Type

class Converters {


    @TypeConverter
    fun fromWeatherResponse(weatherResponse: WeatherResponse):String = ConverterHelper.toJson(weatherResponse)


    //to
    @TypeConverter
    fun toWeatherResponse(json:String): WeatherResponse {
        return Gson().fromJson(json, WeatherResponse::class.java)
    }

}