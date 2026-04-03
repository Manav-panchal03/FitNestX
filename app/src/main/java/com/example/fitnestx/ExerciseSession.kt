package com.example.fitnestx

data class ExerciseSession(
    var exerciseName: String = "",
    var sets: MutableList<WorkoutSetSession> = mutableListOf() // અહીં MutableList રાખવું
)
