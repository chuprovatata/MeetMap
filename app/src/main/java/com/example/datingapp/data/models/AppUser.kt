package com.example.datingapp.data.models

data class AppUser(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val bio: String = "",
    val gender: String = "",
    val age: Int = 0,
    val birthYear: Int? = null,
    val university: String = "",
    val targets: List<Int> = emptyList(),
    val categories: List<Int> = emptyList(),
    val profileComplete: Boolean = false
)