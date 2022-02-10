package com.newcore.wezy.ui.homescreen

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentHomeScreenBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.ApiViewHelper
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

    val homeScreenViewModel by lazy {
        val viewModelFactory = HomeScreenViewModel.Factory(
            requireContext().applicationContext as WeatherApplication,
            WeatherRepo(
                SettingsPreferences(requireContext().applicationContext as WeatherApplication),
                WeatherDatabase(requireContext().applicationContext)
            ),
            viewModel
        )
        ViewModelProvider(this, viewModelFactory)[HomeScreenViewModel::class.java]
    }

    private val hourlyAdapter by lazy{
        HourlyAdapter().apply {
            setOnItemClickListener {}
        }
    }

    var job:Job? = null;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()

        binding.srlRefreshWeather.setOnRefreshListener {
            homeScreenViewModel.refreshCurrent(::hideLoading)
        }

        viewModel.settingsMutableLiveData.observe(viewLifecycleOwner) { settings ->
            homeScreenViewModel.locationChanged(settings)
        }

        viewModel.weatherLangLiveData.observe(viewLifecycleOwner) { weatherState ->
            val settings = viewModel.getSettings();
            println("i am in onViewCreated in home screen fragment")

            when (weatherState) {
                is WeatherState.Loading -> showLoading()
                is WeatherState.NOLocationInSettings -> hideLoading().also {
                    println("need to set location")
//                    showSnackbar("need to set location")
                }
                is WeatherState.NoInternetConnection -> hideLoading().also {
                    println("No Internet Connection")

//                    showSnackbar("No Internet Connection")

                }
                is WeatherState.NoWeatherWasFound -> hideLoading().also {
                    println("No Weather Was Found")

//                    showSnackbar("No Weather Was Found")

                }
                is WeatherState.ServerError -> hideLoading().also {
                    println("server error")

//                    showSnackbar("server error")

                }
                is WeatherState.Success -> {
                    hideLoading()
                    binding.apply {

                        val weatherLang =
                            homeScreenViewModel.getWeatherFromWeatherLang(settings, weatherState)
                        val current = weatherLang?.current


                        hourlyAdapter.differ.submitList(weatherLang?.hourly)

                        current?.apply {
                            val todayWeather = weather[0];

                            weather.showRainOrSnowOrNot(rainy,snow) {
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

    override fun showLoading(message: String?) {
        binding.srlRefreshWeather.isRefreshing = true
    }

    override fun hideLoading() {
        binding.srlRefreshWeather.isRefreshing = false
    }


    //    private fun setupRecycleView(){
//        binding.rvBreakingNews.apply {
//            adapter = newsAdapter
//            layoutManager = LinearLayoutManager(activity)
//        }
//    }

}