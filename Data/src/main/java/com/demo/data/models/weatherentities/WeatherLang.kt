package com.newcore.wezy.models.weatherentities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.demo.core.utils.Constants
import com.demo.data.models.weatherentities.WeatherResponse
import java.io.Serializable


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