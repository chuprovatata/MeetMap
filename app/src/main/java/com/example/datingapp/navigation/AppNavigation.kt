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
import com.example.datingapp.screens.onboarding.PlacesTutorialScreen
import com.example.datingapp.screens.onboarding.PlacesTutorialScreen2
import com.example.datingapp.screens.profile.CategorySelectionScreen
import com.example.datingapp.screens.profile.ProfileSetupScreen
import com.example.datingapp.screens.profile.TargetSelectionScreen
import com.example.datingapp.viewmodels.ProfileSetupViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.datingapp.screens.onboarding.FinalTutorialScreen
import com.example.datingapp.screens.places.PlaceLikedScreen
import com.example.datingapp.screens.places.PlacesOfDayScreen
import com.example.datingapp.screens.myplaces.MyPlacesScreen
import com.example.datingapp.screens.notification.NotificationScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.PlacesTutorial.route
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
                        is ProfileSetupViewModel.SetupEvent.NavigateToMain -> {
                            navController.navigate(Screen.PlacesTutorial.route) {
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

        composable(Screen.PlacesTutorial.route) {
            PlacesTutorialScreen(
                onSkipClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                }
            )

            LaunchedEffect(Unit) {
                delay(4000)
                navController.navigate(Screen.PlacesTutorial2.route)
            }
        }

        composable(Screen.PlacesTutorial2.route) {
            PlacesTutorialScreen2(
                navController = navController,
                onSkipClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                },
                onReadyClick = {
                    navController.navigate(Screen.FinalTutorial.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.FinalTutorial.route) {
            FinalTutorialScreen(
                navController = navController,
                onSkipClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                },
                onReadyClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Screen.PlacesOfDay.route) {
            PlacesOfDayScreen(navController = navController)
        }
        composable(Screen.PlaceLiked.route) {
            PlaceLikedScreen(
                onBackClick = { navController.popBackStack() },
                onVisitedClick = {
                    navController.popBackStack()
                },
                onPlannedClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.MyPlaces.route) {
            MyPlacesScreen(navController = navController)
        }
        composable(Screen.Notification.route) {
            NotificationScreen(navController = navController)
        }
    }
}