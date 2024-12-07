package com.riders.thelab.feature.googledrive.ui

import android.annotation.SuppressLint
import android.content.IntentSender
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.internal.OnConnectionFailedListener
import com.google.api.services.drive.model.FileList
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.data.local.model.Permission
import com.riders.thelab.core.permissions.PermissionManager
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.googledrive.base.BaseGoogleActivity
import com.riders.thelab.feature.googledrive.core.google.GoogleDriveManager
import com.riders.thelab.feature.googledrive.core.google.GooglePlayServicesManager
import com.riders.thelab.feature.googledrive.core.google.GoogleSignInManager
import com.riders.thelab.feature.googledrive.data.local.compose.GoogleDriveUiState
import com.riders.thelab.feature.googledrive.data.local.compose.GoogleSignInState
import com.riders.thelab.feature.googledrive.utils.toGoogleAccountModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class GoogleDriveActivity : BaseGoogleActivity(), OnConnectionFailedListener {

    private val mViewModel: GoogleDriveViewModel by viewModels<GoogleDriveViewModel>()

    private val mGoogleApiAvailability: GoogleApiAvailability by lazy {
        GoogleApiAvailability.getInstance()
    }

    /////////////////////////////////////
    //
    // OVERRIDE
    //
    /////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate()")

        mViewModel.initNetworkManager(this@GoogleDriveActivity, this.lifecycle)

        if (!GooglePlayServicesManager.checkPlayServices(
                activity = this@GoogleDriveActivity,
                googleApiAvailability = mGoogleApiAvailability
            )
        ) {
            mViewModel.updateGoogleDriveUiState(GoogleDriveUiState.GooglePlayServicesUnavailable)
        }

        checkPermissions()

        lifecycleScope.launch {
            Timber.d("coroutine launch with name ${this.coroutineContext}")
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                setContent {

                    val uiState: GoogleDriveUiState by mViewModel.googleDriveUiState.collectAsStateWithLifecycle()
                    val signInState: GoogleSignInState by mViewModel.signInState.collectAsStateWithLifecycle()

                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            GoogleDriveContent(
                                uiState = uiState,
                                signInState = signInState,
                                hasInternetConnection = mViewModel.isConnected,
                                driveFileList = mViewModel.driveFileList
                            ) { event -> mViewModel.onEvent(this@GoogleDriveActivity, event) }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume()")

        val signInManager: GoogleSignInManager = GoogleSignInManager.getInstance(this)
        val driveManager: GoogleDriveManager = GoogleDriveManager.getInstance(this)

        if (signInManager.isUserSignedInLegacy()) {
            signInManager.mLastGoogleAccount?.let { account ->
                mViewModel.updateGoogleSignInState(
                    GoogleSignInState.Connected(
                        account
                            .toGoogleAccountModel()
                            .also { Timber.d("signIn() | account: $it") }
                    )
                )
            }

            driveManager
                .getDriveClientLegacy(signInManager.mLastGoogleAccount!!)
                .apply {
                    Timber.d("onResume() | drive client: ${this.toString()}")
                }

            driveManager.getDrivesFoldersLegacy()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                GoogleDriveManager.getInstance(this@GoogleDriveActivity)
                    .queryFiles()
                    ?.addOnFailureListener(this@GoogleDriveActivity) { throwable ->
                        Timber.e("task | addOnFailureListener | message: ${throwable.message} (class: ${throwable::class.java.canonicalName})")
                    }
                    ?.addOnSuccessListener(this@GoogleDriveActivity) {
                        Timber.d("task | addOnSuccessListener | value: $it")
                    }
                    ?.addOnCompleteListener(this@GoogleDriveActivity) { task ->
                        if (!task.isSuccessful) {
                            Timber.e("task | addOnCompleteListener | failed")
                        } else {
                            Timber.i("task | addOnCompleteListener | successful, result: ${task.result}")
                            val filesList: FileList = task.result.also { Timber.d("filesList size : ${it.size}") }
                        }
                    }
            }
                .onFailure {
                    it.printStackTrace()
                    Timber.e("runCatching | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.canonicalName})")
                }

        }
    }

    override fun backPressed() {
        finish()
    }


    /////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////
    @SuppressLint("NewApi")
    private fun checkPermissions() {
        Timber.e("checkPermissions()")

        PermissionManager
            .from(this@GoogleDriveActivity)
            .request(
                Permission.UserAccounts,
                if (!LabCompatibilityManager.isTiramisu()) Permission.Storage else Permission.MediaLocationAndroid13
            )
            .checkPermission { permissionGranted ->
                if (!permissionGranted) {
                    Timber.e("Storage permissions not granted")
                } else {
                    Timber.i("Storage permissions granted")

                    mViewModel.updateGoogleDriveUiState(GoogleDriveUiState.Success)
                }
            }
    }


    /////////////////////////////////////
    //
    // IMPLEMENTS
    //
    /////////////////////////////////////
    override fun onConnectionFailed(result: ConnectionResult) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // Called whenever the API client fails to connect.
        Timber.e("onConnectionFailed() | GoogleApiClient connection failed: %s", result.toString())

        if (!result.hasResolution()) {

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0)
                ?.show()
            return
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */
        try {

            //result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (e: IntentSender.SendIntentException) {

            e.printStackTrace()
            Timber.e("Exception while starting resolution activity")
        }
    }
}