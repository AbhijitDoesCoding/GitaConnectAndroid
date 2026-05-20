package com.gitaconnect.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gitaconnect.app.feed.FeedScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Mentor : BottomNavItem("mentor", "Gita Mentor", Icons.Filled.Email)
    object Feed : BottomNavItem("feed", "Feed", Icons.Filled.PlayArrow)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Mentor,
        BottomNavItem.Feed,
        BottomNavItem.Profile
    )

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black.copy(alpha = 0.4f),
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable(BottomNavItem.Home.route) { PlaceholderScreen("Home") }
            composable(BottomNavItem.Mentor.route) { PlaceholderScreen("Gita Mentor") }
            composable(BottomNavItem.Feed.route) { FeedScreen() }
            composable(BottomNavItem.Profile.route) { PlaceholderScreen("Profile") }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title (Under Construction)")
    }
}
