package com.riders.thelab.ui.mainactivity

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import com.riders.thelab.R
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.data.local.model.app.LocalApp
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.LabHorizontalViewPagerGeneric
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.findActivity
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.utils.LabAppManager


///////////////////////////////
//
// COMPOSE
//
///////////////////////////////

@Composable
fun WhatsNew(item: App) {

    val context = LocalContext.current

    /* Convert our Image Resource into a Bitmap */
    val bitmap = remember {
        UIManager.drawableToBitmap(item.appDrawableIcon!!)
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
                .width(width = dimensionResource(id = R.dimen.max_card_image_width))
                .height(height = dimensionResource(id = R.dimen.max_card_image_height)),
            onClick = { (context.findActivity() as MainActivity).launchApp(item) },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2F)
                        .background(
                            when (item.appTitle) {
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
                                    } ?: Color.Transparent
                                }

                                stringResource(id = com.riders.thelab.core.ui.R.string.activity_title_flight) -> {
                                    Color(0xFF002f5d)
                                }

                                else -> {
                                    Color.Transparent
                                }
                            })
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxHeight()
                            .padding(32.dp),
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "app_icon",
                        contentScale = ContentScale.FillHeight
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(8.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (item.appName != null) item.appName!! else item.appTitle!!
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (item.appDescription != null) item.appDescription!! else ""
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsNew(item: App, pageOffset: Float) {

    val context = LocalContext.current

    val localApp: LocalApp = item as LocalApp

    /* Convert our Image Resource into a Bitmap */
    val bitmap = remember { localApp.bitmap }
    /* Create the Palette, pass the bitmap to it */
    val palette = remember { bitmap.let { Palette.from(it!!).generate() } }

    /* Get the dark vibrant swatch */
    val darkVibrantSwatch = palette.darkVibrantSwatch

    val colorMatrix = remember { ColorMatrix() }

    TheLabTheme {
        Card(
            modifier = Modifier
                .width(width = dimensionResource(id = R.dimen.max_card_image_width))
                .height(height = dimensionResource(id = R.dimen.max_card_image_height)),
            onClick = { (context.findActivity() as MainActivity).launchApp(item) },
        ) {

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2F)
                        .background(
                            when (item.appTitle) {
                                stringResource(id = R.string.activity_title_colors),
                                stringResource(id = R.string.activity_title_palette),
                                stringResource(id = R.string.activity_title_youtube_like),
                                stringResource(id = R.string.activity_title_google_drive),
                                stringResource(id = R.string.activity_title_google_sign_in),
                                stringResource(id = R.string.activity_title_weather),
                                stringResource(id = R.string.activity_title_compose),
                                stringResource(id = R.string.activity_title_lottie) -> {
                                    darkVibrantSwatch?.rgb?.let {
                                        Color(
                                            it
                                        )
                                    } ?: Color.Transparent
                                }

                                else -> {
                                    Color.Transparent
                                }
                            })
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxHeight()
                            .padding(32.dp)
                            // To adjust the image inside the composable, we use Modifier.graphicsLayer
                            .graphicsLayer {
                                // get a scale value between 1 and 1.75f, 1.75 will be when its resting,
                                // 1f is the smallest it'll be when not the focused page
                                val scale = lerp(1f, 1.75f, pageOffset)
                                // apply the scale equally to both X and Y, to not distort the image
                                scaleX = scale
                                scaleY = scale
                            },
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "app_icon",
                        contentScale = ContentScale.FillHeight,
                        colorFilter = ColorFilter.colorMatrix(colorMatrix)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(8.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (item.appName != null) item.appName!! else item.appTitle!!
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (item.appDescription != null) item.appDescription!! else ""
                    )
                }
            }
        }
    }
}

@Composable
fun ActionsButtons(onSearchClicked: () -> Unit, onSettingsClicked: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search
        IconButton(onClick = onSearchClicked) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = if (!isSystemInDarkTheme()) Color.Black else Color.White
            )
        }
        // Settings
        IconButton(onClick = onSettingsClicked) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                tint = if (!isSystemInDarkTheme()) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun WhatsNewTopContent(
    onSearchClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "What's new",
            fontSize = 18.sp,
            fontWeight = FontWeight.Thin
        )

        ActionsButtons(
            onSearchClicked = onSearchClicked,
            onSettingsClicked = onSettingsClicked
        )
    }
}


