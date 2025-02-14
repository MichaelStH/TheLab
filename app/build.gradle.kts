import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

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
    alias(libs.plugins.thelab.application)
    alias(libs.plugins.thelab.application.compose)
    alias(libs.plugins.thelab.application.jacoco)
    alias(libs.plugins.thelab.hilt)
    alias(libs.plugins.thelab.firebase)
    id("jacoco")
}

/**
 * Log events in console
 *
 * @param tag
 * @param message
 * @return
 */
fun log(tag: String, message: String) {
    println("---> KotlinDSL script logs | $tag | $message")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
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

    /*
    * https://stackoverflow.com/questions/50792428/how-to-access-variant-outputfilename-in-kotlin
    */
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val printFileName = "${applicationId}_${versionName}_${variant.name}.apk"
                val fileName = "${applicationId}_$versionName.apk"
                log("Application Variants | output", "filename: $printFileName")
                output.outputFileName = fileName
            }
    }
    namespace = "com.riders.thelab"
}

composeCompiler {
    featureFlags.addAll(
        ComposeFeatureFlag.StrongSkipping,
        ComposeFeatureFlag.IntrinsicRemember
    )
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:analytics"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:google"))
    implementation(project(":core:permissions"))
    implementation(project(":core:player"))
    implementation(project(":core:ui"))
    implementation(project(":core:testing"))
    implementation(project(":feature:artists"))
    implementation(project(":feature:biometric"))
    implementation(project(":feature:bluetooth"))
    implementation(project(":feature:colors"))
    implementation(project(":feature:download"))
    implementation(project(":feature:flightaware"))
    implementation(project(":feature:googledrive"))
    implementation(project(":feature:kat"))
    implementation(project(":feature:locationonmaps"))
    implementation(project(":feature:lottie"))
    implementation(project(":feature:lottie"))
    implementation(project(":feature:mlkit"))
    implementation(project(":feature:musicrecognition"))
    implementation(project(":feature:palette"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:songplayer"))
    implementation(project(":feature:streaming"))
    implementation(project(":feature:tabs"))
    implementation(project(":feature:theaters"))
    implementation(project(":feature:transitions"))
    implementation(project(":feature:weather"))
    implementation(project(":feature:youtube"))

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
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.window.manager)
    implementation(libs.androidx.window.extensions)
    implementation(libs.material)

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
    ksp(libs.androidx.lifecycle.compiler)
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
    implementation(libs.google.mlkit.barcode.scanning)
    implementation(libs.google.mlkit.objectdetection)
    implementation(libs.google.mlkit.objectdetection.custom)

    /* Retrofit using RxJava3, Okhttp, Okhttp logging interceptor, Moshi  */
    // Retrofit: provided by data module

    // OkHttp: provided by data module

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.glide.blurry)
    implementation(libs.glide.transformation)
    implementation(libs.glide.landscapist)

    //ThreeTen : Alternative to Android Calendar API
    implementation(libs.threeten)

    // Kotools Types: provided by data module

    // Fast Fourier Transform (FFT)
    implementation(libs.jtransforms)


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
    kspAndroidTest(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.ext.compiler)
}

// Aggregating Task
// The Hilt Gradle plugin offers an option for performing Hilt’s classpath aggregation in a dedicated Gradle task.
hilt {
    enableAggregatingTask = true
}

/*
 * Use incremental compilation
 */
tasks.withType<JavaCompile>().configureEach {
    options.isIncremental = true
}

/*
 * Disable reports
 * Gradle automatically creates test reports regardless of whether you want to look at them. That report generation slows down the overall build. You may not need reports if:
 *      you only care if the tests succeeded (rather than why)
 *      you use build scans, which provide more information than a local report
 */
tasks.withType<Test>().configureEach {
    reports.html.required = false
    reports.junitXml.required = false
}

tasks.named("build") {
    doLast {
        print(rootProject.version)
    }
}