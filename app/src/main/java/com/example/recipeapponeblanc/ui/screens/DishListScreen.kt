package com.example.recipeapponeblanc.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipeapponeblanc.data.model.Cuisine
import com.example.recipeapponeblanc.data.model.DishItem
import com.example.recipeapponeblanc.ui.components.DishCard
import com.example.recipeapponeblanc.viewmodel.CartViewModel
import com.example.recipeapponeblanc.viewmodel.DishListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishListScreen(
    cuisine: Cuisine,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel()
) {
    val dishListViewModel: DishListViewModel = viewModel(
        factory = DishListViewModel.provideFactory(cuisine.cuisine_id)
    )
    
    val dishListUiState by dishListViewModel.uiState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.cartState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Show error toast if there's an error message
    LaunchedEffect(dishListUiState.errorMessage) {
        dishListUiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cuisine.cuisine_name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (dishListUiState.isSearchingCuisine) {
                // Show loading state when searching for cuisine
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Finding dishes for ${cuisine.cuisine_name}...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else if (dishListUiState.dishes.isEmpty() && !dishListUiState.isLoading) {
                // Empty state when no dishes found
                DishEmptyState(
                    onRetry = { dishListViewModel.retryLoading() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show dish list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = dishListUiState.dishes,
                        key = { dish -> dish.id }
                    ) { dish ->
                        val quantity = cartState.items[dish.id]?.quantity ?: 0
                        DishCard(
                            dish = dish,
                            quantity = quantity,
                            onAddToCart = { cartViewModel.addToCart(dish) },
                            onRemoveFromCart = { cartViewModel.removeFromCart(dish.id) }
                        )
                    }
                }
            }
            
            // Show loading indicator at the center
            if (dishListUiState.isLoading && !dishListUiState.isSearchingCuisine) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun DishEmptyState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No dishes found for this cuisine",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Retry")
        }
    }
} 