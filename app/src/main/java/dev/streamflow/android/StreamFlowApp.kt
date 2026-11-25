package dev.streamflow.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import dev.streamflow.android.data.network.DownloaderImpl
import org.schabi.newpipe.extractor.NewPipe
import javax.inject.Inject

/**
 * StreamFlow Application class.
 *
 * FIX (NewPipe issue): NewPipe.init() must be called ONCE in Application.onCreate()
 * to prevent NetworkOnMainThreadException and multiple-init crashes.
 */
@HiltAndroidApp
class StreamFlowApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Initialize NewPipeExtractor with our custom OkHttp downloader
        NewPipe.init(DownloaderImpl.getInstance())
    }

    /**
     * Global Coil ImageLoader with disk + memory cache.
     * Thumbnails are cached aggressively to avoid redundant network calls.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.03)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}
