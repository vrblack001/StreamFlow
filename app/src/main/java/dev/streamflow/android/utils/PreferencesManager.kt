package dev.streamflow.android.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized DataStore preferences manager.
 * Exposes reactive Flow-based reads and suspend-function writes.
 *
 * FIX (AI common issue): All settings use typed Keys, never raw String keys.
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        // Engine Source
        val USE_DIRECT_YOUTUBE   = booleanPreferencesKey("use_direct_youtube")

        // Network
        val PIPED_INSTANCE_URL   = stringPreferencesKey("piped_instance_url")
        val DISABLE_PIPED_PROXY  = booleanPreferencesKey("disable_piped_proxy")

        // Playback
        val DEFAULT_QUALITY      = stringPreferencesKey("default_quality")
        val GESTURES_ENABLED     = booleanPreferencesKey("gestures_enabled")
        val PLAYBACK_SPEED       = floatPreferencesKey("playback_speed")
        val AUTO_PLAY            = booleanPreferencesKey("auto_play")
        val BACKGROUND_PLAY      = booleanPreferencesKey("background_play")
        val PIP_ENABLED          = booleanPreferencesKey("pip_enabled")

        // Enhancement toggles
        val SPONSOR_BLOCK_ENABLED  = booleanPreferencesKey("sponsor_block_enabled")
        val RYD_ENABLED            = booleanPreferencesKey("ryd_enabled")
        val DEARROW_ENABLED        = booleanPreferencesKey("dearrow_enabled")

        // UI
        val THEME_MODE             = stringPreferencesKey("theme_mode")          // system/light/dark
        val DYNAMIC_COLOR          = booleanPreferencesKey("dynamic_color")
        val LANGUAGE               = stringPreferencesKey("language")            // system/ar/en/...
        val TRENDING_REGION        = stringPreferencesKey("trending_region")

        // Auth
        val PIPED_AUTH_TOKEN       = stringPreferencesKey("piped_auth_token")
        val PIPED_USERNAME         = stringPreferencesKey("piped_username")

        // Downloads
        val DOWNLOAD_PATH          = stringPreferencesKey("download_path")
        val DOWNLOAD_QUALITY       = stringPreferencesKey("download_quality")

        // Defaults
        const val DEFAULT_PIPED_URL = "https://pipedapi.kavin.rocks/"
    }

    // ── Getters (Flow) ───────────────────────────────────────────────────
    val useDirectYoutube: Flow<Boolean> = dataStore.data.map {
        it[USE_DIRECT_YOUTUBE] ?: true
    }
    val gesturesEnabled: Flow<Boolean> = dataStore.data.map {
        it[GESTURES_ENABLED] ?: true
    }
    val pipedInstanceUrl: Flow<String> = dataStore.data.map {
        it[PIPED_INSTANCE_URL] ?: DEFAULT_PIPED_URL
    }
    val disablePipedProxy: Flow<Boolean> = dataStore.data.map {
        it[DISABLE_PIPED_PROXY] ?: false
    }
    val defaultQuality: Flow<String> = dataStore.data.map {
        it[DEFAULT_QUALITY] ?: "auto"
    }
    val playbackSpeed: Flow<Float> = dataStore.data.map {
        it[PLAYBACK_SPEED] ?: 1.0f
    }
    val autoPlay: Flow<Boolean> = dataStore.data.map {
        it[AUTO_PLAY] ?: true
    }
    val backgroundPlay: Flow<Boolean> = dataStore.data.map {
        it[BACKGROUND_PLAY] ?: true
    }
    val pipEnabled: Flow<Boolean> = dataStore.data.map {
        it[PIP_ENABLED] ?: true
    }
    val sponsorBlockEnabled: Flow<Boolean> = dataStore.data.map {
        it[SPONSOR_BLOCK_ENABLED] ?: true
    }
    val rydEnabled: Flow<Boolean> = dataStore.data.map {
        it[RYD_ENABLED] ?: true
    }
    val deArrowEnabled: Flow<Boolean> = dataStore.data.map {
        it[DEARROW_ENABLED] ?: false
    }
    val themeMode: Flow<String> = dataStore.data.map {
        it[THEME_MODE] ?: "system"
    }
    val dynamicColor: Flow<Boolean> = dataStore.data.map {
        it[DYNAMIC_COLOR] ?: true
    }
    val language: Flow<String> = dataStore.data.map {
        it[LANGUAGE] ?: "system"
    }
    val trendingRegion: Flow<String> = dataStore.data.map {
        it[TRENDING_REGION] ?: "US"
    }
    val authToken: Flow<String?> = dataStore.data.map {
        it[PIPED_AUTH_TOKEN]
    }
    val username: Flow<String?> = dataStore.data.map {
        it[PIPED_USERNAME]
    }

    // ── Sync read (use sparingly, only during DI setup) ──────────────────
    fun getPipedInstanceUrlSync(): String = runBlocking {
        dataStore.data.first()[PIPED_INSTANCE_URL] ?: DEFAULT_PIPED_URL
    }

    // ── Setters ──────────────────────────────────────────────────────────
    suspend fun setUseDirectYoutube(value: Boolean) = dataStore.edit {
        it[USE_DIRECT_YOUTUBE] = value
    }
    suspend fun setGesturesEnabled(value: Boolean) = dataStore.edit {
        it[GESTURES_ENABLED] = value
    }
    suspend fun setPipedInstanceUrl(url: String) = dataStore.edit {
        it[PIPED_INSTANCE_URL] = if (url.endsWith("/")) url else "$url/"
    }
    suspend fun setDisablePipedProxy(value: Boolean) = dataStore.edit {
        it[DISABLE_PIPED_PROXY] = value
    }
    suspend fun setDefaultQuality(value: String) = dataStore.edit {
        it[DEFAULT_QUALITY] = value
    }
    suspend fun setPlaybackSpeed(value: Float) = dataStore.edit {
        it[PLAYBACK_SPEED] = value
    }
    suspend fun setAutoPlay(value: Boolean) = dataStore.edit {
        it[AUTO_PLAY] = value
    }
    suspend fun setBackgroundPlay(value: Boolean) = dataStore.edit {
        it[BACKGROUND_PLAY] = value
    }
    suspend fun setPipEnabled(value: Boolean) = dataStore.edit {
        it[PIP_ENABLED] = value
    }
    suspend fun setSponsorBlockEnabled(value: Boolean) = dataStore.edit {
        it[SPONSOR_BLOCK_ENABLED] = value
    }
    suspend fun setRydEnabled(value: Boolean) = dataStore.edit {
        it[RYD_ENABLED] = value
    }
    suspend fun setDeArrowEnabled(value: Boolean) = dataStore.edit {
        it[DEARROW_ENABLED] = value
    }
    suspend fun setThemeMode(value: String) = dataStore.edit {
        it[THEME_MODE] = value
    }
    suspend fun setDynamicColor(value: Boolean) = dataStore.edit {
        it[DYNAMIC_COLOR] = value
    }
    suspend fun setLanguage(value: String) = dataStore.edit {
        it[LANGUAGE] = value
    }
    suspend fun setTrendingRegion(value: String) = dataStore.edit {
        it[TRENDING_REGION] = value
    }
    suspend fun setAuthToken(token: String?) = dataStore.edit {
        if (token != null) it[PIPED_AUTH_TOKEN] = token
        else it.remove(PIPED_AUTH_TOKEN)
    }
    suspend fun setUsername(name: String?) = dataStore.edit {
        if (name != null) it[PIPED_USERNAME] = name
        else it.remove(PIPED_USERNAME)
    }
    suspend fun logout() = dataStore.edit {
        it.remove(PIPED_AUTH_TOKEN)
        it.remove(PIPED_USERNAME)
    }
}
