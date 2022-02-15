package com.newcore.wezy.ui.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.repository.Utils
import com.newcore.wezy.services.ReCallService
import com.newcore.wezy.shareprefrances.MLocation
import com.newcore.wezy.ui.AppStateViewModel
import com.newcore.wezy.utils.Constants
import com.newcore.wezy.utils.Resource
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MapsViewModel(val app: Application) : AndroidViewModel(app) {
    var locationMutableLiveData = MutableLiveData<Resource<MLocation>>()

    fun setLocation(latLng: LatLng, appStateViewModel: AppStateViewModel) {
        try {
            locationMutableLiveData.postValue(Resource.Loading())
            if (appStateViewModel.hasInternet()) {
                requestUpdateLocation(latLng, appStateViewModel)
            } else {
                locationMutableLiveData.postValue(
                    Resource.Success(
                        MLocation(
                            latLng,
                            "cant get country name but location added successfully",
                            "no internet connection"
                        )
                    )
                )
            }
        } catch (t: Throwable) {
            Log.e("GeoCoderError", t.message.toString())
        }
    }


    private fun requestUpdateLocation(latLng: LatLng, appStateViewModel: AppStateViewModel) {
        val locale = ViewHelpers
            .returnByLanguage(
                appStateViewModel.getSettings().language,
                Locale("ar"),
                Locale("en")
            )

        CoroutineScope(Dispatchers.IO).launch {
            if (appStateViewModel.hasInternet()) {
                val address = Utils.getAddresses(app, latLng, locale)
                if (address != null&&address.isNotEmpty()) {
                    val name = "${address[0].countryName}, ${address[0].adminArea}"
                    withContext(Dispatchers.Main) {
                        locationMutableLiveData.postValue(
                            Resource.Success(
                                MLocation(latLng, name, address[0].countryName)
                            )
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        locationMutableLiveData.postValue(
                            Resource.Success(
                                MLocation(
                                    latLng,
                                    app.getString(R.string.cant_get_country_name),
                                    app.getString(R.string.server_error)
                                )
                            )
                        )
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    locationMutableLiveData.postValue(
                        Resource.Success(
                            MLocation(
                                latLng,
                                app.getString(R.string.cant_get_country_name),
                                app.getString(R.string.no_internet_connection)
                            )
                        )
                    )
                }
            }
        }
    }

    // view model factory
    class Factory(private val app: WeatherApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapsViewModel(app) as T
        }
    }
}


