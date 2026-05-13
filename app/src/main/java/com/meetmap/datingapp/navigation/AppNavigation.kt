package com.meetmap.datingapp.navigation

import com.meetmap.datingapp.screens.notification.NotificationScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meetmap.datingapp.button_navigation.MainBottomMenuScreen
import com.meetmap.datingapp.screens.admin.CloudImagesScreen
import com.meetmap.datingapp.screens.admin.ExcelImportScreen
import com.meetmap.datingapp.screens.admin.PlacesAdminScreen
import com.meetmap.datingapp.screens.admin.TestCloudScreen
import com.meetmap.datingapp.screens.auth.LoginScreen
import com.meetmap.datingapp.screens.auth.RegistrationScreen
import com.meetmap.datingapp.screens.feedback.FeedbackAfterPlacesOfDayScreen
import com.meetmap.datingapp.screens.friends.Cur_Friend
import com.meetmap.datingapp.screens.meets.MainMeets
import com.meetmap.datingapp.screens.meets.ReqFriend
import com.meetmap.datingapp.screens.myplaces.MyPlaceDetailScreen
import com.meetmap.datingapp.screens.onboarding.*
import com.meetmap.datingapp.screens.places.PlacesOfDayScreen
import com.meetmap.datingapp.screens.profile.CategorySelectionScreen
import com.meetmap.datingapp.screens.profile.MyProfile
import com.meetmap.datingapp.screens.profile.MyProfileMap
import com.meetmap.datingapp.screens.profile.ProfileSetupScreen
import com.meetmap.datingapp.screens.profile.TargetSelectionScreen
import com.meetmap.datingapp.screens.recommendation.PeopleOfDay
import com.meetmap.datingapp.screens.settings.SettingsScreen
import com.meetmap.datingapp.viewmodels.AuthState
import com.meetmap.datingapp.viewmodels.AuthViewModel
import com.meetmap.datingapp.viewmodels.OnboardingViewModel
import com.meetmap.datingapp.viewmodels.ProfileSetupViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        GlobalNavController.init(navController)
    }

    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val onboardingViewModel: OnboardingViewModel = viewModel()
    val isFirstLaunch by onboardingViewModel.isFirstLaunch.collectAsState()
    val userViewModel: UserViewModel = viewModel()

    val startDestination = when (authState) {
        AuthState.Loading -> Screen.Splash.route
        else -> Screen.Start.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            LaunchedEffect(Unit) {
                delay(500)
                navController.navigate(Screen.Start.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Start.route) {
            StartScreen(
                onGetStarted = {
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.Start.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Greeting.route) {
            GreetingScreen()
            LaunchedEffect(Unit) {
                delay(2000L)

                when (authState) {
                    AuthState.Authenticated -> {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Greeting.route) { inclusive = true }
                        }
                    }
                    else -> {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Greeting.route) { inclusive = true }
                        }
                    }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ProfileSetup.route) {
            val viewModel: ProfileSetupViewModel = viewModel()

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        ProfileSetupViewModel.SetupEvent.NavigateToTargets -> {
                            navController.navigate(Screen.TargetSelection.route)
                        }
                        ProfileSetupViewModel.SetupEvent.RegistrationStarted -> {
                            println("Регистрация началась! isFirstLaunch true")
                        }
                        is ProfileSetupViewModel.SetupEvent.ShowError -> {}
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
                        ProfileSetupViewModel.SetupEvent.NavigateToTutorial -> {
                            navController.navigate(Screen.PlacesTutorial.route) {
                                popUpTo(0) { inclusive = false }
                            }
                        }
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

        composable(Screen.PlacesTutorial.route) {
            PlacesTutorialScreen(
                navController = navController,
                onSkipClick = {
                    navController.navigate(Screen.PlacesOfDay.route) {
                        popUpTo(Screen.PlacesTutorial.route) { inclusive = true }
                    }
                },
                onReadyClick = {
                    navController.navigate(Screen.PlacesTutorial2.route)
                }
            )
        }

        composable(Screen.PlacesTutorial2.route) {
            PlacesTutorialScreen2(
                navController = navController,
                onSkipClick = {
                    navController.navigate(Screen.PlacesOfDay.route) {
                        popUpTo(Screen.PlacesTutorial2.route) { inclusive = true }
                    }
                },
                onReadyClick = {
                    navController.navigate(Screen.PlacesOfDay.route)
                }
            )
        }

        // Places of day with parameter
        composable(
            route = "places_of_day?fromOnboarding={fromOnboarding}",
            arguments = listOf(
                navArgument("fromOnboarding") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromOnboarding = backStackEntry.arguments?.getBoolean("fromOnboarding") ?: false

            PlacesOfDayScreen(
                navController = navController,
                fromOnboarding = fromOnboarding
            )
        }

        composable(
            route = "feedback_after_places_of_day/{isFirstEntry}",
            arguments = listOf(navArgument("isFirstEntry") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isFirstEntry = backStackEntry.arguments?.getBoolean("isFirstEntry") ?: false

            FeedbackAfterPlacesOfDayScreen(
                navController = navController,
                onContinue = {
                    if (isFirstEntry) {
                        println("DEBUG: First entry - going to FinalTutorial")
                        navController.navigate(Screen.FinalTutorial.route) {
                            popUpTo("feedback_after_places_of_day/${isFirstEntry}") { inclusive = true }
                        }
                    } else {
                        println("DEBUG: Not first entry - going to Main")
                        navController.navigate(Screen.Main.route) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }

        composable(Screen.FeedbackAfterPlacesOfDay.route) {
            FeedbackAfterPlacesOfDayScreen(
                navController = navController,
                onContinue = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.FinalTutorial.route) {
            FinalTutorialScreen(
                navController = navController,
                onSkipClick = {
                    println("DEBUG: FinalTutorial skipped - completing onboarding")
                    onboardingViewModel.setFirstLaunchComplete()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                },
                onReadyClick = {
                    println("DEBUG: FinalTutorial completed - completing onboarding")
                    onboardingViewModel.setFirstLaunchComplete()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Main screen
        composable(Screen.Main.route) {
            MainBottomMenuScreen(startTab = "main")
        }

        // My places detail
        composable(
            route = Screen.MyPlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
            MyPlaceDetailScreen(
                placeId = placeId,
                navController = navController,
                userViewModel = userViewModel  // Передаем существующий ViewModel
            )
        }

        // My places list
        composable(Screen.MyPlaces.route) {
            MainBottomMenuScreen(startTab = "screen_2")
        }

        // People of day
        composable(Screen.PeopleOfDay.route) {
            PeopleOfDay(navController = navController, viewModel = userViewModel)
        }

        // Main meets
        composable(Screen.MainMeets.route) {
            MainMeets(navController = navController, viewModel = userViewModel)
        }

        // Req friend
        composable(
            route = "req_friend/{friendId}/{pageTitle}",
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("pageTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val pageTitle = backStackEntry.arguments?.getString("pageTitle") ?: ""

            ReqFriend(
                navController = navController,
                viewModel = userViewModel,
                friendId = friendId,
                pageTitle = pageTitle
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // My profile
        composable(Screen.MyProfile.route) {
            MyProfile(navController = navController, userViewModel)
        }

        // Current friend
        composable(Screen.CurFriend.route) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            Cur_Friend(
                navController = navController,
                friendId = friendId,
                userViewModel
            )
        }

        // Main bottom menu
        composable(
            route = "main_bottom_menu/{startTab}",
            arguments = listOf(
                navArgument("startTab") {
                    type = NavType.StringType
                    defaultValue = "screen_2"
                }
            )
        ) { backStackEntry ->
            val startTab = backStackEntry.arguments?.getString("startTab")
            MainBottomMenuScreen(startTab = startTab ?: "screen_2")
        }

        // Admin screens
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

        // Notification
        composable(Screen.Notification.route) {
            NotificationScreen(navController = navController)
        }

        composable(Screen.MyProfileMap.route) {
           MyProfileMap(navController = navController, userViewModel)
        }


    }
}