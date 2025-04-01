package com.example.recipeapponeblanc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipeapponeblanc.data.model.Cuisine
import com.example.recipeapponeblanc.ui.screens.DishListScreen
import com.example.recipeapponeblanc.ui.screens.HomeScreen
import com.example.recipeapponeblanc.ui.theme.RecipeAppOneblancTheme
import com.example.recipeapponeblanc.viewmodel.CartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeAppOneblancTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecipeApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onCuisineClick = { cuisine ->
                    // Pass the entire cuisine object through navigation
                    navController.currentBackStackEntry?.savedStateHandle?.set("cuisine", cuisine)
                    navController.navigate("dish_list/${cuisine.cuisine_id}") {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(
            route = "dish_list/{cuisineId}",
            arguments = listOf(
                navArgument("cuisineId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val arguments = requireNotNull(backStackEntry.arguments)
            val cuisineId = arguments.getString("cuisineId") ?: ""
            
            // Get the cuisine from the savedStateHandle
            val cuisine = navController.previousBackStackEntry?.savedStateHandle?.get<Cuisine>("cuisine")
                ?: Cuisine(
                    cuisine_id = cuisineId,
                    cuisine_name = "Cuisine Details",
                    cuisine_image_url = "",
                    items = emptyList()
                )
            
            DishListScreen(
                cuisine = cuisine,
                onBackClick = {
                    navController.popBackStack()
                },
                cartViewModel = cartViewModel
            )
        }
    }
}