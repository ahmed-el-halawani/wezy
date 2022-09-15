package com.newcore.wezy.ui.favorites

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.ui.AppStateViewModel
import kotlinx.coroutines.launch

class FavoritesViewModel (val application: WeatherApplication,
                          private val repository: WeatherRepo,
                          private val appStateViewModel: AppStateViewModel
) : AndroidViewModel(application) {


    fun deleteWeatherLang(weatherLang: WeatherLang) = viewModelScope.launch {
        repository.removeWeatherLang(weatherLang)
    }

    fun getData() : LiveData<List<WeatherLang>> = repository.getAllFav()

    fun addWeatherLang(weatherLang: WeatherLang) = viewModelScope.launch {
        repository.upsert(weatherLang)
    }

    fun createWeather(latLng: LatLng) = viewModelScope.launch {
        repository.createNewFavoriteWeather(application,latLng)
    }




    // view model factory
    class Factory(
        private val app: WeatherApplication,
        private val repository: WeatherRepo,
        private val appStateViewModel: AppStateViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(app, repository, appStateViewModel) as T
        }
    }
}