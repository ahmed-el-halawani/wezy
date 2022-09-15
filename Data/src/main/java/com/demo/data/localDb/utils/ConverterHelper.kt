package com.newcore.wezy.localDb.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object ConverterHelper {

     fun <T> toJson(objectSource: T):String{
        val json = Gson()
        return json.toJson(objectSource)
    }

     fun <T> fromJson(jsonSource:String): T {
        val json = Gson()
         val type: Type = object : TypeToken<T>(){}.type
         return json.fromJson(jsonSource, type)
    }


}