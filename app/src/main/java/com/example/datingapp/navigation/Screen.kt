package com.example.datingapp.navigation

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Greeting : Screen("greeting")
    object Registration : Screen("registration")
    object ProfileSetup : Screen("profileSetup")
    object TargetSelection : Screen("targetSelection")
    object CategorySelection : Screen("categorySelection")
    object PlacesTutorial : Screen("places_tutorial")
    object PlacesTutorial2 : Screen("places_tutorial2")
    object FinalTutorial : Screen("final_tutorial")
    object Main : Screen("main")
    object Notification : Screen("notification")
    object PlacesOfDay : Screen("placesOfDay")
    object PlaceLiked : Screen("placeLiked")
    object MyPlaces : Screen("myPlaces")
    object Settings : Screen("settings") // будет добавлен позже
    object Profile : Screen("profile") // будет добавлен позже
    object Dating : Screen("dating") // будет добавлен позже
    object Friends : Screen("friends") // будет добавлен позже

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}