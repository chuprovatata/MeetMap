package com.example.datingapp.button_navigation

import android.provider.ContactsContract
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
import com.example.datingapp.screens.meets.MainMeets
import com.example.datingapp.screens.meets.ReqFriend
import com.example.datingapp.screens.meets.ReqMeet
import com.example.datingapp.screens.myplaces.MyPlacesScreen
import com.example.datingapp.screens.profile.MyProfile
import com.example.datingapp.screens.recommendation.PeopleOfDay
import com.example.datingapp.screens.settings.SettingsScreen
import com.example.datingapp.viewmodels.UserViewModel


@Composable
fun NavGraph(navHostController: NavHostController, viewModel: UserViewModel) {
    NavHost(navController = navHostController, startDestination = "screen_2") {
        val bottomNav = @Composable {
            ButtonNavigation(navController = navHostController)
        }
        composable("screen_2") {
            MyPlacesScreen(navHostController)
        }


        composable("screen_3") {
            Main_Friends(navHostController)


        }
        composable("screen_1") {
            MainMeets(navHostController, viewModel)


        }
        composable(Screen.MyFriends.route) {
            My_Friends(navHostController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navHostController)
        }
        composable(Screen.MyProfile.route) {
            MyProfile(navHostController, viewModel)
        }



        composable(Screen.CurFriend.route) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            Cur_Friend(
                navController = navHostController,
                friendId = friendId,
                viewModel
            )
        }
        composable(Screen.PeopleOfDay.route) {

            PeopleOfDay(navHostController, viewModel)
        }

        composable(
            route = "req_friend/{friendId}/{pageTitle}",  // Два параметра в路由
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("pageTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val pageTitle = backStackEntry.arguments?.getString("pageTitle") ?: ""

            ReqFriend(
                navController = navHostController,
                viewModel = viewModel,
                friendId = friendId,
                pageTitle = pageTitle
            )
        }




    }


}