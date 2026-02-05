package com.example.navigation.button_navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.navigation.friends.Main_Friends
import com.example.navigation.friends.My_Friends


@Composable
fun NavGraph(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = "screen_2") {
        composable("screen_2") {
            Screen2()
        }
        composable("screen_1") {
            Screen3()
        }
        composable("screen_3") {
            Main_Friends()
        }
    }


}