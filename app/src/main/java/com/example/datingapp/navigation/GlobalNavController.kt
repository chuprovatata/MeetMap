package com.example.datingapp.navigation

import androidx.navigation.NavController

object GlobalNavController {
    private var _navController: NavController? = null
    val navController: NavController
        get() = _navController ?: error("GlobalNavController not initialized. Call init first.")

    fun init(controller: NavController) {
        _navController = controller
    }

    fun navigateToPlaceDetail(placeId: String) {
        navController.navigate("myPlaceDetail/$placeId")
    }
}