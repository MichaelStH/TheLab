plugins {
    id("thelab.android.feature")
    id("thelab.android.library.compose")
    id("thelab.android.hilt")
    id("thelab.android.library.jacoco")
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.riders.thelab.feature.musicrecognition"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(files("libs/acrcloud-universal-sdk-1.3.22.jar"))
//    implementation(project(":shazam-kit", configuration = "default"))
    implementation(project(":spotify-app-remote", configuration = "default"))
    // implementation(files("libs/spotify-auth-release-2.1.0.aar"))
    // implementation(files("libs/spotify-auth-store-release-2.1.0.aar"))

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:analytics"))
    implementation(project(":core:data"))
    implementation(project(":core:permissions"))


    ///////////////////////////////////
    // General Dependencies
    ///////////////////////////////////
    // Kotlin
    implementation(libs.kotlinx.coroutines.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.browser)

    implementation(libs.spotify.auth)

    androidTestImplementation(project(":core:testing"))
}