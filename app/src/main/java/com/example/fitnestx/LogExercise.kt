package com.example.fitnestx

data class LogExercise(
    val name: String = "",
    val image: String = "",
    val target: String = "",
    val sets: MutableList<ExerciseSet> = mutableListOf(ExerciseSet(1)) // Default 1st set
)
