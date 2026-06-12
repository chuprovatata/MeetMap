package com.meetmap.datingapp.data.models

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val place: String = "",
    val university: String = "",     // "НИУ ВШЭ" или "" — для всех желающих
    val isForAll: Boolean = false ,
    val organizerId: String = "",
    val organizerUsername: String = "",
    val organizerAvatarUrl: String? = null,
    val imageUrl: String? = null,
    val maxParticipants: Int = 50,
    val currentParticipants: Int = 0,
    val participantsList: List<String> = emptyList(),  // список ID участников
    val createdAt: Timestamp = Timestamp.now()
)