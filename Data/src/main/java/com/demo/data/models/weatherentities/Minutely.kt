package com.newcore.wezy.models.weatherentities

import com.google.gson.annotations.SerializedName


data class Minutely(

    @SerializedName("dt") var dt: Double? = null,
    @SerializedName("precipitation") var precipitation: Double? = null

)