package com.newcore.wezy

import android.app.Application

class WeatherApplication : Application() {

    companion object{
        var weatherApplication:WeatherApplication? = null
    }

    init {
        weatherApplication = this
    }

}