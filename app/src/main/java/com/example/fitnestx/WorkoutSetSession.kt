package com.example.fitnestx

data class WorkoutSetSession(
    var exercise: String = "",
    var plannedReps: Int = 0,
    var plannedWeight: Float = 0f,
    var actualReps: Int = 0,
    var actualWeight: Float = 0f,
    var completed: Boolean = false
)


