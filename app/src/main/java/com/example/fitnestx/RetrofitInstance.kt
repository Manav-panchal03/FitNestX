package com.example.fitnestx

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Hu tamane ek stable static link api dau chu jya badhu data mix che
    private const val BASE_URL = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/"

    val api: ExerciseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApiService::class.java)
    }
}