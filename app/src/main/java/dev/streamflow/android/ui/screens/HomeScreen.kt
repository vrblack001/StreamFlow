package dev.streamflow.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.streamflow.android.R
import dev.streamflow.android.ui.components.*
import dev.streamflow.android.ui.navigation.Screen
import dev.streamflow.android.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.createRoute()) }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        bottomBar = {
            StreamFlowBottomBar(navController = navController, selectedRoute = Screen.Home.route)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category chips
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                val categories = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.music),
                    stringResource(R.string.gaming),
                    stringResource(R.string.news),
                    stringResource(R.string.sports),
                    stringResource(R.string.tech)
                )
                categories.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) }
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadTrending() }
                )
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(uiState.videos, key = { it.videoId }) { video ->
                            VideoCard(
                                item = video,
                                onClick = { navController.navigate(Screen.Player.createRoute(video.videoId)) },
                                onChannelClick = {
                                    val channelId = video.uploaderUrl.substringAfterLast("/")
                                    navController.navigate(Screen.Channel.createRoute(channelId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
