package com.newcore.wezy.models.weatherentities

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Alerts(

    @SerializedName("sender_name") var senderName: String? = null,
    @SerializedName("event") var event: String? = null,
    @SerializedName("start") var start: Double? = null,
    @SerializedName("end") var end: Double? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("tags") var tags: ArrayList<String> = arrayListOf()

):Serializable