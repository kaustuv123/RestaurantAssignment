package com.example.recipeapponeblanc.data.model

import com.example.recipeapponeblanc.viewmodel.CartItem
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class Order(
    val id: String,
    val items: List<OrderItem>,
    val netTotal: Int,
    val cgst: Int,
    val sgst: Int,
    val grandTotal: Int,
    val timestamp: Long
) {
    fun getFormattedDateTime(): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        return dateTime.format(formatter)
    }
}

@Serializable
data class OrderItem(
    val id: String,
    val name: String,
    val price: Int,
    val quantity: Int,
    val imageUrl: String
)

// Extension function to convert CartItem to OrderItem
fun CartItem.toOrderItem(): OrderItem = OrderItem(
    id = dish.id,
    name = dish.name,
    price = dish.price,
    quantity = quantity,
    imageUrl = dish.image_url
) 