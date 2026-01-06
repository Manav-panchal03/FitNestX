package com.example.fitnestx

data class WorkoutRoutine(
    val routineName: String = "",
    val exercises: List<LogExercise> = emptyList()
)
