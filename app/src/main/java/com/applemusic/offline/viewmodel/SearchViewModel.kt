package com.applemusic.offline.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applemusic.offline.data.model.Song
import com.applemusic.offline.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Debounce na busca
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        search(query)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            songRepository.searchSongs(query).collect { results ->
                _searchResults.value = results
                _isLoading.value = false
            }
        }
    }

    fun addToRecentSearches(query: String) {
        if (query.isNotBlank() && ! _recentSearches.value.contains(query)) {
            _recentSearches.value = (listOf(query) + _recentSearches.value).take(10)
        }
    }
}
