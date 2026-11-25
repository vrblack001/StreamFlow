package dev.streamflow.android.utils

import java.util.concurrent.TimeUnit

/** Format seconds → "HH:MM:SS" or "MM:SS" */
fun Long.formatDuration(): String {
    val hours = TimeUnit.SECONDS.toHours(this)
    val minutes = TimeUnit.SECONDS.toMinutes(this) % 60
    val secs = this % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%d:%02d".format(minutes, secs)
    }
}

/** Format large numbers → "1.2M", "54K", etc. */
fun Long.formatCount(): String = when {
    this >= 1_000_000_000 -> "${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000     -> "${"%.1f".format(this / 1_000_000.0)}M"
    this >= 1_000         -> "${"%.1f".format(this / 1_000.0)}K"
    else                  -> this.toString()
}

/** Sanitize a Piped instance URL — always ends with "/" */
fun String.normalizePipedUrl(): String =
    trim().let { if (it.endsWith("/")) it else "$it/" }

/** Extract YouTube video ID from any known URL format */
fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
        Regex("[?&]v=([A-Za-z0-9_-]{11})"),
        Regex("shorts/([A-Za-z0-9_-]{11})"),
        Regex("embed/([A-Za-z0-9_-]{11})")
    )
    return patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.get(1) }
}
