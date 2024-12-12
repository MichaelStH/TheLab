package com.riders.thelab.feature.locationonmaps

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.riders.thelab.core.common.utils.LabLocationManager
import com.riders.thelab.core.common.utils.toLocation
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.component.loading.LabLoader
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LocationOnMapsActivity : BaseComponentActivity() {

    private val mViewModel: LocationOnMapsViewModel by viewModels<LocationOnMapsViewModel>()

    private val mLocationManager: LabLocationManager by lazy {
        LabLocationManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLocationManager.setLocationListener()
        getCurrentLocation()

        mViewModel.initPlaces()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                setContent {

                    val location by mLocationManager.locationState.collectAsStateWithLifecycle()

                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            if (null == location) {
                                LabLoader(modifier = Modifier.size(30.dp))
                            } else {
                                LocationOnMapsContent(
                                    location = location!!,
                                    isSearchPlaceVisible = mViewModel.isSearchPlaceVisible,
                                    uiEvent = { event ->
                                        when (event) {
                                            is UiEvent.OnPlaceSelected -> {
                                                event.place.location?.let {
                                                    Timber.d("Recomposition | UiEvent.OnPlaceSelected | ${it.latitude}, ${it.longitude}")
                                                    mLocationManager.updateLocationState((it.latitude to it.longitude).toLocation())
                                                }
                                            }

                                            else -> mViewModel.onEvent(event)
                                        }
                                    },
                                    mapUiEvent = { mapEvent ->
                                        when (mapEvent) {
                                            is GoogleMapUiEvent.OnMyLocationButtonClick -> getCurrentLocation()
                                            else -> mViewModel.onGoogleMapEvent(mapEvent)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun backPressed() {
        Timber.e("onBackPressed()")
        finish()
    }

    fun getCurrentLocation() {
        if (mLocationManager.canGetLocation()) {
            runOnUiThread { mLocationManager.getCurrentLocation() }
        }
    }
}