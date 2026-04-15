package com.meetmap.datingapp.data.models

import com.google.firebase.firestore.PropertyName

data class PlaceCategory(
    val id: String = "",
    val name: String = "",
    @PropertyName("display_name")
    val displayName: String = "",
    @PropertyName("icon_name")
    val iconName: String = "", // например: "ic_category_art"
    val color: String = "#A75CC6",
    val order: Int = 0
)