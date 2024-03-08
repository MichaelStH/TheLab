plugins {
    id("thelab.android.library")
    id("thelab.android.hilt")
    id("thelab.android.room")
    id("thelab.android.library.jacoco")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes.forEach {
        it.buildConfigField(
            "String",
            "SERVER_API_KEY_OPEN_WEATHER",
            "\"5DqVP9+VVFrVkvP0T/uQg+nMLe0aolqAw1GUhz11K7kuFPM9tV/Uc1+PJnxrsvTj\""
        )

        it.buildConfigField(
            "String",
            "SERVER_API_KEY_FCM",
            "\"p4VULzqDBI5HWbwOKU3zXZQI7+NU4DdKiTQ+qa8HkmZCwpW/SWK78boLuqaZPJkxUnS8IRoVerujUGXSCnfyRnSut4lKBXF1f6JatwPd+EE40LCqECTduZ6WqgFYlvtpAtjs8usgKUuuBDEo/q8sKdhiLE3smCpJ7K2HUvma49uBK1cY7i1Pu3lOxh/nEVQmOMT/2DqZySHx9/R3KtnXtA==\""
        )

        it.buildConfigField(
            "String",
            "SERVER_API_KEY_TMDB",
            "\"K4+f0Ethj2eHySkBTYzGi9DB8QbH4hn2tC6M3ikbnNAhlXXzwmN+H6IpOoAQa+vt\""
        )

        it.buildConfigField(
            "String",
            "SERVER_API_KEY_FLIGHT_AWARE_AERO",
            "\"npbZVG8Yp0vOgxGwNQRuk2f+uG2tLiNPEuWiVgJIEi/NpLZiqdSnlHkf6nHd9i9M\""
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    namespace = "com.riders.thelab.core.data"
}

dependencies {

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:common"))
    implementation(project(":core:testing"))

    ///////////////////////////////////
    // General Dependencies
    ///////////////////////////////////
    // Kotlin
    api(libs.kotlinx.coroutines.android)
    api(libs.androidx.core.ktx)
    api(libs.kotlinx.serialization.json)

    // AndroidX
    api(libs.androidx.appsearch)
    kapt(libs.androidx.appsearch.compiler)
    api(libs.androidx.appsearch.local.storage)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling)

    // Room
    api(libs.room.ktx)
    api(libs.room.runtime)
    api(libs.room.paging)
    ksp(libs.room.compiler)
    androidTestImplementation(libs.room.testing)

    // Worker & concurrent
    androidTestImplementation(libs.androidx.work.testing)

    // Datastore and Preferences
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.preferences)
    api(libs.androidx.preferences)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    /* Retrofit using RxJava3, Okhttp, Okhttp logging interceptor, Moshi, Serialization  */
    api(libs.retrofit.core)
    api(libs.retrofit.moshi)
    api(libs.retrofit.kotlin.serialization)

    // OkHttp
    // define a BOM and its version
    api(platform(libs.okhttp.bom))
    // define any required OkHttp artifacts without version
    api(libs.okhttp)
    api(libs.okhttp.logging)

    // api("org.apache.httpcomponents:httpcore:4.4.15")

    // Moshi
    api(libs.moshi)
    api(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    api(libs.kotools.types)

    /////////////////////////////
    // Tests Dependencies
    /////////////////////////////
    //tesImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))

    androidTestImplementation(libs.androidx.test.truth)
}