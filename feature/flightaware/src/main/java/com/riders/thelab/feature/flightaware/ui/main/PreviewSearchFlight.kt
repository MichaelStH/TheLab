import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.riders.thelab.core.data.local.model.flight.AirportSearchModel
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.LabHorizontalViewPagerGeneric
import com.riders.thelab.core.ui.compose.component.dropdown.LabDropdownMenu2
import com.riders.thelab.core.ui.compose.component.tab.LabTabRow
import com.riders.thelab.core.ui.compose.component.textfield.LabOutlinedTextField
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.flightaware.R
import com.riders.thelab.feature.flightaware.core.theme.backgroundColor
import com.riders.thelab.feature.flightaware.core.theme.buttonColor
import com.riders.thelab.feature.flightaware.core.theme.cardBackgroundColor
import com.riders.thelab.feature.flightaware.core.theme.searchTextColor
import com.riders.thelab.feature.flightaware.core.theme.textColor
import com.riders.thelab.feature.flightaware.ui.airport.AirportSearchItem
import com.riders.thelab.feature.flightaware.ui.main.UiEvent
import kotlinx.coroutines.launch
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.text.NotBlankString
import timber.log.Timber

///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@OptIn(ExperimentalKotoolsTypesApi::class)
@Composable
fun SearchFlightByCode(uiEvent: (UiEvent) -> Unit) {
    var flightNumber by remember { mutableStateOf("") }

    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.placeholder_search_a_flight),
                    style = TextStyle(color = Color.White)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), contentAlignment = Alignment.CenterStart
                ) {
                    LabOutlinedTextField(
                        modifier = Modifier.fillMaxSize(),
                        onOutsideBoundariesClicked = false,
                        query = flightNumber,
                        onUpdateQuery = { newValue -> flightNumber = newValue },
                        placeholder = stringResource(id = R.string.search_flight_by_number),
                        label = stringResource(id = R.string.placeholder_flight_number),
                        leadingContent = {
                            Icon(
                                modifier = Modifier.rotate(90f),
                                imageVector = Icons.Filled.AirplanemodeActive,
                                contentDescription = "icon_flight_takeoff",
                                tint = Color.LightGray
                            )
                        },
                        trailingContent = {
                            AnimatedContent(
                                targetState = flightNumber.isNotEmpty(),
                                label = "close icon transition"
                            ) { targetState ->
                                if (!targetState) {
                                    Box {}
                                } else {
                                    IconButton(onClick = { flightNumber = "" }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "close icon"
                                        )
                                    }
                                }
                            }
                        },
                        /*focusedIndicatorColor = Color.LightGray,
                        unfocusedIndicatorColor = Color.Transparent,*/
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = cardBackgroundColor,
//                        hasBorders = true,
                        focusedBorderColor = searchTextColor,
                        unfocusedBorderColor = textColor
                    )
                }

                Button(
                    onClick = {
                        if (flightNumber.isEmpty()) {
                            return@Button
                        }

                        uiEvent.invoke(
                            UiEvent.OnSearchFlightByID(NotBlankString.create(flightNumber))
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.placeholder_search),
                        style = TextStyle(color = Color.LightGray)
                    )
                }
            }

        }
    }
}


