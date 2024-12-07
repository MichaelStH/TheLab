package com.riders.thelab.feature.googledrive.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.model.File
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import com.riders.thelab.feature.googledrive.BuildConfig
import com.riders.thelab.feature.googledrive.core.google.GoogleSignInManager
import com.riders.thelab.feature.googledrive.data.local.compose.GoogleDriveUiState
import com.riders.thelab.feature.googledrive.data.local.compose.GoogleSignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class GoogleDriveViewModel @Inject constructor() : BaseViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()


    /////////////////////////////////////////////////
    // Variables
    /////////////////////////////////////////////////
    private var mNetworkManager: LabNetworkManager? = null

    /////////////////////////////////////////////////
    // Composable states
    /////////////////////////////////////////////////
    private var _googleDriveUiState: MutableStateFlow<GoogleDriveUiState> =
        MutableStateFlow(GoogleDriveUiState.Loading)
    val googleDriveUiState: StateFlow<GoogleDriveUiState> = _googleDriveUiState.asStateFlow()
    private var _signInState: MutableStateFlow<GoogleSignInState> =
        MutableStateFlow(GoogleSignInState.Disconnected)
    val signInState: StateFlow<GoogleSignInState> = _signInState.asStateFlow()

    private var hasInternetConnection: Boolean by mutableStateOf(BuildConfig.DEBUG)

    private val networkState by lazy { this.mNetworkManager!!.networkState }
    val isConnected: Boolean by derivedStateOf { networkState.value is NetworkState.Available }

    var driveFileList: SnapshotStateList<File> = mutableStateListOf()
    private set

    fun updateGoogleDriveUiState(newState: GoogleDriveUiState) {
        this._googleDriveUiState.value = newState
    }

    fun updateGoogleSignInState(newState: GoogleSignInState) {
        this._signInState.value = newState
    }

    private fun updateHasInternetConnection(hasInternet: Boolean) {
        this.hasInternetConnection = hasInternet
    }


    //////////////////////////////////////////
    // Coroutines
    //////////////////////////////////////////
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        Timber.e("Coroutine Exception caught with message: ${throwable.message} (${throwable.javaClass})")
    }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////

    fun initNetworkManager(activity: GoogleDriveActivity, lifecycle: Lifecycle) {
        Timber.d("initNetworkManager()")

        mNetworkManager = LabNetworkManager
            .getInstance(activity, lifecycle)
            .also { observeNetworkState(it) }
    }


    private fun observeNetworkState(networkManager: LabNetworkManager) {
        Timber.d("observeNetworkState()")

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            networkManager.getNetworkState().collect { networkState ->
                when (networkState) {
                    is NetworkState.Available -> {
                        Timber.d("network state is Available. All set.")
                        updateHasInternetConnection(true)
                    }

                    is NetworkState.Losing -> {
                        Timber.w("network state is Losing. Internet connection about to be lost")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Lost -> {
                        Timber.e("network state is Lost. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Unavailable -> {
                        Timber.e("network state is Unavailable. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Undefined -> {
                        Timber.i("network state is Undefined. Do nothing")
                    }
                }
            }
        }
    }

    fun onEvent(activity: GoogleDriveActivity, uiEvent: UiEvent) {
        Timber.d("onEvent() | event: ${uiEvent.javaClass.simpleName}")

        when (uiEvent) {
            is UiEvent.OnSignIn -> {
                GoogleSignInManager.getInstance(activity).signInLegacy()
            }

            is UiEvent.OnHandleAccount -> {
                updateGoogleSignInState(GoogleSignInState.Connected(uiEvent.account))
            }

            is UiEvent.OnSignOut -> {
                GoogleSignInManager.getInstance(activity)
                    .signOut(
                        activity = activity,
                        onFailure = {
                            Timber.e("onEvent() | message: ${it?.message}")
                        },
                        onSuccess = { isLoggedOut ->
                            if (isLoggedOut) {
                                updateGoogleSignInState(GoogleSignInState.Disconnected)
                            }
                        }
                    )
            }
        }
    }

    fun getGoogleDriveFiles() {
        viewModelScope.launch(coroutineContext + coroutineExceptionHandler) {
        }
    }
}