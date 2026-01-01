package com.example.fitnestx

import retrofit2.Call
import retrofit2.http.GET

interface ExerciseApiService {
    // Apde ahiya ek evo URL vaparishu jema badhi 1000+ exercises mix hase
    @GET("dist/exercises.json")
    fun getAllExercises(): Call<List<ExerciseModel>>
}