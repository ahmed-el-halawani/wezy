package com.newcore.wezy.ui.locationPreviewFragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentLocationPreviewBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.demo.data.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.ui.homescreen.DailyAdapter
import com.newcore.wezy.ui.homescreen.HourlyAdapter
import com.newcore.wezy.ui.homescreen.WeatherState
import com.demo.core.utils.ApiViewHelper
import com.newcore.wezy.ui.utils.ViewHelpers
import com.newcore.wezy.ui.utils.ViewHelpers.convertFromKelvin
import com.newcore.wezy.ui.utils.ViewHelpers.getWeatherFromWeatherLang
import com.newcore.wezy.ui.utils.ViewHelpers.numberLocalizer
import com.newcore.wezy.ui.utils.ViewHelpers.showRainOrSnowOrNot
import com.newcore.wezy.ui.utils.ViewHelpers.windSpeedFromMeterBerSecond
import kotlinx.coroutines.*
import java.util.*


class LocationPreviewFragment
    : BaseFragment<FragmentLocationPreviewBinding>( FragmentLocationPreviewBinding::inflate ) {

    private val locationPreviewFragmentArgs:LocationPreviewFragmentArgs by navArgs()

    private val previewViewModel by lazy{
        ViewModelProvider(this,LocationPreviewViewModel.Factory(
            requireContext().applicationContext as WeatherApplication,
            WeatherRepo(
                SettingsPreferences(requireContext().applicationContext as WeatherApplication),
                WeatherDatabase(requireContext()),
            ),
            viewModel,
            locationPreviewFragmentArgs.location
        ))[LocationPreviewViewModel::class.java]
    }


    private var hourlyAdapter =
        HourlyAdapter().apply {
            setOnItemClickListener {}
        }
    private val dailyAdapter by lazy{
        DailyAdapter().apply {
            setOnItemClickListener {}
        }
    }


    var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(previewViewModel.isLoaded)
            binding.flLoading.flLoadingScreen.visibility = View.GONE


        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            withContext(Dispatchers.Main){
                setupRecycleView()
                setupDailyRecycleView()
            }
        }


        binding.srlRefreshWeather.setOnRefreshListener {
            previewViewModel.refreshCurrent(::hideLoading)
        }

        viewModel.settingsMutableLiveData.observe(viewLifecycleOwner) { settings ->
            if(previewViewModel.locationChanged(settings))
            {
                println("settings changes")
                Log.e("settings changes", "settings changes:")

                CoroutineScope(Dispatchers.IO).launch{
                    delay(200)
                    withContext(Dispatchers.Main){
                        setupRecycleView()
                        setupDailyRecycleView()
                    }
                }
            }
        }

        previewViewModel.weatherLangLiveData.observe(viewLifecycleOwner) { weatherState ->
            val settings = viewModel.getSettings();
            println("i am in onViewCreated in home screen fragment")

            when (weatherState) {
                is WeatherState.Loading -> showLoading()
                is WeatherState.NOLocationInSettings -> hideLoading().also {
                    println("need to set location")
//                    Snackbar.make(view, "need to set location", Snackbar.LENGTH_LONG).apply {
//                        show()
//                    }
                }
                is WeatherState.NoInternetConnection -> hideLoading().also {
                    println("No Internet Connection")
//                    Snackbar.make(view, "No Internet Connection", Snackbar.LENGTH_LONG).apply {
//                        show()
//                    }

                }
                is WeatherState.NoWeatherWasFound -> hideLoading().also {
                    println("No Weather Was Found")
//                    Snackbar.make(view, "No Weather Was Found", Snackbar.LENGTH_LONG).apply {
//                        show()
//                    }

                }
                is WeatherState.ServerError -> hideLoading().also {
                    println("server error")
//                    Snackbar.make(view, "server error", Snackbar.LENGTH_LONG).apply {
//                        show()
//                    }

                }
                is WeatherState.Success -> {
                    hideLoading()
                    binding.apply {

                        val weatherLang = getWeatherFromWeatherLang(settings, weatherState)

                        val current = weatherLang?.current

                        hourlyAdapter.differ.submitList(weatherLang?.hourly)
                        dailyAdapter.differ.submitList(weatherLang?.daily)

                        current?.apply {
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

                                tvWindSpeedUnit.text = ViewHelpers.getStringSpeedUnit(settings)

                                tvHumidity.text= humidity?.numberLocalizer(settings.language)

                                tvPressure.text = pressure?.numberLocalizer(settings.language)

                                tvUv.text = uvi?.numberLocalizer(settings.language)

                                tvCloud.text = clouds?.numberLocalizer(settings.language)
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
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
    }
    private fun setupDailyRecycleView(){
        binding.rvDailyDetails.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun showLoading(message: String?) {
        binding.srlRefreshWeather.isRefreshing = true
    }

    override fun hideLoading() {
        binding.srlRefreshWeather.isRefreshing = false


        if(!previewViewModel.isLoaded)
            binding.flLoading.flLoadingScreen.animate()
                .alpha(0f)
                .setDuration(400)
                .setListener(object:AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        binding.flLoading.flLoadingScreen.alpha = 0f
                        previewViewModel.isLoaded = true;
                    }
                });

    }


}