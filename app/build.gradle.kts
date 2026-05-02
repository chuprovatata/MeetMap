import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.meetmap.datingapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.meetmap.datingapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val yandexAccessKey = localProperties.getProperty("yandex.access.key")
            ?: System.getenv("YANDEX_ACCESS_KEY_ID")
            ?: "default_dev_key"

        val yandexSecretKey = localProperties.getProperty("yandex.secret.key")
            ?: System.getenv("YANDEX_SECRET_ACCESS_KEY")
            ?: "default_dev_secret"
        val yandexOAuthToken = localProperties.getProperty("yandex.oauth.token")
            ?: System.getenv("YANDEX_OAUTH_TOKEN")
            ?: "default_oauth_token"
        buildConfigField("String", "YANDEX_ACCESS_KEY_ID", "\"${yandexAccessKey}\"")
        buildConfigField("String", "YANDEX_SECRET_ACCESS_KEY", "\"${yandexSecretKey}\"")
        buildConfigField("String", "YANDEX_OAUTH_TOKEN", "\"${yandexOAuthToken}\"")
    }
    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Firebase BOM (актуальная версия)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Firebase зависимости БЕЗ версий (берутся из BOM)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-storage-ktx")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.foundation)

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    implementation ("com.google.dagger:hilt-android:2.48")
    implementation(libs.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    kapt ("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("com.google.code.gson:gson:2.10.1") // Добавляем Gson

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Для Firebase await()
    implementation("com.google.android.gms:play-services-auth:20.7.0")


    // Для работы с Яндекс.Облаком
    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    // Для загрузки изображений
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("com.amazonaws:aws-android-sdk-s3:2.72.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.72.0")
    implementation("io.coil-kt:coil-svg:2.4.0")

    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")

    // Для работы с уведомлениями в фоне
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // AppMetrica SDK.
    implementation("io.appmetrica.analytics:analytics:8.0.0")
    // Google Places SDK for Android (новая версия)
    implementation("com.google.android.libraries.places:places:4.0.0")
}