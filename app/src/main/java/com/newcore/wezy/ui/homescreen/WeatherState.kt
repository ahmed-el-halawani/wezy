package com.newcore.wezy.ui.homescreen

sealed class WeatherState<D>{

    data class Success<D>(val data:D) : WeatherState<D>()

    data class NoWeatherWasFound<D>(val message: String?=null) : WeatherState<D>()
    data class NOLocationInSettings<D>(val message: String?=null) : WeatherState<D>()
    data class ServerError<D>(val message: String?=null) : WeatherState<D>()
    data class NoInternetConnection<D>(val message: String?=null) : WeatherState<D>()

    class Loading<D> : WeatherState<D>()

}