package com.example.fitnestx.model

data class WeightLog(
    var id: String = "",
    var weight: Float = 0f,
    var timestamp: Long = System.currentTimeMillis()
)
