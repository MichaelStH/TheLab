plugins {
    /**
     * You should use `apply false` in the top-level build.gradle file
     * to add a Gradle plugin as a build dependency, but not apply it to the
     * current (root) project. You should not use `apply false` in sub-projects.
     * For more information, see
     * Applying external plugins with same version to subprojects.
     */

    /**
     * Defined in build-logic/convention/build.gradle.kts class
     */
    id("thelab.android.application")
    id("thelab.android.application.compose")
    id("thelab.android.application.jacoco")
    id("thelab.android.hilt")
    id("jacoco")
    id("thelab.firebase")
    alias(libs.plugins.ksp)
}

android {

    /**
     * Defined in AndroidApplicationConventionPlugin class
     */
    // compileSdk = ConfigData.compileSdkVersion

    ndkVersion = "21.3.6528147"

    defaultConfig {
        applicationId = "com.riders.thelab"

        /**
         * Defined in AndroidApplicationConventionPlugin class
         */
        /*minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
        versionCode = ConfigData.versionCode
        versionName = ConfigData.versionName*/

        // Enabling multidex support.
        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true

            // Enables CODE shrinking, obfuscation, and optimization for only
            // your project's release build type.
            isMinifyEnabled = false

            //applicationIdSuffix = LabBuildType.DEBUG.applicationIdSuffix
        }

        getByName("release") {
            isDebuggable = false

            // Enables CODE shrinking, obfuscation, and optimization for only
            // your project's release build type.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true  // Enables CODE shrinking for the release build type.

            // Disables PNG crunching for the "release" build type.
            isCrunchPngs = false

            // Enables CODE shrinking, obfuscation, and optimization for only
            // your project's release build type.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //applicationIdSuffix = LabBuildType.RELEASE.applicationIdSuffix
        }
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        // Determines whether to support Data Binding.
        // Note that the dataBinding.enabled property is now deprecated.
        dataBinding = true
    }

    lint {
        abortOnError = false
        ignoreWarnings = true
        quiet = true
    }

    namespace = "com.riders.thelab"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:analytics"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:permissions"))
    implementation(project(":core:ui"))
    implementation(project(":core:testing"))
    implementation(project(":feature:biometric"))
    implementation(project(":feature:bluetooth"))
    implementation(project(":feature:colors"))
    implementation(project(":feature:download"))
    implementation(project(":feature:kat"))
    implementation(project(":feature:lottie"))
    implementation(project(":feature:musicrecognition"))
    implementation(project(":feature:palette"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:songplayer"))
    implementation(project(":feature:streaming"))
    implementation(project(":feature:tabs"))
    implementation(project(":feature:theaters"))
    implementation(project(":feature:weather"))

    /////////////////////////////
    // General Dependencies
    /////////////////////////////
    //Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlin.parcelize)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.play.services)

    // AndroidX
    implementation(libs.androidx.startup)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.material)
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.window.manager)
    implementation(libs.androidx.window.extensions)

    // Compose: provided by ui module
    // The others dependencies has been added into the AndroidCompose plugin convention class

    // CameraX
    implementation(libs.androidx.camera)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // Media
    implementation(libs.androidx.media)
    implementation(libs.androidx.media.router)
    implementation(libs.androidx.media.exoplayer)
    implementation(libs.androidx.media.ui)

    // Auto fill
    implementation(libs.androidx.autofill)

    // Worker & concurrent: implemented in weather feature

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewModel.ktx)
    implementation(libs.androidx.lifecycle.viewModel.savedState)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    kapt(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.process)

    // Datastore and Preferences: provided by data module

    // Firebase BOM and Dependencies: provided by analytics module

    /* Hilt - We are going to use hilt.android which includes
     * support for Activity and fragment injection so we need to include
     * the following dependencies */
    // Hilt
    // The others dependencies has been added into the Hilt plugin convention class
    implementation(libs.hilt.ext.work)
    ksp(libs.hilt.ext.compiler)

    // Google Location (Maps / Places)
    implementation(libs.maps)
    implementation(libs.maps.utils)
    implementation(libs.location)
    implementation(libs.places)

    // Google API
    implementation(libs.google.play.services.base)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.api.drive)
    implementation(libs.google.http.client)
    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    // https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client-jetty
    implementation(libs.google.api.client.jetty)
    // https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client-java6
    implementation(libs.google.api.client.oauth.java6)
    implementation(libs.google.api.client.oauth.jackson2)

    // Google ML Kit
    implementation(libs.google.barcode.scanning)
    implementation(libs.google.objectdetection)
    implementation(libs.google.objectdetection.custom)

    /* Retrofit using RxJava3, Okhttp, Okhttp logging interceptor, Moshi  */
    // Retrofit: provided by data module

    // OkHttp: provided by data module

    // Moshi: provided by data module
    ksp(libs.moshi.kotlin.codegen)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.glide.blurry)
    implementation(libs.glide.transformation)
    implementation(libs.glide.landscapist)

    //ThreeTen : Alternative to Android Calendar API
    implementation(libs.threeten)

    // Kotools Types: provided by data module


    /////////////////////////////
    // Tests Dependencies
    /////////////////////////////
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.mockito)
    androidTestImplementation(libs.mockito.android)
    /*
        androidTestImplementation(libs.androidx.compose.ui.test)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.testManifest)*/

    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    kaptAndroidTest(libs.hilt.ext.compiler)
}

kapt {
    correctErrorTypes = true
}

// Aggregating Task
// The Hilt Gradle plugin offers an option for performing Hilt’s classpath aggregation in a dedicated Gradle task.
hilt {
    enableAggregatingTask = true
}

tasks.named("build") {
    doLast {
        print(rootProject.version)
    }
}