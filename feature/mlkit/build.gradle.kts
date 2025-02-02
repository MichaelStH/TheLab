plugins {
    id("thelab.android.feature")
    id("thelab.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.riders.thelab.feature.mlkit"
}

dependencies {

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:permissions"))
    implementation(project(":core:ui"))


    ///////////////////////////////////
    // General Dependencies
    ///////////////////////////////////
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.camera)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.google.guava)
    
    // ML Kit
    implementation(libs.google.mlkit.barcode.scanning)
    implementation(libs.google.mlkit.objectdetection)
    implementation(libs.google.mlkit.objectdetection.custom)

    implementation(libs.volley)
}