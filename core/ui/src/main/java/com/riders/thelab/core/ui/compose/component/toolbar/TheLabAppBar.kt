package com.riders.thelab.core.ui.compose.component.toolbar

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import com.riders.thelab.core.ui.compose.previewprovider.TextContentPreviewProvider
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.executeOnBackPressed
import timber.log.Timber


///////////////////////////
//
// COMPOSABLE
//
///////////////////////////
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
fun TheLabTopAppBar(
    @PreviewParameter(TextContentPreviewProvider::class) title: String,
    toolbarHeight: Dp = 96.dp,
    hasTransparentBackground: Boolean = false,
    isDarkThemeForced: Boolean = false,
    isDarkTheme: Boolean = false,
    navigationIcon: @Composable (() -> Unit)? = null,
    actionBlock: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    TheLabTheme(if (isDarkThemeForced) isDarkTheme else isSystemInDarkTheme()) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(toolbarHeight)
                .background(
                    if (!hasTransparentBackground) Brush.verticalGradient(
                        listOf(Color.Black, MaterialTheme.colorScheme.background)
                    ) else Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent)
                    )
                ),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(text = title, color = Color.White)
                }
            },
            navigationIcon = {
                IconButton(onClick = { executeOnBackPressed(context) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(Color.Transparent)
        )
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheLabTopAppBar(viewModel: BaseViewModel, title: String) {
    val context = LocalContext.current

    TheLabTheme(darkTheme = viewModel.isDarkMode) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black, MaterialTheme.colorScheme.background)
                    )
                ),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(text = title, color = Color.White)
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    executeOnBackPressed(context)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(Color.Transparent)
        )
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheLabTopAppBar(isDarkMode: Boolean, title: String) {
    val context = LocalContext.current

    TheLabTheme(darkTheme = isDarkMode) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black, MaterialTheme.colorScheme.background)
                    )
                ),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(text = title, color = Color.White)
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    executeOnBackPressed(context)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(Color.Transparent)
        )
    }
}


