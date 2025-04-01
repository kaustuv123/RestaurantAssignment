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
                val newTotalPrice = calculateTotalPrice(currentItems)
                currentState.copy(
                    items = currentItems,
                    totalPrice = newTotalPrice,
                    taxes = calculateTaxes(newTotalPrice),
                    grandTotal = calculateGrandTotal(newTotalPrice)
                )
            } else {
                currentItems[dish.id] = CartItem(dish = dish, quantity = 1)
                val newTotalPrice = calculateTotalPrice(currentItems)
                currentState.copy(
                    items = currentItems,
                    totalPrice = newTotalPrice,
                    taxes = calculateTaxes(newTotalPrice),
                    grandTotal = calculateGrandTotal(newTotalPrice)
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
                    val newTotalPrice = calculateTotalPrice(currentItems)
                    currentState.copy(
                        items = currentItems,
                        totalPrice = newTotalPrice,
                        taxes = calculateTaxes(newTotalPrice),
                        grandTotal = calculateGrandTotal(newTotalPrice)
                    )
                } else {
                    currentItems.remove(dishId)
                    val newTotalPrice = calculateTotalPrice(currentItems)
                    currentState.copy(
                        items = currentItems,
                        totalPrice = newTotalPrice,
                        taxes = calculateTaxes(newTotalPrice),
                        grandTotal = calculateGrandTotal(newTotalPrice)
                    )
                }
            } else {
                currentState
            }
        }
    }
    
    fun updateQuantity(dishId: String, quantity: Int) {
        if (quantity < 0) return
        
        _cartState.update { currentState ->
            val currentItems = currentState.items.toMutableMap()
            val currentItem = currentItems[dishId]
            
            if (currentItem != null) {
                if (quantity == 0) {
                    // Remove item if quantity is set to 0
                    currentItems.remove(dishId)
                } else {
                    // Update quantity
                    currentItems[dishId] = currentItem.copy(quantity = quantity)
                }
                
                val newTotalPrice = calculateTotalPrice(currentItems)
                currentState.copy(
                    items = currentItems,
                    totalPrice = newTotalPrice,
                    taxes = calculateTaxes(newTotalPrice),
                    grandTotal = calculateGrandTotal(newTotalPrice)
                )
            } else {
                currentState
            }
        }
    }

    fun getQuantity(dishId: String): Int {
        return _cartState.value.items[dishId]?.quantity ?: 0
    }
    
    private fun calculateTotalPrice(items: Map<String, CartItem>): Int {
        return items.values.sumOf { it.dish.price * it.quantity }
    }
    
    private fun calculateTaxes(totalPrice: Int): Taxes {
        val cgst = (totalPrice * 0.025).toInt()
        val sgst = (totalPrice * 0.025).toInt()
        return Taxes(cgst = cgst, sgst = sgst)
    }
    
    private fun calculateGrandTotal(totalPrice: Int): Int {
        val taxes = calculateTaxes(totalPrice)
        return totalPrice + taxes.cgst + taxes.sgst
    }
    
    fun clearCart() {
        _cartState.update { 
            CartState() 
        }
    }
}

data class CartState(
    val items: Map<String, CartItem> = emptyMap(),
    val totalPrice: Int = 0,
    val taxes: Taxes = Taxes(0, 0),
    val grandTotal: Int = 0
)

data class CartItem(
    val dish: DishItem,
    val quantity: Int
)

data class Taxes(
    val cgst: Int,
    val sgst: Int
) 