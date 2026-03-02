package com.example.datingapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Start : Screen("start")
    object Greeting : Screen("greeting")
    object Registration : Screen("registration")
    object Login : Screen("login")
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
    object MyPlaceDetail : Screen("myPlaceDetail/{placeId}") {
        fun passPlaceId(placeId: String): String = "myPlaceDetail/$placeId"
    }
    object MainFriends : Screen("main_friends")
    object MyFriends : Screen("my_friends")
    object CurFriend : Screen("cur_friend")
    object MyProfile: Screen("my_profile")
    object Settings: Screen("settings")
    object ExcelImport : Screen("excel_import")
    object PlacesAdmin : Screen("places_admin")
    object CloudImages : Screen("cloud_images")
    object TestCloud : Screen("test_cloud")
    object  ReqMeet: Screen("req_meet")
    object  ReqFriend: Screen("req_friend")
    object FeedbackAfterPlacesOfDay: Screen("feedback_after_places_of_day")



    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}