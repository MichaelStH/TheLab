package com.riders.thelab.ui.login

import android.annotation.SuppressLint
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
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.GoogleApiAvailability
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.data.local.model.compose.WindowSizeClass
import com.riders.thelab.core.google.BaseGoogleActivity
import com.riders.thelab.core.google.GooglePlayServicesManager
import com.riders.thelab.core.google.GoogleSignInManager
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.navigator.Navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : BaseGoogleActivity() {

    //TODO : Due to Heroku back-end free services ending,
    // Use of the database to store and log users
    private val mViewModel: LoginViewModel by viewModels()

    private var mLabNetworkManager: LabNetworkManager? = null
    private var mNavigator: Navigator? = null

    private var windowSize: WindowSizeClass? = null

    private val mGoogleApiAvailability: GoogleApiAvailability by lazy {
        GoogleApiAvailability.getInstance()
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLabNetworkManager = LabNetworkManager
            .getInstance(this, lifecycle)
            .also { mViewModel.observeNetworkState(it) }

        mNavigator = Navigator(this)

        mViewModel.retrieveAppVersion(this@LoginActivity)

        // Start a coroutine in the lifecycle scope
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                computeWindowSizeClasses()

                setContent {
                    val loginUiState by mViewModel.loginUiState.collectAsStateWithLifecycle()
                    val loginFieldState by mViewModel.loginFieldUiState.collectAsStateWithLifecycle()
                    val loginHasError by mViewModel.loginHasError.collectAsStateWithLifecycle()
                    val passwordFieldState by mViewModel.passwordFieldUiState.collectAsStateWithLifecycle()

                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LoginContent(
                                version = mViewModel.version,
                                loginUiState = loginUiState,
                                loginFieldState = loginFieldState,
                                login = mViewModel.login,
                                loginHasError = loginHasError,
                                loginHasLocalError = mViewModel.loginHasLocalError,
                                passwordFieldState = passwordFieldState,
                                password = mViewModel.password,
                                isRememberCredentialsChecked = mViewModel.isRememberCredentials,
                                uiEvent = { event ->
                                    when (event) {
                                        is UiEvent.OnSignUpClicked -> mNavigator?.callSignUpActivity()
                                        is UiEvent.OnGoogleButtonLoginClicked -> authenticateWithGoogle()
                                        is UiEvent.OnLaunchMainActivity -> {
                                            mNavigator?.callMainActivity()
                                            this@LoginActivity.finish()
                                        }

                                        else -> mViewModel.onEvent(event)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (mViewModel.hasInternetConnection) {
                    UIManager.showConnectionStatusInSnackBar(
                        this@LoginActivity,
                        true
                    )
                }
            }
        }

        mNavigator?.let { mViewModel.isGoogleUserLogged(it) }
    }

    override fun onResume() {
        super.onResume()

        if (!GooglePlayServicesManager.checkPlayServices(
                activity = this@LoginActivity,
                googleApiAvailability = mGoogleApiAvailability
            )
        ) {
            Timber.e("Play services are NOT available")
        }
    }

    override fun backPressed() {
        Timber.e("backPressed()")

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("onDestroy()")
    }

    /////////////////////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////////////////////
    private fun computeWindowSizeClasses() {
        Timber.d("computeWindowSizeClasses()")

        val metrics = WindowMetricsCalculator
            .getOrCreate()
            .computeCurrentWindowMetrics(this)

        val widthDp = metrics.bounds.width() /
                resources.displayMetrics.density
        val widthWindowSizeClass = when {
            widthDp < 600f -> WindowSizeClass.COMPACT
            widthDp < 840f -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }

        Timber.i("widthWindowSizeClass: $widthWindowSizeClass")

        val heightDp = metrics.bounds.height() /
                resources.displayMetrics.density
        val heightWindowSizeClass = when {
            heightDp < 480f -> WindowSizeClass.COMPACT
            heightDp < 900f -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
        Timber.i("heightWindowSizeClass: $heightWindowSizeClass")

        // Use widthWindowSizeClass and heightWindowSizeClass.
        windowSize = widthWindowSizeClass
    }

    fun getDeviceWindowsSizeClass(): WindowSizeClass {
        Timber.d("getDeviceWindowsSizeClass()")
        return windowSize!!
    }

    private fun authenticateWithGoogle() {
        Timber.d("authenticateWithGoogle()")

        val signInManager = GoogleSignInManager.getInstance(this@LoginActivity)

        // Check if the user is already signed in.
        if (signInManager.isUserSignedInLegacy()) {
            signInManager.mLastGoogleAccount?.let {
                UIManager.showToast(
                    this@LoginActivity,
                    "User is already signed in with : ${it.email}"
                )
                mNavigator?.callMainActivity()
            }
        }

        // Start the sign-in process.
        signInManager.signInLegacy()
    }

    override fun onConnected(account: GoogleSignInAccount) {
        Timber.d("onConnected()")
        mNavigator?.let { navigator -> mViewModel.updateGoogleUser(navigator, account) }
    }

    override fun onDisconnected() {
        Timber.e("onDisconnected()")
    }
}