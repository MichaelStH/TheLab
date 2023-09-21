// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.secrets) apply false
    // Google Services
    alias(libs.plugins.playservices) apply false
    //  Crashlytics Gradle plugin
    alias(libs.plugins.crashlytics) apply false
    // Performance Monitoring plugin
    alias(libs.plugins.performances) apply false
    // Navigation
    alias(libs.plugins.navigation) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}


tasks.register("printVersionName") {
    doLast {
        print(rootProject.version)
        rootProject.version
    }
}