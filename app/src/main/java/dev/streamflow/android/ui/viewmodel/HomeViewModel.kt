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

data class HomeUiState(
    val videos: List<StreamItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VideoRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrending()
    }

    fun loadTrending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val region = prefs.trendingRegion.first()
            repository.getTrending(region)
                .onSuccess { videos ->
                    _uiState.update { it.copy(videos = videos, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = buildUserFriendlyError(e)
                        )
                    }
                }
        }
    }

    private fun buildUserFriendlyError(e: Throwable): String {
        return when {
            e.message?.contains("429") == true || e.message?.contains("403") == true ->
                "YouTube blocks the current Piped instance. Try switching the instance in Settings."
            e.message?.contains("timeout") == true || e.message?.contains("connect") == true ->
                "No internet connection. Please check your network."
            else -> "Something went wrong: ${e.localizedMessage}"
        }
    }
}
