package com.example.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.ui.components.BottomPlayerBar
import com.example.musicplayer.ui.navigation.Screen
import com.example.musicplayer.ui.navigation.bottomNavItems
import com.example.musicplayer.ui.screens.HomeScreen
import com.example.musicplayer.ui.screens.LibraryScreen
import com.example.musicplayer.ui.screens.FullPlayerScreen
import com.example.musicplayer.ui.screens.EqualizerScreen
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import com.example.musicplayer.ui.theme.SpotifyBlack
import com.example.musicplayer.ui.theme.SpotifyDarkGray
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            Column {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                if (currentRoute != Screen.Player.route && currentRoute != Screen.Equalizer.route) {
                    BottomPlayerBar(
                        onPlayerClick = {
                            navController.navigate(Screen.Player.route)
                        }
                    )
                }
                BottomNavigationBar(navController = navController)
            }
        },
        containerColor = SpotifyBlack
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationHost(navController = navController)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { item.icon?.let { Icon(it, contentDescription = item.title) } },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = SpotifyDarkGray
                )
            )
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Library.route) {
            LibraryScreen()
        }
        composable(Screen.Player.route) {
            FullPlayerScreen(
                onBackClick = { navController.popBackStack() },
                onEqualizerClick = { navController.navigate(Screen.Equalizer.route) }
            )
        }
        composable(Screen.Equalizer.route) {
            EqualizerScreen(
                presets = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock"),
                onPresetSelected = { /* TODO */ },
                onBandLevelChanged = { band, level -> /* TODO */ }
            )
        }
    }
}
