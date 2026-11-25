package dev.streamflow.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.streamflow.android.R
import dev.streamflow.android.ui.components.*
import dev.streamflow.android.ui.navigation.Screen
import dev.streamflow.android.ui.viewmodel.TrendingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    navController: NavController,
    viewModel: TrendingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_trending)) })
        },
        bottomBar = {
            StreamFlowBottomBar(navController = navController, selectedRoute = Screen.Trending.route)
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorView(uiState.error!!, onRetry = { viewModel.load() })
            else -> LazyColumn(contentPadding = padding) {
                items(uiState.videos, key = { it.videoId }) { video ->
                    VideoCard(
                        item = video,
                        onClick = { navController.navigate(Screen.Player.createRoute(video.videoId)) },
                        onChannelClick = {
                            navController.navigate(
                                Screen.Channel.createRoute(video.uploaderUrl.substringAfterLast("/"))
                            )
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Stub screens — each can be fleshed out as a full feature
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_subscriptions)) }) },
        bottomBar = { StreamFlowBottomBar(navController, Screen.Subscriptions.route) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_library)) }) },
        bottomBar = { StreamFlowBottomBar(navController, Screen.Library.route) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(navController: NavController, channelId: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(channelId) }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(navController: NavController, playlistId: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(playlistId) }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Downloads") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Login to Piped") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}
