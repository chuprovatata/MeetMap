package com.example.datingapp.button_navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.datingapp.navigation.Screen
import com.example.datingapp.screens.friends.Cur_Friend
import com.example.datingapp.screens.friends.Main_Friends
import com.example.datingapp.screens.friends.My_Friends
import com.example.datingapp.screens.main.MainScreen
import com.example.datingapp.screens.meets.MainMeets
import com.example.datingapp.screens.meets.ReqFriend
import com.example.datingapp.screens.meets.ReqMeet
import com.example.datingapp.screens.myplaces.MyPlaceDetailScreen
import com.example.datingapp.screens.myplaces.MyPlacesScreen
import com.example.datingapp.screens.profile.MyProfile
import com.example.datingapp.screens.recommendation.PeopleOfDay
import com.example.datingapp.screens.settings.SettingsScreen
import com.example.datingapp.viewmodels.UserViewModel

@Composable
fun NavGraph(
    localNavController: NavHostController,  // для навигации внутри меню
    globalNavController: NavController,     // для навигации вне меню
    viewModel: UserViewModel
) {
    NavHost(navController = localNavController, startDestination = "screen_2") {

        // Экран "Мои места" - нужен доступ к глобальной навигации для перехода к деталям места
        composable("screen_2") {
            MyPlacesScreen(
                navController = globalNavController,  // Даем глобальный контроллер
                localNavController = localNavController // И локальный на всякий случай
            )
        }
        composable(
            route = "myPlaceDetail/{placeId}",
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
            MyPlaceDetailScreen(
                placeId = placeId,
                navController = globalNavController  // Используем глобальный контроллер
            )
        }

        // Экран "Главная" - нужен доступ к глобальной навигации для уведомлений
        composable("main") {
            MainScreen(
                navController = globalNavController,  // Даем глобальный контроллер
                localNavController = localNavController // И локальный на всякий случай
            )
        }

        // Экран "Знакомства" - работает в основном внутри меню
        composable("screen_1") {
            MainMeets(
                navController = localNavController,
                viewModel = viewModel
            )
        }

        // Эти экраны открываются из меню, но могут требовать глобальной навигации
        composable(Screen.MyFriends.route) {
            My_Friends(
                navController = localNavController
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = globalNavController  // Настройки - глобальный экран
            )
        }

        composable(Screen.MyProfile.route) {
            MyProfile(
                navController = globalNavController,  // Профиль - глобальный экран
                viewModel = viewModel
            )
        }

        composable(Screen.CurFriend.route) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            Cur_Friend(
                navController = localNavController,  // Друзья - внутри меню
                friendId = friendId,
                viewModel = viewModel
            )
        }

        composable(Screen.PeopleOfDay.route) {
            PeopleOfDay(
                navController = localNavController,  // Люди дня - внутри меню
                viewModel = viewModel
            )
        }

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
                navController = localNavController,
                viewModel = viewModel,
                friendId = friendId,
                pageTitle = pageTitle
            )
        }
    }
}