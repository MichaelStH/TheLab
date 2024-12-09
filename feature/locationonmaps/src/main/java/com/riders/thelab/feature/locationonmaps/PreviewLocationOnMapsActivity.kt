package com.riders.thelab.feature.locationonmaps

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import com.riders.thelab.core.common.utils.toLocation
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import kotlinx.coroutines.launch
import timber.log.Timber


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationOnMapsContent(
    location: Location,
    isSearchPlaceVisible: Boolean,
    uiEvent: (UiEvent) -> Unit,
    mapUiEvent: (GoogleMapUiEvent) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    var mainBoxCoordinates: Size? by remember { mutableStateOf(Size.Zero) }

    var markerTitle: String? by remember { mutableStateOf(null) }
    var markerSnippet: String? by remember { mutableStateOf(null) }

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                zoomGesturesEnabled = true,
                rotationGesturesEnabled = true,
                myLocationButtonEnabled = true
            )
        )
    }
    val properties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = true,
                isTrafficEnabled = false
            )
        )
    }
//    val location = (1.35 to 103.87).toLocation()
    val userPosition = LatLng(location.latitude, location.longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userPosition, 10f)
    }

    TheLabTheme {
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetShape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
            sheetContent = {
                PlaceBottomSheetContent(
                    type = PlaceBottomSheetType.POI,
                    userPosition = userPosition,
                    isHalfExpanded = scaffoldState.bottomSheetState.hasPartiallyExpandedState,
                    onCloseBottomSheet = {
                        if (scaffoldState.bottomSheetState.isVisible) scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    uiEvent = uiEvent
                )
            },
            sheetSwipeEnabled = true
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .onGloballyPositioned { coordinates ->
                        mainBoxCoordinates = coordinates.size.toSize()
                    }
            ) {
                BoxWithConstraints(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleMap(
                        modifier = Modifier
                            .zIndex(0f)
                            .size(width = this.maxWidth, height = this.maxHeight),
                        properties = properties,
                        uiSettings = uiSettings,
                        location = location,
                        myLocationButtonPosition = this.maxHeight - 140.dp,
                        markerTitle = markerTitle,
                        markerSnippet = markerSnippet,
                        mapUiEvent = mapUiEvent,
                        onMapLoaded = { Timber.d("onMapLoaded") }
                    )
                }

                DisappearingScaleBar(
                    modifier = Modifier
                        .zIndex(2f)
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp)
                        .align(Alignment.TopStart),
                    cameraPositionState = cameraPositionState
                )

                Switch(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 16.dp)
                        .align(Alignment.TopEnd),
                    checked = uiSettings.zoomControlsEnabled,
                    onCheckedChange = {
                        uiSettings = uiSettings.copy(zoomControlsEnabled = it)
                    }
                )

                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .zIndex(5f),
                    visible = isSearchPlaceVisible,
                    enter = slideInVertically() + fadeIn(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    PlacesCard(
                        uiEvent = {
                            when (it) {
                                is UiEvent.OnPlaceSelected -> {
                                    @Suppress("DEPRECATION")
                                    markerTitle = it.place.name?.ifBlank { null }
                                    @Suppress("DEPRECATION")
                                    markerSnippet = it.place.name?.ifBlank { null }
                                }

                                else -> {
                                    // do nothing
                                    Timber.e("Recomposition | uiEvent | else branch")
                                }
                            }

                            uiEvent.invoke(it)
                        }
                    )
                }
            }
        }
    }
}


///////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewLocationOnMapsContent() {
    TheLabTheme {
        LocationOnMapsContent(
            location = (1.35 to 103.87).toLocation(),
            isSearchPlaceVisible = true,
            uiEvent = {},
            mapUiEvent = {}
        )
    }
}