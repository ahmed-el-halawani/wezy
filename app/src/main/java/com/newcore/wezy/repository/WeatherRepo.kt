package com.newcore.wezy.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.androiddevs.mvvmnewsapp.api.RetrofitInstance
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.api.CallLanguage
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.models.MyAlert
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.repository.Utils.getAddresses
import com.newcore.wezy.shareprefrances.Settings
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.Constants.HOME_WEATHER_ID
import com.newcore.wezy.utils.Either
import com.newcore.wezy.utils.NetworkingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.*

class WeatherRepo(
    private val settingsPreferences: SettingsPreferences,
    private val db: WeatherDatabase
) {

    suspend fun removeWeatherLang(weatherLang: WeatherLang) {
        db.weatherDeo().deleteWeatherLang(weatherLang)
    }

    suspend fun removeAlert(alert: MyAlert) {
        db.weatherDeo().deleteAlert(alert)
    }

    suspend fun removeAlertWithId(alertId: Long) {
        db.weatherDeo().deleteAlertWithId(alertId)
    }

    fun getAllFav(): LiveData<List<WeatherLang>> =
        db.weatherDeo().getAll()


    fun getAllAlert(): LiveData<List<MyAlert>> =
        db.weatherDeo().getAllAlerts()

    suspend fun upsert(weatherLang: WeatherLang) = db.weatherDeo().upsert(weatherLang)

    suspend fun upsertAlert(context: Context, alert: MyAlert): MyAlert {
        var arabicCountry: String? = null

        var englishCountry: String? = null

        try {
            var addresses =
                getAddresses(context, LatLng(alert.lat ?: 0.0, alert.lon ?: 0.0), Locale.ENGLISH)
            if (addresses?.isNotEmpty() == true) {
                englishCountry = addresses[0].countryName
            }

            addresses =
                getAddresses(context, LatLng(alert.lat ?: 0.0, alert.lon ?: 0.0), Locale("ar"))
            if (addresses?.isNotEmpty() == true) {
                arabicCountry = addresses[0].countryName
            }

        } catch (t: Throwable) {

        }

        alert.apply {
            arabicCountryName = arabicCountry
            englishCountryName = englishCountry
        }

        val id: Long = db.weatherDeo().upsertAlert(alert)

        return alert.copy(id = id.toInt())
    }

    suspend fun getFromLocal(weatherId: String): Either<WeatherLang, RepoErrors> {
        val weatherLangFromLocal = db.weatherDeo().getWithId(weatherId)

        return if (weatherLangFromLocal == null)
            Either.Error(
                errorCode = RepoErrors.WeatherNotFound,
                message = "no Local Weather was found"
            )
        else
            Either.Success(weatherLangFromLocal)
    }

    suspend fun getHomeFromLocal(): Either<WeatherLang, RepoErrors> {
        return getFromLocal(HOME_WEATHER_ID);
    }

    data class GeoCoderDetails(val country: String?, val addressLine: String?)

    suspend fun getAlert(
        context: Context,
        latLng: LatLng
    ): Either<WeatherLang, RepoErrors> {

        return try {
        return if (NetworkingHelper.hasInternet(context)) {


            withContext(Dispatchers.IO) {

                val englishDetails = async {
                    val addresses = getAddresses(context, latLng, Locale.ENGLISH)
                    if (addresses?.isNotEmpty() == true) {
                        GeoCoderDetails(
                            addresses[0].countryName,
                            "${addresses[0].countryName}, ${addresses[0].adminArea}"
                        )
                    }else{
                        null
                    }

                }

                val arabicDetails = async {
                    val addresses = getAddresses(context, latLng, Locale("ar"))
                    if (addresses?.isNotEmpty() == true) {
                        GeoCoderDetails(
                            addresses[0].countryName,
                            "${addresses[0].countryName}, ${addresses[0].adminArea}"
                        )
                    }else{
                        null
                    }
                }


                val arabicResponse = async {
                    RetrofitInstance.weatherApi.getAlerts(
                        latLng.latitude,
                        latLng.longitude,
                        CallLanguage.Ar
                    )
                }

                val englishResponse = async {
                    RetrofitInstance.weatherApi.getAlerts(
                        latLng.latitude,
                        latLng.longitude,
                        CallLanguage.En
                    )
                }




                val arabicRes= arabicResponse.await()
                val englishRes = englishResponse.await()

                return@withContext if (arabicRes.isSuccessful && englishRes.isSuccessful)
                    Either.Success(
                        WeatherLang(
                            id = UUID.randomUUID().toString(),
                            lat = latLng.latitude,
                            lon = latLng.longitude,
                            arabicResponse = arabicRes.body()?.copy(
                                country = arabicDetails.await()?.country ?: arabicRes.body()?.timezone ?: "",
                                addressLine = arabicDetails.await()?.addressLine ?: arabicRes.body()?.timezone
                                ?: ""
                            ),
                            englishResponse = englishRes.body()?.copy(
                                country = englishDetails.await()?.country ?: englishRes.body()?.timezone ?: "",
                                addressLine = englishDetails.await()?.addressLine ?: englishRes.body()?.timezone
                                ?: ""
                            )
                        )
                    )
                else
                    Either.Error(
                        RepoErrors.ServerError
                    )
            }

        } else {
            Either.Error(
                RepoErrors.NoInternetConnection
            )
        }

        }catch (t:Throwable){
             Either.Error(
                RepoErrors.ServerError,
                 message = t.toString()
            )
        }
    }

    suspend fun getORUpdateWeather(
        context: Context,
        latLng: LatLng,
        weatherId: String
    ): Either<WeatherLang, RepoErrors> = withContext(Dispatchers.IO){

        try {
            return@withContext if (NetworkingHelper.hasInternet(context)) {

                   val englishDetails = async {
                       val addresses = getAddresses(context, latLng, Locale.ENGLISH)
                       if (addresses?.isNotEmpty() == true) {
                           GeoCoderDetails(
                               addresses[0].countryName,
                               "${addresses[0].countryName}, ${addresses[0].adminArea}"
                           )
                       }else{
                           null
                       }

                   }

                   val arabicDetails = async {
                       val addresses = getAddresses(context, latLng, Locale("ar"))
                       if (addresses?.isNotEmpty() == true) {
                           GeoCoderDetails(
                               addresses[0].countryName,
                               "${addresses[0].countryName}, ${addresses[0].adminArea}"
                           )
                       }else{
                           null
                       }
                   }

                val arabicResponse = async {
                    RetrofitInstance.weatherApi.getWeather(
                        latLng.latitude,
                        latLng.longitude,
                        CallLanguage.Ar
                    )
                }

                val englishResponse = async {
                    RetrofitInstance.weatherApi.getWeather(
                        latLng.latitude,
                        latLng.longitude,
                        CallLanguage.En
                    )
                }




                val arabicRes= arabicResponse.await()
                val englishRes = englishResponse.await()

                if (arabicRes.isSuccessful && englishRes.isSuccessful)
                    Either.Success(
                        WeatherLang(
                            id = weatherId,
                            lat = latLng.latitude,
                            lon = latLng.longitude,
                            arabicResponse = arabicRes.body()?.copy(
                                country = arabicDetails.await()?.country ?: arabicRes.body()?.timezone ?: "",
                                addressLine = arabicDetails.await()?.addressLine ?: arabicRes.body()?.timezone
                                ?: ""
                            ),
                            englishResponse = englishRes.body()?.copy(
                                country = englishDetails.await()?.country ?: englishRes.body()?.timezone ?: "",
                                addressLine = englishDetails.await()?.addressLine ?: englishRes.body()?.timezone
                                ?: ""
                            )
                        ).also {
                            db.weatherDeo().upsert(it)
                        }
                    )
                else
                    getFromLocal(weatherId)
//                    Either.Error(
//                        errorCode = RepoErrors.ServerError,
//                        message = "${arabicResponse.errorBody()} , ${englishResponse.errorBody()}")
            } else {
                getFromLocal(weatherId)
            }
        } catch (t: Throwable) {
            getFromLocal(weatherId)
        }
    }


    suspend fun getOrUpdateHomeWeatherLang(
        context: Context,
        latLng: LatLng
    ): Either<WeatherLang, RepoErrors> {
        return getORUpdateWeather(context, latLng, HOME_WEATHER_ID)
    }

    suspend fun createNewFavoriteWeather(
        context: Context,
        latLng: LatLng
    ): Either<WeatherLang, RepoErrors> {
        if (NetworkingHelper.hasInternet(context)) {
            var arabicCountry: String? = null
            var arabicAddressLine: String? = null

            var englishCountry: String? = null
            var englishAddressLine: String? = null
            try {

                var addresses = getAddresses(context, latLng, Locale.ENGLISH)
                if (addresses?.isNotEmpty() == true) {
                    englishCountry = addresses[0].countryName
                    englishAddressLine = "${addresses[0].countryName}, ${addresses[0].adminArea}"
                }

                val locale = Locale("ar")
                addresses = getAddresses(context, latLng, locale)
                if (addresses?.isNotEmpty() == true) {
                    arabicCountry = addresses[0].countryName
                    arabicAddressLine = "${addresses[0].countryName}, ${addresses[0].adminArea}"
                }

            } catch (t: Throwable) {
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
                            country = arabicCountry ?: arabicResponse.body()?.timezone ?: "",
                            addressLine = arabicAddressLine ?: arabicResponse.body()?.timezone ?: ""
                        ),
                        englishResponse = englishResponse.body()?.copy(
                            country = englishCountry ?: englishResponse.body()?.timezone ?: "",
                            addressLine = englishAddressLine ?: englishResponse.body()?.timezone
                            ?: ""
                        )
                    ).also {
                        db.weatherDeo().upsert(it)
                    }
                )
            else
                Either.Error(
                    errorCode = RepoErrors.ServerError,
                    message = "${arabicResponse.errorBody()} , ${englishResponse.errorBody()}"
                )
        } else {
            return Either.Error(
                errorCode = RepoErrors.NoInternetConnection,
                message = "no internet connection"
            )
        }
    }


    fun updateSettings(settings: Settings) {
        return settingsPreferences.insert(settings)
    }

    fun getSettings(): Settings {
        return settingsPreferences.get()
    }
}