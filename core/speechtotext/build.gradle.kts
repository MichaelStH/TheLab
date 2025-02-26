plugins {
    alias(libs.plugins.thelab.library)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    namespace = "com.riders.thelab.core.speechtotext"
}

dependencies {
    ///////////////////////////////////
    // Project
    ///////////////////////////////////
    implementation(project(":core:common"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.google.cloud.texttospeech)

    /////////////////////////////
    // Tests Dependencies
    /////////////////////////////
    androidTestImplementation(project(":core:testing"))
}