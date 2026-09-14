package com.example.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.musicplayer.ui.theme.SpotifyBlack
import com.example.musicplayer.ui.theme.SpotifyGreen

@Composable
fun FullPlayerScreen(
    onBackClick: () -> Unit,
    onEqualizerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.KeyboardArrowDown, "Back", tint = Color.White)
            }
            Text("Now Playing", color = Color.White, fontWeight = FontWeight.Bold)
            IconButton(onClick = onEqualizerClick) {
                Icon(Icons.Default.Settings, "Equalizer", tint = Color.White) // using settings icon as equalizer
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Album Art Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, "Album Art", modifier = Modifier.size(100.dp), tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Song Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Song Title", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Artist Name", color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = { /* TODO: Add to playlist */ }) {
                Icon(Icons.Default.FavoriteBorder, "Add to Playlist", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        var progress by remember { mutableFloatStateOf(0.3f) }
        Slider(
            value = progress,
            onValueChange = { progress = it },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.DarkGray
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1:23", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            Text("3:45", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Shuffle */ }) {
                Icon(Icons.Default.Refresh, "Shuffle", tint = Color.LightGray)
            }
            IconButton(onClick = { /* TODO: Previous */ }) {
                Icon(Icons.Default.KeyboardArrowLeft, "Previous", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SpotifyGreen)
                    .clickable { /* TODO: Play/Pause */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { /* TODO: Next */ }) {
                Icon(Icons.Default.KeyboardArrowRight, "Next", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { /* TODO: Repeat */ }) {
                Icon(Icons.Default.Refresh, "Repeat", tint = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
