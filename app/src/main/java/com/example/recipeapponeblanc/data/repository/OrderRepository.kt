package com.example.recipeapponeblanc.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.recipeapponeblanc.data.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

// Extension property for accessing DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "orders")

class OrderRepository(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val ORDERS_KEY = stringPreferencesKey("orders")
    }
    
    // Get all orders
    fun getOrders(): Flow<List<Order>> {
        return context.dataStore.data.map { preferences ->
            val ordersJson = preferences[ORDERS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Order>>(ordersJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // Save a new order
    suspend fun saveOrder(order: Order) {
        context.dataStore.edit { preferences ->
            val existingOrdersJson = preferences[ORDERS_KEY] ?: "[]"
            val existingOrders = try {
                json.decodeFromString<List<Order>>(existingOrdersJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedOrders = existingOrders + order
            preferences[ORDERS_KEY] = json.encodeToString(updatedOrders)
        }
    }
    
    // Generate a unique order ID
    fun generateOrderId(): String {
        return UUID.randomUUID().toString()
    }
} 