package dev.streamflow.android.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.streamflow.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Background media playback service using Media3.
 *
 * FIX (LibreTube issue #1): Declared as foregroundServiceType="mediaPlayback"
 * in manifest — required for Android 14+ to avoid ForegroundServiceStartNotAllowedException.
 *
 * FIX (LibreTube issue #2): Session ID uniqueness — MediaSession is created ONCE
 * and destroyed with the service. Never recreate during autoplay transitions.
 *
 * FIX (LibreTube issue #3): AudioAttributes set to USAGE_MEDIA with
 * CONTENT_TYPE_MOVIE so Android properly handles audio focus (pause on call, etc.)
 */
@AndroidEntryPoint
class PlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus= */ true
            )
            .setHandleAudioBecomingNoisy(true)   // pause on headphone unplug
            .setWakeMode(C.WAKE_MODE_NETWORK)    // keep CPU + WiFi awake during BG play
            .build()

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    // ── Custom session commands (SponsorBlock skip, etc.) ────────────────
    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CMD_SKIP_SPONSOR, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.accept(
                sessionCommands,
                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == CMD_SKIP_SPONSOR) {
                val skipToMs = args.getLong("skip_to_ms", -1L)
                if (skipToMs >= 0) player.seekTo(skipToMs)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    companion object {
        const val CMD_SKIP_SPONSOR = "cmd_skip_sponsor"
    }
}
