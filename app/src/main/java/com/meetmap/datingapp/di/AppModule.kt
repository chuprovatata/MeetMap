package com.meetmap.datingapp.di

import com.meetmap.datingapp.data.repository.NotificationRepository
import com.meetmap.datingapp.data.repository.UserPlacesRepository
import com.meetmap.datingapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "MeetMap-Android-App/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        okHttpClient: OkHttpClient,
        notificationRepository: NotificationRepository
    ): UserRepository = UserRepository(auth, firestore, okHttpClient, notificationRepository)

    @Provides
    @Singleton
    fun provideUserPlacesRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository
    ): UserPlacesRepository = UserPlacesRepository(auth, firestore, notificationRepository)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): NotificationRepository = NotificationRepository(auth, firestore)
}