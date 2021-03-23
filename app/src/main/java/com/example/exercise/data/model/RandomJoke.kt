package com.example.exercise.data.model

import com.google.gson.annotations.SerializedName

class RandomJokeApi {
    @SerializedName("value")
    val valueObj: RandomJokeValue? = null
}

class RandomJokeValue(
        val joke: String
)