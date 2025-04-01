package com.example.recipeapponeblanc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapponeblanc.data.model.DishItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun addToCart(dish: DishItem) {
        _cartState.update { currentState ->
            val currentItems = currentState.items.toMutableMap()
            val currentItem = currentItems[dish.id]
            
            if (currentItem != null) {
                currentItems[dish.id] = currentItem.copy(quantity = currentItem.quantity + 1)
                currentState.copy(
                    items = currentItems,
                    totalPrice = currentState.totalPrice + dish.price
                )
            } else {
                currentItems[dish.id] = CartItem(dish = dish, quantity = 1)
                currentState.copy(
                    items = currentItems,
                    totalPrice = currentState.totalPrice + dish.price
                )
            }
        }
    }

    fun removeFromCart(dishId: String) {
        _cartState.update { currentState ->
            val currentItems = currentState.items.toMutableMap()
            val currentItem = currentItems[dishId]
            
            if (currentItem != null) {
                if (currentItem.quantity > 1) {
                    currentItems[dishId] = currentItem.copy(quantity = currentItem.quantity - 1)
                    currentState.copy(
                        items = currentItems,
                        totalPrice = currentState.totalPrice - currentItem.dish.price
                    )
                } else {
                    currentItems.remove(dishId)
                    currentState.copy(
                        items = currentItems,
                        totalPrice = currentState.totalPrice - currentItem.dish.price
                    )
                }
            } else {
                currentState
            }
        }
    }

    fun getQuantity(dishId: String): Int {
        return _cartState.value.items[dishId]?.quantity ?: 0
    }
}

data class CartState(
    val items: Map<String, CartItem> = emptyMap(),
    val totalPrice: Int = 0
)

data class CartItem(
    val dish: DishItem,
    val quantity: Int
) 