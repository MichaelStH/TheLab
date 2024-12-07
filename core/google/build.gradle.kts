plugins {
    id("thelab.android.library")
}

android {
    namespace = "com.riders.thelab.core.google"

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))

    /////////////////////////////
    // General Dependencies
    /////////////////////////////
    // Google Play Services
    api(libs.google.play.services.base)
    api(libs.google.play.services.auth)
    api(libs.google.play.services.drive)
    api(libs.google.play.services.tasks)

    // AndroidX Credentials
    api(libs.androidx.credentials)
    api(libs.androidx.credentials.play.services.auth)
    api(libs.google.identity)

    // Google Api
    api(libs.google.api.client)
    
    //Drive
    api(libs.google.api.drive)
}
