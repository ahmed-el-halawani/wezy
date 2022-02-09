package com.newcore.wezy.models.geocoder

import com.google.gson.annotations.SerializedName


data class LocalNames (

  @SerializedName("ar") var ar: String? = null,
  @SerializedName("en") var en: String? = null,

  )