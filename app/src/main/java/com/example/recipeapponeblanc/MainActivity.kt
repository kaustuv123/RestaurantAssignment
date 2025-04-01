package com.example.recipeapponeblanc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.recipeapponeblanc.data.model.Cuisine
import com.example.recipeapponeblanc.ui.screens.HomeScreen
import com.example.recipeapponeblanc.ui.theme.RecipeAppOneblancTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeAppOneblancTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RestaurantApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    HomeScreen(
        onCuisineClick = { cuisine ->
            // For now, just show a toast. We'll implement navigation to details screen later
            Toast.makeText(context, "Selected: ${cuisine.cuisine_name}", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier
    )
}