package com.example.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.ui.viewmodels.EqualizerViewModel

@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val presets by viewModel.presets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initEqualizer()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Equalizer",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Simulating 6 bands UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until 6) {
                var sliderValue by remember { mutableFloatStateOf(0f) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Vertical slider workaround using a standard slider rotated if needed,
                    // or just a horizontal one for simplicity in this dummy view.
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            // Typically Android band levels are in millibels (e.g. -1500 to 1500)
                            // This is a simplified scaling for our slider
                            val level = (it * 1500).toInt().toShort()
                            viewModel.setBandLevel(i.toShort(), level)
                        },
                        valueRange = -1f..1f,
                        modifier = Modifier.width(100.dp)
                    )
                    Text(text = "B${i + 1}", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Presets", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(presets) { index, preset ->
                Button(onClick = { viewModel.applyPreset(index.toShort()) }) {
                    Text(text = preset)
                }
            }
        }
    }
}
