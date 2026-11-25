package dev.streamflow.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import dev.streamflow.android.ui.screens.*

sealed class Screen(val route: String) {
    object Home        : Screen("home")
    object Search      : Screen("search?query={query}") {
        fun createRoute(query: String = "") = "search?query=$query"
    }
    object Trending    : Screen("trending")
    object Subscriptions : Screen("subscriptions")
    object Library     : Screen("library")
    object Settings    : Screen("settings")
    object Player      : Screen("player/{videoId}") {
        fun createRoute(videoId: String) = "player/$videoId"
    }
    object Channel     : Screen("channel/{channelId}") {
        fun createRoute(channelId: String) = "channel/$channelId"
    }
    object Playlist    : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist/$playlistId"
    }
    object Downloads   : Screen("downloads")
    object History     : Screen("history")
    object Login       : Screen("login")
}

@Composable
fun StreamFlowNavGraph(startVideoId: String? = null) {
    val navController = rememberNavController()

    // If app was opened via a YouTube share, go straight to Player
    val startDest = if (startVideoId != null)
        Screen.Player.createRoute(startVideoId)
    else
        Screen.Home.route

    NavHost(navController = navController, startDestination = startDest) {

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.Search.route,
            arguments = listOf(navArgument("query") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            SearchScreen(
                navController = navController,
                initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            )
        }

        composable(Screen.Trending.route) {
            TrendingScreen(navController = navController)
        }

        composable(Screen.Subscriptions.route) {
            SubscriptionsScreen(navController = navController)
        }

        composable(Screen.Library.route) {
            LibraryScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "streamflow://video?id={videoId}" })
        ) { backStackEntry ->
            PlayerScreen(
                navController = navController,
                videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            )
        }

        composable(
            route = Screen.Channel.route,
            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
        ) { backStackEntry ->
            ChannelScreen(
                navController = navController,
                channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            )
        }

        composable(
            route = Screen.Playlist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            PlaylistScreen(
                navController = navController,
                playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(navController = navController)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
    }
}
