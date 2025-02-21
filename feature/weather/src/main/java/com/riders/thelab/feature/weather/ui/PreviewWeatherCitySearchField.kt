package com.riders.thelab.feature.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.riders.thelab.core.data.local.model.weather.CityModel
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.weather.utils.Constants
import java.util.UUID


///////////////////////////////////////////////////
//
// COMPOSABLE
//
///////////////////////////////////////////////////
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherCitySearchField(
    suggestions: List<CityModel>,
    searchCityQuery: String,
    onSearchTextChange: (String) -> Unit,
    searchMenuExpanded: Boolean,
    onUpdateSearchMenuExpanded: (Boolean) -> Unit,
    onFetchWeatherRequest: (Double, Double) -> Unit,
    onDismissSearch: () -> Unit
) {
    val focusManager: FocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (searchMenuExpanded)
        Icons.Filled.ArrowDropUp //it requires androidx.compose.material:material-icons-extended
    else
        Icons.Filled.ArrowDropDown

    TheLabTheme {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Weather city search field
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = searchMenuExpanded,
                onExpandedChange = {
                    onUpdateSearchMenuExpanded(!searchMenuExpanded)
                }
            ) {
                TextField(
                    value = searchCityQuery,
                    onValueChange = { onSearchTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            //This value is used to assign to the DropDown the same width
                            textFieldSize = coordinates.size.toSize()
                        }
                        .focusRequester(focusRequester)
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable),
                    label = { Text("Search a Country, City,...") },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier
                                .rotate(if (!searchMenuExpanded) 0f else 180f)
                                .clickable { onUpdateSearchMenuExpanded(!searchMenuExpanded) },
                            imageVector = icon,
                            contentDescription = "contentDescription"
                        )
                    },
                    singleLine = true,
                    maxLines = 1
                    //readOnly = true,
                )

                ExposedDropdownMenu(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() }),
                    expanded = searchMenuExpanded,
                    onDismissRequest = { onUpdateSearchMenuExpanded(false) }
                ) {
                    suggestions.forEachIndexed { _, city ->

                        val countryURL: String =
                            (Constants.BASE_ENDPOINT_WEATHER_FLAG
                                    + city.country.lowercase()
                                    + Constants.WEATHER_FLAG_PNG_SUFFIX)

                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest
                                .Builder(LocalContext.current)
                                .data(countryURL)
                                .apply {
                                    crossfade(true)
                                    allowHardware(false)
                                    //transformations(RoundedCornersTransformation(32.dp.value))
                                }
                                .build(),
                            placeholder = painterResource(R.drawable.logo_colors),
                        )

                        DropdownMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onSearchTextChange("${city.name}, ${city.country}")
                                onUpdateSearchMenuExpanded(false)
                                focusManager.clearFocus(true)
                                onFetchWeatherRequest(city.latitude, city.longitude)
                            },
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "${city.name}, ${city.country}"
                                    )

                                    Image(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp)),
                                        painter = painter,
                                        contentDescription = "palette image wth coil",
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewWeatherCitySearchField() {
    TheLabTheme {
        WeatherCitySearchField(
            suggestions = listOf(
                CityModel(
                    id = 1,
                    uuid = UUID.randomUUID().toString(),
                    name = "Johanesburg",
                    state = "",
                    country = "South Africa",
                    longitude = 48.3535,
                    latitude = 3.58978
                )
            ),
            searchMenuExpanded = true,
            searchCityQuery = "Johannesbu",
            onSearchTextChange = {},
            onUpdateSearchMenuExpanded = {},
            onFetchWeatherRequest = { _, _ -> },
            onDismissSearch = {}
        )
    }
}