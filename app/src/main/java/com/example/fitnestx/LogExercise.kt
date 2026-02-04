package com.example.fitnestx

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class LogExercise(
    val name: String = "",
    val image: String = "",
    val target: String = "",
    val sets: MutableList<ExerciseSet> = mutableListOf() // Default 1st set
) : Parcelable
