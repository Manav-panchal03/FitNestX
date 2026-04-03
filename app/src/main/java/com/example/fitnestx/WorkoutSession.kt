package com.example.fitnestx

data class WorkoutSession(
    var id: String = "",
    var routineName: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var exercises: List<ExerciseSession> = listOf() // હવે અહીં Exercises આવશે
)

