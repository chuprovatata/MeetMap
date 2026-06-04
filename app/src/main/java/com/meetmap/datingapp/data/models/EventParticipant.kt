package com.meetmap.datingapp.data.models

import com.google.firebase.Timestamp

data class EventParticipant(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val createdAt: Timestamp? = null
)