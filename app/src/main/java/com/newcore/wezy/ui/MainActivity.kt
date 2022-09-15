package com.newcore.wezy.ui

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ActivityMainBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.Language
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.Constants.CHANNEL_ID
import com.newcore.wezy.utils.Constants.INTERNET_NOT_WORKING
import com.newcore.wezy.utils.Extensions.setupWithNavController2
import com.newcore.wezy.utils.INetwork
import com.newcore.wezy.utils.Resource
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.*
import java.time.Duration

data class ActivityResultData(val requestCode: Int, val resultCode: Int, val data: Intent?)
data class ActivityPermissionResultData(val requestCode: Int, val permissions: Array<out String>,val grantResults: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityPermissionResultData

        if (requestCode != other.requestCode) return false
        if (!permissions.contentEquals(other.permissions)) return false
        if (!grantResults.contentEquals(other.grantResults)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestCode
        result = 31 * result + permissions.contentHashCode()
        result = 31 * result + grantResults.contentHashCode()
        return result
    }
}


class MainActivity : AppCompatActivity(), INetwork {

    var activityResultLiveData = MutableLiveData<ActivityResultData>()

    var activityPermissionResultData = MutableLiveData<ActivityPermissionResultData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNotificationChanel()



//
//        val request = OneTimeWorkRequestBuilder<LongRunningWorker>()
//            .setInitialDelay(Duration.ofMillis(10000L))
////            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
//            .build()
//
//        WorkManager.getInstance(this)
//            .enqueue(request)

        appStateViewModel.splashScreenLiveData.observe(this) {
            if (it) {
//                requestOverLayPermission()

                setContentView(binding.root)

                setupBottomNavBar()

                internetStateObserver()

                splashScreenFadeOutAnimation()
            } else {
                initSplash()
            }
        }
    }


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

    fun showSnackbar(message: String? = null, undoAction: View.OnClickListener? = null,anchorViews:View?) {
        Snackbar.make(binding.newsNavHostFragment, message ?: "", Snackbar.LENGTH_LONG).apply {
            undoAction?.let { setAction("UNDO", it) }
            anchorView = anchorViews?:binding.bottomNavigationView;
            show()
        }
    }

    // get Gps location
    fun getGpsLocation() {
        appStateViewModel.locationPermissionMutableLiveData.postValue(Resource.Loading())

        showLoading("getting location ...")

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
            hideLoading()
            return
        }


        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create(),
            object : LocationCallback() {

                override fun onLocationResult(location: LocationResult) {
                    println("(fusedLocationClient.requestLocationUpdates)")
                    super.onLocationResult(location)
                    hideLoading()
                    appStateViewModel.updateSettingsLocation(
                        LatLng(location.locations[0].latitude, location.locations[0].longitude)
                    )
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

    private fun setupBottomNavBar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.newsNavHostFragment.id) as NavHostFragment

        binding.bottomNavigationView
            .setupWithNavController2(
                navHostFragment.navController,
                mapOf(
                    Pair(
                        R.id.favoriteScreen,
                        listOf(
                            R.id.favoriteScreen,
                            R.id.locationPreviewFragment
                        )
                    ),
                    Pair(
                        R.id.settingsFragment,
                        listOf(
                            R.id.settingsFragment,
                            R.id.mapsFragment
                        )
                    )
                )
            )
    }

    private fun internetStateObserver() {
        try {
            appStateViewModel.internetState.observe(this@MainActivity) { internetState ->
                if (internetState.equals(INTERNET_NOT_WORKING))
                    showNoInternet()
                else
                    hideNoInternet()
            }
        }catch (t:Throwable){
            Log.e("internetStateObserver", "internetStateObserver: "+t.message )
        }

    }

    private fun splashScreenFadeOutAnimation() {
        if(!appStateViewModel.isLoaded)
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                withContext(Dispatchers.Main){

                    binding.clHomeView.animate()
                        .alpha(1f).duration = 800

                    appStateViewModel.isLoaded = true
                }
            }
        else
            binding.clHomeView.alpha = 1f

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
                appStateViewModel.splashDone = true
                appStateViewModel.settingsMutableLiveData.postValue(settings)

                when (settings.language) {
                    Language.Arabic -> setAppLocale("ar")
                    Language.English -> setAppLocale("en")
                    Language.Default -> setAppLocale()
                }

            }
    }


    override fun onSupportNavigateUp(): Boolean {
        val mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.settingsFragment,
            R.id.favoriteScreen,
            R.id.alertsScreen,
            R.id.homeScreenFragment
        ).build()

        val navHostFragment = supportFragmentManager.findFragmentById(binding.newsNavHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController
        return (navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp())
    }



    private fun setupNotificationChanel(){
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText ="setting weather alarm"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        onActivityResult?.invoke(requestCode,resultCode,data);

        activityResultLiveData.postValue(ActivityResultData(requestCode, resultCode, data))



//
//        if (requestCode == 500) {
//            if (canDrawOverlays(this)) {
//                val request = OneTimeWorkRequestBuilder<LongRunningWorker>()
//                    .setInitialDelay(Duration.ofMillis(10000L))
////            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
//                    .build()
//
//                WorkManager.getInstance(this)
//                    .enqueue(request)
//            }
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        activityPermissionResultData.postValue(ActivityPermissionResultData(requestCode, permissions, grantResults))

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
                    return
                }
            }
            getGpsLocation()
        }

    }

     fun showLoading(message:String?) {
        dialog = ProgressDialog.show(this, "",message?:"Loading. Please wait...", true);
    }

     fun hideLoading() {
        dialog?.dismiss()
    }

    var dialog: AlertDialog? = null

}
