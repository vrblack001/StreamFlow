package dev.streamflow.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.streamflow.android.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsPrefs(
    val useDirectYoutube: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val pipedUrl: String = PreferencesManager.DEFAULT_PIPED_URL,
    val disableProxy: Boolean = false,
    val sponsorBlock: Boolean = true,
    val ryd: Boolean = true,
    val deArrow: Boolean = false,
    val autoPlay: Boolean = true,
    val backgroundPlay: Boolean = true,
    val pip: Boolean = true,
    val dynamicColor: Boolean = false,
    val themeMode: String = "dark",
    val language: String = "ar"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: PreferencesManager
) : ViewModel() {

    val prefs: StateFlow<SettingsPrefs> = combine(
        listOf(
            prefsManager.useDirectYoutube,
            prefsManager.gesturesEnabled,
            prefsManager.pipedInstanceUrl,
            prefsManager.disablePipedProxy,
            prefsManager.sponsorBlockEnabled,
            prefsManager.rydEnabled,
            prefsManager.deArrowEnabled,
            prefsManager.autoPlay,
            prefsManager.backgroundPlay,
            prefsManager.pipEnabled,
            prefsManager.dynamicColor,
            prefsManager.themeMode,
            prefsManager.language
        )
    ) { values ->
        SettingsPrefs(
            useDirectYoutube = values[0] as Boolean,
            gesturesEnabled  = values[1] as Boolean,
            pipedUrl         = values[2] as String,
            disableProxy     = values[3] as Boolean,
            sponsorBlock     = values[4] as Boolean,
            ryd              = values[5] as Boolean,
            deArrow          = values[6] as Boolean,
            autoPlay         = values[7] as Boolean,
            backgroundPlay   = values[8] as Boolean,
            pip              = values[9] as Boolean,
            dynamicColor     = values[10] as Boolean,
            themeMode        = values[11] as String,
            language         = values[12] as String
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsPrefs())

    fun setUseDirectYoutube(v: Boolean) = viewModelScope.launch { prefsManager.setUseDirectYoutube(v) }
    fun setGesturesEnabled(v: Boolean) = viewModelScope.launch { prefsManager.setGesturesEnabled(v) }
    fun savePipedUrl(url: String) = viewModelScope.launch { prefsManager.setPipedInstanceUrl(url) }
    fun setDisableProxy(v: Boolean) = viewModelScope.launch { prefsManager.setDisablePipedProxy(v) }
    fun setSponsorBlock(v: Boolean) = viewModelScope.launch { prefsManager.setSponsorBlockEnabled(v) }
    fun setRyd(v: Boolean) = viewModelScope.launch { prefsManager.setRydEnabled(v) }
    fun setDeArrow(v: Boolean) = viewModelScope.launch { prefsManager.setDeArrowEnabled(v) }
    fun setAutoPlay(v: Boolean) = viewModelScope.launch { prefsManager.setAutoPlay(v) }
    fun setBackgroundPlay(v: Boolean) = viewModelScope.launch { prefsManager.setBackgroundPlay(v) }
    fun setPip(v: Boolean) = viewModelScope.launch { prefsManager.setPipEnabled(v) }
    fun setDynamicColor(v: Boolean) = viewModelScope.launch { prefsManager.setDynamicColor(v) }
    fun setThemeMode(v: String) = viewModelScope.launch { prefsManager.setThemeMode(v) }
}
