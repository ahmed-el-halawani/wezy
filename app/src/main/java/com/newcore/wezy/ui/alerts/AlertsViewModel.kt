package com.newcore.wezy.ui.alerts

import android.content.Intent
import androidx.lifecycle.*
import androidx.work.*
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.models.MyAlert
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.services.LongRunningWorker
import com.newcore.wezy.services.StopAlarmBroadcast
import com.newcore.wezy.utils.Constants
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*

class AlertsViewModel(
    val application: WeatherApplication,
    private val repository: WeatherRepo
) : AndroidViewModel(application) {

    val loadingLiveData = MutableLiveData<Boolean>()


    fun getData(): LiveData<List<MyAlert>> = repository.getAllAlert()


    fun deleteAlert(alert: MyAlert) = viewModelScope.launch {
        application.sendBroadcast(Intent(application, StopAlarmBroadcast::class.java).apply {
            putExtra(Constants.EXTRA_NOTIFICATION_ID_CUSTOM, alert.id)
        })
//        WorkManager.getInstance(application).cancelUniqueWork(alert.id.toString())
        repository.removeAlert(alert)
    }

    suspend fun addAlert(alert: MyAlert) {
        val resAlert = repository.upsertAlert(application, alert)
        setupWorker(resAlert)
    }

    private fun setupWorker(alert: MyAlert) {

        val data = Data.Builder()

        data.putInt(Constants.MY_ALERT_ID, alert.id)
        data.putDouble(Constants.MY_ALERT_LAT, alert.lat?:0.0)
        data.putDouble(Constants.MY_ALERT_LNG, alert.lon?:0.0)
        data.putLong(Constants.MY_ALERT_TO, alert.toDT)
        data.putBoolean(Constants.IS_ALERT, alert.isAlarm)
        val waitingDuration =  alert.fromDT - Date().time

        if(waitingDuration<0)
            return

        val request = OneTimeWorkRequestBuilder<LongRunningWorker>()
            .setInitialDelay(Duration.ofMillis(waitingDuration))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .setInputData(data.build())
            .build()

        WorkManager.getInstance(application).enqueueUniqueWork(
            alert.id.toString(),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }


    // view model factory
    class Factory(
        private val app: WeatherApplication,
        private val repository: WeatherRepo
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlertsViewModel(app, repository) as T
        }
    }
}