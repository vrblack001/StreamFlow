package dev.streamflow.android.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.streamflow.android.R
import dev.streamflow.android.ui.components.*
import dev.streamflow.android.ui.navigation.Screen
import dev.streamflow.android.ui.viewmodel.PlayerViewModel
import dev.streamflow.android.utils.formatCount
import dev.streamflow.android.utils.formatDuration

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    videoId: String,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val uiState by viewModel.uiState.collectAsState()
    var isFullscreen by remember { mutableStateOf(false) }
    var showDescription by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }

    // ── Fullscreen handling ──────────────────────────────────────────────
    LaunchedEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    title = {}
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullscreen) PaddingValues(0.dp) else paddingValues)
        ) {
            // ── Video player surface ──────────────────────────────────────
            StreamFlowPlayer(
                player = viewModel.player,
                isFullscreen = isFullscreen,
                onFullscreenToggle = { isFullscreen = !isFullscreen },
                onPip = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity?.enterPictureInPictureMode(
                            PictureInPictureParams.Builder()
                                .setAspectRatio(Rational(16, 9))
                                .build()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isFullscreen) Modifier.fillMaxSize()
                        else Modifier.aspectRatio(16f / 9f)
                    )
            )

            if (!isFullscreen) {
                when {
                    uiState.isLoading -> LoadingIndicator()
                    uiState.error != null -> ErrorView(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadVideo(videoId) }
                    )
                    uiState.stream != null -> {
                        val stream = uiState.stream!!
                        LazyColumn {
                            // ── Video info ───────────────────────────────
                            item {
                                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    // SponsorBlock skip banner
                                    if (uiState.showSponsorSkipBanner) {
                                        SponsorSkipBanner(
                                            category = uiState.currentSponsorCategory,
                                            onSkip = { viewModel.skipCurrentSegment() }
                                        )
                                    }

                                    // Title (DeArrow replacement if enabled)
                                    Text(
                                        text = uiState.displayTitle ?: stream.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = if (showDescription) Int.MAX_VALUE else 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.clickable { showDescription = !showDescription }
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    // Views • Upload date
                                    Text(
                                        text = "${stream.views.formatCount()} ${stringResource(R.string.views)} • ${stream.uploadDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Action row: Like • Dislike • Share • Download • Save
                                    VideoActionsRow(
                                        likes = stream.likes,
                                        dislikes = uiState.dislikes ?: stream.dislikes,
                                        onLike = { viewModel.like() },
                                        onDislike = { viewModel.dislike() },
                                        onShare = { viewModel.shareVideo(context, videoId) },
                                        onDownload = { viewModel.showDownloadDialog() },
                                        onSave = { viewModel.showPlaylistDialog() }
                                    )

                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                                    // Channel row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val channelId = stream.uploaderUrl.substringAfterLast("/")
                                                    navController.navigate(Screen.Channel.createRoute(channelId))
                                                }
                                        ) {
                                            AsyncImage(
                                                model = stream.uploaderAvatar,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = stream.uploader,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                        }
                                        // Subscribe button
                                        Button(
                                            onClick = { viewModel.toggleSubscription() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (uiState.isSubscribed)
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else
                                                    MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(
                                                if (uiState.isSubscribed)
                                                    stringResource(R.string.unsubscribe)
                                                else
                                                    stringResource(R.string.subscribe)
                                            )
                                        }
                                    }

                                    // Description
                                    AnimatedVisibility(visible = showDescription) {
                                        Text(
                                            text = stream.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }

                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                }
                            }

                            // ── Tabs: Related / Comments ─────────────────
                            item {
                                TabRow(selectedTabIndex = selectedTab) {
                                    Tab(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        text = { Text(stringResource(R.string.related)) }
                                    )
                                    Tab(
                                        selected = selectedTab == 1,
                                        onClick = {
                                            selectedTab = 1
                                            if (uiState.comments.isEmpty()) viewModel.loadComments(videoId)
                                        },
                                        text = { Text(stringResource(R.string.comments)) }
                                    )
                                    Tab(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        text = { Text(stringResource(R.string.chapters)) }
                                    )
                                }
                            }

                            when (selectedTab) {
                                // Related videos
                                0 -> items(stream.relatedStreams, key = { it.videoId }) { related ->
                                    VideoCardCompact(
                                        item = related,
                                        onClick = { navController.navigate(Screen.Player.createRoute(related.videoId)) }
                                    )
                                }
                                // Comments
                                1 -> {
                                    if (uiState.isLoadingComments) {
                                        item { LoadingIndicator() }
                                    } else {
                                        items(uiState.comments) { comment ->
                                            CommentItem(comment = comment)
                                        }
                                    }
                                }
                                // Chapters
                                2 -> items(stream.chapters) { chapter ->
                                    ChapterItem(
                                        chapter = chapter,
                                        onClick = { viewModel.seekToChapter(chapter.start) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
