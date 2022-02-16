package com.newcore.wezy.localDb

import androidx.lifecycle.LiveData
import androidx.room.*
import com.newcore.wezy.models.MyAlert
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.utils.Constants.MY_ALERTS_TABLE
import com.newcore.wezy.utils.Constants.WEATHER_TABLE

@Dao
interface WeatherDao {

    //insert article come from api
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weatherResponse: WeatherLang):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlert(myAlert: MyAlert):Long

    // get all articles stored in db
    @Query("Select * from $WEATHER_TABLE")
    fun getAll():LiveData<List<WeatherLang>>

    @Query("Select * from $MY_ALERTS_TABLE")
    fun getAllAlerts():LiveData<List<MyAlert>>

    @Query("SELECT * FROM $WEATHER_TABLE WHERE id=:id ")
    suspend fun getWithId(id: String): WeatherLang?

    @Delete
    suspend fun deleteWeatherLang(weatherLang: WeatherLang)

    @Delete
    suspend fun deleteAlert(myAlert: MyAlert)


    @Query("DELETE FROM $MY_ALERTS_TABLE WHERE id = :id")
    fun deleteAlertWithId(id: Long)

}