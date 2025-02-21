package com.riders.thelab.feature.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.riders.thelab.core.common.utils.DateTimeUtils
import com.riders.thelab.core.data.local.model.weather.WeatherModel
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme


///////////////////////////////////////////////////
//
// COMPOSABLE
//
///////////////////////////////////////////////////
@Composable
fun WeatherDailyForecast(modifier: Modifier = Modifier, dailyWeatherList: List<WeatherModel>) {
    val listState = rememberLazyListState()

    TheLabTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Weather trends for the next 5 days"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                // colors = CardDefaults.cardColors(containerColor = if (!isSystemInDarkTheme()) md_theme_light_primary else md_theme_dark_primary)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = dailyWeatherList.subList(
                            1,
                            dailyWeatherList.size - 2
                        )
                    ) { dailyWeather ->

                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest
                                .Builder(LocalContext.current)
                                .data(dailyWeather.weatherIconUrl.toString())
                                .apply {
                                    crossfade(true)
                                    allowHardware(false)
                                }
                                .build(),
                            placeholder = painterResource(R.drawable.logo_colors),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Day of the week
                            Text(
                                text = DateTimeUtils.getDayFromTime(dailyWeather.dateTimeUTC),
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icons with temperature
                                // Weather icon
                                Image(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    painter = painter,
                                    contentDescription = "weather icon wth coil",
                                    contentScale = ContentScale.Crop,
                                )
                                // current temperature
                                Text(
                                    text = "${dailyWeather.temperature?.max?.toInt()}°",
                                    fontWeight = FontWeight.Bold
                                )
                                // current temperature
                                Text(
                                    text = "${dailyWeather.temperature?.min?.toInt()}°"
                                )
                            }
                        }
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
private fun PreviewWeatherDailyForecast(@PreviewParameter(PreviewProviderWeather::class) weather: WeatherModel) {

    TheLabTheme {
        WeatherDailyForecast(
            modifier = Modifier.fillMaxSize(),
            dailyWeatherList = weather.dailyWeather!!
        )
    }
}