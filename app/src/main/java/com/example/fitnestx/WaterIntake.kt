package com.example.fitnestx

data class WaterIntake(
    val waterId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val date: String = "" // yyyy-MM-dd format
)
