package com.riders.thelab.feature.flightaware.ui.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.Lottie
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.executeOnBackPressed
import com.riders.thelab.feature.flightaware.core.theme.backgroundColor
import com.riders.thelab.feature.flightaware.core.theme.searchTextColor
import com.riders.thelab.feature.flightaware.core.theme.textColor
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.text.NotBlankString


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@Composable
fun FlightDetailErrorContent(reason: NotBlankString) {
    val context = LocalContext.current

    TheLabTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (LocalInspectionMode.current) backgroundColor else Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(.8f)
                    .height(dimensionResource(id = com.riders.thelab.core.ui.R.dimen.max_card_image_height)),
                contentAlignment = Alignment.Center
            ) {
                Lottie(
                    modifier = Modifier.size(this.maxWidth, this.maxHeight),
                    rawResId = if (!isSystemInDarkTheme()) com.riders.thelab.core.ui.R.raw.error_rolling else com.riders.thelab.core.ui.R.raw.error_rolling_dark_theme
                )
            }

            Text(text = reason.toString(), color = textColor)

            Button(
                onClick = { executeOnBackPressed(context) },
                colors = ButtonDefaults.buttonColors(containerColor = searchTextColor)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = textColor
                    )
                    Text(text = "GO BACK", color = textColor)
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
@OptIn(ExperimentalKotoolsTypesApi::class)
@DevicePreviews
@Composable
private fun PreviewFlightDetailErrorContent() {
    TheLabTheme {
        FlightDetailErrorContent(reason = NotBlankString.create("Error occurred while getting value"))
    }
}