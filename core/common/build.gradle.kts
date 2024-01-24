plugins {
    id("thelab.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.riders.thelab.core.common"
}

dependencies {
    ///////////////////////////////////
    // General Dependencies
    ///////////////////////////////////
    // Kotlin
    // api(platform(libs.kotlin.bom))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.serialization.json)

    // AndroidX
    api(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.biometric)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)

    // Media
    api(libs.androidx.media)
    api(libs.androidx.media.router)

    // OkHttp
    // define a BOM and its version
    api(platform(libs.okhttp.bom))
    // define any required OkHttp artifacts without version
    api(libs.okhttp)

    // Moshi
    api(libs.moshi)
    api(libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)


    ///////////////////////////////////
    // Tests Dependencies
    ///////////////////////////////////
    androidTestImplementation(project(":core:testing"))
}