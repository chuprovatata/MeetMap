package com.example.datingapp.button_navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.datingapp.navigation.GlobalNavController
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
                popUpTo(0)
            }
        }
    }

    Scaffold(
        bottomBar = {
            ButtonNavigation(navController = navBottomController)
        }
    ) { paddingValues ->
        // Передаем оба контроллера в NavGraph
        NavGraph(
            localNavController = navBottomController,
            globalNavController = GlobalNavController.navController, // Добавляем глобальный контроллер
            viewModel = userViewModel
        )
    }
}