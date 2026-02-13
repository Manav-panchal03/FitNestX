package com.example.fitnestx

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Habit(
    val id: String = "",
    val name: String = "",
    val goalValue: Int = 1,     // e.g., 8
    val unit: String = "Times", // e.g., "Glasses"
    val color: String = "#92A3FD",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable