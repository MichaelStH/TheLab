package com.riders.thelab.feature.settings.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.riders.thelab.core.google.BaseGoogleActivity
import com.riders.thelab.core.google.GoogleSignInManager
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.settings.profile.UserProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SettingsActivity : BaseGoogleActivity() {

    private val mViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate()")

        mViewModel.intWeakReference(this@SettingsActivity)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                setContent {
                    val deviceInformationUiState by mViewModel.deviceInformationUiState.collectAsStateWithLifecycle()
                    val userUiState by mViewModel.userUiState.collectAsStateWithLifecycle()

                    TheLabTheme(darkTheme = mViewModel.isDarkMode) {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            SettingsContent(
                                deviceInformationUiState = deviceInformationUiState,
                                userUiState = userUiState,
                                version = mViewModel.version,
                                themeOptions = mViewModel.themeOptions,
                                isDarkMode = mViewModel.isDarkMode,
                                isVibration = mViewModel.isVibration,
                                isActivitiesSplashEnabled = mViewModel.isActivitiesSplashEnabled,
                                showModeInfo = mViewModel.showMoreInfoOnDevice,
                                uiEvent = mViewModel::onEvent
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        mViewModel.retrieveAppVersion(this@SettingsActivity)
        mViewModel.fetchDeviceInformation()
        mViewModel.getLoggedUser()
    }

    override fun backPressed() {
        Timber.e("backPressed()")
        finish()
    }

    fun launchEditProfileActivity() =
        Intent(this, UserProfileActivity::class.java).run { startActivity(this) }

    fun signOut() {
        GoogleSignInManager.getInstance(this)
            .signOut(
                activity = this,
                onSuccess = { loggedOut -> Timber.i("signOut() | loggedOut: $loggedOut") },
                onFailure = { throwable -> Timber.e("signOut() | throwable: $throwable") }
            )
    }

    override fun onConnected(account: GoogleSignInAccount) {
    }

    override fun onDisconnected() {
        Timber.e("onDisconnected()")
    }
}