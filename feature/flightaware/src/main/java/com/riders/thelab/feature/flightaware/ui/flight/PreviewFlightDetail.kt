package com.riders.thelab.feature.flightaware.ui.flight

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riders.thelab.core.data.local.model.flight.FlightModel
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.toolbar.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.getCoilAsyncImagePainter
import com.riders.thelab.feature.flightaware.core.theme.backgroundColor
import com.riders.thelab.feature.flightaware.core.theme.cardBackgroundColor
import com.riders.thelab.feature.flightaware.core.theme.textColor
import com.riders.thelab.feature.flightaware.ui.main.UiEvent
import com.riders.thelab.feature.flightaware.utils.Constants
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.text.NotBlankString
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////

@Composable
fun FlightStatusCard(
    flightId: NotBlankString,
    airlineOperatorId: NotBlankString,
    departureAirportIataCode: NotBlankString,
    arrivalAirportIataCode: NotBlankString,
    flightStatus: NotBlankString
) {
    val context = LocalContext.current

    val flightIATA = flightId.toString().take(2)
    val painter = getCoilAsyncImagePainter(
        context = context,
        dataUrl = "${Constants.ENDPOINT_FLIGHT_FULL_LOGO}$flightIATA${Constants.EXTENSION_SVG}",
        isSvg = true
    )

    TheLabTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                // Flight ID & Operator ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = flightId.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W200,
                            fontSize = 16.sp,
                            color = textColor
                        )
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .width(120.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = Color.White.copy(alpha = .95f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .size(
                                    width = this.maxWidth,
                                    height = this.maxHeight
                                )
                                .padding(horizontal = 8.dp),
                            painter = painter,
                            contentDescription = "airline_logo_icon",
                            contentScale = ContentScale.Fit
                        )
                    }

                    Text(
                        text = airlineOperatorId.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W200,
                            fontSize = 16.sp,
                            color = textColor
                        )
                    )
                }
                // Departure & Arrival
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = departureAirportIataCode.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = arrivalAirportIataCode.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                }
                // Flight status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = flightStatus.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = flightStatus.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FlightInfoContainer(
    departureDate: NotBlankString,
    departureTime: NotBlankString,
    arrivalDate: NotBlankString,
    arrivalTime: NotBlankString
) {

    TheLabTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                // Departure date time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = departureDate.toLocalDateTime().toLocalDate().toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = departureTime.toLocalDateTime().toLocalTime().toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                }

                // Arrival date time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = arrivalDate.toLocalDateTime().toLocalDate().toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = arrivalTime.toLocalDateTime().toLocalTime().toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}

private val zoneId: ZoneId = ZoneId.systemDefault()
private fun NotBlankString.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.parse(this.toString()), zoneId)

@OptIn(ExperimentalKotoolsTypesApi::class)
@Composable
fun FlightDetailContent(flight: FlightModel, uiEvent: (UiEvent) -> Unit) {
    val lazyListState = rememberLazyListState()

    // this is to disable the ripple effect
    val interactionSource = remember { MutableInteractionSource() }

    TheLabTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TheLabTopAppBar(
                    navigationIconColor = Color.White,
                    backgroundColor = backgroundColor
                )
            }
        ) { contentPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(color = backgroundColor)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .size(width = this.maxWidth, height = this.maxHeight)
                        .indication(
                            indication = null,
                            interactionSource = interactionSource
                        ),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FlightStatusCard(
                            flightId = NotBlankString.create(
                                flight.faFlightID.toString().split("-")[0]
                            ),
                            airlineOperatorId = flight.operatorID,
                            departureAirportIataCode = flight.origin?.codeIcao
                                ?: NotBlankString.create("N/A"),
                            arrivalAirportIataCode = flight.destination?.codeIcao
                                ?: NotBlankString.create("N/A"),
                            flightStatus = flight.status
                        )
                    }

                    item {
                        FlightInfoContainer(
                            departureDate = flight.estimatedOut ?: NotBlankString.create("N/A"),
                            departureTime = flight.scheduledOut ?: NotBlankString.create("N/A"),
                            arrivalDate = flight.estimatedOn ?: NotBlankString.create("N/A"),
                            arrivalTime = flight.scheduledOn ?: NotBlankString.create("N/A"),
                        )
                    }

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
private fun PreviewFlightStatusCard(@PreviewParameter(PreviewProviderFlight::class) flight: FlightModel) {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            FlightStatusCard(
                flightId = NotBlankString.create(flight.faFlightID.toString().split("-")[0]),
                airlineOperatorId = flight.operatorID,
                departureAirportIataCode = flight.origin?.codeIata ?: NotBlankString.create("N/A"),
                arrivalAirportIataCode = flight.destination?.codeIata
                    ?: NotBlankString.create("N/A"),
                flightStatus = flight.status
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewSearchFlightByCode(@PreviewParameter(PreviewProviderFlight::class) flight: FlightModel) {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            FlightDetailContent(
                flight = flight,
                uiEvent = {}
            )
        }
    }
}

