package com.meetmap.datingapp

import android.app.Application
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@HiltAndroidApp
class DatingAppApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        val playerId = OneSignal.User.pushSubscription.id
        if (playerId != null && playerId.isNotEmpty()) {
            savePlayerIdToFirestore(playerId)
        }


        //карты
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        Log.d("MAPKIT", "KEY = ${BuildConfig.MAPKIT_API_KEY}")
    }

    private fun savePlayerIdToFirestore(playerId: String) {
        applicationScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("oneSignalPlayerId", playerId)
                        .await()
                    Log.d("OneSignal", "PlayerId сохранён: $playerId")
                } else {
                    Log.d("OneSignal", "Пользователь не авторизован")
                }
            } catch (e: Exception) {
                Log.e("OneSignal", "Ошибка: ${e.message}")
            }
        }
    }
}