@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheLabTopAppBar(
    @PreviewParameter(TextContentPreviewProvider::class) title: String? = null,
    toolbarMaxHeight: Dp = 96.dp,
    mainCustomContent: @Composable (() -> Unit)? = null,
    withGradientBackground: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    viewModel: BaseViewModel? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    navigationIconColor: Color = if (!isSystemInDarkTheme()) Color.Black else Color.White,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val context = LocalContext.current

    TheLabTheme(viewModel?.isDarkMode ?: isSystemInDarkTheme()) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(56.dp, toolbarMaxHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (!withGradientBackground) Color.Transparent else Color.Black,
                            Color.Transparent
                        )
                    )
                ),
            title = {
                if (null != title && null == mainCustomContent) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(text = title, color = Color.White)
                    }
                } else if (null != mainCustomContent && null == title) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        mainCustomContent()
                    }
                } else {
                    Timber.e("Both title and mainCustomContent cannot be null or Not null. You have to define one them only")
                }
            },
            navigationIcon = {
                if (null == navigationIcon) {
                    IconButton(
                        onClick = { executeOnBackPressed(context) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back_icon",
                            tint = navigationIconColor
                        )
                    }
                } else {
                    navigationIcon()
                }
            },
            actions = {
                if (null != actions) {
                    actions()
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = backgroundColor)
        )
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheLabTopAppBar(
    toolbarSize: ToolbarSize,
    title: String? = null,
    titleColor: Color = Color.White,
    toolbarMaxHeight: Dp = 96.dp,
    mainCustomContent: @Composable (() -> Unit)? = null,
    withGradientBackground: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    viewModel: BaseViewModel? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    navigationIconColor: Color = if (!isSystemInDarkTheme()) Color.Black else Color.White,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val context = LocalContext.current

    TheLabTheme(viewModel?.isDarkMode ?: isSystemInDarkTheme()) {
        when (toolbarSize) {
            ToolbarSize.SMALL -> {
                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (!withGradientBackground) Color.Transparent else Color.Black,
                                    Color.Transparent
                                )
                            )
                        ),
                    title = {
                        if (null != title && null == mainCustomContent) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 8.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = title, color = titleColor)
                            }
                        } else if (null != mainCustomContent && null == title) {
                            mainCustomContent()
                        } else {
                            Timber.e("Both title and mainCustomContent cannot be null or Not null. You have to define one them only")
                        }
                    },
                    navigationIcon = {
                        if (null == navigationIcon) {
                            IconButton(
                                modifier = Modifier.zIndex(10f),
                                onClick = {
                                    executeOnBackPressed(context)
                                }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back_icon",
                                    tint = navigationIconColor
                                )
                            }
                        } else {
                            navigationIcon()
                        }
                    },
                    actions = {
                        if (null != actions) {
                            actions()
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
                )
            }

            ToolbarSize.MEDIUM -> {
                MediumTopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeightIn(56.dp, toolbarMaxHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (!withGradientBackground) Color.Transparent else Color.Black,
                                    Color.Transparent
                                )
                            )
                        ),
                    title = {
                        if (null != title && null == mainCustomContent) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    modifier = Modifier.padding(start = 16.dp),
                                    text = title,
                                    color = Color.White
                                )
                            }
                        } else if (null != mainCustomContent && null == title) {
                            mainCustomContent()
                        } else {
                            Timber.e("Both title and mainCustomContent cannot be null or Not null. You have to define one them only")
                        }
                    },
                    navigationIcon = {
                        if (null == navigationIcon) {
                            IconButton(
                                onClick = {
//                        (context.findActivity() as BaseComponentActivity).backPressed()
                                    executeOnBackPressed(context)
                                }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back_icon",
                                    tint = navigationIconColor
                                )
                            }
                        } else {
                            navigationIcon()
                        }
                    },
                    actions = {
                        if (null != actions) {
                            actions()
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = backgroundColor)
                )
            }

            ToolbarSize.LARGE -> {
                LargeTopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(toolbarMaxHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (!withGradientBackground) Color.Transparent else Color.Black,
                                    Color.Transparent
                                )
                            )
                        ),
                    title = {
                        if (null != title && null == mainCustomContent) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(start = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(text = title, color = Color.White)
                            }
                        } else if (null != mainCustomContent && null == title) {
                            mainCustomContent()
                        } else {
                            Timber.e("Both title and mainCustomContent cannot be null or Not null. You have to define one them only")
                        }
                    },
                    navigationIcon = {
                        if (null == navigationIcon) {
                            IconButton(
                                onClick = {
                                    executeOnBackPressed(context)
                                }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back_icon",
                                    tint = navigationIconColor
                                )
                            }
                        } else {
                            navigationIcon()
                        }
                    },
                    actions = {
                        if (null != actions) {
                            actions()
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = backgroundColor)
                )
            }
        }
    }
}

