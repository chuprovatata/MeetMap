package com.example.datingapp.di

import com.example.datingapp.data.repository.UserPlacesRepository
import com.example.datingapp.data.repository.UserRepository
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
        okHttpClient: OkHttpClient
    ): UserRepository = UserRepository(auth, firestore, okHttpClient)

    @Provides
    @Singleton
    fun provideUserPlacesRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserPlacesRepository = UserPlacesRepository(auth, firestore)
}