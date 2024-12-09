import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.riders.thelab.configureKotlinAndroid
import com.riders.thelab.configurePrintApksTask
import com.riders.thelab.configureTimber
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-parcelize")
                apply("kotlinx-serialization")
                apply("com.google.devtools.ksp")
            }

            // Configure Jvm ToolChain
            with(kotlinExtension) {
                jvmToolchain(21)
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = AndroidConfiguration.Sdk.TARGET

                defaultConfig.versionCode = AndroidConfiguration.Application.CODE
                defaultConfig.versionName = AndroidConfiguration.Application.version.toString()
                // configureFlavors(this)
                configureTimber()
            }
            extensions.configure<ApplicationAndroidComponentsExtension> {
                configurePrintApksTask(this)
            }
        }
    }
}