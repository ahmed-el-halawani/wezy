package com.newcore.wezy.ui

import android.Manifest
import android.app.ProgressDialog.show
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ActivityMainBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.DefineLocationType
import com.newcore.wezy.shareprefrances.Language
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.Constants.INTERNET_NOT_WORKING
import com.newcore.wezy.utils.INetwork
import com.newcore.wezy.utils.Resource
import com.newcore.wezy.utils.ViewHelpers

class MainActivity : AppCompatActivity(), INetwork {

    fun setAppLocale(localeCode: String? = null) =
        ViewHelpers.setAppLocale(localeCode, resources, this)

    val appStateViewModel by lazy {
        val viewModelFactory = AppStateViewModel.Factory(
            this.applicationContext as WeatherApplication,
            WeatherRepo(
                SettingsPreferences(this.applicationContext as WeatherApplication),
                WeatherDatabase(this.applicationContext)
            )
        )
        ViewModelProvider(this, viewModelFactory)[AppStateViewModel::class.java]
    }

    fun showSnackbar(message: String? = null, undoAction: View.OnClickListener) {
        Snackbar.make(binding.root, message ?: "", Snackbar.LENGTH_LONG).apply {
            setAction("UNDO", undoAction)
            show()
        }
    }

    // get Gps location
    fun getGpsLocation() {
        appStateViewModel.locationPermissionMutableLiveData.postValue(Resource.Loading())

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                200
            )

            return;
        }


        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create(),
            object : LocationCallback() {

                override fun onLocationResult(location: LocationResult) {
                    println("(fusedLocationClient.requestLocationUpdates)")
                    super.onLocationResult(location)

                    Log.e("Location", location.toString() ?: "")

                    appStateViewModel.updateSettingsLocation(
                        LatLng(location.locations[0].latitude, location.locations[0].longitude)
                    )
                }

                override fun onLocationAvailability(p0: LocationAvailability) {
                    super.onLocationAvailability(p0)
                }
            },
            Looper.getMainLooper()
        )

        appStateViewModel.locationPermissionMutableLiveData
            .postValue(Resource.Success(PackageManager.PERMISSION_GRANTED))

    }

    //show no internet notification
    override fun showNoInternet() {
        binding.tvNoInternetConnection.visibility = View.VISIBLE
    }

    //hide no internet notification
    override fun hideNoInternet() {
        binding.tvNoInternetConnection.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appStateViewModel.splashScreenLiveData.observe(this) {
            if (it) {
                setContentView(binding.root)

                window.setBackgroundDrawableResource(R.color.surfaceColor)

                val navHostFragment =
                    supportFragmentManager.findFragmentById(binding.newsNavHostFragment.id) as NavHostFragment

                binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)

                appStateViewModel.internetState.observe(this) { internetState ->
                    if (internetState.equals(INTERNET_NOT_WORKING)) {
                        showNoInternet()
                    } else {
                        hideNoInternet()
                    }

                    println(internetState)
                }
            } else {
                initSplash()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            permissions.forEach {
                println(it)
            }
            grantResults.forEach {
                println(it)
                if (it == PackageManager.PERMISSION_DENIED) {
                    appStateViewModel.locationPermissionMutableLiveData
                        .postValue(
                            Resource.Error(
                                "PERMISSION_DENIED check it in main",
                                PackageManager.PERMISSION_DENIED
                            )
                        )
                    println("(it==PackageManager.PERMISSION_DENIED)")
                    return;
                }
            }
            getGpsLocation()
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    private fun initSplash() {
        if (!appStateViewModel.splashDone)
            appStateViewModel.getSettings().also { settings ->
                appStateViewModel.settingsMutableLiveData.postValue(settings)

                if (settings.location == null) {
                    when (settings.defineLocationType) {
                        DefineLocationType.Gps -> TODO()
                        DefineLocationType.Maps -> {

                        }
                    }
                }

                when (settings.language) {
                    Language.Arabic -> setAppLocale("ar")
                    Language.English -> setAppLocale("en")
                    Language.Default -> setAppLocale()
                }

                appStateViewModel.splashDone = true
            }
    }

}
