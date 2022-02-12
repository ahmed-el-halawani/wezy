package com.newcore.wezy.models.weatherentities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.newcore.wezy.utils.Constants
import java.io.Serializable
import java.util.*


@Entity(
    tableName = Constants.WEATHER_TABLE
)
data class WeatherLang (


    @PrimaryKey var id: String,
    var lat: Double? = null,
    var lon: Double? = null,
    var arabicResponse: WeatherResponse? = null,
    var englishResponse: WeatherResponse? = null,

    ) : Serializable