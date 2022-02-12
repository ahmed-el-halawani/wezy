package com.newcore.wezy.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import java.util.*

object Utils {
    suspend fun getAddresses(context: Context, latLng: LatLng, locale: Locale): List<Address>?{
        var addresses:List<Address>? = null;
        var trays = 30
        Thread {
            addresses = try {
                val geocoder = Geocoder(context,locale)
                geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
            }catch (t:Throwable){
                ArrayList()
            }
        }.start()
        while(addresses==null){
            delay(100)
            if(trays--<=0)break
        }

        return addresses;
    }
}