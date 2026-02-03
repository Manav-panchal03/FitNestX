package com.example.fitnestx

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize

data class ExerciseSet(
    val setNumber: Int = 1,
    var weight: String = "",
    var reps: String = ""
) : Parcelable
