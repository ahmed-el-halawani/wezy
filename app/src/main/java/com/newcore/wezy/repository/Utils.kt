package com.newcore.wezy.repository

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.shareprefrances.Language
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.Either
import com.newcore.wezy.utils.ViewHelpers
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


    suspend fun getAddresses2(context: Context, latLng: LatLng, locale: Locale): List<String>?{

        locale.language
        val location =  WeatherRepo(
            SettingsPreferences(context.applicationContext as Application),
            WeatherDatabase(context)
        ).getAlert(context,latLng)

        var addresses = when(location){
            is Either.Error -> null
            is Either.Success ->ArrayList<String>().apply {
                add(
                    ViewHelpers.returnByLanguage(
                        ViewHelpers.languageEnumFromLocale(locale),
                        location.data.arabicResponse?.addressLine?:"",
                        location.data.englishResponse?.addressLine?:""
                    )
                )
            }
        }



        return addresses;
    }
}