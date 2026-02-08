package com.example.datingapp.button_navigation

import com.example.datingapp.R

sealed class BottomItem(
    val title: String,
    val iconId: Int,
    val route: String,
    val relatedRoutes: List<String>
) {
    object Screen1 : BottomItem("Знакомства", R.drawable.meet, "screen_1", listOf("screen_1"))
    object Screen2 : BottomItem("Мои места", R.drawable.map, "screen_2", listOf("screen_2"))
    object Screen3 :
        BottomItem("Друзья", R.drawable.friends, "screen_3", listOf("screen_3", "my_friends","cur_friend"))

}

