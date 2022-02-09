package com.newcore.wezy.localDb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newcore.wezy.localDb.utils.ConverterHelper
import com.newcore.wezy.models.weatherentities.*
import java.lang.reflect.Type

class Converters {

    //from
    @TypeConverter
    fun fromAlerts(alerts: ArrayList<Alerts>):String = ConverterHelper.toJson(alerts)

    @TypeConverter
    fun fromCurrent(current: Current):String = ConverterHelper.toJson(current)

    @TypeConverter
    fun fromDaily(daily: ArrayList<Daily>):String = ConverterHelper.toJson(daily)

    @TypeConverter
    fun fromFeelsLike(feelsLike: FeelsLike):String = ConverterHelper.toJson(feelsLike)

    @TypeConverter
    fun fromHourly(hourly: ArrayList<Hourly>):String = ConverterHelper.toJson(hourly)

    @TypeConverter
    fun fromMinutely(minutely: ArrayList<Minutely>):String = ConverterHelper.toJson(minutely)

    @TypeConverter
    fun fromTemp(temp: Temp):String = ConverterHelper.toJson(temp)

    @TypeConverter
    fun fromWeather(weather: ArrayList<Weather>):String = ConverterHelper.toJson(weather)

    @TypeConverter
    fun fromTags(tags: ArrayList<String>):String = ConverterHelper.toJson(tags)

    @TypeConverter
    fun fromWeatherResponse(weatherResponse: WeatherResponse):String = ConverterHelper.toJson(weatherResponse)


    //to
    @TypeConverter
    fun toWeatherResponse(json:String): WeatherResponse {
        return Gson().fromJson(json, WeatherResponse::class.java)
    }

    @TypeConverter
    fun toAlerts(json:String): ArrayList<Alerts> = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toCurrent(json:String): Current = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toDaily(json:String): ArrayList<Daily> = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toFeelsLike(json:String): ArrayList<FeelsLike> = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toHourly(json:String): ArrayList<Hourly> = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toMinutely(json:String): ArrayList<Minutely> = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toTemp(json:String): Temp = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toWeather(json:String): ArrayList<Weather> = ConverterHelper.fromJson(json)

    @TypeConverter
    fun toTags(json:String):ArrayList<String> = ConverterHelper.fromJson(json)

}