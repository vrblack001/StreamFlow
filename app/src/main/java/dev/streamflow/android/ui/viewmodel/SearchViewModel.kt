package dev.streamflow.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.streamflow.android.data.model.SearchResult
import dev.streamflow.android.data.model.StreamItem
import dev.streamflow.android.data.repository.VideoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val results: List<StreamItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val nextpage: String? = null,
    val searchHistory: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private var suggestionJob: Job? = null

    fun onQueryChanged(query: String) {
        // Debounce suggestions: wait 300ms after last keystroke
        suggestionJob?.cancel()
        if (query.length < 2) { _suggestions.value = emptyList(); return }
        suggestionJob = viewModelScope.launch {
            delay(300)
            repository.getSearchSuggestions(query)
                .onSuccess { _suggestions.value = it }
        }
    }

    fun search(query: String, filter: String = "all") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, results = emptyList()) }
            _suggestions.value = emptyList()
            repository.search(query, filter)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            results = result.items,
                            nextpage = result.nextpage,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
        }
    }

    fun loadMoreResults(query: String, filter: String = "all") {
        val nextpage = _uiState.value.nextpage ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            repository.search(query, filter, nextpage)
                .onSuccess { result ->
                    _uiState.update { state ->
                        state.copy(
                            results = state.results + result.items,
                            nextpage = result.nextpage,
                            isLoadingMore = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun clearSearch() {
        _uiState.update { SearchUiState() }
        _suggestions.value = emptyList()
    }

    fun clearHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }
}
