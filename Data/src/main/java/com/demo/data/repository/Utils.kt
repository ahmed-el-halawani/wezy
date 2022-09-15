package com.demo.data.repository

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.demo.core.utils.Either
import com.demo.data.shareprefrances.SettingsPreferences
import com.newcore.wezy.repository.WeatherRepo
import kotlinx.coroutines.delay
import java.util.*

object Utils {
    suspend fun getAddresses(context: Context, latLng: LatLng, locale: Locale): List<Address>?{
        var addresses:List<Address>? = null;
        var trays = 100


        Thread {
            addresses = try {

                val geocoder = Geocoder(context,locale)
                geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
            }catch (t:Throwable){
                ArrayList()
            }
        }.start()
        while(addresses==null){
            delay(20)
            if(trays--<=0)break
        }

        return addresses;
    }


    suspend fun getAddresses2(context: Context, latLng: LatLng): WeatherLang?{

        val location =  WeatherRepo(
            SettingsPreferences(context.applicationContext as Application),
            WeatherDatabase(context)
        ).getAlert(context,latLng)

        return  when(location){
            is Either.Error -> null
            is Either.Success -> location.data
        }
    }
}