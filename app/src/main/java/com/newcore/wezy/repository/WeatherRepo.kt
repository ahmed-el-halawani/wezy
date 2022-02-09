package com.newcore.wezy.repository

import android.content.Context
import android.location.Geocoder
import com.androiddevs.mvvmnewsapp.api.RetrofitInstance
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.api.CallLanguage
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.shareprefrances.Settings
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.Constants.HOME_WEATHER_ID
import com.newcore.wezy.utils.Either
import com.newcore.wezy.utils.NetworkingHelper
import com.newcore.wezy.utils.Resource
import java.util.*

class WeatherRepo(
    private val settingsPreferences: SettingsPreferences,
    private val db: WeatherDatabase
) {

    suspend fun getORUpdateWeather(
        context: Context,
        latLng: LatLng,
        weatherId: String
    ): Either<WeatherLang,RepoErrors> {

//        try {
            if (NetworkingHelper.hasInternet(context)) {

                var arabicCountry:String?=null
                var arabicAddressLine:String?=null

                var englishCountry:String?=null
                var englishAddressLine:String?=null
                try {
                    var geocoder = Geocoder(context,Locale.ENGLISH)
                    var addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                    if(addresses.isNotEmpty()){
                        englishCountry = addresses[0].countryName
                        englishAddressLine = "${addresses[0].countryName}, ${addresses[0].adminArea}"
                    }


                    val locale = Locale("ar")
                    geocoder = Geocoder(context,locale)
                    addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                    if(addresses.isNotEmpty()){
                        arabicCountry = addresses[0].countryName
                        arabicAddressLine = "${addresses[0].countryName}, ${addresses[0].adminArea}"
                    }

                }catch (t:Throwable){

                }

                val arabicResponse = RetrofitInstance.weatherApi.getWeather(
                    latLng.latitude,
                    latLng.longitude,
                    CallLanguage.Ar
                )
                val englishResponse = RetrofitInstance.weatherApi.getWeather(
                    latLng.latitude,
                    latLng.longitude,
                    CallLanguage.En
                )

                return if (arabicResponse.isSuccessful && englishResponse.isSuccessful)
                    Either.Success(
                        WeatherLang(
                            id = weatherId,
                            lat = latLng.latitude,
                            lon = latLng.longitude,
                            arabicResponse = arabicResponse.body()?.copy(
                                country = arabicCountry?:englishCountry,
                                addressLine = arabicAddressLine?:englishAddressLine
                            ),
                            englishResponse = englishResponse.body()?.copy(
                                country = englishCountry,
                                addressLine = englishAddressLine
                            )
                        ).also {
                            db.weatherDeo().upsert(it)
                        }
                    )
                else
                    Either.Error(
                        errorCode = RepoErrors.ServerError,
                        message = "${arabicResponse.errorBody()} , ${englishResponse.errorBody()}")
            } else {
                val weatherLangFromLocal = db.weatherDeo().getWithId(weatherId)

                return if (weatherLangFromLocal == null)
                    Either.Error(
                        errorCode = RepoErrors.WeatherNotFound,
                        message = "no Local Weather was found")
                else
                    Either.Success(weatherLangFromLocal)
            }
//        }catch (t:Throwable){
//           return Either.Error(RepoErrors.ServerError, message = t.message)
//        }
    }

    suspend fun getOrUpdateHomeWeatherLang(context: Context, latLng: LatLng): Either<WeatherLang,RepoErrors> {
        return getORUpdateWeather(context,latLng, HOME_WEATHER_ID)
    }

    suspend fun createNewFavoriteWeather(
        context: Context,
        latLng: LatLng
    ):Either<WeatherLang,RepoErrors>{
        if (NetworkingHelper.hasInternet(context)) {
            var arabicCountry:String?=null
            var arabicAddressLine:String?=null

            var englishCountry:String?=null
            var englishAddressLine:String?=null
            try {
                var geocoder = Geocoder(context,Locale.ENGLISH)
                var addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                englishCountry = addresses[0].countryName
                if(addresses[0].maxAddressLineIndex>=0){
                    englishAddressLine = addresses[0].getAddressLine(0)
                }

                val locale = Locale("ar")
                geocoder = Geocoder(context,locale)
                addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                arabicCountry = addresses[0].countryName
                if(addresses[0].maxAddressLineIndex>=0){
                    arabicAddressLine = addresses[0].getAddressLine(0)
                }
            }catch (t:Throwable){

            }

            val arabicResponse = RetrofitInstance.weatherApi.getWeather(
                latLng.latitude,
                latLng.longitude,
                CallLanguage.Ar
            )
            val englishResponse = RetrofitInstance.weatherApi.getWeather(
                latLng.latitude,
                latLng.longitude,
                CallLanguage.En
            )

            return if (arabicResponse.isSuccessful && englishResponse.isSuccessful)
                Either.Success(
                    WeatherLang(
                        id = UUID.randomUUID().toString(),
                        lat = latLng.latitude,
                        lon = latLng.longitude,
                        arabicResponse = arabicResponse.body()?.copy(
                            country = arabicCountry?:englishCountry,
                            addressLine = arabicAddressLine?:englishAddressLine
                        ),
                        englishResponse = englishResponse.body()?.copy(
                            country = englishCountry,
                            addressLine = englishAddressLine
                        )
                    ).also {
                        db.weatherDeo().upsert(it)
                    }
                )
            else
                Either.Error(
                    errorCode = RepoErrors.ServerError,
                    message = "${arabicResponse.errorBody()} , ${englishResponse.errorBody()}")
        } else {
            return Either.Error(
                errorCode = RepoErrors.NoInternetConnection,
                message = "no internet connection")
        }
    }


    fun updateSettings(settings: Settings) {
        return settingsPreferences.insert(settings)
    }

    fun getSettings(): Settings {
        return settingsPreferences.get()
    }
}