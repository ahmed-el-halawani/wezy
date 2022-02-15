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



    init {
        println("i am here mather faker")
        location = appStateViewModel.getSettings().location;
        language = appStateViewModel.getSettings().language;
        settings = appStateViewModel.getSettings()
    }

    fun refreshCurrent(afterFinish:(()->Unit)?=null)=viewModelScope.launch {

        appStateViewModel.getHomeWeather(appStateViewModel.getSettings().location)
        afterFinish?.invoke()
    }

    fun locationChanged2(settings: Settings):Boolean {
        if (settings == this.settings)
            return false;

        return if (settings.location?.latLng?.latitude != location?.latLng?.latitude ||
            settings.location?.latLng?.longitude != location?.latLng?.longitude) {
            location = settings.location;
            false
        }
        else{
            this.settings = settings
            true
        }
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