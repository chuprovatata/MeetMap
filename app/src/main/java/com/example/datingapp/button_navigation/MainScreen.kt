package com.example.navigation.button_navigation

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun MainScreen() {
    val navBottomController = rememberNavController()
    Scaffold(
        bottomBar = {
            ButtonNavigation (navController= navBottomController)

        }
    ) {
        NavGraph(navHostController = navBottomController)

    }
}