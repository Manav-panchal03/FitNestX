package com.example.fitnestx

data class AppUsers(
    // Basic Authentication & Identity
    val uid: String = "",
    val name: String = "", //
    val email: String = "", //

    // Physical Metrics (Profile Setup mathi malse)
    val age: Int? = null,
    val gender: String? = null,
    val height: Double? = null, // in cm
    val weight: Double? = null, // in kg

    // Personal Details
    val bio: String? = "Fitness Enthusiast",
    val avatarUrl: String? = null,

    // Fitness Goals
    val goalType: String? = null, // e.g., "Weight Loss", "Muscle Gain"
    val fitnessLevel: String? = "Beginner", // e.g., "Beginner", "Intermediate"

    // Membership & Subscription (Nava suggest karyela tables mujab)
    val membershipPlanId: String? = "free_plan",
    val membershipStatus: String? = "Active", // Active, Expired, Pending
    val membershipExpiryDate: String? = null,

    // Internal App Flow Control
    @field:JvmField
    val isProfileComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
