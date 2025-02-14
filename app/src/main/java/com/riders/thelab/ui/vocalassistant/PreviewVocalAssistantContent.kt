package com.riders.thelab.ui.vocalassistant

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme

@Composable
fun VocalAssistantContent() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var frequencies by remember { mutableStateOf(listOf<Double>()) }
    val audioProcessor =
        remember { AudioProcessor { newFrequencies -> frequencies = newFrequencies } }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (isRecording) {
                audioProcessor.stopRecording()
            } else {
                audioProcessor.startRecording()
            }
            isRecording = !isRecording
        }
    }

    LaunchedEffect(isRecording) {
        
    }

    TheLabTheme {
        Box(
            modifier = Modifier
                .background(color = Color.Transparent)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FrequencyVisualizer(frequencies = frequencies)
                Spacer(modifier = Modifier.height(30.dp))
                Button(onClick = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            if (isRecording) {
                                audioProcessor.stopRecording()
                            } else {
                                audioProcessor.startRecording()
                            }
                            isRecording = !isRecording
                        }

                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }) {
                    Text(if (isRecording) "Stop Recording" else "Start Recording")
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewVocalAssistantContent() {
    TheLabTheme {
        VocalAssistantContent()
    }
}