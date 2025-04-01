package com.example.recipeapponeblanc.data.repository

import com.example.recipeapponeblanc.data.model.ApiResponse
import com.example.recipeapponeblanc.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CuisineRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getCuisines(page: Int, count: Int): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = mapOf("page" to page, "count" to count)
                val response = apiService.getCuisineList(request)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 