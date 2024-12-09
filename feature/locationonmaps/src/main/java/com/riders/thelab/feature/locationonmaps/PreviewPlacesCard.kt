package com.riders.thelab.feature.locationonmaps

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import kotlinx.coroutines.launch
import timber.log.Timber


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@Composable
fun PlacesCard(uiEvent: (UiEvent) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var addressString by remember { mutableStateOf("Search for a place...") }

    val placeAutoCompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        val place = Autocomplete.getPlaceFromIntent(it.data!!)

        // do what you want with the place - here we can move the camera and add a marker
        scope.launch {
            Timber.d("Recomposition() | scope.launch | do what you want with the place - here we can move the camera and add a marker")

            place.location.let { location ->
                /*googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        location,
                        18.0f
                    )
                )*/
                uiEvent.invoke(UiEvent.OnPlaceSelected(place))
                addressString = place.formattedAddress ?: "Search for a place..."
                /*setMarker(
                    googleMap = googleMap,
                    location = location,
                    markerIcon = markerIcon,
                    title = if (place.name != null) place.name else addressString.value,
                    snippet = if (place.name != null) addressString.value else null
                )*/
            }

            // uiEvent.invoke(UiEvent.OnSearchPlaceVisible(true))
        }
    }

    TheLabTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp),
            onClick = {
                // Start the autocomplete intent.
                val intent = Autocomplete
                    .IntentBuilder(AutocompleteActivityMode.OVERLAY, Constants.CURRENT_PLACE_FIELDS)
                    .build(context)
                // Use the launcher to fire the intent
                placeAutoCompleteLauncher.launch(
                    intent,
                    ActivityOptionsCompat.makeBasic()
                )
            },
            shape = RoundedCornerShape(50),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "search_icon"
                )

                Text(modifier = Modifier.weight(1f), text = addressString)

                // Icon Button Microphone
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { uiEvent.invoke(UiEvent.OnVocalSearch) }
                ) {
                    Icon(imageVector = Icons.Rounded.Mic, contentDescription = null)
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
private fun PreviewPlacesCard() {
    TheLabTheme { PlacesCard {} }
}