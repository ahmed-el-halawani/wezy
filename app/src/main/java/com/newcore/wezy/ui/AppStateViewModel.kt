package com.newcore.wezy.ui

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.*
import com.demo.core.utils.Constants
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.repository.RepoErrors
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.services.ReCallService
import com.demo.data.shareprefrances.MLocation
import com.demo.data.shareprefrances.Settings
import com.newcore.wezy.ui.homescreen.WeatherState
import com.demo.core.utils.*
import com.demo.core.utils.Constants.INTERNET_NOT_WORKING
import com.demo.core.utils.Constants.INTERNET_WORKING
import com.demo.core.utils.Either
import com.demo.core.utils.Resource
import com.demo.data.shareprefrances.DefineLocationType
import com.newcore.wezy.ui.utils.ViewHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AppStateViewModel(val application: WeatherApplication, private val weatherRepo: WeatherRepo) :
    AndroidViewModel(application) {

    var isLoaded = false

    var settingsMutableLiveData = MutableLiveData<Settings>()

    var locationPermissionMutableLiveData = MutableLiveData<Resource<Int>>()

    var splashScreenLiveData = MutableLiveData<Boolean>()
    var splashDone = false

    var internetState = MutableLiveData<String>()

    val weatherLangLiveData = MutableLiveData<WeatherState<WeatherLang>>()


    init {
        splashScreenLiveData.postValue(false)
        runSplash()
        hasInternet()
    }

    suspend fun getHomeWeather(location: MLocation?) {
        weatherLangLiveData.postValue(WeatherState.Loading())

        if (location == null) {
            weatherLangLiveData.postValue(WeatherState.NOLocationInSettings())
        } else {
            var res = weatherRepo.getOrUpdateHomeWeatherLang(application, location.latLng);
            var weatherState = handleHomeWeatherResponse(res);
            withContext(Dispatchers.Main) {
                weatherLangLiveData.postValue(weatherState)
            }
            if (!hasInternet()) {
                ReCallService.recall(
                    Constants.GET_OR_REFRESH_HOME_WITH_DATA,
                    {
                        weatherLangLiveData.postValue(WeatherState.Loading())
                        res = weatherRepo.getOrUpdateHomeWeatherLang(application, location.latLng);
                        weatherState = handleHomeWeatherResponse(res);
                        withContext(Dispatchers.Main) {
                            weatherLangLiveData.postValue(weatherState)
                        }
                    },
                    application
                )
            }
        }
    }


    private fun handleHomeWeatherResponse(response: Either<WeatherLang, RepoErrors>): WeatherState<WeatherLang> {
        return response.let {
            when (it) {
                is Either.Error -> when (it.errorCode) {
                    RepoErrors.NoInternetConnection -> WeatherState.NOLocationInSettings(it.message)
                    RepoErrors.ServerError -> WeatherState.ServerError(it.message)
                    RepoErrors.WeatherNotFound -> WeatherState.NoWeatherWasFound(it.message)
                    RepoErrors.CantCreateWeather -> WeatherState.ServerError(it.message)
                }
                is Either.Success -> WeatherState.Success(it.data)
            }
        }
    }


    fun hasInternet(): Boolean {
//        return false
        return if (NetworkingHelper.hasInternet(application)) {
            internetState.postValue(INTERNET_WORKING)
            true
        } else {
            internetState.postValue(INTERNET_NOT_WORKING)
            ReCallService.recall("hasInternet", ::hasInternet, application)
            false
        }
    }

    private fun runSplash() = viewModelScope.launch {
        val location = getSettings().location;
        if (location == null) {
            withContext(Dispatchers.Main) {
                weatherLangLiveData.postValue(WeatherState.NOLocationInSettings())
                splashScreenLiveData.postValue(true)
            }
            return@launch;
        }
        val res = weatherRepo.getHomeFromLocal()
        val weatherState = handleHomeWeatherResponse(res);
        println(weatherState)
        withContext(Dispatchers.Main) {
            weatherLangLiveData.postValue(weatherState)
            splashScreenLiveData.postValue(true)
        }

        delay(2000)
        getHomeWeather(location)

    }


    // shared preferences
    fun getSettings(): Settings = weatherRepo.getSettings()

    fun updateSettings(settings: (Settings) -> Settings) {
        settings(getSettings()).also {
            weatherRepo.updateSettings(it)
            settingsMutableLiveData.postValue(it)
        }
    }


    fun updateSettingsLocation(locationMutableLiveData: MutableLiveData<MLocation>) {
        locationMutableLiveData.value?.also { mLocation ->
            updateSettings {
                it.apply {
                    location = mLocation
                }
            }
        }
    }

    fun updateSettingsLocation(latLng: LatLng,locationType: DefineLocationType) {

        try {
            val mLocation = MLocation(latLng, "", "")


//            if (hasInternet()) {
//                mLocation = locationDetailsFromLatLng(latLng)
//            } else {
//                ReCallService.recall(
//                    GET_ADDRESS_AFTER_INTERNET_BACK,
//                    {
//                        mLocation = locationDetailsFromLatLng(latLng)
//                        updateSettings { it.copy(location = mLocation) }
//                    },
//                    application
//                )
//            }

            updateSettings { it.copy(location = mLocation, defineLocationType = locationType) }
        } catch (t: Throwable) {
            Log.e("updateSettingsLocation", t.message ?: "")
        }
    }

    fun locationDetailsFromLatLng(latLng: LatLng): MLocation {
        var mLocation = MLocation(latLng, "", "")

        return try {
            val locale = ViewHelpers
                .returnByLanguage(
                    getSettings().language,
                    Locale("ar"),
                    Locale("en")
                )


            val geocoder = Geocoder(application, locale)
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val name = address.getAddressLine(0)
                mLocation = MLocation(latLng, name, address.countryName)
            }
            mLocation
        } catch (t: Throwable) {
            Log.e("locationDetailsFromLatLng", t.message ?: "")
            mLocation
        }
    }

    // view model factory
    class Factory(private val app: WeatherApplication, private val repository: WeatherRepo) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppStateViewModel(app, repository) as T
        }
    }
}