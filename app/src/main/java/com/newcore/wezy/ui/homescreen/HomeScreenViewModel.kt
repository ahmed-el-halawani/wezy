package com.newcore.wezy.ui.homescreen

import androidx.lifecycle.*
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.demo.data.models.weatherentities.WeatherResponse
import com.newcore.wezy.repository.WeatherRepo
import com.demo.data.shareprefrances.Language
import com.demo.data.shareprefrances.MLocation
import com.demo.data.shareprefrances.Settings
import com.newcore.wezy.ui.AppStateViewModel
import com.newcore.wezy.ui.utils.ViewHelpers
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    val application: WeatherApplication,
    private val repository: WeatherRepo,
    private val appStateViewModel: AppStateViewModel
) : AndroidViewModel(application) {

    var location: MLocation? = null
    var language: Language = Language.Default;
    var settings: Settings



    init {
        location = appStateViewModel.getSettings().location;
        language = appStateViewModel.getSettings().language;
        settings = appStateViewModel.getSettings()
    }

    fun refreshCurrent(afterFinish:(()->Unit)?=null)=viewModelScope.launch {

        appStateViewModel.getHomeWeather(appStateViewModel.getSettings().location)
        afterFinish?.invoke()
    }

    fun locationChanged(settings: Settings):Boolean {
        println("i am in locationChanged")

        if (settings == this.settings)
            return false;

        if (settings.location?.latLng?.latitude != location?.latLng?.latitude ||
            settings.location?.latLng?.longitude != location?.latLng?.longitude) {
            location = settings.location;

            viewModelScope.launch {
                appStateViewModel.getHomeWeather(location)
            }
            this.settings = settings
            return false;
        } else {
            this.settings = settings
            appStateViewModel.weatherLangLiveData.postValue(appStateViewModel.weatherLangLiveData.value)
            return true;
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