package com.riders.thelab.ui.mainactivity

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.riders.thelab.R
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.Lottie
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.theme.md_theme_dark_background
import com.riders.thelab.core.ui.compose.utils.findActivity
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.utils.LabAppManager


@Composable
fun NoItemFound(searchValue: String) {
    TheLabTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                Lottie(
                    modifier = Modifier.fillMaxSize(),
                    rawResId = if (!isSystemInDarkTheme()) com.riders.thelab.core.ui.R.raw.error_rolling else com.riders.thelab.core.ui.R.raw.error_rolling_dark_theme
                )
            }

            Text(
                text = "Oops! No item found for value \"$searchValue\"",
                color = if (!isSystemInDarkTheme()) Color.Black else Color.White
            )
            Text(
                text = "Please retry...",
                color = if (!isSystemInDarkTheme()) Color.Black else Color.White
            )
        }
    }
}


@Composable
fun App(item: App, onAppItemClick:(App) -> Unit) {

    val context = LocalContext.current

    // App item variables
    val title = if (item.appName != null) item.appName!! else item.appTitle!!
    val description = item.appDescription ?: item.appVersion!!

    /* Convert our Image Resource into a Bitmap */
    val bitmap =
        when (title) {
            stringResource(id = R.string.activity_title_palette) -> {
                remember {
                    UIManager.addGradientToImageView(
                        context,
                        UIManager.drawableToBitmap(item.appDrawableIcon!!)
                    )
                }
            }

            else -> {
                remember { UIManager.drawableToBitmap(item.appDrawableIcon!!) }
            }
        }


    /* Create the Palette, pass the bitmap to it */
    val palette = remember {
        bitmap.let { Palette.from(it).generate() }
    }

    /* Get the dark vibrant swatch */
    val darkVibrantSwatch = palette.darkVibrantSwatch

    TheLabTheme {
        Card(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.max_card_image_height))
                .fillMaxWidth(),
            onClick = { onAppItemClick.invoke(item) },
            // colors = CardDefaults.cardColors(containerColor = md_theme_dark_background),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2F),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        when (title) {
                            stringResource(id = R.string.activity_title_colors),
                            stringResource(id = R.string.activity_title_colors),
                            stringResource(id = R.string.activity_title_palette),
                            stringResource(id = R.string.activity_title_youtube_like),
                            stringResource(id = R.string.activity_title_google_drive),
                            stringResource(id = R.string.activity_title_google_sign_in),
                            stringResource(id = R.string.activity_title_weather),
                            stringResource(id = R.string.activity_title_recycler_view),
                            stringResource(id = R.string.activity_title_lottie) -> {
                                darkVibrantSwatch?.rgb?.let {
                                    Color(
                                        it
                                    )
                                } ?: md_theme_dark_background
                            }

                            stringResource(id = com.riders.thelab.core.ui.R.string.activity_title_flight) -> {
                                Color(0xFF002f5d)
                            }

                            else -> {
                                md_theme_dark_background
                            }
                        }
                    )

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            modifier = Modifier.size(64.dp),
                            bitmap = when (title) {
                                stringResource(id = R.string.activity_title_palette) -> {
                                    UIManager.addGradientToImageView(context, bitmap)
                                        .asImageBitmap()
                                }

                                else -> {
                                    bitmap.asImageBitmap()
                                }
                            },
                            contentDescription = "app_icon",
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        color = if (!isSystemInDarkTheme()) Color.Black else Color.White
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = description,
                        maxLines = 2,
                        color = if (!isSystemInDarkTheme()) Color.Black else Color.White
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
private fun PreviewNoItemFound() {
    TheLabTheme {
        NoItemFound("Cool")
    }
}

@DevicePreviews
@Composable
private fun PreviewApp() {
    val context = LocalContext.current
    val appItem = LabAppManager.getActivityList(context)[12]
    TheLabTheme {
        App(appItem) {}
    }
}