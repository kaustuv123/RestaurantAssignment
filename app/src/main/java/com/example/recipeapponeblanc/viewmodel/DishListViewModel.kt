package com.example.recipeapponeblanc.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipeapponeblanc.data.model.DishItem
import com.example.recipeapponeblanc.data.repository.CuisineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishListUiState(
    val isLoading: Boolean = false,
    val isSearchingCuisine: Boolean = false,
    val dishes: List<DishItem> = emptyList(),
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

class DishListViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val repository = CuisineRepository()
    private val _uiState = MutableStateFlow(DishListUiState())
    val uiState: StateFlow<DishListUiState> = _uiState.asStateFlow()
    
    private val cuisineId: String = checkNotNull(savedStateHandle["cuisineId"])
    
    init {
        loadDishes()
    }
    
    fun loadDishes() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                isSearchingCuisine = _uiState.value.dishes.isEmpty(),
                errorMessage = null
            ) }
            
            val currentPage = _uiState.value.currentPage
            val result = repository.getCuisines(currentPage, 10)
            
            result.fold(
                onSuccess = { response ->
                    // Find the cuisine with the matching ID
                    val matchingCuisine = response.cuisines.find { it.cuisine_id == cuisineId }
                    
                    if (matchingCuisine != null) {
                        // Found the cuisine, update dishes
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isSearchingCuisine = false,
                                dishes = matchingCuisine.items,
                                currentPage = response.total_pages + 1, // To prevent further loading
                                totalPages = response.total_pages
                            )
                        }
                    } else if (currentPage < response.total_pages) {
                        // Not found yet, continue to next page
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                currentPage = currentPage + 1,
                                totalPages = response.total_pages
                            )
                        }
                        // Load next page
                        loadDishes()
                    } else {
                        // Reached the end, no matching cuisine found
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isSearchingCuisine = false,
                                errorMessage = "No items found for this cuisine."
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isSearchingCuisine = false,
                            errorMessage = error.message ?: "An unknown error occurred"
                        ) 
                    }
                }
            )
        }
    }
    
    fun retryLoading() {
        if (_uiState.value.errorMessage != null) {
            _uiState.update { 
                it.copy(
                    errorMessage = null,
                    currentPage = 1
                ) 
            }
            loadDishes()
        }
    }
    
    companion object {
        fun provideFactory(cuisineId: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DishListViewModel::class.java)) {
                        val savedStateHandle = SavedStateHandle().apply {
                            set("cuisineId", cuisineId)
                        }
                        return DishListViewModel(savedStateHandle) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
} 