package com.newcore.wezy.localDb

import androidx.room.TypeConverter
import com.newcore.wezy.models.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source):String =
        source.name?:""

    @TypeConverter
    fun toSource(name:String): Source =
        Source(name,name)

}