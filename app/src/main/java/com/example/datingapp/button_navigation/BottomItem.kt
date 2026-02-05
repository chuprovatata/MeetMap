package com.example.navigation.button_navigation

import com.example.navigation.R

sealed class BottomItem (val title :String, val iconId: Int, val route: String){
    object Screen1: BottomItem("Знакомства", R.drawable.meet, "screen_1")
    object Screen2: BottomItem("Мои места", R.drawable.map, "screen_2")
    object Screen3: BottomItem("Друзья", R.drawable.friends, "screen_3")

}