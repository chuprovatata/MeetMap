package com.meetmap.datingapp.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.meetmap.datingapp.BuildConfig
import com.meetmap.datingapp.data.models.EventDateSlot
import com.meetmap.datingapp.data.models.EventInfo
import com.meetmap.datingapp.data.models.EventParticipant
import com.meetmap.datingapp.data.models.EventStatus
import com.meetmap.datingapp.utils.CloudImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val eventsCollection = firestore.collection("events")
    private val participantsCollection = firestore.collection("event_participants")

    suspend fun getApprovedEvents(): Result<List<EventInfo>> {
        return try {
            archiveExpiredEvents()

            val snapshot = eventsCollection
                .whereEqualTo("status", EventStatus.APPROVED.value)
                .get()
                .await()

            val events = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(EventInfo::class.java)?.copy(id = doc.id)
                }
                .sortedByDescending { it.createdAt?.seconds ?: 0L }

            Result.success(events)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки approved мероприятий", e)
            Result.failure(e)
        }
    }

    suspend fun getMyEvents(): Result<List<EventInfo>> {
        return try {
            archiveExpiredEvents()
            removeArchivedParticipantEventsForCurrentUser()

            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val myCreatedSnapshot = eventsCollection
                .whereEqualTo("createdByUserId", userId)
                .get()
                .await()

            val myCreatedEvents = myCreatedSnapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(EventInfo::class.java)?.copy(id = doc.id)
                }
                .filter { event ->
                    event.status == EventStatus.APPROVED.value ||
                            event.status == EventStatus.ARCHIVE.value
                }

            val participantSnapshot = participantsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val participantEventIds = participantSnapshot.documents
                .mapNotNull { it.getString("eventId") }
                .distinct()

            val participantEvents = getEventsByIds(participantEventIds)
                .filter { event ->
                    event.status == EventStatus.APPROVED.value &&
                            event.createdByUserId != userId
                }

            val result = (myCreatedEvents + participantEvents)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt?.seconds ?: 0L }

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки моих мероприятий", e)
            Result.failure(e)
        }
    }

    suspend fun getMyUnpublishedEvents(): Result<List<EventInfo>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val snapshot = eventsCollection
                .whereEqualTo("createdByUserId", userId)
                .get()
                .await()

            val events = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(EventInfo::class.java)?.copy(id = doc.id)
                }
                .filter { event ->
                    event.status == EventStatus.DRAFT.value ||
                            event.status == EventStatus.CREATED.value
                }
                .sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }

            Result.success(events)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки черновиков и мероприятий на модерации", e)
            Result.failure(e)
        }
    }

    suspend fun moveEventToDraft(eventId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val docRef = eventsCollection.document(eventId)
            val doc = docRef.get().await()
            val event = doc.toObject(EventInfo::class.java)?.copy(id = doc.id)
                ?: return Result.failure(Exception("Мероприятие не найдено"))

            if (event.createdByUserId != userId) {
                return Result.failure(Exception("Редактировать может только создатель мероприятия"))
            }

            docRef.update(
                mapOf(
                    "status" to EventStatus.DRAFT.value,
                    "updatedAt" to Timestamp.now(),
                    "submittedAt" to null,
                    "moderatedAt" to null,
                    "moderatorFeedback" to ""
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка перевода мероприятия в черновик", e)
            Result.failure(e)
        }
    }

    suspend fun getEventById(eventId: String): Result<EventInfo> {
        return try {
            if (eventId.isBlank()) {
                return Result.failure(Exception("Пустой id мероприятия"))
            }

            val doc = eventsCollection
                .document(eventId)
                .get()
                .await()

            val event = doc.toObject(EventInfo::class.java)?.copy(id = doc.id)

            if (event == null) {
                Result.failure(Exception("Мероприятие не найдено"))
            } else {
                Result.success(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки мероприятия $eventId", e)
            Result.failure(e)
        }
    }

    suspend fun saveDraftEvent(
        event: EventInfo,
        photoUri: Uri?,
        contentResolver: ContentResolver
    ): Result<EventInfo> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val docRef = if (event.id.isNotBlank()) {
                eventsCollection.document(event.id)
            } else {
                eventsCollection.document()
            }

            var photoUrl = event.photoUrl

            if (photoUri != null) {
                photoUrl = uploadEventPhoto(
                    eventId = docRef.id,
                    photoUri = photoUri,
                    contentResolver = contentResolver
                ).getOrThrow()
            }

            val eventToSave = event.copy(
                id = docRef.id,
                status = EventStatus.DRAFT.value,
                photoUrl = photoUrl,
                createdByUserId = user.uid,
                createdByUserName = user.displayName ?: "",
                createdAt = event.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            docRef.set(eventToSave).await()

            Result.success(eventToSave)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения черновика мероприятия", e)
            Result.failure(e)
        }
    }

    suspend fun submitEventForModeration(
        event: EventInfo,
        photoUri: Uri?,
        contentResolver: ContentResolver
    ): Result<EventInfo> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val docRef = if (event.id.isNotBlank()) {
                eventsCollection.document(event.id)
            } else {
                eventsCollection.document()
            }

            var photoUrl = event.photoUrl

            if (photoUri != null) {
                photoUrl = uploadEventPhoto(
                    eventId = docRef.id,
                    photoUri = photoUri,
                    contentResolver = contentResolver
                ).getOrThrow()
            }

            val eventToSave = event.copy(
                id = docRef.id,
                status = EventStatus.CREATED.value,
                photoUrl = photoUrl,
                createdByUserId = user.uid,
                createdByUserName = user.displayName ?: "",
                createdAt = event.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now(),
                submittedAt = Timestamp.now()
            )

            docRef.set(eventToSave).await()

            Result.success(eventToSave)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отправки мероприятия на модерацию", e)
            Result.failure(e)
        }
    }

    suspend fun uploadEventPhoto(
        eventId: String,
        photoUri: Uri,
        contentResolver: ContentResolver
    ): Result<String> {
        return try {
            val imageUrl = withContext(Dispatchers.IO) {
                var file: File? = null

                try {
                    val extension = getFileExtension(photoUri, contentResolver) ?: "jpg"

                    file = uriToFile(
                        uri = photoUri,
                        contentResolver = contentResolver,
                        extension = extension
                    ) ?: throw Exception("Не удалось обработать изображение")

                    val fileName = "events/$eventId.$extension"

                    CloudImageUtils.uploadFile(
                        file = file,
                        fileName = fileName,
                        accessKey = BuildConfig.YANDEX_ACCESS_KEY_ID,
                        secretKey = BuildConfig.YANDEX_SECRET_ACCESS_KEY
                    )
                } finally {
                    file?.delete()
                }
            }

            Result.success(imageUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки фото мероприятия", e)
            Result.failure(e)
        }
    }

    private fun uriToFile(
        uri: Uri,
        contentResolver: ContentResolver,
        extension: String
    ): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("event_", ".$extension")

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            inputStream.close()
            file
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка преобразования Uri в File", e)
            null
        }
    }

    private fun getFileExtension(
        uri: Uri,
        contentResolver: ContentResolver
    ): String? {
        val extension = when {
            uri.scheme == ContentResolver.SCHEME_CONTENT -> {
                val mime = android.webkit.MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(contentResolver.getType(uri))
            }

            else -> {
                val path = uri.path ?: return null
                path.substringAfterLast(".", "").takeIf { it.isNotEmpty() }
            }
        }?.lowercase()

        return when (extension) {
            "jpg", "jpeg", "png", "svg" -> extension
            else -> "jpg"
        }
    }

    suspend fun archiveExpiredEvents(): Result<Unit> {
        return try {
            val now = Timestamp.now()

            val snapshot = eventsCollection
                .whereIn(
                    "status",
                    listOf(
                        EventStatus.CREATED.value,
                        EventStatus.APPROVED.value
                    )
                )
                .get()
                .await()

            val batch = firestore.batch()
            var archivedCount = 0

            snapshot.documents.forEach { doc ->
                val event = doc.toObject(EventInfo::class.java)?.copy(id = doc.id)
                    ?: return@forEach

                if (event.isExpired(now)) {
                    batch.update(
                        doc.reference,
                        mapOf(
                            "status" to EventStatus.ARCHIVE.value,
                            "updatedAt" to now,
                            "archivedAt" to now
                        )
                    )

                    archivedCount++
                }
            }

            if (archivedCount > 0) {
                batch.commit().await()
            }

            Log.d(TAG, "Автоархивация мероприятий: архивировано=$archivedCount")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка автоархивации мероприятий", e)
            Result.failure(e)
        }
    }

    private fun participantDocId(eventId: String, userId: String): String {
        return "${eventId}_$userId"
    }

    suspend fun isCurrentUserGoingToEvent(eventId: String): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val doc = participantsCollection
                .document(participantDocId(eventId, userId))
                .get()
                .await()

            Result.success(doc.exists())
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка проверки участия в мероприятии", e)
            Result.failure(e)
        }
    }

    suspend fun addCurrentUserToEvent(event: EventInfo): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            if (event.status == EventStatus.ARCHIVE.value) {
                return Result.failure(Exception("Нельзя отметить архивное мероприятие"))
            }

            if (event.createdByUserId == user.uid) {
                return Result.success(Unit)
            }

            val userDoc = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            val userName = userDoc.getString("name").orEmpty()
            val userPhotoUrl = userDoc.getString("profileImageUrl").orEmpty()

            val participant = EventParticipant(
                id = participantDocId(event.id, user.uid),
                eventId = event.id,
                userId = user.uid,
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                createdAt = Timestamp.now()
            )

            participantsCollection
                .document(participant.id)
                .set(participant)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка добавления пользователя в мероприятие", e)
            Result.failure(e)
        }
    }

    suspend fun removeCurrentUserFromEvent(eventId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            participantsCollection
                .document(participantDocId(eventId, userId))
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления мероприятия из моих", e)
            Result.failure(e)
        }
    }

    suspend fun getFriendsGoingToEvent(eventId: String): Result<List<EventParticipant>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            val currentUserDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val friendsMap = currentUserDoc.get("friends") as? Map<*, *> ?: emptyMap<Any, Any>()

            val friendIds = friendsMap.entries
                .filter { entry ->
                    val value = entry.value as? Map<*, *>
                    value?.get("status") == "friend"
                }
                .mapNotNull { it.key as? String }
                .distinct()

            if (friendIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val participantDocs = mutableListOf<EventParticipant>()

            friendIds.chunked(10).forEach { chunk ->
                val snapshot = participantsCollection
                    .whereEqualTo("eventId", eventId)
                    .whereIn("userId", chunk)
                    .get()
                    .await()

                val participants = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(EventParticipant::class.java)?.copy(id = doc.id)
                }

                participantDocs.addAll(participants)
            }

            if (participantDocs.isEmpty()) {
                return Result.success(emptyList())
            }

            val participantUserIds = participantDocs
                .map { it.userId }
                .filter { it.isNotBlank() }
                .distinct()

            val usersById = mutableMapOf<String, Pair<String, String>>()

            participantUserIds.chunked(10).forEach { chunk ->
                val usersSnapshot = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                usersSnapshot.documents.forEach { doc ->
                    val name = doc.getString("name").orEmpty()
                    val photoUrl = doc.getString("profileImageUrl").orEmpty()
                    usersById[doc.id] = name to photoUrl
                }
            }

            val result = participantDocs.map { participant ->
                val userInfo = usersById[participant.userId]

                participant.copy(
                    userName = userInfo?.first
                        ?.takeIf { it.isNotBlank() }
                        ?: participant.userName,
                    userPhotoUrl = userInfo?.second
                        ?.takeIf { it.isNotBlank() }
                        ?: participant.userPhotoUrl
                )
            }

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки друзей мероприятия", e)
            Result.failure(e)
        }
    }

    private suspend fun getEventsByIds(eventIds: List<String>): List<EventInfo> {
        val distinctIds = eventIds
            .filter { it.isNotBlank() }
            .distinct()

        if (distinctIds.isEmpty()) return emptyList()

        val events = mutableListOf<EventInfo>()

        distinctIds.chunked(10).forEach { chunk ->
            val snapshot = eventsCollection
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            val chunkEvents = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EventInfo::class.java)?.copy(id = doc.id)
            }

            events.addAll(chunkEvents)
        }

        return events
    }

    private suspend fun removeArchivedParticipantEventsForCurrentUser() {
        val userId = auth.currentUser?.uid ?: return

        val participantSnapshot = participantsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val participantDocs = participantSnapshot.documents
        val eventIds = participantDocs
            .mapNotNull { it.getString("eventId") }
            .distinct()

        if (eventIds.isEmpty()) return

        val eventsById = getEventsByIds(eventIds).associateBy { it.id }

        val batch = firestore.batch()
        var deleteCount = 0

        participantDocs.forEach { participantDoc ->
            val eventId = participantDoc.getString("eventId") ?: return@forEach
            val event = eventsById[eventId] ?: return@forEach

            if (
                event.status == EventStatus.ARCHIVE.value &&
                event.createdByUserId != userId
            ) {
                batch.delete(participantDoc.reference)
                deleteCount++
            }
        }

        if (deleteCount > 0) {
            batch.commit().await()
        }

        Log.d(TAG, "Удалено архивных мероприятий из моих: $deleteCount")
    }

    companion object {
        private const val TAG = "EventsRepository"
    }
}

private fun EventInfo.isExpired(now: Timestamp): Boolean {
    if (dates.isEmpty()) return false

    val lastEventTime = dates
        .mapNotNull { slot ->
            slot.endAt ?: slot.startAt
        }
        .maxByOrNull { it.seconds }
        ?: return false

    return lastEventTime.seconds < now.seconds
}