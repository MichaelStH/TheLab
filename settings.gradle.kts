pluginManagement {

    /**
     * The pluginManagement {repositories {...}} block configures the
     * repositories Gradle uses to search or download the Gradle plugins and
     * their transitive dependencies. Gradle pre-configures support for remote
     * repositories such as JCenter, Maven Central, and Ivy. You can also use
     * local repositories or define your own remote repositories. The CODE below
     * defines the Gradle Plugin Portal, Google's Maven repository,
     * and the Maven Central Repository as the repositories Gradle should use to look for its dependencies.
     */

    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        // Android Build Server
        maven { url = uri("../nowinandroid-prebuilts/m2repository") }
    }
}

dependencyResolutionManagement {

    /**
     * The dependencyResolutionManagement { repositories {...}}* block is where you configure the repositories and dependencies used by
     * all modules in your project, such as libraries that you are using to
     * create your application. However, you should configure module-specific
     * dependencies in each module-level build.gradle.kts file. For new projects,
     * Android Studio includes Google's Maven repository and the
     * Maven Central Repository by
     * default, but it does not configure any dependencies (unless you select a
     * template that requires some).
     */

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        // Android Build Server
        maven { url = uri("../nowinandroid-prebuilts/m2repository") }
    }
}

rootProject.name = "The Lab"
include(":app")
include(":core:analytics")
include(":core:camera")
include(":core:common")
include(":core:data")
include(":core:google")
include(":core:permissions")
include(":core:player")
include(":core:speechtotext")
include(":core:ui")
include(":core:testing")
include(":feature:artists")
include(":feature:biometric")
include(":feature:bluetooth")
include(":feature:colors")
include(":feature:download")
include(":feature:flightaware")
include(":feature:googledrive")
include(":feature:kat")
include(":feature:locationonmaps")
include(":feature:lottie")
include(":feature:mlkit")
include(":feature:musicrecognition")
include(":feature:palette")
include(":feature:schedule")
include(":feature:settings")
include(":feature:songplayer")
include(":feature:streaming")
include(":feature:tabs")
include(":feature:theaters")
include(":feature:transitions")
include(":feature:weather")
include(":feature:youtube")
//include(":shazam-kit")
include(":spotify-app-remote")
include(":spotify-auth")
include(":spotify-auth-store")
