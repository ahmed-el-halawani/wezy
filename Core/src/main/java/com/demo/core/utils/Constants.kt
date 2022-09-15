package com.demo.core.utils

import com.demo.core.BuildConfig
import com.demo.core.utils.Constants.WEATHER_API_KEY

object Constants {

    //room constants
    const val WEATHER_TABLE = "weather"
    const val MY_ALERTS_TABLE = "MyAlerts"

    const val DATABASE_NAME = "weatherDB"
    const val HOME_WEATHER_ID = "HOME_WEATHER_ID"

    // api keys
    val WEATHER_API_KEY = BuildConfig.WEATHER_API_KEY

    //const val BASE_URL= "https://newsapi.org/v2/"
    const val WEATHER_BASE_URL= "https://api.openweathermap.org/"

    //coroutines
    const val SEARCH_TIME_DELAY = 500L

    // sharedPreferences Tags
    const val ALL_DATA_ROUTE = "ALL_DATA_ROUTE"

    // internet State Code
    const val INTERNET_NOT_WORKING = "INTERNET_NOT_WORKING"
    const val INTERNET_WORKING = "INTERNET_WORKING"

    //Recall Service Tags
    const val GET_ADDRESS_AFTER_INTERNET_BACK = "GET_ADDRESS_AFTER_INTERNET_BACK"
    const val GET_OR_REFRESH_HOME_WITH_DATA = "GET_OR_REFRESH_HOME_WITH_DATA"

    //notification constants
    const val CHANNEL_ID = "CHANNEL_ID"
    const val EXTRA_NOTIFICATION_ID_CUSTOM = "EXTRA_NOTIFICATION_ID"
    const val MY_ALERT_LAT = "MY_ALERT_LAT"
    const val MY_ALERT_LNG = "MY_ALERT_LNG"
    const val MY_ALERT_TO = "MY_ALERT_TO"
    const val MY_ALERT_ID = "MY_ALERT_ID"
    const val ALERT_TITLE = "ALERT_TITLE"
    const val ALERT_BODY = "ALERT_BODY"
    const val ALERT_COUNTRY = "ALERT_COUNTRY"
    const val IS_ALERT = "IS_ALERT"

}