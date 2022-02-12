package com.newcore.wezy.localDb

import androidx.lifecycle.LiveData
import androidx.room.*
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.utils.Constants.WEATHER_TABLE

@Dao
interface WeatherDao {

    //insert article come from api
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weatherResponse: WeatherLang):Long

    // get all articles stored in db
    @Query("Select * from $WEATHER_TABLE")
    fun getAll():LiveData<List<WeatherLang>>

    @Query("SELECT * FROM $WEATHER_TABLE WHERE id=:id ")
    suspend fun getWithId(id: String): WeatherLang?

    @Delete
    suspend fun deleteWeatherLang(weatherLang: WeatherLang)

}