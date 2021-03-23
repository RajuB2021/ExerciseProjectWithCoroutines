package com.example.exercise.data.util

class JokeResponse(result:Int,isNetworkError:Boolean,errorMessage:String?,joke:String?,statusCode:Int) {
    val result :Int  = result
    val isNetworkError : Boolean  = isNetworkError
    val errorMessage :String?  = errorMessage
    val joke : String? = joke
    val statusCode :Int = statusCode
}