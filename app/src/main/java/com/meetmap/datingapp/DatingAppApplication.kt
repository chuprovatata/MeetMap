package com.meetmap.datingapp

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import com.meetmap.datingapp.BuildConfig

@HiltAndroidApp
class DatingAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this)
    }
}