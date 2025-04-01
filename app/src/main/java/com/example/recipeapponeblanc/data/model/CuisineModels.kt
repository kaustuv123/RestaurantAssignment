package com.example.recipeapponeblanc.data.model

data class ApiResponse(
    val response_code: Int,
    val outcome_code: Int,
    val response_message: String,
    val page: Int,
    val count: Int,
    val total_pages: Int,
    val total_items: Int,
    val cuisines: List<Cuisine>
)

data class Cuisine(
    val cuisine_id: String,
    val cuisine_name: String,
    val cuisine_image_url: String,
    val items: List<DishItem>
)

data class DishItem(
    val id: String,
    val name: String,
    val image_url: String,
    val price: String,
    val rating: String
) 