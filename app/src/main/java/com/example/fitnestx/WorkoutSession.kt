package com.example.fitnestx

data class WorkoutSession(
    var id: String = "",
    var routineName: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var sets: MutableList<WorkoutSetSession> = mutableListOf()
)

