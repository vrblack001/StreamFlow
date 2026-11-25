package dev.streamflow.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.streamflow.android.R
import dev.streamflow.android.ui.components.*
import dev.streamflow.android.ui.navigation.Screen
import dev.streamflow.android.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: SearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf(initialQuery) }
    val uiState by viewModel.uiState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) viewModel.search(initialQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.onQueryChanged(it)
                        },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            if (query.isNotBlank()) viewModel.search(query)
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = ""; viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Filter chips
            var selectedFilter by remember { mutableStateOf("all") }
            val filters = mapOf(
                "all"      to stringResource(R.string.filter_all),
                "videos"   to stringResource(R.string.filter_videos),
                "channels" to stringResource(R.string.filter_channels),
                "playlists" to stringResource(R.string.filter_playlists),
                "music_songs" to stringResource(R.string.filter_music)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters.entries.toList()) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = {
                            selectedFilter = key
                            if (query.isNotBlank()) viewModel.search(query, filter = key)
                        },
                        label = { Text(label) }
                    )
                }
            }

            when {
                // Show autocomplete suggestions
                uiState.results.isEmpty() && suggestions.isNotEmpty() && query.isNotBlank() -> {
                    LazyColumn {
                        items(suggestions) { suggestion ->
                            ListItem(
                                headlineContent = { Text(suggestion) },
                                leadingContent = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                            HorizontalDivider()
                        }
                    }
                }
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.search(query, filter = selectedFilter) }
                )
                uiState.results.isNotEmpty() -> {
                    LazyColumn {
                        items(uiState.results, key = { it.videoId }) { video ->
                            VideoCard(
                                item = video,
                                onClick = { navController.navigate(Screen.Player.createRoute(video.videoId)) },
                                onChannelClick = {
                                    val channelId = video.uploaderUrl.substringAfterLast("/")
                                    navController.navigate(Screen.Channel.createRoute(channelId))
                                }
                            )
                        }

                        // Pagination: load next page
                        if (uiState.nextpage != null) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    if (uiState.isLoadingMore) {
                                        CircularProgressIndicator()
                                    } else {
                                        Button(onClick = {
                                            viewModel.loadMoreResults(query, selectedFilter)
                                        }) {
                                            Text(stringResource(R.string.load_more))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                query.isBlank() -> {
                    // Show search history
                    SearchHistorySection(
                        history = uiState.searchHistory,
                        onItemClick = { q -> query = q; viewModel.search(q) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
