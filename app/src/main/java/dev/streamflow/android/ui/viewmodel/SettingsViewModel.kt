package dev.streamflow.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.streamflow.android.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsPrefs(
    val pipedUrl: String = PreferencesManager.DEFAULT_PIPED_URL,
    val disableProxy: Boolean = false,
    val sponsorBlock: Boolean = true,
    val ryd: Boolean = true,
    val deArrow: Boolean = false,
    val autoPlay: Boolean = true,
    val backgroundPlay: Boolean = true,
    val pip: Boolean = true,
    val dynamicColor: Boolean = true,
    val themeMode: String = "system",
    val language: String = "system"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: PreferencesManager
) : ViewModel() {

    val prefs: StateFlow<SettingsPrefs> = combine(
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
    ) { values ->
        SettingsPrefs(
            pipedUrl       = values[0] as String,
            disableProxy   = values[1] as Boolean,
            sponsorBlock   = values[2] as Boolean,
            ryd            = values[3] as Boolean,
            deArrow        = values[4] as Boolean,
            autoPlay       = values[5] as Boolean,
            backgroundPlay = values[6] as Boolean,
            pip            = values[7] as Boolean,
            dynamicColor   = values[8] as Boolean,
            themeMode      = values[9] as String,
            language       = values[10] as String
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsPrefs())

    fun savePipedUrl(url: String) = viewModelScope.launch {
        prefsManager.setPipedInstanceUrl(url)
    }
    fun setDisableProxy(v: Boolean) = viewModelScope.launch { prefsManager.setDisablePipedProxy(v) }
    fun setSponsorBlock(v: Boolean) = viewModelScope.launch { prefsManager.setSponsorBlockEnabled(v) }
    fun setRyd(v: Boolean) = viewModelScope.launch { prefsManager.setRydEnabled(v) }
    fun setDeArrow(v: Boolean) = viewModelScope.launch { prefsManager.setDeArrowEnabled(v) }
    fun setAutoPlay(v: Boolean) = viewModelScope.launch { prefsManager.setAutoPlay(v) }
    fun setBackgroundPlay(v: Boolean) = viewModelScope.launch { prefsManager.setBackgroundPlay(v) }
    fun setPip(v: Boolean) = viewModelScope.launch { prefsManager.setPipEnabled(v) }
    fun setDynamicColor(v: Boolean) = viewModelScope.launch { prefsManager.setDynamicColor(v) }
}
