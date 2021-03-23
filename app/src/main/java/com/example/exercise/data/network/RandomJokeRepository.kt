package com.example.exercise.data.network

import com.example.exercise.data.api.RandomJokeWebService
import com.example.exercise.data.model.JokeCache
import com.example.exercise.data.model.RandomJokeApi
import com.example.exercise.data.util.JokeResponse
import com.example.exercise.ui.util.CommonUtil
import retrofit2.HttpException
import retrofit2.Response


class RandomJokeRepository(remoteDataSource: RandomJokeWebService?, localJokeCache: JokeCache?) {

    private val randomJokeWebService = remoteDataSource
    private val jokeCache = localJokeCache

    private var previousPageIndex: Int = 0 // variable used in logic to load joke from cache or network

    suspend fun fetchRandomJoke(firstName: String, lastName: String, pageIndex: Int): JokeResponse? {
        var cache: HashMap<Int, String>? = null
        if (jokeCache != null) {
            cache = jokeCache.getCache();
        }
        
        if (previousPageIndex > pageIndex) {  // if true loads from cache
            val joke: String? = cache?.get(pageIndex!!)
            previousPageIndex = pageIndex
            return JokeResponse(CommonUtil.RESULT_SUCCESS, false, null, joke, 200)
        } else {
            try {
                val response: Response<RandomJokeApi> = randomJokeWebService!!.getRandomJoke(firstName, lastName)
                val joke = response.body()?.valueObj?.joke
                if (joke != null) {
                    cache?.put(pageIndex, joke)
                }
                previousPageIndex = pageIndex // update only when network call is successfull 
                return JokeResponse(CommonUtil.RESULT_SUCCESS, false, null, joke, response.code())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        return JokeResponse(CommonUtil.RESULT_FAILURE, false, throwable.response()?.errorBody()?.string().toString(), null, throwable.code())
                    }
                    else -> {
                        return JokeResponse(CommonUtil.RESULT_FAILURE, true, "network error ", null, 0)
                    }
                }
            }
        }
    }
    
}