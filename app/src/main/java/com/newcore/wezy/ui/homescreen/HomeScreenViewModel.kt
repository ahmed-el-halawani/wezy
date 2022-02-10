package com.newcore.wezy.ui.homescreen

import androidx.lifecycle.*
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
import com.newcore.wezy.utils.Constants.GET_OR_REFRESH_HOME_WITH_DATA
import com.newcore.wezy.utils.Either
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreenViewModel(
    val application: WeatherApplication,
    private val repository: WeatherRepo,
    private val appStateViewModel: AppStateViewModel
) : AndroidViewModel(application) {

    var location: MLocation? = null
    var language: Language = Language.Default;
    var settings: Settings

    val weatherLangLiveData = MutableLiveData<WeatherState<WeatherLang>>()

    init {
        println("i am here mather faker")
        location = appStateViewModel.getSettings().location;
        language = appStateViewModel.getSettings().language;
        settings = appStateViewModel.getSettings()
        viewModelScope.launch {
            getHomeWeather(location)
        }
    }

    fun refreshCurrent(afterFinish:(()->Unit)?=null)=viewModelScope.launch {
        getHomeWeather(location)
        afterFinish?.invoke()
    }

    fun locationChanged(settings: Settings) {
        println("i am in locationChanged")

        if (settings == this.settings)
            return

        if (settings.location?.latLng?.latitude != location?.latLng?.latitude ||
            settings.location?.latLng?.longitude != location?.latLng?.longitude
        ) {
            location = settings.location;

            viewModelScope.launch {
                getHomeWeather(location)
            }
        } else {
            this.settings = settings
            weatherLangLiveData.postValue(weatherLangLiveData.value)
        }
    }

    fun getWeatherFromWeatherLang(
        settings: Settings,
        weatherState: WeatherState.Success<WeatherLang>
    ): WeatherResponse? {
        return ViewHelpers.returnByLanguage(
            settings.language,
            weatherState.data.arabicResponse,
            weatherState.data.englishResponse
        )

    }



    suspend fun getHomeWeather(location: MLocation?) {

        weatherLangLiveData.postValue(WeatherState.Loading())

        if (location == null) {
            weatherLangLiveData.postValue(WeatherState.NOLocationInSettings())
        } else {
            val weatherState = handleHomeWeatherData(location);
            withContext(Dispatchers.Main) {
                weatherLangLiveData.postValue(weatherState)
            }

            if (!appStateViewModel.hasInternet()) {
                ReCallService.recall(
                    GET_OR_REFRESH_HOME_WITH_DATA,
                    {
                        weatherLangLiveData.postValue(WeatherState.Loading())

                        val weatherState = handleHomeWeatherData(location);
                        withContext(Dispatchers.Main) {
                            weatherLangLiveData.postValue(weatherState)
                        }
                    }, application
                )
            }
        }
    }


    suspend fun handleHomeWeatherData(location: MLocation?): WeatherState<WeatherLang> {
        return if (location == null)
            WeatherState.NOLocationInSettings()
        else repository.getOrUpdateHomeWeatherLang(application, location.latLng).let {
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


    // view model factory
    class Factory(
        private val app: WeatherApplication,
        private val repository: WeatherRepo,
        private val appStateViewModel: AppStateViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeScreenViewModel(app, repository, appStateViewModel) as T
        }
    }
}