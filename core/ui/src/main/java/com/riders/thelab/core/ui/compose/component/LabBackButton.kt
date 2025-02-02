package com.riders.thelab.core.ui.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.executeOnBackPressed

///////////////////////////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////////////////////////
@Composable
fun LabBackButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    icon: ImageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current

    TheLabTheme {
        Card(
            modifier = Modifier
                .size(48.dp)
                .then(modifier),
            onClick = { executeOnBackPressed(context) },
            colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = .5345f)),
            shape = CircleShape
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
        }
    }
}


///////////////////////////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewLabBackButton() {
    TheLabTheme {
        LabBackButton(icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft)
    }
}