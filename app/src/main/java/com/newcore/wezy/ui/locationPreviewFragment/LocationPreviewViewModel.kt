package com.newcore.wezy.ui.locationPreviewFragment

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.models.weatherentities.WeatherResponse
import com.newcore.wezy.repository.RepoErrors
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.services.ReCallService
import com.newcore.wezy.shareprefrances.Language
import com.newcore.wezy.shareprefrances.MLocation
import com.newcore.wezy.shareprefrances.Settings
import com.newcore.wezy.ui.AppStateViewModel
import com.newcore.wezy.ui.homescreen.WeatherState
import com.newcore.wezy.utils.Constants
import com.newcore.wezy.utils.Either
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationPreviewViewModel(val application: WeatherApplication,
                               private val repository: WeatherRepo,
                               private val appStateViewModel: AppStateViewModel,
                               val weatherLang:WeatherLang
) : AndroidViewModel(application) {

    val weatherLangLiveData = MutableLiveData<WeatherState<WeatherLang>>()
    var isLoaded = false

    val location by lazy {
        MLocation(LatLng(weatherLang.lat?:0.0,weatherLang.lon?:0.0))
    }

    var settings: Settings

    init {
        settings = appStateViewModel.getSettings()
        Log.e("AndroidViewModel", "AndroidViewModel", )
        viewModelScope.launch {
            getWeather()
        }
    }


    private suspend fun getWeather() {
        var res = repository.getORUpdateWeather(application, location.latLng,weatherLang.id);
        var weatherState = handleHomeWeatherResponse(res);
        withContext(Dispatchers.Main) {
            weatherLangLiveData.postValue(weatherState)
        }

        if (!appStateViewModel.hasInternet()) {
            ReCallService.recall(
                Constants.GET_OR_REFRESH_HOME_WITH_DATA,
                {
                    weatherLangLiveData.postValue(WeatherState.Loading())
                    res = repository.getORUpdateWeather(application, location.latLng,weatherLang.id);
                    weatherState = handleHomeWeatherResponse(res);
                    withContext(Dispatchers.Main) {
                        weatherLangLiveData.postValue(weatherState)
                    }
                },
                application
            )
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

    fun refreshCurrent(afterFinish:(()->Unit)?=null)=viewModelScope.launch {
        getWeather()
        afterFinish?.invoke()
    }

    fun locationChanged(settings: Settings):Boolean {
        if (settings == this.settings)
            return false;

        this.settings = settings
        weatherLangLiveData.postValue(weatherLangLiveData.value)
        return true;
    }



    // view model factory
    class Factory(
        private val app: WeatherApplication,
        private val repository: WeatherRepo,
        private val appStateViewModel: AppStateViewModel,
        private val weatherLang: WeatherLang
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationPreviewViewModel(app, repository, appStateViewModel,weatherLang) as T
        }
    }
}