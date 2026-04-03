package com.example.fitnestx

data class WorkoutSetSession(
    val exercise: String,
    val setNumber: Int, // 1, 2, 3...
    val plannedReps: Int,
    val plannedWeight: Float,
    var actualReps: Int = 0,
    var actualWeight: Float = 0f,
    var completed: Boolean = false
)

