package com.riders.thelab.ui.vocalassistant

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme

@Composable
fun FrequencyVisualizer(frequencies: List<Double>) {
    val barWidth = 5.dp
    val spacing = 2.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
            .background(Color.LightGray)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val numBars = frequencies.size
            val barMaxWidth = (canvasWidth - (numBars - 1) * spacing.toPx()) / numBars
            var currentX = 0f

            if (barMaxWidth > 0) {
                for (frequency in frequencies) {
                    val barHeight = frequency.toFloat() * 0.001f
                    val barColor = Color.Blue

                    drawRect(
                        color = barColor,
                        topLeft = Offset(currentX, canvasHeight - barHeight),
                        size = androidx.compose.ui.geometry.Size(barMaxWidth, barHeight)
                    )
                    currentX += barMaxWidth + spacing.toPx()
                }
            } else {
                drawCircle(
                    color = Color.Red,
                    radius = 30.dp.toPx(),
                    center = Offset(canvasWidth / 2, canvasHeight / 2)
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewFrequencyVisualizer() {
    TheLabTheme {
        FrequencyVisualizer(
            listOf(
                3.0, 16.0, 12.0,
                3.0, 16.0, 12.0,
                3.0, 16.0, 12.0
            )
        )
    }
}