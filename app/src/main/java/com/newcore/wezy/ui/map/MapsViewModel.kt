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
import com.demo.data.repository.Utils
import com.demo.data.shareprefrances.MLocation
import com.newcore.wezy.ui.AppStateViewModel
import com.demo.core.utils.Resource
import com.newcore.wezy.ui.utils.ViewHelpers
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
                val address = Utils.getAddresses2(app, latLng)
                if (address != null) {
                   val address2 = ViewHelpers.returnByLanguage(
                       appStateViewModel.getSettings().language,
                       address.arabicResponse,
                       address.englishResponse
                   )
                    val name = address2?.timezone?.split("/")?.get(1) ?:""
                    withContext(Dispatchers.Main) {
                        locationMutableLiveData.postValue(
                            Resource.Success(
                                MLocation(latLng, address2?.timezone, name)
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


