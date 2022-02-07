package com.newcore.wezy.models

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
){
    companion object{
        fun emptyResponse():NewsResponse{
            return NewsResponse(mutableListOf(),"200",0)
        }
    }
}