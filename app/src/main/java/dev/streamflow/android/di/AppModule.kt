package dev.streamflow.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.streamflow.android.data.api.DeArrowService
import dev.streamflow.android.data.api.PipedApiService
import dev.streamflow.android.data.api.ReturnYouTubeDislikeService
import dev.streamflow.android.data.api.SponsorBlockService
import dev.streamflow.android.data.local.StreamFlowDatabase
import dev.streamflow.android.utils.PreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamflow_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── OkHttpClient ─────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    // ── Piped API ────────────────────────────────────────────────────────
    /**
     * FIX: Base URL is sourced from DataStore so the user can switch
     * Piped instances without reinstalling the app.
     * Default fallback: pipedapi.kavin.rocks
     */
    @Provides
    @Singleton
    @Named("piped")
    fun providePipedRetrofit(
        client: OkHttpClient,
        prefsManager: PreferencesManager
    ): Retrofit {
        val baseUrl = prefsManager.getPipedInstanceUrlSync()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePipedService(@Named("piped") retrofit: Retrofit): PipedApiService =
        retrofit.create(PipedApiService::class.java)

    // ── SponsorBlock ─────────────────────────────────────────────────────
    @Provides
    @Singleton
    @Named("sponsorblock")
    fun provideSponsorBlockRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://sponsor.ajay.app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSponsorBlockService(@Named("sponsorblock") retrofit: Retrofit): SponsorBlockService =
        retrofit.create(SponsorBlockService::class.java)

    // ── Return YouTube Dislike ───────────────────────────────────────────
    @Provides
    @Singleton
    @Named("ryd")
    fun provideRydRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://returnyoutubedislikeapi.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideRydService(@Named("ryd") retrofit: Retrofit): ReturnYouTubeDislikeService =
        retrofit.create(ReturnYouTubeDislikeService::class.java)

    // ── DeArrow ──────────────────────────────────────────────────────────
    @Provides
    @Singleton
    @Named("dearrow")
    fun provideDeArrowRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://dearrow.ajay.app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideDeArrowService(@Named("dearrow") retrofit: Retrofit): DeArrowService =
        retrofit.create(DeArrowService::class.java)

    // ── Room DB ──────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): StreamFlowDatabase =
        Room.databaseBuilder(ctx, StreamFlowDatabase::class.java, "streamflow.db")
            .fallbackToDestructiveMigration()   // safe for now; use migrations in production
            .build()

    // ── DataStore ────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.dataStore

    // ── PreferencesManager ───────────────────────────────────────────────
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext ctx: Context): PreferencesManager =
        PreferencesManager(ctx.dataStore)

    // ── ExoPlayer (non-singleton; released when PlayerViewModel clears) ──
    @Provides
    fun provideExoPlayer(@ApplicationContext ctx: Context): ExoPlayer =
        ExoPlayer.Builder(ctx)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
}
