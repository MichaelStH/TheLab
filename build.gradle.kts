// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    // Compose Compiler comes with Kotlin 2.0
    alias(libs.plugins.compose.compiler) apply false
    // Kotlin
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Hilt
    alias(libs.plugins.hilt) apply false
    // Google
    alias(libs.plugins.playservices) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.ksp) apply false
    // Firebase
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.performances) apply false
    // Protobuf
    alias(libs.plugins.protobuf) apply false
}

tasks.register("clean", Delete::class) {
    delete("$projectDir/build")
}


tasks.register("printVersionName") {
    doLast {
        print("--> tasks.register printVersionName: ${rootProject.version}")
        rootProject.version
    }
}