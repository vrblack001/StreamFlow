package dev.streamflow.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.streamflow.android.data.model.StreamItem
import dev.streamflow.android.data.repository.VideoRepository
import dev.streamflow.android.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrendingUiState(
    val videos: List<StreamItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val repository: VideoRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrendingUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val region = prefs.trendingRegion.first()
            repository.getTrending(region)
                .onSuccess { _uiState.update { s -> s.copy(videos = it, isLoading = false) } }
                .onFailure { e -> _uiState.update { s -> s.copy(isLoading = false, error = e.localizedMessage) } }
        }
    }
}
