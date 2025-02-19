package com.riders.thelab.feature.flightaware.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

open class BaseFlightViewModel: BaseViewModel() {
    //////////////////////////////////////////
    // Composable states
    //////////////////////////////////////////

    // Network State
    var hasInternetConnection: Boolean by mutableStateOf(false)

    private fun updateHasInternetConnection(hasInternet: Boolean) {
        this.hasInternetConnection = hasInternet
    }

    /////////////////////////////////////
    // Coroutine
    /////////////////////////////////////
    private val mNetworkCoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Timber.e("mNetworkCoroutineExceptionHandler | Error caught with message: ${throwable.message}")
        }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    fun observeNetworkState(networkManager: LabNetworkManager) {
        viewModelScope.launch(Dispatchers.IO + mNetworkCoroutineExceptionHandler) {
            networkManager.getNetworkState().collect { networkState ->
                when (networkState) {
                    is NetworkState.Available -> {
                        Timber.d("observeNetworkState() | network state is Available. All set.")
                        updateHasInternetConnection(true)
                    }

                    is NetworkState.Losing -> {
                        Timber.w("observeNetworkState() | network state is Losing. Internet connection about to be lost")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Lost -> {
                        Timber.e("observeNetworkState() | network state is Lost. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Unavailable -> {
                        Timber.e("observeNetworkState() | network state is Unavailable. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Undefined -> {
                        Timber.i("observeNetworkState() | network state is Undefined. Do nothing")
                    }
                }
            }
        }
    }
}