package com.example.fitnestx

data class AppUsers(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int? = null,
    val gender: String? = null,
    val height: Double? = null,  // in cm
    val weight: Double? = null   // in kg
)
