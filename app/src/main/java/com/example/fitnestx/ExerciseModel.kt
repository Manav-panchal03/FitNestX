package com.example.fitnestx

data class ExerciseModel(
    val id: String,
    val name: String,
    val bodyPart: String,
    val target: String,
    val gifUrl: String
)