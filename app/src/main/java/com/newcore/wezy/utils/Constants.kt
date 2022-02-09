package com.newcore.wezy.utils

import android.content.res.Resources
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.WeatherApplication.Companion.weatherApplication

object Constants {

    //room constants
    const val WEATHER_TABLE = "weather"
    const val DATABASE_NAME = "weatherDB"
    const val HOME_WEATHER_ID = "HOME_WEATHER_ID"

    // api keys
    const val API_KEY = ""

    val WEATHER_API_KEY by lazy {
        weatherApplication?.getString(R.string.weather_api_key)!!
    }

    //const val BASE_URL= "https://newsapi.org/v2/"
    const val WEATHER_BASE_URL= "https://api.openweathermap.org/"
    const val TOTAL_NUMBER_OF_ITEMS_PER_REQUEST = 20
    const val MAX_RESULT_FOR_FREE_API = 100

    //coroutines
    const val SEARCH_TIME_DELAY = 500L

    // Error tags
    const val BREAKING_ERROR_TAG = "BREAKING_ERROR_TAG"
    const val SEARCH_ERROR_TAG = "SEARCH_ERROR_TAG"
    const val No_INTERNET_CONNECTION = "No_INTERNET_CONNECTION"

    // navigation args keys
    const val ARTICLE = "article"

    // sharedPreferences Tags
    const val ALL_DATA_ROUTE = "ALL_DATA_ROUTE"

    // internet State Code
    const val INTERNET_NOT_WORKING = "INTERNET_NOT_WORKING"
    const val INTERNET_WORKING = "INTERNET_WORKING"

    //Recall Service Tags
    const val GET_ADDRESS_AFTER_INTERNET_BACK = "GET_ADDRESS_AFTER_INTERNET_BACK"
    const val GET_OR_REFRESH_HOME_WITH_DATA = "GET_OR_REFRESH_HOME_WITH_DATA"
}