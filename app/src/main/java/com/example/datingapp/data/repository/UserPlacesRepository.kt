package com.example.datingapp.data.repository

import com.example.datingapp.data.models.UserPlace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class UserPlacesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("user_places")

    suspend fun likePlace(placeId: String, source: String = "places_of_day"): Result<UserPlace> {
        return try {
            val userId = auth.currentUser?.uid
            Log.d("LIKE_DEBUG", "likePlace - userId: $userId, placeId: $placeId")

            if (userId == null) {
                Log.e("LIKE_DEBUG", "User not authenticated")
                return Result.failure(Exception("Пользователь не авторизован"))
            }

            // Проверяем, существует ли уже такой лайк
            val existing = checkIfUserLikedPlace(userId, placeId)
            Log.d("LIKE_DEBUG", "Existing like: $existing")

            if (existing != null) {
                Log.d("LIKE_DEBUG", "Place already liked, returning existing")
                return Result.success(existing)
            }

            val userPlace = UserPlace(
                userId = userId,
                placeId = placeId,
                status = "liked",
                addedTime = com.google.firebase.Timestamp(Date()),
                source = source
            )

            Log.d("LIKE_DEBUG", "Creating new UserPlace: $userPlace")

            val docRef = collection.document()
            val placeWithId = userPlace.copy(id = docRef.id)

            Log.d("LIKE_DEBUG", "Saving to Firestore: ${docRef.path}")

            docRef.set(placeWithId).await()

            Log.d("LIKE_DEBUG", "Successfully saved place with id: ${placeWithId.id}")

            Result.success(placeWithId)
        } catch (e: Exception) {
            Log.e("LIKE_DEBUG", "Error in likePlace", e)
            Result.failure(e)
        }
    }

    suspend fun unlikePlace(placeId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Пользователь не авторизован")

            val existing = checkIfUserLikedPlace(userId, placeId)
            if (existing != null) {
                collection.document(existing.id).delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun checkIfUserLikedPlace(userId: String, placeId: String): UserPlace? {
        return try {
            Log.d("LIKE_DEBUG", "Checking if user $userId liked place $placeId")

            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("placeId", placeId)
                .whereEqualTo("status", "liked")
                .limit(1)
                .get()
                .await()

            Log.d("LIKE_DEBUG", "Query result size: ${snapshot.size()}")

            val result = snapshot.documents.firstOrNull()?.toObject<UserPlace>()
            Log.d("LIKE_DEBUG", "Found existing like: $result")

            result
        } catch (e: Exception) {
            Log.e("LIKE_DEBUG", "Error checking if user liked place", e)
            null
        }
    }

    suspend fun getUserLikedPlaces(): Result<List<UserPlace>> {
        return try {
            val userId = auth.currentUser?.uid
            Log.d("GET_PLACES", "getUserLikedPlaces - userId: $userId")

            if (userId == null) {
                Log.e("GET_PLACES", "User not authenticated")
                return Result.failure(Exception("Пользователь не авторизован"))
            }

            Log.d("GET_PLACES", "Querying collection: user_places")

            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "liked")
                .orderBy("addedTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("GET_PLACES", "Query result size: ${snapshot.size()}")

            val places = snapshot.documents.mapNotNull { doc ->
                try {
                    val place = doc.toObject<UserPlace>()
                    Log.d("GET_PLACES", "Found place: ${place?.placeId}")
                    place
                } catch (e: Exception) {
                    Log.e("GET_PLACES", "Error converting document", e)
                    null
                }
            }

            Log.d("GET_PLACES", "Successfully loaded ${places.size} places")
            Result.success(places)
        } catch (e: Exception) {
            Log.e("GET_PLACES", "Error in getUserLikedPlaces", e)
            Result.failure(e)
        }
    }

    suspend fun getPlaceLikesCount(placeId: String): Int {
        return try {
            val snapshot = collection
                .whereEqualTo("placeId", placeId)
                .whereEqualTo("status", "liked")
                .get()
                .await()

            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}