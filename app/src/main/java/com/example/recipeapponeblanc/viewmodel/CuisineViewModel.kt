package com.example.recipeapponeblanc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapponeblanc.data.model.Cuisine
import com.example.recipeapponeblanc.data.repository.CuisineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CuisineUiState(
    val isLoading: Boolean = false,
    val cuisines: List<Cuisine> = emptyList(),
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMoreData: Boolean = true
)

class CuisineViewModel : ViewModel() {
    
    private val repository = CuisineRepository()
    private val _uiState = MutableStateFlow(CuisineUiState())
    val uiState: StateFlow<CuisineUiState> = _uiState.asStateFlow()
    
    // Keep track of unique cuisine IDs
    private val uniqueCuisineIds = mutableSetOf<String>()
    
    init {
        loadCuisines()
    }
    
    fun loadCuisines() {
        if (_uiState.value.isLoading || !_uiState.value.hasMoreData) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val currentPage = _uiState.value.currentPage
            val result = repository.getCuisines(currentPage, 10)
            
            result.fold(
                onSuccess = { response ->
                    // Filter out duplicate cuisines
                    val newCuisines = response.cuisines.filter { cuisine ->
                        uniqueCuisineIds.add(cuisine.cuisine_id)
                    }
                    
                    // Only update state if we have new unique cuisines
                    if (newCuisines.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                cuisines = state.cuisines + newCuisines,
                                currentPage = currentPage + 1,
                                totalPages = response.total_pages,
                                hasMoreData = currentPage < response.total_pages
                            )
                        }
                    } else {
                        // If no new unique cuisines, just update the page and check if we should continue
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                currentPage = currentPage + 1,
                                totalPages = response.total_pages,
                                hasMoreData = currentPage < response.total_pages
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = error.message ?: "An unknown error occurred"
                        ) 
                    }
                }
            )
        }
    }
    
    fun retryLoading() {
        if (_uiState.value.errorMessage != null) {
            _uiState.update { it.copy(errorMessage = null) }
            loadCuisines()
        }
    }
} 