package com.newcore.wezy.ui.homescreen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentHomeScreenBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.ApiViewHelper
import com.newcore.wezy.utils.Resource
import com.newcore.wezy.utils.ViewHelpers
import com.newcore.wezy.utils.ViewHelpers.convertFromKelvin
import com.newcore.wezy.utils.ViewHelpers.getStringSpeedUnit
import com.newcore.wezy.utils.ViewHelpers.numberLocalizer
import com.newcore.wezy.utils.ViewHelpers.showRainOrSnowOrNot
import com.newcore.wezy.utils.ViewHelpers.windSpeedFromMeterBerSecond
import kotlinx.coroutines.*
import java.util.*

class HomeScreenFragment
    : BaseFragment<FragmentHomeScreenBinding>(FragmentHomeScreenBinding::inflate) {

    lateinit var homeScreenViewModel:HomeScreenViewModel


    fun initHomeViewModel(){
        val viewModelFactory = HomeScreenViewModel.Factory(
            requireContext().applicationContext as WeatherApplication,
            WeatherRepo(
                SettingsPreferences(requireContext().applicationContext as WeatherApplication),
                WeatherDatabase(requireContext().applicationContext)
            ),
            viewModel
        )
        homeScreenViewModel = ViewModelProvider(this, viewModelFactory)[HomeScreenViewModel::class.java]
    }

    private val hourlyAdapter by lazy{
        HourlyAdapter().apply {
            setOnItemClickListener {}
        }
    }

    private val dailyAdapter by lazy{
        DailyAdapter().apply {
            setOnItemClickListener {}
        }
    }

    fun fromGps(){
        viewModel.locationPermissionMutableLiveData
            .observe(viewLifecycleOwner,object: Observer<Resource<Int>> {
                override fun onChanged(permissionState: Resource<Int>?) {
                    when(permissionState){
                        is Resource.Error -> {
                            println("location.gps.error")
                            hideLoading()
                            viewModel.locationPermissionMutableLiveData.removeObserver(this)
                            viewModel.locationPermissionMutableLiveData = MutableLiveData()
                        }
                        is Resource.Loading -> {
                            println("location.gps.Loading")

                            showLoading()
                        }
                        is Resource.Success -> {
                            println("location.gps.Success")
                            hideLoading()
                            viewModel.locationPermissionMutableLiveData.removeObserver(this)
                            viewModel.locationPermissionMutableLiveData = MutableLiveData()
                        }
                        null -> {
                            println("location.gps.null")
                        }
                    }
                }

            })

        getLocationWithGps()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupRecycleView()
        setupDailyRecycleView()
        Log.e("onAttachLocation Home", "onAttach:", )

    }

    var job:Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHomeViewModel()
        if(viewModel.hasInternet()){

            viewModel.settingsMutableLiveData.observe(viewLifecycleOwner) { settings ->
                if( homeScreenViewModel.locationChanged(settings)){
                    CoroutineScope(Dispatchers.IO).launch{
                        delay(500)
                        withContext(Dispatchers.Main){
                            setupRecycleView()
                            setupDailyRecycleView()
                        }
                    }
                }


            }
        }


        binding.srlRefreshWeather.setOnRefreshListener {
            if(viewModel.hasInternet())
                homeScreenViewModel.refreshCurrent(::hideLoading)
            else
                hideLoading()
        }


        viewModel.weatherLangLiveData.observe(viewLifecycleOwner) { weatherState ->
            val settings = viewModel.getSettings();

            when (weatherState) {
                is WeatherState.Loading -> showLoading()
                is WeatherState.NOLocationInSettings -> hideLoading().also {
                    binding.flNoLocation.visibility = View.VISIBLE
                    binding.flContent.visibility = View.GONE

                    binding.incNoLocation.btnGps.setOnClickListener { fromGps() }
                    binding.incNoLocation.btnMap.setOnClickListener {
                        findNavController()
                            .navigate(R.id.action_homeScreenFragment_to_mapsFragment)
                    }
                }
                is WeatherState.NoInternetConnection -> hideLoading().also {}

                is WeatherState.NoWeatherWasFound -> hideLoading().also {
                    Snackbar.make(binding.srlRefreshWeather, getString(R.string.no_weather_ws_found), Snackbar.LENGTH_LONG).apply {
                        show()
                    }

                }
                is WeatherState.ServerError -> hideLoading().also {
                    Snackbar.make(binding.srlRefreshWeather, getString(R.string.server_error), Snackbar.LENGTH_LONG).apply {
                        show()
                    }
                }

                is WeatherState.Success -> {
                    hideLoading()
                    if(binding.flNoLocation.visibility == View.VISIBLE){
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(300)
                            withContext(Dispatchers.Main){
                                binding.flNoLocation.visibility = View.GONE
                                binding.flContent.visibility = View.VISIBLE
                                binding.flContent.alpha = 0f
                                binding.flContent.animate()
                                    .alpha(1f).duration = 700
                            }
                        }
                    }else{
                        binding.flContent.visibility = View.VISIBLE
                    }


                    binding.apply {

                        val weatherLang =
                            homeScreenViewModel.getWeatherFromWeatherLang(settings, weatherState)
                        val current = weatherLang?.current


                        hourlyAdapter.differ.submitList(weatherLang?.hourly)
                        dailyAdapter.differ.submitList(weatherLang?.daily)

                        current?.apply {

                            if(weather.isEmpty())
                                return@observe
                            val todayWeather = weather[0];

                            weather.showRainOrSnowOrNot(rainy,snow,::liteon)

                            tvDescription.text = todayWeather.description

                            Glide.with(requireContext())
                                .load(ApiViewHelper.iconImagePathMaker(todayWeather.icon ?: "01d"))
                                .into(ivIcon)

                            tvTemp.text = convertFromKelvin(settings)
                                .numberLocalizer(settings.language)

                            tvFeelsLike.text = convertFromKelvin(settings)
                                .numberLocalizer(settings.language)

                            tvDayViewer.text = ViewHelpers.getDayFromUnix(sunrise?.toLong(),settings.language)
                            tvDate.text = ViewHelpers.getDateFromUnix(sunrise?.toLong(),settings.language)

                            tvSunrise.text = ViewHelpers.getTimeFromUnix(sunrise?.toLong(),settings.language)
                            tvSunset.text = ViewHelpers.getTimeFromUnix(sunset?.toLong(),settings.language)

                            tvTempUnit.text = ViewHelpers.getStringTempUnit(settings.tempUnit)
                            tvTempUnit2.text = ViewHelpers.getStringTempUnit(settings.tempUnit)

                            tvAddressLine.text = weatherLang.addressLine


                            incWeatherDetails.apply {
                                tvWindSpeed.text = windSpeedFromMeterBerSecond(settings)
                                    .numberLocalizer(settings.language)

                                tvWindSpeedUnit.text = getStringSpeedUnit(settings)

                                tvHumidity.text= humidity?.numberLocalizer(settings.language)

                                tvPressure.text = pressure?.numberLocalizer(settings.language)

                                tvUv.text = uvi?.numberLocalizer(settings.language)
                            }



                        }
                    }


                }
            }

        }

    }

    private fun liteon(){
        job?.cancel()
        job = lifecycleScope.launch {
            while (true) {
                delay(Random().nextInt(10000).toLong())
                binding.lit.visibility = View.VISIBLE
                delay(50)
                binding.lit.visibility = View.INVISIBLE
                delay(50)
                binding.lit.visibility = View.VISIBLE
                delay(50)
                binding.lit.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupRecycleView(){
        binding.rvHourlyWeather.apply {
            adapter = hourlyAdapter
            layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL, false)
        }
    }
    private fun setupDailyRecycleView(){
        binding.rvDailyDetails.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun showLoading(message: String?) {
        binding.srlRefreshWeather.isRefreshing = true
    }

    override fun hideLoading() {
        binding.srlRefreshWeather.isRefreshing = false
    }

}