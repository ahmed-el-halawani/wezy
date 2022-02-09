package com.newcore.wezy.models.weatherentities

import com.google.gson.annotations.SerializedName
import com.newcore.wezy.shareprefrances.Language
import java.io.Serializable



data class WeatherResponse(

    var country:String?=null,
    var addressLine:String?=null,
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("lon") var lon: Double? = null,
    @SerializedName("timezone") var timezone: String? = null,
    @SerializedName("timezone_offset") var timezoneOffset: Double? = null,
    @SerializedName("current") var current: Current? = Current(),
    @SerializedName("minutely") var minutely: ArrayList<Minutely> = arrayListOf(),
    @SerializedName("hourly") var hourly: ArrayList<Hourly> = arrayListOf(),
    @SerializedName("daily") var daily: ArrayList<Daily> = arrayListOf(),
    @SerializedName("alerts") var alerts: ArrayList<Alerts> = arrayListOf()

)  : Serializable