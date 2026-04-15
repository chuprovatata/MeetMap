package com.meetmap.datingapp.button_navigation

import com.meetmap.datingapp.R

sealed class BottomItem(
    val title: String,
    val iconId: Int,
    val route: String,
    val relatedRoutes: List<String>
) {
    object Screen1 : BottomItem("Знакомства", R.drawable.friends, "screen_1", listOf("screen_1"))
    object Screen2 : BottomItem("Мои места", R.drawable.map, "screen_2", listOf("screen_2"))
    object ScreenMain : BottomItem("Главная", R.drawable.home, "main", listOf("main"))
}