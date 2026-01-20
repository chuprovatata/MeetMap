package com.example.datingapp.navigation

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Greeting : Screen("greeting")
    object Registration : Screen("registration")
    object ProfileSetup : Screen("profileSetup")
    object TargetSelection : Screen("targetSelection")
    object CategorySelection : Screen("categorySelection")
    object Main : Screen("main")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}