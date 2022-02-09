package com.newcore.wezy.models.geocoder

import com.google.gson.annotations.SerializedName


data class GeocoderResponse (
  @SerializedName("local_names" ) var localNames : LocalNames? = LocalNames(),
)