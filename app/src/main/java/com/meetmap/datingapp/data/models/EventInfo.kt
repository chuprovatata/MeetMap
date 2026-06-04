package com.meetmap.datingapp.data.models

import com.google.firebase.Timestamp

data class EventInfo(
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val photoUrl: String = "",

    val dates: List<EventDateSlot> = emptyList(),
    val duration: String = "",

    val address: String = "",
    val organization: String = "",
    val isOrganizer: Boolean = false,
    val sourceUrl: String = "",

    val ageLimit: Int? = null,
    val participantNotes: String = "",

    val status: String = EventStatus.CREATED.value,
    val moderatorNotes: String = "",
    val moderatorFeedback: String = "",

    val createdByUserId: String = "",
    val createdByUserName: String = "",

    val notifyAboutModeration: Boolean = true,

    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val submittedAt: Timestamp? = null,
    val moderatedAt: Timestamp? = null,
    val archivedAt: Timestamp? = null
)

data class EventDateSlot(
    val dateFrom: String = "",
    val dateTo: String = "",
    val startTime: String = "",
    val startAt: Timestamp? = null,
    val endAt: Timestamp? = null
)

enum class EventStatus(val value: String) {
    DRAFT("draft"),
    CREATED("created"),
    APPROVED("approved"),
    ARCHIVE("archive")
}