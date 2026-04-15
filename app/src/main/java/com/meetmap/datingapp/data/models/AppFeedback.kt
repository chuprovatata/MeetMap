package com.meetmap.datingapp.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

enum class FeedbackType {
    PLACES_OF_DAY,
    PLACE_ADDED_FEEDBACK,
    PLACE_DELETED_FEEDBACK
}

data class AppFeedback(
    val id: String = "",
    val userId: String = "",
    @PropertyName("user_name")
    val userName: String = "",
    @PropertyName("user_username")
    val userUsername: String = "",
    @PropertyName("feedback_type")
    val feedbackType: String = "",
    @PropertyName("created_at")
    val createdAt: Timestamp? = null,
    @PropertyName("date")
    val date: String = "",

    // Places of Day feedback
    val rating: Int = 0,
    @PropertyName("selected_option_index")
    val selectedOptionIndex: Int = -1,
    @PropertyName("want_more_categories")
    val wantMoreCategories: List<String> = emptyList(),
    @PropertyName("source")
    val source: String = "",

    // Place data (используется для всех типов, связанных с местами)
    @PropertyName("place_id")
    val placeId: String = "",
    @PropertyName("place_name")
    val placeName: String = "",

    // Place Added/Deleted Feedback
    @PropertyName("heard_about_option")
    val heardAboutOption: Int = -1,
    @PropertyName("deleted_reason_option")
    val deletedReasonOption: Int = -1,

    val metadata: Map<String, Any> = emptyMap()
)