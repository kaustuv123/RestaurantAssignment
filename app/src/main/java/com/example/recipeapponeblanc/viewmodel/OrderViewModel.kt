package com.example.recipeapponeblanc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipeapponeblanc.data.model.Order
import com.example.recipeapponeblanc.data.model.OrderItem
import com.example.recipeapponeblanc.data.model.toOrderItem
import com.example.recipeapponeblanc.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = OrderRepository(application.applicationContext)
    
    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()
    
    init {
        loadOrderHistory()
    }
    
    private fun loadOrderHistory() {
        viewModelScope.launch {
            repository.getOrders().collect { orders ->
                _orderHistory.value = orders.sortedByDescending { it.timestamp }
            }
        }
    }
    
    fun placeOrder(cartState: CartState, onOrderPlaced: () -> Unit) {
        viewModelScope.launch {
            // Convert cart items to order items
            val orderItems = cartState.items.values.map { it.toOrderItem() }
            
            // Create a new order
            val order = Order(
                id = repository.generateOrderId(),
                items = orderItems,
                netTotal = cartState.totalPrice,
                cgst = cartState.taxes.cgst,
                sgst = cartState.taxes.sgst,
                grandTotal = cartState.grandTotal,
                timestamp = Date().time
            )
            
            // Save the order
            repository.saveOrder(order)
            
            // Update order history
            loadOrderHistory()
            
            // Notify that order is placed
            onOrderPlaced()
        }
    }
    
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
                return OrderViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 