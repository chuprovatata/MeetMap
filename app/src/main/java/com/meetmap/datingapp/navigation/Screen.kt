package com.meetmap.datingapp.navigation

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
    object CurFriend : Screen("cur_friend/{friendId}") {
        fun passFriendId(friendId: String): String = "cur_friend/$friendId"
    }
    object MyProfile: Screen("my_profile")
    object Settings: Screen("settings")
    object ExcelImport : Screen("excel_import")
    object PlacesAdmin : Screen("places_admin")
    object CloudImages : Screen("cloud_images")
    object TestCloud : Screen("test_cloud")
    object ReqFriend : Screen("req_friend/{friendId}/{pageTitle}") {
        fun passParams(friendId: String, pageTitle: String): String =
            "req_friend/$friendId/$pageTitle"
    }
    object  MainMeets: Screen("screen_1")
    object FeedbackAfterPlacesOfDay: Screen("feedback_after_places_of_day")
    object PeopleOfDay: Screen("people_of_day")

    object MyProfileMap: Screen("my_profile_map")
    object FavoritePlace: Screen("favorite_place")




    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}