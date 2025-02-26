plugins {
    alias(libs.plugins.thelab.library)
    alias(libs.plugins.protobuf)
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

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media)
    implementation(libs.androidx.media.common)

    // Google Cloud Speech API
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.texttospeech)
    implementation(libs.google.cloud.speech)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.stub)
    implementation(libs.jflac.codec)

    // Protobuf
    implementation(libs.protobuf.protoc)
    implementation(libs.protobuf.kotlin.lite)


    /////////////////////////////
    // Tests Dependencies
    /////////////////////////////
    androidTestImplementation(project(":core:testing"))
}