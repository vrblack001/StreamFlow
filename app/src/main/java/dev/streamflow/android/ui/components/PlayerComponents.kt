package dev.streamflow.android.ui.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * StreamFlowPlayer (مسلم توب):
 * - Gesture Controls:
 *   * Slide vertical on Right side -> Volume control
 *   * Slide vertical on Left side  -> Brightness control
 *   * Double tap Left/Right        -> Seek -10s / +10s
 *   * Single tap                   -> Show/Hide controls overlay
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
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }

    // Gesture status overlays (Volume / Brightness indicator)
    var gestureStatusText by remember { mutableStateOf<String?>(null) }
    var gestureStatusIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3500)
            showControls = false
        }
    }

    // Auto-hide gesture indicator
    LaunchedEffect(gestureStatusText) {
        if (gestureStatusText != null) {
            delay(1200)
            gestureStatusText = null
            gestureStatusIcon = null
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
        // Native ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Touch & Gesture Detection Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                        onDoubleTap = { offset ->
                            val isLeft = offset.x < size.width / 2f
                            if (isLeft) {
                                val newPos = maxOf(0, player.currentPosition - 10_000)
                                player.seekTo(newPos)
                                gestureStatusText = "-10 ثوانٍ"
                                gestureStatusIcon = Icons.Default.Replay10
                            } else {
                                val newPos = player.currentPosition + 10_000
                                player.seekTo(newPos)
                                gestureStatusText = "+10 ثوانٍ"
                                gestureStatusIcon = Icons.Default.Forward10
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val isRightSide = change.position.x > size.width / 2f
                            val delta = -dragAmount.y // Drag up is positive

                            if (isRightSide) {
                                // Volume Control (Right Side)
                                audioManager?.let { am ->
                                    val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    val step = if (delta > 0) 1 else -1
                                    val newVol = (currentVol + step).coerceIn(0, maxVol)
                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)

                                    val percent = (newVol.toFloat() / maxVol.toFloat() * 100).toInt()
                                    gestureStatusText = "الصوت: $percent%"
                                    gestureStatusIcon = if (newVol == 0) Icons.Default.VolumeMute else Icons.Default.VolumeUp
                                }
                            } else {
                                // Brightness Control (Left Side)
                                activity?.window?.let { win ->
                                    val lp = win.attributes
                                    val currentBrightness = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                                    val changeVal = delta / size.height
                                    val newBrightness = (currentBrightness + changeVal).coerceIn(0.01f, 1.0f)
                                    lp.screenBrightness = newBrightness
                                    win.attributes = lp

                                    val percent = (newBrightness * 100).toInt()
                                    gestureStatusText = "السطوع: $percent%"
                                    gestureStatusIcon = Icons.Default.BrightnessMedium
                                }
                            }
                        }
                    )
                }
        )

        // Gesture feedback Pill (Volume / Brightness / Seek indicator)
        AnimatedVisibility(
            visible = gestureStatusText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gestureStatusIcon?.let { icon ->
                        Icon(icon, contentDescription = null, tint = Color.White)
                    }
                    Text(
                        text = gestureStatusText ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Overlay Controls (Play/Pause, Seek, Fullscreen, PiP)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Centre Action Buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { player.seekTo(maxOf(0, player.currentPosition - 10_000)) }) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "10s Back",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (isPlaying) player.pause() else player.play() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(onClick = { player.seekTo(player.currentPosition + 10_000) }) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "10s Forward",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Top Bar Actions
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(onClick = onPip) {
                        Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = Color.White)
                    }
                    IconButton(onClick = onFullscreenToggle) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Progress Bar
                val duration = player.duration.takeIf { it > 0 } ?: 1L
                val progress = (player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
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
                text = "⏭ تخطي فاصل: $category",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onSkip) {
                Text("تخطي")
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
            label = "مشاركة",
            onClick = onShare
        )
        VideoActionButton(
            icon = Icons.Default.Download,
            label = "تحميل",
            onClick = onDownload
        )
        VideoActionButton(
            icon = Icons.Default.PlaylistAdd,
            label = "حفظ",
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
