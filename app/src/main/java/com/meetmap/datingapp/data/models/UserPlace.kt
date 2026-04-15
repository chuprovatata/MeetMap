package com.meetmap.datingapp.data.models

import com.google.firebase.Timestamp

data class UserPlace(
    val id: String = "",
    val userId: String = "",  // Убираем аннотации
    val placeId: String = "",
    val status: String = "",
    val addedTime: Timestamp? = null,
    val visitedTime: Timestamp? = null,
    val rating: Int? = null,
    val notes: String? = null,
    val source: String = ""
)