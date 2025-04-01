package com.example.recipeapponeblanc.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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

@Parcelize
data class Cuisine(
    val cuisine_id: String,
    val cuisine_name: String,
    val cuisine_image_url: String,
    val items: List<DishItem>
) : Parcelable

@Parcelize
data class DishItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val image_url: String,
    val rating: String
) : Parcelable

@Parcelize
data class CuisineResponse(
    val status: Boolean,
    val message: String,
    val data: List<Cuisine>
) : Parcelable 