package com.example.datingapp.navigation

object NavigationProgress {

    private val setupScreens = listOf(
        Screen.Registration,
        Screen.ProfileSetup,
        Screen.TargetSelection,
        Screen.CategorySelection,
        Screen.PlacesTutorial,
        Screen.PlacesTutorial2,
        Screen.FinalTutorial
    )

    fun getProgress(currentScreen: Screen): Float {
        val currentIndex = setupScreens.indexOfFirst { it == currentScreen }
        if (currentIndex == -1) return 0f

        return (currentIndex + 1).toFloat() / setupScreens.size
    }
}