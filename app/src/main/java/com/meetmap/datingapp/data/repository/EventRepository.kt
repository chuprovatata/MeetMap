package com.meetmap.datingapp.data.repository


import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.meetmap.datingapp.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.meetmap.datingapp.data.models.Event
import com.meetmap.datingapp.extensions.await
import com.meetmap.datingapp.utils.CloudImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val EVENTS_COLLECTION = "events"
        private const val TAG = "EventRepository"
    }




    suspend fun uploadEventImage(
        uri: Uri,
        contentResolver: ContentResolver,
        eventId: String
    ): String = withContext(Dispatchers.IO) {
        Log.d("EventRepository", "=== uploadEventImage START ===")
        Log.d("EventRepository", "uri = $uri")
        Log.d("EventRepository", "eventId = $eventId")

        var file: File? = null

        try {
            file = uriToFile(uri, contentResolver)
            Log.d("EventRepository", "File created: ${file?.absolutePath}, size: ${file?.length()}")

            if (file == null) {
                throw Exception("Не удалось создать файл из URI")
            }

            val extension = getFileExtension(uri, contentResolver) ?: "jpg"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val fileName = "profile-photo/event_$eventId-$timestamp.$extension"
            Log.d("EventRepository", "fileName = $fileName")

            Log.d("EventRepository", "Calling CloudImageUtils.uploadFile...")
            val imageUrl = CloudImageUtils.uploadFile(
                file = file,
                fileName = fileName,
                accessKey = BuildConfig.YANDEX_ACCESS_KEY_ID,
                secretKey = BuildConfig.YANDEX_SECRET_ACCESS_KEY
            )
            Log.d("EventRepository", "CloudImageUtils.uploadFile returned: $imageUrl")

            withContext(Dispatchers.Main) {
                Log.d("EventRepository", "Updating Firestore...")
                firestore.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .update("imageUrl", imageUrl)
                    .await()
                Log.d("EventRepository", "Firestore updated successfully")
            }

            return@withContext imageUrl

        } catch (e: Exception) {
            Log.e("EventRepository", "uploadEventImage ERROR", e)
            throw Exception("Ошибка загрузки фото мероприятия: ${e.message}")
        } finally {
            file?.delete()
            Log.d("EventRepository", "=== uploadEventImage END ===")
        }
    }


    suspend fun deleteEventImage(eventId: String) {
        try {
            firestore.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("imageUrl", null)
                .await()

        } catch (e: Exception) {

            throw Exception("Ошибка удаления фото мероприятия: ${e.message}")
        }
    }


    private fun uriToFile(uri: Uri, contentResolver: ContentResolver): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("event_", getFileExtension(uri, contentResolver) ?: "jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        return when {
            uri.scheme == ContentResolver.SCHEME_CONTENT -> {
                val mime = android.webkit.MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(contentResolver.getType(uri))
            }
            else -> {
                val path = uri.path ?: return null
                path.substringAfterLast(".", "").takeIf { it.isNotEmpty() }
            }
        }?.lowercase()
    }




    suspend fun getAllEvents(): List<Event> {
        return try {
            val snapshot = firestore.collection(EVENTS_COLLECTION)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    fun observeEvents(): Flow<List<Event>> = callbackFlow {
        val listenerRegistration = firestore.collection(EVENTS_COLLECTION)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(events)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }


    suspend fun getEventById(eventId: String): Event? {
        return try {
            val document = firestore.collection(EVENTS_COLLECTION)
                .document(eventId)
                .get()
                .await()

            if (!document.exists()) return null
            document.toObject(Event::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }




    suspend fun joinEvent(eventId: String, userId: String): Boolean {
        return try {
            val eventRef = firestore.collection(EVENTS_COLLECTION).document(eventId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val event = snapshot.toObject(Event::class.java) ?: return@runTransaction


                if (event.currentParticipants >= event.maxParticipants) return@runTransaction

                if (event.participantsList.contains(userId)) return@runTransaction

                val newParticipantsList = event.participantsList.toMutableList().apply {
                    add(userId)
                }

                transaction.update(eventRef, "currentParticipants", event.currentParticipants + 1)
                transaction.update(eventRef, "participantsList", newParticipantsList)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun leaveEvent(eventId: String, userId: String): Boolean {
        return try {
            val eventRef = firestore.collection(EVENTS_COLLECTION).document(eventId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val event = snapshot.toObject(Event::class.java) ?: return@runTransaction

                if (!event.participantsList.contains(userId)) return@runTransaction

                val newParticipantsList = event.participantsList.toMutableList().apply {
                    remove(userId)
                }

                transaction.update(eventRef, "currentParticipants", event.currentParticipants - 1)
                transaction.update(eventRef, "participantsList", newParticipantsList)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun isUserJoined(eventId: String, userId: String): Boolean {
        return try {
            val event = getEventById(eventId) ?: return false
            event.participantsList.contains(userId)
        } catch (e: Exception) {
            false
        }
    }


    suspend fun getParticipantsCount(eventId: String): Int {
        return try {
            val event = getEventById(eventId) ?: return 0
            event.currentParticipants
        } catch (e: Exception) {
            0
        }
    }




    suspend fun createEvent(event: Event): String {
        val docRef = firestore.collection(EVENTS_COLLECTION).document()
        val eventWithId = event.copy(
            id = docRef.id,
            createdAt = com.google.firebase.firestore.FieldValue.serverTimestamp() as? com.google.firebase.Timestamp
                ?: com.google.firebase.Timestamp.now()
        )
        docRef.set(eventWithId).await()
        return docRef.id
    }


    suspend fun updateEvent(eventId: String, data: Map<String, Any>) {
        firestore.collection(EVENTS_COLLECTION)
            .document(eventId)
            .update(data)
            .await()
    }


    suspend fun deleteEvent(eventId: String) {
        firestore.collection(EVENTS_COLLECTION)
            .document(eventId)
            .delete()
            .await()
    }





    suspend fun getEventsForUser(userUniversity: String): List<Event> {
        val allEvents = getAllEvents()
        return allEvents.filter { event ->
            event.isForAll || event.university == userUniversity
        }
    }
}
