package com.newcore.wezy.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newcore.wezy.utils.Constants.MY_ALERTS_TABLE
import java.io.Serializable


@Entity(
    tableName = MY_ALERTS_TABLE
)
data class MyAlert(
    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    var englishCountryName:String?=null,
    var arabicCountryName:String?=null,
    var fromDT:Long,
    var toDT:Long,
    var lat: Double? = null,
    var lon: Double? = null,
    var isAlarm:Boolean = true
):Serializable