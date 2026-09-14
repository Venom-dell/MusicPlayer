package com.example.musicplayer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Your Library", Icons.Default.List)
    object Player : Screen("player", "Player", null)
    object Equalizer : Screen("equalizer", "Equalizer", null)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Library
)
