package com.newcore.wezy.ui.homescreen

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.animation.addListener
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
import com.newcore.wezy.ui.adapters.NewsAdapter
import com.newcore.wezy.utils.ApiViewHelper
import com.newcore.wezy.utils.ILoading
import com.newcore.wezy.utils.ViewHelpers
import com.newcore.wezy.utils.ViewHelpers.convertFromKelvin
import com.newcore.wezy.utils.ViewHelpers.numberLocalizer
import com.newcore.wezy.utils.ViewHelpers.showRainOrSnowOrNot
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
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

        homeScreenViewModel.weatherLangLiveData.observe(viewLifecycleOwner) { weatherState ->

            val settings = viewModel.getSettings();
            println("i am in onViewCreated in home screen fragment")

            when (weatherState) {
                is WeatherState.Loading -> showLoading()
                is WeatherState.NOLocationInSettings -> hideLoading().also {
                    binding.tvDemo1.text = weatherState.message
                    showSnackbar("need to set location")
                }
                is WeatherState.NoInternetConnection -> hideLoading().also {
                    binding.tvDemo1.text = weatherState.message
                    showSnackbar("No Internet Connection")

                }
                is WeatherState.NoWeatherWasFound -> hideLoading().also {
                    binding.tvDemo1.text = weatherState.message
                    showSnackbar("No Weather Was Found")

                }
                is WeatherState.ServerError -> hideLoading().also {
                    binding.tvDemo1.text = weatherState.message
                    showSnackbar("server error")

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

                            tvTemp.text = temp
                                ?.convertFromKelvin(settings.tempUnit)
                                ?.numberLocalizer(settings.language)?:""

                            tvFeelsLike.text = feelsLike
                                ?.convertFromKelvin(settings.tempUnit)
                                ?.numberLocalizer(settings.language)?:""

                            tvDayViewer.text = ViewHelpers.getDayFromUnix(sunrise?.toLong(),settings.language)
                            tvDate.text = ViewHelpers.getDateFromUnix(sunrise?.toLong(),settings.language)

                            tvSunrise.text = ViewHelpers.getTimeFromUnix(sunrise?.toLong(),settings.language)
                            tvSunset.text = ViewHelpers.getTimeFromUnix(sunset?.toLong(),settings.language)

                            tvTempUnit.text = ViewHelpers.getStringTempUnit(settings.tempUnit)
                            tvTempUnit2.text = ViewHelpers.getStringTempUnit(settings.tempUnit)

                            tvCountryName.text = weatherLang.country
                            tvAddressLine.text = weatherLang.addressLine


                            tvDemo1.text = weatherLang.toString() ?: "no data"
                            tvDemo2.text = ViewHelpers
                                .convertFromMeterBerSecond(
                                    windSpeed ?: 0.0,
                                    settings.windSpeedUnit
                                )
                                .toString() ?: "no data"


                            tvDemo3.text =temp
                                ?.convertFromKelvin(settings.tempUnit)
                                ?.numberLocalizer(settings.language)?:""

                            tvDemo4.text = uvi.toString() ?: "no data"
                        }
                    }


                }
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