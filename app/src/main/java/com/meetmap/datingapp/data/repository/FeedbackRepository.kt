package com.meetmap.datingapp.data.repository

import com.meetmap.datingapp.data.models.AppFeedback
import com.meetmap.datingapp.data.models.FeedbackType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.google.firebase.Timestamp

@Singleton
class FeedbackRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("app_feedback")
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun hasUserSubmittedToday(userId: String, date: String): Boolean {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("feedbackType", FeedbackType.PLACES_OF_DAY.name)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private fun getStartOfDay(date: String): Timestamp {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val parsedDate = formatter.parse(date)
        return Timestamp(parsedDate)
    }

    private fun getEndOfDay(date: String): Timestamp {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val parsedDate = formatter.parse(date)
        val nextDay = Date(parsedDate.time + 24 * 60 * 60 * 1000)
        return Timestamp(nextDay)
    }

    private suspend fun getUserData(): Triple<String, String, String> {
        val userId = auth.currentUser?.uid ?: throw Exception("Пользователь не авторизован")

        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        val userName = userDoc.getString("name") ?: ""
        val userUsername = userDoc.getString("username") ?: ""

        return Triple(userId, userName, userUsername)
    }

    private suspend fun saveFeedbackInternal(feedback: AppFeedback): Result<AppFeedback> {
        return try {
            val docRef = collection.document()
            val feedbackWithId = feedback.copy(id = docRef.id)
            docRef.set(feedbackWithId).await()
            Log.d("FeedbackRepo", "Feedback saved: ${feedback.feedbackType}")
            Result.success(feedbackWithId)
        } catch (e: Exception) {
            Log.e("FeedbackRepo", "Error saving feedback", e)
            Result.failure(e)
        }
    }

    suspend fun savePlacesOfDayFeedback(
        rating: Int,
        selectedOptionIndex: Int,
        wantMoreCategories: List<String>,
        source: String
    ): Result<AppFeedback> {
        val (userId, userName, userUsername) = getUserData()

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        val feedback = AppFeedback(
            userId = userId,
            userName = userName,
            userUsername = userUsername,
            feedbackType = FeedbackType.PLACES_OF_DAY.name,
            createdAt = Timestamp(Date()),
            date = todayDate,
            rating = rating,
            selectedOptionIndex = selectedOptionIndex,
            wantMoreCategories = wantMoreCategories,
            source = source
        )

        return saveFeedbackInternal(feedback)
    }

    suspend fun savePlaceAddedFeedback(
        placeId: String,
        placeName: String,
        heardAboutOption: Int,
        metadata: Map<String, Any> = emptyMap()
    ): Result<AppFeedback> {
        val (userId, userName, userUsername) = getUserData()

        val feedback = AppFeedback(
            userId = userId,
            userName = userName,
            userUsername = userUsername,
            feedbackType = FeedbackType.PLACE_ADDED_FEEDBACK.name,
            createdAt = Timestamp(Date()),
            placeId = placeId,
            placeName = placeName,
            heardAboutOption = heardAboutOption,
            metadata = metadata
        )

        return saveFeedbackInternal(feedback)
    }
    suspend fun savePlaceDeletedFeedback(
        placeId: String,
        placeName: String,
        deletedReasonOption: Int,
        metadata: Map<String, Any> = emptyMap()
    ): Result<AppFeedback> {
        val (userId, userName, userUsername) = getUserData()

        val feedback = AppFeedback(
            userId = userId,
            userName = userName,
            userUsername = userUsername,
            feedbackType = FeedbackType.PLACE_DELETED_FEEDBACK.name,
            createdAt = Timestamp(Date()),
            placeId = placeId,
            placeName = placeName,
            deletedReasonOption = deletedReasonOption,
            metadata = metadata
        )

        return saveFeedbackInternal(feedback)
    }

    suspend fun getAllFeedback(): Result<List<AppFeedback>> {
        return try {
            val snapshot = collection
                .orderBy("created_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val feedbacks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AppFeedback::class.java)?.copy(id = doc.id)
            }

            Result.success(feedbacks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}