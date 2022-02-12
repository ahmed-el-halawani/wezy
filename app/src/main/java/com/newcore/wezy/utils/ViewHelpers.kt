package com.newcore.wezy.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.newcore.wezy.models.weatherentities.Current
import com.newcore.wezy.models.weatherentities.Weather
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.models.weatherentities.WeatherResponse
import com.newcore.wezy.shareprefrances.Language
import com.newcore.wezy.shareprefrances.Settings
import com.newcore.wezy.shareprefrances.TempUnit
import com.newcore.wezy.shareprefrances.WindSpeedUnit
import com.newcore.wezy.ui.homescreen.WeatherState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ViewHelpers {

    fun List<Weather>.showRainOrSnowOrNot(rain:View, snow: View,lite:()->Unit){
        rain.visibility = View.GONE
        snow.visibility = View.GONE
        forEach {
            when(it.main){
                "Drizzle"   ->    rain.visibility = View.VISIBLE
                "Rain"      ->    rain.visibility = View.VISIBLE
                "Snow"      ->    snow.visibility = View.VISIBLE
                "thunderstorm"  ->   lite()
            }
        }
    }

    fun localeFromLanguage(language: Language):Locale{
        return returnByLanguage(language,Locale("ar"),Locale.ENGLISH)
    }

    fun Number.numberLocalizer(language: Language):String{
        return  NumberFormat.getInstance(localeFromLanguage(language)).format(this);
    }


    fun getTimeFromUnix(long: Long?,language: Language):String{
        val timeStr =
            SimpleDateFormat("hh:mm aa", localeFromLanguage(language))

        return long?.let { timeStr.format(Date(it*1000)) }?:"00:00"
    }

    fun getHourFromUnix(long: Long?,language: Language):String{
        val timeStr =
            SimpleDateFormat("hhaa", localeFromLanguage(language))

        return long?.let { timeStr.format(Date(it*1000)) }?:"00PM"
    }

    fun getDateFromUnix(long: Long?,language: Language):String{
        val timeStr =
            SimpleDateFormat("EE, d MMM", localeFromLanguage(language))

        return long?.let { timeStr.format(Date(it*1000)) }?:"00, 00 00"
    }


    fun getDayFromUnix(long: Long?,language: Language):String{
        val timeStr =
            SimpleDateFormat("EEEE", localeFromLanguage(language))

        val compareTime =
            SimpleDateFormat("EEEE, d", localeFromLanguage(language))

        val day = long?.let { timeStr.format(Date(it*1000)) }?:""
        val comparableDay = long?.let { compareTime.format(Date(it*1000)) }?:""
        val today = compareTime.format(Date())

        return if(comparableDay==today)
            returnByLanguage(language,"اليوم","Today")
        else day
    }

    fun <T> returnByLanguage(language: Language, arabic: T, english: T): T {
        return when (language) {
            Language.Arabic -> arabic
            Language.English -> english
            Language.Default -> when (languageEnumFromLocale()) {
                Language.Arabic -> arabic
                Language.English -> english
                else -> english
            }
        }
    }



    fun Current.convertFromKelvin(settings: Settings): Int {
        return when (settings.tempUnit) {
            TempUnit.Kelvin -> (this.temp?:0.0).toInt()
            TempUnit.Celsius -> ((this.temp?:0.0) - 273.15).toInt()
            TempUnit.Fahrenheit -> (((this.temp?:0.0) - 273.15) * 1.8 + 32).toInt()
        }
    }

    fun convertFromKelvin(temp:Double?,settings: Settings): Int {
        return when (settings.tempUnit) {
            TempUnit.Kelvin -> (temp?:0.0).toInt()?:0
            TempUnit.Celsius -> ((temp?:0.0) - 273.15).toInt()
            TempUnit.Fahrenheit -> (((temp?:0.0) - 273.15) * 1.8 + 32).toInt()
        }
    }


    fun getStringTempUnit(temp: TempUnit): String {
        return when (temp) {
            TempUnit.Kelvin -> "°K"
            TempUnit.Celsius -> "°C"
            TempUnit.Fahrenheit -> "°F"
        }
    }

    fun Current.windSpeedFromMeterBerSecond(settings:Settings): Double {
        return when (settings.windSpeedUnit) {
            WindSpeedUnit.MeterBerSecond -> windSpeed?:0.0
            WindSpeedUnit.MileBerHour -> (windSpeed?:0.0) * 2.236936
        }
    }

    fun getStringSpeedUnit(settings:Settings): String {
        return when (settings.windSpeedUnit) {
            WindSpeedUnit.MeterBerSecond -> returnByLanguage(settings.language,"متر/ثانية","meter/second")
            WindSpeedUnit.MileBerHour -> returnByLanguage(settings.language,"ميل/ساعة","mile/hour")
        }
    }

    class SwipeToRemove(
        private val swipe: (position: Int) -> Unit,
    ) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            swipe(position)


        }

    }

    fun languageEnumFromLocale(): Language {
        return when (Locale.getDefault().language) {
            "en" -> Language.English
            "ar" -> Language.Arabic
            else -> Language.English
        }
    }

    private fun updateResources(context: Context, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    fun setAppLocale(localeCode: String? = null, resources: Resources, reBuildActivity: Activity) {
        val dm = resources.displayMetrics
        val config = resources.configuration
        config.setLocale(
            Locale(
                localeCode ?: Locale.getDefault().language
            )
        )

        resources.updateConfiguration(config, dm)

        ActivityCompat.recreate(reBuildActivity)
    }


    fun getWeatherFromWeatherLang(
        settings: Settings,
        weatherState: WeatherState.Success<WeatherLang>
    ): WeatherResponse? {
        return ViewHelpers.returnByLanguage(
            settings.language,
            weatherState.data.arabicResponse,
            weatherState.data.englishResponse
        )
    }
    fun getWeatherFromWeatherLang(
        settings: Settings,
        weatherState:WeatherLang
    ): WeatherResponse? {
        return ViewHelpers.returnByLanguage(
            settings.language,
            weatherState.arabicResponse,
            weatherState.englishResponse
        )
    }



}