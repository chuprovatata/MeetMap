package com.example.datingapp.button_navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.datingapp.viewmodels.UserViewModel


@Composable
fun MainBottomMenuScreen(
    startTab: String = "screen_2"
) {
    val navBottomController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()

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
        NavGraph(navHostController = navBottomController, userViewModel)
    }
}