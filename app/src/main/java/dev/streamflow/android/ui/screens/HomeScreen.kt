package dev.streamflow.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "v1.1",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
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
            // شريط الفئات الإسلامية والعامة
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                val categories = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.quran),
                    stringResource(R.string.lectures),
                    stringResource(R.string.nasheed),
                    stringResource(R.string.education),
                    stringResource(R.string.news)
                )
                categories.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index > 0) {
                                navController.navigate(Screen.Search.createRoute(label))
                            }
                        },
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
