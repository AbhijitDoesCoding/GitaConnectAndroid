package com.gitaconnect.app

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gitaconnect.app.authentication.LoginScreen
import com.gitaconnect.app.feed.FeedScreen
import com.gitaconnect.app.library.models.Chapter
import com.gitaconnect.app.library.models.Verse
import com.gitaconnect.app.library.ui.LibraryHomeScreen
import com.gitaconnect.app.library.ui.GitaLibraryChaptersScreen
import com.gitaconnect.app.library.ui.ReadVerseScreen
import com.gitaconnect.app.library.ui.VerseListScreen
import com.gitaconnect.app.library.ui.ChallengesScreen
import com.gitaconnect.app.library.ui.MantraLibraryScreen
import com.gitaconnect.app.library.viewmodel.GitaLibraryViewModel
import com.gitaconnect.app.mentor.MentorScreen
import com.gitaconnect.app.profilepage.*

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home    : BottomNavItem("home",    "Home",        Icons.Filled.Home)
    object Mentor  : BottomNavItem("mentor",  "Gita Mentor", Icons.Filled.Email)
    object Feed    : BottomNavItem("feed",    "Feed",        Icons.Filled.PlayArrow)
    object Profile : BottomNavItem("profile", "Profile",     Icons.Filled.Person)
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
        containerColor = Color(0xFFFAF7F0), // GitaBeigeLight to fix black screen transitions
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = Color(0xFFE0D9CC), // GitaDivider
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = Color(0xFFFFFDF9), // WarmBeigeLight solid bottom bar
                    contentColor = Color(0xFF2C251C) // TextDark
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color(0xFF7A7063), // TextMuted
                                unselectedTextColor  = Color(0xFF7A7063), // TextMuted
                                selectedIconColor    = Color(0xFFE27D60), // GitaSaffron
                                selectedTextColor    = Color(0xFFE27D60), // GitaSaffron
                                indicatorColor       = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(BottomNavItem.Home.route)   { HomeLibraryFlow() }
            composable(BottomNavItem.Mentor.route) { MentorScreen() }
            composable(BottomNavItem.Feed.route)   { FeedScreen() }
            composable(BottomNavItem.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                val currentScreen by profileViewModel.currentScreen.collectAsState()

                when (currentScreen) {
                    Screen.PROFILE       -> ProfileScreen(viewModel = profileViewModel)
                    Screen.LOGIN         -> LoginScreen(viewModel = profileViewModel)
                    Screen.PERSONAL_INFO -> PersonalInfoScreen(viewModel = profileViewModel)
                    Screen.LIKED         -> LikedScreen(viewModel = profileViewModel)
                    Screen.STATS         -> StatsScreen(viewModel = profileViewModel)
                    Screen.ACCESSIBILITY -> AccessibilityScreen(viewModel = profileViewModel)
                    Screen.NOTIFICATIONS -> NotificationsScreen(viewModel = profileViewModel)
                    Screen.ABOUT         -> AboutScreen(viewModel = profileViewModel)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Nested Library navigation flow (Home tab)
// ---------------------------------------------------------------------------

@Composable
fun HomeLibraryFlow() {
    val libraryNavController = rememberNavController()
    val gitaViewModel: GitaLibraryViewModel = viewModel()

    // Shared state passed between library screens (avoids Parcelable/serialization)
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var selectedVerse   by remember { mutableStateOf<Verse?>(null) }
    var selectedVerseList by remember { mutableStateOf<List<Verse>>(emptyList()) }

    NavHost(
        navController = libraryNavController,
        startDestination = "library_home"
    ) {
        composable("library_home") {
            LibraryHomeScreen(
                onLibraryClick = {
                    libraryNavController.navigate("chapter_list")
                },
                onChallengesClick = {
                    libraryNavController.navigate("challenges")
                },
                onMantrasClick = {
                    libraryNavController.navigate("mantras")
                },
                onVerseClick = { verse, allVerses ->
                    selectedVerse = verse
                    selectedVerseList = allVerses
                    libraryNavController.navigate("read_verse")
                }
            )
        }
        composable("chapter_list") {
            GitaLibraryChaptersScreen(
                viewModel = gitaViewModel,
                onChapterClick = { chapter ->
                    selectedChapter = chapter
                    libraryNavController.navigate("verse_list")
                },
                onBack = {
                    libraryNavController.popBackStack()
                }
            )
        }
        composable("verse_list") {
            selectedChapter?.let { chapter ->
                VerseListScreen(
                    chapter = chapter,
                    viewModel = gitaViewModel,
                    onVerseClick = { verse, allVerses ->
                        selectedVerse = verse
                        selectedVerseList = allVerses
                        libraryNavController.navigate("read_verse")
                    },
                    onBack = { libraryNavController.popBackStack() }
                )
            }
        }
        composable("read_verse") {
            val verse = selectedVerse
            if (verse != null) {
                ReadVerseScreen(
                    initialVerse = verse,
                    allVerses    = selectedVerseList,
                    onBack       = { libraryNavController.popBackStack() }
                )
            }
        }
        composable("challenges") {
            ChallengesScreen(
                onBack = { libraryNavController.popBackStack() }
            )
        }
        composable("mantras") {
            MantraLibraryScreen(
                onBack = { libraryNavController.popBackStack() }
            )
        }
    }
}
