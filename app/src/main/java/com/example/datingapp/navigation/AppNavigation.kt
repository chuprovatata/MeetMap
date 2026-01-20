package com.example.datingapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.datingapp.screens.auth.RegistrationScreen
import com.example.datingapp.screens.main.MainScreen
import com.example.datingapp.screens.onboarding.GreetingScreen
import com.example.datingapp.screens.onboarding.StartScreen
import com.example.datingapp.screens.profile.CategorySelectionScreen
import com.example.datingapp.screens.profile.ProfileSetupScreen
import com.example.datingapp.screens.profile.TargetSelectionScreen
import com.example.datingapp.viewmodels.ProfileSetupViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Registration.route
    ) {
        composable(Screen.Start.route) {
            StartScreen {
                navController.navigate(Screen.Greeting.route)
            }
        }

        composable(Screen.Greeting.route) {
            GreetingScreen {
                navController.navigate(Screen.Registration.route) {
                    popUpTo(Screen.Greeting.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(navController = navController)
        }

        composable(Screen.ProfileSetup.route) {
            val viewModel: ProfileSetupViewModel = viewModel()

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        ProfileSetupViewModel.SetupEvent.NavigateToTargets -> {
                            navController.navigate(Screen.TargetSelection.route)
                        }
                        else -> {}
                    }
                }
            }

            ProfileSetupScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.TargetSelection.route) {
            val viewModel: ProfileSetupViewModel = viewModel()

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        ProfileSetupViewModel.SetupEvent.NavigateToCategories -> {
                            navController.navigate(Screen.CategorySelection.route)
                        }
                        else -> {}
                    }
                }
            }

            TargetSelectionScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.CategorySelection.route) {
            val viewModel: ProfileSetupViewModel = viewModel()

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        ProfileSetupViewModel.SetupEvent.NavigateToMain -> {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(0)
                            }
                        }
                        else -> {}
                    }
                }
            }

            CategorySelectionScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}