package com.newcore.wezy.utils

object Constants {
    // api
    private  val apis = arrayListOf(
        "2d14384aa56941229beac19e083497f7",
        "2b3d0e87db064dd493868c7eb5d89a96"
    )

    val API_KEY = apis[1]
    const val BASE_URL= "https://newsapi.org/v2/"
    const val TOTAL_NUMBER_OF_ITEMS_PER_REQUEST = 20
    const val MAX_RESULT_FOR_FREE_API = 100

    //coroutines
    const val SEARCH_TIME_DELAY = 500L

    // tags
    const val BREAKING_ERROR_TAG = "BREAKING_ERROR_TAG"
    const val SEARCH_ERROR_TAG = "SEARCH_ERROR_TAG"
    const val No_INTERNET_CONNECTION = "No_INTERNET_CONNECTION"

    // navigation args keys
    const val ARTICLE = "article"

    // sharedPreferences Tags
    const val ALL_DATA_ROUTE = "ALL_DATA_ROUTE"
}