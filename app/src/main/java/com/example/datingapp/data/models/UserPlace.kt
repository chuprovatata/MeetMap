package com.example.datingapp.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class UserPlace(
    val id: String = "",
    @PropertyName("user_id")
    val userId: String = "",
    @PropertyName("place_id")
    val placeId: String = "",
    val status: String = "", // "visited", "planned", "favorite"
    @PropertyName("added_time")
    val addedTime: Timestamp? = null,
    @PropertyName("visited_time")
    val visitedTime: Timestamp? = null,
    val rating: Int? = null,
    val notes: String? = null
)