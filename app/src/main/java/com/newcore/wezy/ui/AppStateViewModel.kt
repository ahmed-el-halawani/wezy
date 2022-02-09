package com.newcore.wezy.ui

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng

import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.services.ReCallService
import com.newcore.wezy.shareprefrances.MLocation
import com.newcore.wezy.shareprefrances.Settings
import com.newcore.wezy.utils.Constants.GET_ADDRESS_AFTER_INTERNET_BACK
import com.newcore.wezy.utils.Constants.INTERNET_NOT_WORKING
import com.newcore.wezy.utils.Constants.INTERNET_WORKING
import com.newcore.wezy.utils.NetworkingHelper
import com.newcore.wezy.utils.Resource
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.*
import java.util.*

class AppStateViewModel(val application: WeatherApplication,private val weatherRepo: WeatherRepo)
    :AndroidViewModel(application) {


    private val geocoder = Geocoder(application, Locale.getDefault())
    var settingsMutableLiveData = MutableLiveData<Settings>()

    var locationPermissionMutableLiveData = MutableLiveData<Resource<Int>>()

    var splashScreenLiveData = MutableLiveData<Boolean>()
    var splashDone = false

    var internetState = MutableLiveData<String>()

    init {
        splashScreenLiveData.postValue(false)
        runSplash()
        hasInternet()
    }


     fun hasInternet():Boolean {
        return if (NetworkingHelper.hasInternet(application)){
            internetState.postValue(INTERNET_WORKING)
            true
        }else{
            internetState.postValue(INTERNET_NOT_WORKING)
            ReCallService.recall("hasInternet",::hasInternet,application)
            false
        }
    }

    private fun runSplash() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main){
                splashScreenLiveData.postValue(true)
            }
        }
    }


    // shared preferences
    fun getSettings():Settings = weatherRepo.getSettings()

    fun updateSettings(settings:(Settings)->Settings){
        settings(getSettings()).also {
            weatherRepo.updateSettings(it)
            settingsMutableLiveData.postValue(it)
        }
    }



    fun updateSettingsLocation(locationMutableLiveData:MutableLiveData<MLocation>){
        locationMutableLiveData.value?.also {mLocation->
            updateSettings {
                it.apply {
                    location = mLocation
                }
            }
        }
    }

    fun updateSettingsLocation(latLng: LatLng) {

           try {
               var  mLocation = MLocation(latLng,"","")

               if(hasInternet()){
                   mLocation = locationDetailsFromLatLng(latLng)
               }else{
                       ReCallService.recall(
                           GET_ADDRESS_AFTER_INTERNET_BACK,
                           {
                               mLocation = locationDetailsFromLatLng(latLng)
                               updateSettings { it.copy(location=mLocation) }
                           },
                           application
                       )
               }

                   updateSettings { it.copy(location=mLocation) }
           }catch (t:Throwable){
               Log.e("updateSettingsLocation", t.message?:"" )
           }
    }

    fun locationDetailsFromLatLng(latLng:LatLng):MLocation {
        var  mLocation = MLocation(latLng,"","")

        return try {
            val locale = ViewHelpers
                .returnByLanguage(getSettings().language,
                    Locale("ar"),
                    Locale("en")
                )

            val geocoder = Geocoder(application,locale)
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if(addresses.isNotEmpty()) {
                val address = addresses[0]
                val name = address.getAddressLine(0)
                mLocation = MLocation(latLng,name,address.countryName)
            }
            mLocation
        }catch (t:Throwable){
            Log.e("locationDetailsFromLatLng", t.message?:"" )
            mLocation
        }
    }

    // view model factory
    class Factory(private val app: WeatherApplication, private val repository: WeatherRepo) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppStateViewModel(app,repository) as T
        }
    }
}