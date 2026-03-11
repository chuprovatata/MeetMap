package com.example.datingapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class MyUser(
    @DocumentId
    val uid: String = "",  // ID документа из Firebase (бывшее поле id)
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val telegram: String = "",
    val age: Int = 0,
    val birthYear: Int = 0,
    val bio: String = "",
    val gender: String = "",
    val university: String = "",
    val profileComplete: Boolean = false,
    val targets: List<Int> = emptyList(),
    val categories: List<Int> = emptyList(),
    val friends: Map<String, FriendInfo> = emptyMap(),
    val favoritePlace: String = "",
    val favoritePlacePhoto: String = "",
    val fcmToken: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

data class FriendInfo(
    val status: String = "",
    // friend - взаимный друг,
    // deny - мой отказ,
    // my_application (моя заявка на дружбу),
    // request - зявка МНЕ
    val since: Timestamp? = null
)


enum class FriendStatus(val value: String) {
    FRIEND("friend"),      // Взаимный друг
    DENY("deny"),              // Мой отказ
    MY_APPLICATION("my_application"), // Моя заявка на дружбу
    REQUEST("request"),        // Заявка МНЕ

}