@OptIn(ExperimentalKotoolsTypesApi::class)
@Composable
fun SearchFlightByDestination(
    uiEvent: (UiEvent) -> Unit,
    onOutsideBoundariesClicked: Boolean,
    departureExpanded: Boolean,
    onDepartureExpanded: (Boolean) -> Unit,
    departureSuggestions: List<AirportSearchModel>,
    arrivalExpanded: Boolean,
    onArrivalExpanded: (Boolean) -> Unit,
    arrivalSuggestions: List<AirportSearchModel>,
) {

    val departureQuery by remember { mutableStateOf("") }
    val arrivalQuery by remember { mutableStateOf("") }

    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .wrapContentSize(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.placeholder_search_a_flight),
                    style = TextStyle(color = Color.White)
                )

                // Departure
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlightTakeoff,
                        contentDescription = "icon_flight_takeoff",
                        tint = Color.LightGray
                    )

                    // Departure
                    LabDropdownMenu2(
                        modifier = Modifier.fillMaxWidth(),
                        query = departureQuery,
                        onUpdateQuery = { uiEvent.invoke(UiEvent.OnUpdateDepartureQuery(it)) },
                        onOutsideBoundariesClicked = onOutsideBoundariesClicked,
                        expanded = departureExpanded || departureSuggestions.isNotEmpty(),
                        onExpandedChanged = onDepartureExpanded,
                        placeholder = stringResource(id = R.string.search_flight_from),
                        label = stringResource(id = R.string.search_flight_from),
                        suggestions = departureSuggestions,
                        onOptionsSelected = { uiEvent.invoke(UiEvent.OnDepartureOptionsSelected(it)) },
                        dropdownItemContent = {
                            AirportSearchItem(item = departureSuggestions[it], isSuggestion = true)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalDivider()

                    IconButton(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface),
                        onClick = {
                            if (departureQuery.trim().isBlank()) {
                                Timber.e("You cannot switch entries because the departure input is empty")
                                return@IconButton
                            }
                            if (arrivalQuery.trim().isBlank()) {
                                Timber.e("You cannot switch entries because the arrival input is empty")
                                return@IconButton
                            }

                            val temp = departureQuery
                            uiEvent.invoke(UiEvent.OnUpdateDepartureQuery(arrivalQuery))
                            uiEvent.invoke(UiEvent.OnUpdateArrivalQuery(temp))
                        }
                    ) {
                        Icon(
                            modifier = Modifier.rotate(90f),
                            imageVector = Icons.Filled.SyncAlt,
                            contentDescription = null,
                            tint = if (!isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
                        )
                    }
                }


                // Arrival
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlightLand,
                        contentDescription = "icon_flight_land",
                        tint = Color.LightGray
                    )

                    // Arrival
                    LabDropdownMenu2(
                        modifier = Modifier.fillMaxWidth(),
                        query = arrivalQuery,
                        onUpdateQuery = { uiEvent.invoke(UiEvent.OnUpdateArrivalQuery(it)) },
                        onOutsideBoundariesClicked = onOutsideBoundariesClicked,
                        expanded = arrivalExpanded || arrivalSuggestions.isNotEmpty(),
                        onExpandedChanged = onArrivalExpanded,
                        placeholder = stringResource(id = R.string.search_flight_destination),
                        label = stringResource(id = R.string.search_flight_destination),
                        suggestions = arrivalSuggestions,
                        onOptionsSelected = { uiEvent.invoke(UiEvent.OnArrivalOptionsSelected(it)) },
                        dropdownItemContent = {
                            AirportSearchItem(item = arrivalSuggestions[it], isSuggestion = true)
                        }
                    )
                }

                Button(
                    onClick = {
                        if (departureQuery.isEmpty() && arrivalQuery.isEmpty()) {
                            return@Button
                        }
                        uiEvent.invoke(
                            UiEvent.OnSearchFlightByRoute(
                                departureAirportIcaoCode = NotBlankString.create(
                                    departureQuery
                                ),
                                arrivalAirportIcaoCode = NotBlankString.create(
                                    arrivalQuery
                                )
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(
                        text = "Search",
                        style = TextStyle(color = Color.LightGray)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchFlightContent(
    uiEvent: (UiEvent) -> Unit,
    searchPageIndex: Int,
    onOutsideBoundariesClicked: Boolean,
    departureExpanded: Boolean,
    onDepartureExpanded: (Boolean) -> Unit,
    departureSuggestions: List<AirportSearchModel>,
    arrivalExpanded: Boolean,
    onArrivalExpanded: (Boolean) -> Unit,
    arrivalSuggestions: List<AirportSearchModel>,
) {
    val scope = rememberCoroutineScope()

    // this is to disable the ripple effect
    val interactionSource = remember { MutableInteractionSource() }

    val tabs = listOf(
        stringResource(id = R.string.search_flight_by_id),
        stringResource(id = R.string.search_flight_by_route)
    )

    val pagerState = rememberPagerState { tabs.size }

    TheLabTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .indication(
                    indication = null,
                    interactionSource = interactionSource
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LabTabRow(
                        modifier = Modifier.fillMaxWidth(),
                        items = tabs,
                        selectedItemIndex = searchPageIndex,
                        tabWidth = this@BoxWithConstraints.maxWidth / tabs.size,
                        selectedTextColor = searchTextColor,
                        unselectedTextColor = Color.Gray,
                        backgroundColor = backgroundColor,
                        indicatorColor = cardBackgroundColor,
                        hasCustomShape = true
                    ) { index ->
                        Timber.d("SearchFlightContent | Recomposition | index: $index")
                        uiEvent.invoke(UiEvent.OnSearchCategorySelected(index))
                    }

                    LabHorizontalViewPagerGeneric(
                        pagerState = pagerState,
                        items = tabs,
                        onCurrentPageChanged = {}
                    ) { page, _ ->
                        when (page) {
                            0 -> SearchFlightByCode(
                                uiEvent = uiEvent
                            )

                            1 -> SearchFlightByDestination(
                                uiEvent = uiEvent,
                                onOutsideBoundariesClicked = onOutsideBoundariesClicked,
                                departureExpanded = departureExpanded,
                                onDepartureExpanded = onDepartureExpanded,
                                departureSuggestions = departureSuggestions,
                                arrivalExpanded = arrivalExpanded,
                                onArrivalExpanded = onArrivalExpanded,
                                arrivalSuggestions = arrivalSuggestions,
                            )
                        }
                    }
                }

            }
        }
    }

    LaunchedEffect(searchPageIndex) {
        scope.launch { pagerState.animateScrollToPage(searchPageIndex) }
    }
}


///////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewSearchFlightByCode() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            SearchFlightByCode(uiEvent = {})
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewSearchFlightByDestination() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            SearchFlightByDestination(
                uiEvent = {},
                onOutsideBoundariesClicked = false,
                departureExpanded = false,
                onDepartureExpanded = {},
                departureSuggestions = listOf(),
                arrivalExpanded = true,
                onArrivalExpanded = {},
                arrivalSuggestions = listOf(),
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewSearchFlightContent() {
    TheLabTheme {
        SearchFlightContent(uiEvent = {}, 0, false, false, {}, emptyList(), false, {}, emptyList())
    }
}