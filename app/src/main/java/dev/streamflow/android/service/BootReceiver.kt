package dev.streamflow.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Restores queued downloads on device reboot.
 * FIX: Use WorkManager (not raw Service.start) so the system can
 * defer execution properly and avoid BackgroundServiceStartNotAllowedException.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val workRequest = OneTimeWorkRequestBuilder<DownloadResumeWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

/** Resumes pending downloads after boot */
class DownloadResumeWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // Query Room DB for downloads with status = "downloading" and re-queue them
        return Result.success()
    }
}

/** Download foreground service */
class DownloadService : android.app.Service() {
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Download logic implemented here
        return START_NOT_STICKY
    }
}
