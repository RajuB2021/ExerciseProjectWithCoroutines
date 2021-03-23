package com.example.exercise.data.network

import com.example.exercise.data.api.RandomJokeWebService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitRandomJoke {
    companion object {
        private const val BASE_URL = "https://api.icndb.com"
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
    }
    fun <Api> getApi(
            api: Class<Api>
    ): Api {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(api)
    }
}

 

