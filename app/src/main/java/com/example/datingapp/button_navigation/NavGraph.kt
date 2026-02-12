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
import com.example.datingapp.screens.settings.SettingsScreen


@Composable
fun NavGraph(navHostController: NavHostController) {
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
            MainMeets(navHostController)


        }
        composable(Screen.MyFriends.route) {
            My_Friends(navHostController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navHostController)
        }
        composable(Screen.MyProfile.route) {
            MyProfile(navHostController)
        }



        composable(Screen.CurFriend.route) {
            Cur_Friend(navHostController)
        }

        composable(Screen.ReqMeet.route) {
            ReqMeet(navHostController)
        }

        composable(Screen.ReqFriend.route) {
            ReqFriend(navHostController)
        }

        //Когда будет бд в таком стиле функции
        /*

        composable(
            route = "cur_friend/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            Cur_Friend(username = username,navHostController)
        }*/

    }


}