/**
 * Weather toolbar
 */
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheLabTopAppBar(
    @PreviewParameter(TextContentPreviewProvider::class) title: String,
    iconState: Boolean,
    actionBlock: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    TheLabTheme {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black, MaterialTheme.colorScheme.background)
                    )
                ),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(text = title, color = Color.White)
                }
            },
            navigationIcon = {
                IconButton(onClick = { (context as BaseComponentActivity).backPressed() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    IconButton(onClick = {
                        if (null != actionBlock) {
                            actionBlock()
                        }
                    }) {
                        Icon(
                            imageVector = if (!iconState) Icons.Filled.GpsOff else Icons.Filled.GpsFixed,
                            contentDescription = "action icon "
                        )
                    }
                }

            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(Color.Transparent)
        )
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
fun TheLabTopAppBarLarge() {
    val context = LocalContext.current

    TheLabTheme {
        LargeTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black, Color.Transparent)
                    )
                ),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 16.dp, bottom = 0.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(modifier = Modifier, text = "Welcome to ", color = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                        Image(
                            modifier = Modifier.height(16.dp),
                            painter = painterResource(id = R.drawable.ic_lab_6_the),
                            contentDescription = "the_icon",
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Image(
                            modifier = Modifier.height(16.dp),
                            painter = painterResource(id = R.drawable.ic_lab_6_lab),
                            contentDescription = "lab_icon",
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Image(
                            modifier = Modifier.height(16.dp),
                            painter = painterResource(id = R.drawable.ic_the_lab_12_logo_white),
                            contentDescription = "lab_twelve",
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        executeOnBackPressed(context)
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(Color.Transparent)
        )
    }
}


///////////////////////////
//
// PREVIEWS
//
///////////////////////////
@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBar(@PreviewParameter(TextContentPreviewProvider::class) title: String) {
    TheLabTheme {
        TheLabTopAppBar(
            title = title,
            toolbarHeight = 56.dp,
            navigationIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = null
                )
            },
            isDarkThemeForced = false
        )
    }
}

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarDarkThemeForced(@PreviewParameter(TextContentPreviewProvider::class) title: String) {
    TheLabTheme {
        TheLabTopAppBar(
            title = title,
            toolbarHeight = 56.dp,
            navigationIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = null
                )
            },
            hasTransparentBackground = true,
            isDarkThemeForced = true,
            isDarkTheme = true
        )
    }
}

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarNoNavigationIcon(@PreviewParameter(TextContentPreviewProvider::class) title: String) {
    TheLabTheme {
        TheLabTopAppBar(title = title, navigationIcon = null)
    }
}

/*@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarWithBaseViewModel(@PreviewParameter(TextContentPreviewProvider::class) title: String) {
    val viewModel: BaseViewModel = hiltViewModel()
    TheLabTheme {
        TheLabTopAppBar(viewModel = viewModel, title = title, navigationIcon = null)
    }
}*/

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarWithToolbarSizeTitleString(
    @PreviewParameter(
        PreviewProviderToolbarSize::class
    ) toolbarSize: ToolbarSize
) {
    TheLabTheme {
        TheLabTopAppBar(
            toolbarSize = toolbarSize,
            toolbarMaxHeight = if (ToolbarSize.LARGE == toolbarSize) dimensionResource(id = R.dimen.max_card_image_height) else 96.dp,
            title = "Palette",
            backgroundColor = Color(0xFF032342),
            withGradientBackground = false,
            navigationIcon = null
        )
    }
}

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarWithToolbarSizeCustomContent(
    @PreviewParameter(
        PreviewProviderToolbarSize::class
    ) toolbarSize: ToolbarSize
) {
    TheLabTheme {
        TheLabTopAppBar(
            toolbarSize = toolbarSize,
            toolbarMaxHeight = if (ToolbarSize.LARGE == toolbarSize) dimensionResource(id = R.dimen.max_card_image_height) else 96.dp,
            mainCustomContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = if (ToolbarSize.LARGE == toolbarSize) 56.dp else 0.dp)
                        .zIndex(3f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Palette")
                    Text(text = "Custom Toolbar Size")
                }
            },
            backgroundColor = Color(0xFF032342),
            withGradientBackground = false,
            navigationIcon = null
        )
    }
}

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarWeatherGpsOn(@PreviewParameter(TextContentPreviewProvider::class) title: String) {
    TheLabTheme {
        TheLabTopAppBar(title = title, iconState = true, actionBlock = {})
    }
}

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarWeatherGpsOff(@PreviewParameter(TextContentPreviewProvider::class) title: String) {
    TheLabTheme {
        TheLabTopAppBar(title = title, iconState = false, actionBlock = {})
    }
}

@DevicePreviews
@Composable
private fun PreviewTheLabTopAppBarLarge() {
    TheLabTheme {
        TheLabTopAppBarLarge()
    }
}