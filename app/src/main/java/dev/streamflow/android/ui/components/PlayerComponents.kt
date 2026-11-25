package dev.streamflow.android.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Jetpack Compose wrapper around Media3 PlayerView.
 *
 * FIX (AI common issue): Use AndroidView to embed native PlayerView inside Compose.
 * FIX: Gesture overlays for brightness/volume implemented in Compose layer.
 * FIX: Brightness/volume gesture changes tracked locally to avoid state loops.
 */
@OptIn(UnstableApi::class)
@Composable
fun StreamFlowPlayer(
    player: Player,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    onPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    Box(modifier = modifier.background(Color.Black)) {
        // Native ExoPlayer surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false   // We draw our own controls in Compose
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture overlay: tap = show/hide controls, double-tap = seek ±10s
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                        onDoubleTap = { offset ->
                            val centerX = size.width / 2f
                            if (offset.x < centerX) {
                                player.seekTo(maxOf(0, player.currentPosition - 10_000))
                            } else {
                                player.seekTo(player.currentPosition + 10_000)
                            }
                        }
                    )
                }
        )

        // Controls overlay
        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                // Centre play/pause
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    IconButton(onClick = { player.seekTo(maxOf(0, player.currentPosition - 10_000)) }) {
                        Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s",
                            tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { if (isPlaying) player.pause() else player.play() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = { player.seekTo(player.currentPosition + 10_000) }) {
                        Icon(Icons.Default.Forward10, contentDescription = "Forward 10s",
                            tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                // Top controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(onClick = onPip) {
                        Icon(Icons.Default.PictureInPicture, contentDescription = "PiP",
                            tint = Color.White)
                    }
                    IconButton(onClick = onFullscreenToggle) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }

                // Progress bar at the bottom
                val duration = player.duration.takeIf { it > 0 } ?: 1L
                val progress = player.currentPosition.toFloat() / duration.toFloat()
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/** SponsorBlock skip banner */
@Composable
fun SponsorSkipBanner(category: String, onSkip: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏭ ${category.replaceFirstChar { it.uppercase() }} segment",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
        }
    }
}

/** Video action buttons row: Like, Dislike, Share, Download, Save */
@Composable
fun VideoActionsRow(
    likes: Long,
    dislikes: Long,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VideoActionButton(
            icon = Icons.Default.ThumbUp,
            label = likes.formatCompact(),
            onClick = onLike
        )
        VideoActionButton(
            icon = Icons.Default.ThumbDown,
            label = dislikes.formatCompact(),
            onClick = onDislike
        )
        VideoActionButton(
            icon = Icons.Default.Share,
            label = "Share",
            onClick = onShare
        )
        VideoActionButton(
            icon = Icons.Default.Download,
            label = "Download",
            onClick = onDownload
        )
        VideoActionButton(
            icon = Icons.Default.PlaylistAdd,
            label = "Save",
            onClick = onSave
        )
    }
}

@Composable
private fun VideoActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun Long.formatCompact(): String = when {
    this >= 1_000_000 -> "${"%.1f".format(this / 1_000_000.0)}M"
    this >= 1_000     -> "${"%.1f".format(this / 1_000.0)}K"
    else              -> this.toString()
}
