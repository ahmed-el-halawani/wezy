package com.newcore.wezy.ui.alerts

import androidx.lifecycle.*
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.models.MyAlert
import com.newcore.wezy.repository.WeatherRepo
import java.util.*

class ModelAlertsViewModel(
    val application: WeatherApplication,
    private val repository: WeatherRepo
) : AndroidViewModel(application) {

    val myAlert by lazy {
        MyAlert(
            fromDT = Date().time,
            toDT = Calendar.getInstance().let {
                it.add(Calendar.HOUR,1)
                it.timeInMillis
            }
        )
    }




    // view model factory
    class Factory(
        private val app: WeatherApplication,
        private val repository: WeatherRepo
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ModelAlertsViewModel(app, repository) as T
        }
    }
}