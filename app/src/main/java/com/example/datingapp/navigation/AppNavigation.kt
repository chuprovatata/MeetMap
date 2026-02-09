package com.example.datingapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.datingapp.button_navigation.MainBottomMenuScreen
import com.example.datingapp.screens.admin.CloudImagesScreen
import com.example.datingapp.screens.admin.ExcelImportScreen
import com.example.datingapp.screens.admin.PlacesAdminScreen
import com.example.datingapp.screens.admin.TestCloudScreen
import com.example.datingapp.screens.friends.Cur_Friend
import com.example.datingapp.screens.onboarding.FinalTutorialScreen
import com.example.datingapp.screens.places.PlaceLikedScreen
import com.example.datingapp.screens.places.PlacesOfDayScreen
import com.example.datingapp.screens.myplaces.MyPlacesScreen
import com.example.datingapp.screens.notification.NotificationScreen
import com.example.datingapp.screens.settings.SettingsScreen
import com.example.datingapp.screens.profile.MyProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = Screen.FinalTutorial.route
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
                navController = navController, viewModel = viewModel
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
                navController = navController, viewModel = viewModel
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
                navController = navController, viewModel = viewModel
            )
        }

        composable(Screen.PlacesTutorial.route) {
            PlacesTutorialScreen(
                onSkipClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                })

            LaunchedEffect(Unit) {
                delay(4000)
                navController.navigate(Screen.PlacesTutorial2.route)
            }
        }

        composable(Screen.PlacesTutorial2.route) {
            PlacesTutorialScreen2(navController = navController, onSkipClick = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(0)
                }
            }, onReadyClick = {
                navController.navigate(Screen.FinalTutorial.route) {
                    popUpTo(0)
                }
            })
        }

        composable(Screen.FinalTutorial.route) {
            FinalTutorialScreen(navController = navController, onSkipClick = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(0)
                }
            }, onReadyClick = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(0)
                }
            })
        }

        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Screen.PlacesOfDay.route) {
            PlacesOfDayScreen(navController = navController)
        }
        composable(Screen.PlaceLiked.route) {
            PlaceLikedScreen(onBackClick = { navController.popBackStack() }, onVisitedClick = {
                navController.popBackStack()
            }, onPlannedClick = {
                navController.popBackStack()
            })
        }

        composable(Screen.Notification.route) {
            NotificationScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.MyProfile.route) {
            MyProfile(navController = navController)
        }

        composable(Screen.CurFriend.route) {
            Cur_Friend(navController = navController)
        }
        composable(
            route = "main_bottom_menu/{startTab}", arguments = listOf(
                navArgument("startTab") {
                    type = NavType.StringType
                    defaultValue = "screen_2"
                })) { backStackEntry ->
            val startTab = backStackEntry.arguments?.getString("startTab")
            MainBottomMenuScreen(
                startTab = startTab ?: "screen_2",

                )

        }

        composable(Screen.PlacesAdmin.route) {
            PlacesAdminScreen(navController = navController)
        }
        composable(Screen.ExcelImport.route) {
            ExcelImportScreen(navController = navController)
        }
        composable(Screen.CloudImages.route) {
            CloudImagesScreen(navController = navController)
        }
        composable(Screen.TestCloud.route) {
            TestCloudScreen(navController = navController)
        }
    }
}