// app/src/main/java/com/example/datingapp/data/models/PlaceInfo.kt
package com.meetmap.datingapp.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class PlaceInfo(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @PropertyName("metro_station")
    val metroStation: String = "",
    @PropertyName("metro_line")
    val metroLine: String = "",
    @PropertyName("distance_to_metro")
    val distanceToMetro: Double = 0.0,
    @PropertyName("photoUrl")
    val photoUrl: String = "",
    val categories: List<String> = emptyList(),
    @PropertyName("likes_count")
    val likesCount: Int = 0,
    @PropertyName("fire_icon")
    val hasFireIcon: Boolean = false,
    @PropertyName("place_ofday")
    val place_ofday: Boolean = false,
    @PropertyName("unique_id")
    val uniqueId: String = "",
    @PropertyName("description")
    val description: String = "",
    @PropertyName("rarity")
    val rarity: String = "common",
    @ServerTimestamp
    @PropertyName("created_at")
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    @PropertyName("updated_at")
    val updatedAt: Timestamp? = null
) {
    fun generateUniqueId(): String {
        return "${name.lowercase().replace(" ", "_")}_${latitude}_${longitude}"
    }

    companion object {
        const val RARITY_COMMON = "common"
        const val RARITY_UNCOMMON = "uncommon"
        const val RARITY_RARE = "rare"
        const val RARITY_EPIC = "epic"
        const val RARITY_UNIQUE = "unique"

        val rarityDisplayNames = mapOf(
            RARITY_COMMON to "Базовое",
            RARITY_UNCOMMON to "Среднее",
            RARITY_RARE to "Редкое",
            RARITY_EPIC to "Эпическое",
            RARITY_UNIQUE to "Уникальное"
        )
    }
}