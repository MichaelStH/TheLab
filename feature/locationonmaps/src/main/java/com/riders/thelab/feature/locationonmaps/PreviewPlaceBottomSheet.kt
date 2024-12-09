package com.riders.thelab.feature.locationonmaps

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.StreetViewPanoramaOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.compose.streetview.StreetView
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PlaceBottomSheetContent(
    type: PlaceBottomSheetType,
    place: Place? = null,
    userPosition: LatLng,
    isHalfExpanded: Boolean,
    onCloseBottomSheet: () -> Unit,
    uiEvent: (UiEvent) -> Unit,
) {
    // TODO : Add Shimmer effect when place object is null
    TheLabTheme {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Address & Postal Code
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Address
                        Text(text = "Street name")
                        // Postal code
                        Text(text = "94320 Thiais")
                    }

                    // Street View content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(1.dp, 195.dp), contentAlignment = Alignment.Center
                    ) {
                        StreetView(
                            modifier = Modifier.align(Alignment.TopCenter),
                            streetViewPanoramaOptionsFactory = {
                                StreetViewPanoramaOptions().position(userPosition)
                            },
                            isPanningGesturesEnabled = !isHalfExpanded,
                            isStreetNamesEnabled = !isHalfExpanded,
                            isUserNavigationEnabled = !isHalfExpanded,
                            isZoomGesturesEnabled = !isHalfExpanded
                        )
                    }
                }

                // Close Icon
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd),
                    onClick = onCloseBottomSheet,
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5334f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp)
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
private fun PreviewPlaceBottomSheetContent() {
    TheLabTheme {
        PlaceBottomSheetContent(
            type = PlaceBottomSheetType.SEARCH_LOCATION,
            userPosition = LatLng(1.35, 103.87),
            isHalfExpanded = true,
            onCloseBottomSheet = {}
        ) {}
    }
}