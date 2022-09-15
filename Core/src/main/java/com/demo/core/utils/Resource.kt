package com.demo.core.utils

sealed class Resource<T>(
    var data: T? = null,
    var message:String? = null
) {

    class Success<T>(data:T) : Resource<T>(data)
    class Error<T>(message: String?=null,data: T?=null) : Resource<T>(data,message)
    class Loading<T> : Resource<T>()

}




sealed class Either<S,E> {

    data class Success<S,E>(val data:S) : Either<S, E>()
    data class Error<S,E>(val errorCode:E,val message:String?=null) : Either<S, E>()

}