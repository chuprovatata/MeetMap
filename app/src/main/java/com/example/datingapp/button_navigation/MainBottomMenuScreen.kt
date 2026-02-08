package com.example.datingapp.button_navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController


@Composable
fun MainBottomMenuScreen(
    startTab: String = "screen_2"
) {
    val navBottomController = rememberNavController()

    LaunchedEffect(startTab) {

        if (navBottomController.currentBackStackEntry?.destination?.route != startTab) {
            navBottomController.navigate(startTab) {
                popUpTo(0)  // Очищаем стек
            }
        }
    }

    Scaffold(
        bottomBar = {
            ButtonNavigation(navController = navBottomController)
        }
    ) {
        NavGraph(navHostController = navBottomController)
    }
}