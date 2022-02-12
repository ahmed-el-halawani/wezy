package com.newcore.wezy.shareprefrances

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.newcore.wezy.utils.Constants.ALL_DATA_ROUTE
import java.io.Serializable

class SettingsPreferences(private val application:Application) {


    companion object{
        private var instance: SettingsPreferences? = null
        private val Lock = Any()

        operator fun invoke(application:Application) = instance ?:synchronized(Lock){
            instance ?: SettingsPreferences(application)
        }
    }


    private val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
    }

    private fun settingsToJson(settings:Settings):String{
        val json = Gson()
        return json.toJson(settings)
    }

    private fun settingsFromJson(settings:String):Settings{
        val json = Gson()
        return json.fromJson(settings,Settings::class.java)
    }

    fun insert(settings: Settings) {
        sp.edit {
            putString(ALL_DATA_ROUTE,settingsToJson(settings))
            apply()
        }
    }

    fun get(): Settings {
        return sp.getString(ALL_DATA_ROUTE,null)?.let { settingsFromJson(it) }?:Settings.getDefault()
    }

}

data class Settings(
    var language:Language,
    var tempUnit: TempUnit,
    var defineLocationType: DefineLocationType,
    var windSpeedUnit: WindSpeedUnit,
    var notificationState: NotificationState,
    var location:MLocation?=null
    ){
        companion object{
            fun getDefault():Settings=Settings(
                Language.Default,
                TempUnit.Kelvin,
                DefineLocationType.Maps,
                WindSpeedUnit.MeterBerSecond,
                NotificationState.Enable
            )
        }
    }

enum class Language{
    Arabic,English,Default
}

enum class TempUnit{
    Kelvin,Celsius,Fahrenheit
}

enum class DefineLocationType{
    Gps,Maps
}

enum class WindSpeedUnit{
    MeterBerSecond,MileBerHour
}

enum class NotificationState{
    Enable,Disable
}

data class MLocation( val latLng: LatLng, val locationName:String?=null,val country: String?= null) : Serializable