import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    id("thelab.android.library")
    id("thelab.android.library.compose")
    id("thelab.android.hilt")
    id("thelab.android.library.jacoco")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.riders.thelab.core.ui"
}

composeCompiler {
    featureFlags.addAll(
        ComposeFeatureFlag.StrongSkipping,
        ComposeFeatureFlag.OptimizeNonSkippingGroups
    )
}

dependencies {

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:analytics"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))


    ///////////////////////////////////
    // General Dependencies
    ///////////////////////////////////
    // Kotlin
    implementation(libs.kotlinx.datetime)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment)
    api(libs.androidx.appcompat)
    api(libs.androidx.palette)
    api(libs.material)
    api(libs.androidx.browser)
    api(libs.androidx.glance.appwidget)
    api(libs.androidx.glance.material3)

    // Accompanist
    api(libs.accompanist.adaptive)
    api(libs.accompanist.drawablepainter)
    api(libs.accompanist.flowlayout)
    api(libs.accompanist.navigation.material)
    api(libs.accompanist.pager)
    api(libs.accompanist.permissions)
    api(libs.accompanist.systemuicontroller)
    api(libs.accompanist.testharness)

    // Compose
    api(libs.androidx.compose.compiler)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.runtime.livedata)
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.fonts)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material.icons)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3.windowSizeClass)
    api(libs.androidx.compose.material)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.android)
    api(libs.androidx.compose.material3.adaptative)
    api(libs.androidx.compose.material3.adaptative.layout)
    api(libs.androidx.compose.material3.adaptative.navigation)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.runtime.livedata)
    api(libs.androidx.compose.runtime.tracing)
    debugApi(libs.androidx.compose.ui.tooling)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.hilt.navigation.compose)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.androidx.lifecycle.viewModelCompose)
    api(libs.androidx.metrics)
    api(libs.androidx.tracing.ktx)

    // Glide
    api(libs.glide)
    ksp(libs.glide.compiler)
    api(libs.glide.compose)
    api(libs.glide.blurry)
    api(libs.glide.transformation)
    api(libs.glide.landscapist)

    // Lottie
    api(libs.lottie)
    api(libs.lottie.compose)

    // Coil
    api(libs.coil.kt)
    api(libs.coil.kt.compose)
    api(libs.coil.kt.svg)

    api(libs.kotools.types)


    /////////////////////////////
    // Tests Dependencies
    /////////////////////////////
    androidTestImplementation(project(":core:testing"))

    androidTestApi(libs.androidx.compose.ui.test)
    androidTestDebugApi(libs.androidx.compose.ui.tooling)
    androidTestDebugApi(libs.androidx.compose.ui.testManifest)
}