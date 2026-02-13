package com.example.fitnestx

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoutineModel(
    var id: String = "",              // Firebase ni Push ID store karva mate
    val routineName: String = "",      // Routine nu name (e.g., Chest Day)
    val exercises: List<LogExercise> = emptyList(), // Badhi selected exercises
    val createdAt: Long = 0            // Kyare banavyu hatu e time
) : Parcelable
