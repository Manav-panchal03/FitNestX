package com.example.fitnestx

data class SleepLog(
    val sleepId: String = "",
    val userId: String = "",
    val hours: Double = 0.0,
    val date: String = "" // yyyy-MM-dd format
)
