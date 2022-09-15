package com.demo.core.utils

object ApiViewHelper {

    private const val ICON_ROUTE = "https://openweathermap.org/img/wn/"
    private const val IMAGE_EXTENSION = "@2x.png"

    fun iconImagePathMaker(icon:String):String{
        return ICON_ROUTE+ icon + IMAGE_EXTENSION
    }


}