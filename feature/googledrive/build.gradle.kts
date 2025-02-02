plugins {
    alias(libs.plugins.thelab.feature)
    alias(libs.plugins.thelab.library.compose)
    alias(libs.plugins.thelab.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    namespace = "com.riders.thelab.feature.googledrive"
}

configurations.forEach {
    it.exclude(group = "org.apache.httpcomponents", module = "guava-jdk5")
}

dependencies {

    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:analytics"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:google"))
    implementation(project(":core:permissions"))
    implementation(project(":core:ui"))

    ///////////////////////////////////
    // General Dependencies
    ///////////////////////////////////
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.concurrent)
    // Google Play Services
    implementation(libs.google.play.services.base)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.play.services.drive)
    implementation(libs.google.play.services.tasks)

    // AndroidX Credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity)

    // Google Api
    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    // https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client-jetty
    implementation(libs.google.api.client.jetty)
    // https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client-java6
    implementation(libs.google.api.client.oauth.java6)
    implementation(libs.google.api.client.oauth.jackson2)

    // Google Http
    implementation(libs.google.http.client)
    implementation(libs.google.http.client.gson)

    //Drive
    implementation(libs.google.api.drive)

    // Guava
    implementation(libs.google.guava)
    implementation(libs.google.guava.listenablefuture)

}