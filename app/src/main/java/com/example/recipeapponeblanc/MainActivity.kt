package com.example.recipeapponeblanc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipeapponeblanc.data.model.Cuisine
import com.example.recipeapponeblanc.ui.screens.CheckoutScreen
import com.example.recipeapponeblanc.ui.screens.DishListScreen
import com.example.recipeapponeblanc.ui.screens.HomeScreen
import com.example.recipeapponeblanc.ui.theme.RecipeAppOneblancTheme
import com.example.recipeapponeblanc.viewmodel.CartViewModel
import com.example.recipeapponeblanc.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeAppOneblancTheme {
                RecipeApp()
            }
        }
    }
}

@Composable
fun RecipeApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val cartState by cartViewModel.cartState.collectAsState()
    
    // Create OrderViewModel
    val context = LocalContext.current
    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModel.Factory(context.applicationContext as android.app.Application)
    )
    
    // Check current route to determine if we should show the bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "checkout" && cartState.items.isNotEmpty()
    
    val brownColor = Color(0xFF8B4513)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = brownColor,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { 
                            navController.navigate("checkout") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brownColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Proceed to Checkout (${cartState.items.size} ${if (cartState.items.size == 1) "item" else "items"})",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onCuisineClick = { cuisine ->
                        // Pass the entire cuisine object through navigation
                        navController.currentBackStackEntry?.savedStateHandle?.set("cuisine", cuisine)
                        navController.navigate("dish_list/${cuisine.cuisine_id}") {
                            launchSingleTop = true
                        }
                    },
                    orderViewModel = orderViewModel,
                    cartViewModel = cartViewModel
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
            
            composable("checkout") {
                CheckoutScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    cartViewModel = cartViewModel,
                    onOrderPlaced = {
                        // Place the order
                        orderViewModel.placeOrder(cartState) {
                            // Clear the cart
                            cartViewModel.clearCart()
                            
                            // Navigate back to home
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}