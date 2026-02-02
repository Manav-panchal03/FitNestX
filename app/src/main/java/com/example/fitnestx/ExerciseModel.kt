package com.example.fitnestx

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseModel(
    val name: String? = null,
    val primaryMuscles: List<String>? = null, // Aa list che
    val images: List<String>? = null,         // Aa pan list che
    val bodyPart: String? = null,             // Jo JSON ma na hoy to blank avse
    var isSelected: Boolean = false
) : Parcelable