@Composable
fun WhatsNewList(
    viewModel: MainActivityViewModel,
    whatsNewList: List<LocalApp>,
    pagerAutoScroll: Boolean
) {
    val pagerState: PagerState =
        rememberPagerState(initialPageOffsetFraction = .25f) { whatsNewList.size }

    TheLabTheme {
        Row(modifier = Modifier.fillMaxWidth()) {
            LabHorizontalViewPagerGeneric(
                viewModel = viewModel,
                pagerState = pagerState,
                items = whatsNewList,
                autoScroll = pagerAutoScroll
            ) { page, pageOffset ->
                WhatsNew(
                    item = whatsNewList[page],
                    pageOffset = pageOffset
                )
            }
        }
    }
}

@Composable
fun WhatsNewContent(
    whatsNewList: List<LocalApp>,
    pagerAutoScroll: Boolean,
    onSearchClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val pagerState: PagerState =
        rememberPagerState { whatsNewList.size }

    TheLabTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.size(24.dp))

            // Title and Actions
            WhatsNewTopContent(onSearchClicked, onSettingsClicked)

            Spacer(modifier = Modifier.size(16.dp))

            // Whats New ViewPager
            Row(modifier = Modifier.fillMaxWidth()) {
                LabHorizontalViewPagerGeneric(
                    pagerState = pagerState,
                    items = whatsNewList,
                    onCurrentPageChanged = {},
                    pageCount = whatsNewList.size,
                    viewPagerDotVisibility = true,
                    updateViewPagerDotVisibility = { visible -> },
                    viewPagerDotExpanded = false,
                    updateViewPagerExpanded = { expanded -> },
                    autoScroll = pagerAutoScroll
                ) { page: Int, pageOffset: Float ->
                    WhatsNew(
                        item = whatsNewList[page],
                        pageOffset = pageOffset
                    )
                }
            }
        }
    }
}


///////////////////////////////
//
// PREVIEWS
//
///////////////////////////////
@DevicePreviews
@Composable
private fun PreviewActionsButtons() {
    TheLabTheme {
        ActionsButtons({}, {})
    }
}

@DevicePreviews
@Composable
private fun PreviewWhatsNewTopContent() {
    TheLabTheme {
        WhatsNewTopContent({}) {}
    }
}

@DevicePreviews
@Composable
private fun PreviewWhatsNew() {
    val context = LocalContext.current
    val appItem = LabAppManager.getActivityList(context)[0]
    TheLabTheme {
        WhatsNew(item = appItem)
    }
}

@DevicePreviews
@Composable
private fun PreviewWhatsNewForPager() {
    val context = LocalContext.current
    val appItem = LabAppManager.getActivityList(context)[12]
    TheLabTheme {
        WhatsNew(appItem, 2f)
    }
}

@DevicePreviews
@Composable
private fun PreviewWhatsNewList() {
    val context = LocalContext.current
    val viewModel: MainActivityViewModel = hiltViewModel()
    val appList = LabAppManager.getActivityList(context).take(3).let {
        it.map { localApp ->

            val bitmap: Bitmap? = when (localApp.appDrawableIcon) {
                is BitmapDrawable -> {
                    (localApp.appDrawableIcon as BitmapDrawable).bitmap as Bitmap
                }

                is VectorDrawable -> {
                    App.getBitmap(localApp.appDrawableIcon as VectorDrawable)!!
                }

                else -> {
                    null
                }
            }

            LocalApp(
                localApp.id,
                localApp.appTitle!!,
                localApp.appDescription!!,
                null,
                localApp.appActivity,
                localApp.appDate!!
            ).apply {
                this.bitmap = bitmap
            }
        }
    }

    TheLabTheme {
        WhatsNewList(viewModel = viewModel, whatsNewList = appList, pagerAutoScroll = true)
    }
}