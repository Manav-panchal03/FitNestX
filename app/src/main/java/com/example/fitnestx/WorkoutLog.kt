package com.example.fitnestx.model

data class WorkoutLog(
    var exercise: String = "",
    var weight: Float = 0f,
    var reps: Int = 0,
    var timestamp: Long = System.currentTimeMillis()
)
