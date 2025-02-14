package com.riders.thelab.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.riders.thelab.feature.settings.main.SettingsActivity
import com.riders.thelab.feature.weather.ui.WeatherActivity
import com.riders.thelab.ui.contacts.addcontact.AddContactActivity
import com.riders.thelab.ui.login.LoginActivity
import com.riders.thelab.ui.mainactivity.MainActivity
import com.riders.thelab.ui.signup.SignUpActivity
import com.riders.thelab.ui.splashscreen.SplashScreenActivity
import com.riders.thelab.ui.vocalassistant.VocalAssistantActivity
import timber.log.Timber

class Navigator(private val activity: Activity) {

    /* Activities */
    fun callSplashActivity() {
        activity.startActivity(Intent(activity, SplashScreenActivity::class.java))
    }

    fun callLoginActivity() {
        activity.startActivity(Intent(activity, LoginActivity::class.java))
    }

    fun callLoginActivity(intent: Intent, optionsCompat: Bundle) {
        Timber.d("Apply login activity transition")
        activity.startActivity(intent, optionsCompat)
    }

    fun callSignUpActivity() {
        activity.startActivity(Intent(activity, SignUpActivity::class.java))
    }

    fun callSignUpActivity(intent: Intent, optionsCompat: Bundle) {
        Timber.d("Apply login activity transition")
        activity.startActivity(intent, optionsCompat)
    }

    fun callMainActivity() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
    }

    fun callSettingsActivity() {
        activity.startActivity(
            Intent(
                activity,
                SettingsActivity::class.java
            )
        )
    }

    /* Detail Activities */
    fun callContactDetailActivity(intent: Intent) {
        activity.startActivity(intent)
    }

    fun callAddContactActivity() {
        activity.startActivity(Intent(activity, AddContactActivity::class.java))
    }

    fun callMultipaneDetailActivity(intent: Intent) {
        activity.startActivity(intent)
    }

    fun callYoutubeDetailActivity(intent: Intent, optionsCompat: Bundle) {
        Timber.d("Apply activity transition")
        activity.startActivity(intent, optionsCompat)
    }

    fun callWeatherActivity() {
        activity.startActivity(Intent(activity, WeatherActivity::class.java))
    }

    /* Activities */
    fun callIntentActivity(targetClass: Class<out Activity?>?) {
        if (null == targetClass) {
            Timber.e("target class is null")
            return
        } else {
            activity.startActivity(Intent(activity, targetClass))
        }
    }

    /* Packages */
    fun callIntentForPackageActivity(intentPackageName: String) {
        activity.startActivity(activity.packageManager.getLaunchIntentForPackage(intentPackageName))
    }
companion object {

    fun callVoiceAssistantActivity(context: Context) {
        val intent = Intent(context, VocalAssistantActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

}