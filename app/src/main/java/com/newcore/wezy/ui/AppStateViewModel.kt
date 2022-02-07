package com.newcore.wezy.ui

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.Settings
import com.newcore.wezy.shareprefrances.SettingsPreferences

class AppStateViewModel(private val application: WeatherApplication, val weatherRepo: WeatherRepo) :AndroidViewModel(application) {

    val sharedPreferences:SettingsPreferences by lazy {
        SettingsPreferences(application)
    }




    // shared preferences

    var settings:Settings
        get() =sharedPreferences.get()
        set(value) = sharedPreferences.insert(value)


    // view model factory
    class Factory(private val app: WeatherApplication, private val repository: WeatherRepo) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppStateViewModel(app,repository) as T
        }
    }
}