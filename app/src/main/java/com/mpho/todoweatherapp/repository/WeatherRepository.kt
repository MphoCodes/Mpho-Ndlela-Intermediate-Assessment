package com.mpho.todoweatherapp.repository

import android.content.Context
import com.mpho.todoweatherapp.R
import com.mpho.todoweatherapp.data.model.WeatherResponse
import com.mpho.todoweatherapp.data.model.AstronomyResponse
import com.mpho.todoweatherapp.data.remote.WeatherApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Weather data operations
 * 
 * This repository handles all weather-related data operations,
 * serving as a single source of truth for weather data.
 * Handles API calls and error management.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    @ApplicationContext private val context: Context
) {
    
    private val apiKey: String by lazy {
        context.getString(R.string.weather_api_key)
    }
    
    /**
     * Get current weather data for a location
     * 
     * @param location Location query (city name, coordinates, etc.)
     * @return Result containing WeatherResponse or error
     */
    suspend fun getCurrentWeather(location: String): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getCurrentWeather(
                    apiKey = apiKey,
                    location = location,
                    aqi = 0
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        Result.success(weatherResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get current weather data by coordinates
     * 
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Result containing WeatherResponse or error
     */
    suspend fun getCurrentWeatherByCoordinates(
        latitude: Double, 
        longitude: Double
    ): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val coordinates = WeatherApiService.formatCoordinates(latitude, longitude)
                val response = weatherApiService.getCurrentWeatherByCoordinates(
                    apiKey = apiKey,
                    coordinates = coordinates,
                    aqi = 0
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        Result.success(weatherResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get weather data for current device location
     * This method would typically use LocationManager to get current coordinates
     * For now, it uses a default location (can be updated with actual location logic)
     * 
     * @return Result containing WeatherResponse or error
     */
    suspend fun getCurrentLocationWeather(): Result<WeatherResponse> {
        // TODO: Implement actual location fetching
        // For now, using a default location (Johannesburg, South Africa)
        return getCurrentWeather("Johannesburg")
    }
    
    /**
     * Refresh weather data for a location
     * This method can be used to force refresh weather data
     * 
     * @param location Location query
     * @return Result containing fresh WeatherResponse or error
     */
    suspend fun refreshWeatherData(location: String): Result<WeatherResponse> {
        return getCurrentWeather(location)
    }
    
    /**
     * Get astronomy data (sunrise/sunset) for a location
     *
     * @param location Location query
     * @param date Date for astronomy data (format: yyyy-MM-dd), defaults to today
     * @return Result containing AstronomyResponse or error
     */
    suspend fun getAstronomyData(
        location: String,
        date: String = getCurrentDateString()
    ): Result<AstronomyResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getAstronomyData(
                    apiKey = apiKey,
                    location = location,
                    date = date
                )

                if (response.isSuccessful) {
                    response.body()?.let { astronomyResponse ->
                        Result.success(astronomyResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get weather data with error handling and user-friendly messages
     *
     * @param location Location query
     * @return Result with WeatherResponse or user-friendly error message
     */
    suspend fun getWeatherWithErrorHandling(location: String): Result<WeatherResponse> {
        return try {
            val result = getCurrentWeather(location)
            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                val userFriendlyMessage = when {
                    exception?.message?.contains("network", ignoreCase = true) == true ->
                        context.getString(R.string.error_network)
                    exception?.message?.contains("location", ignoreCase = true) == true ->
                        context.getString(R.string.error_location)
                    else -> exception?.message ?: "Unknown error occurred"
                }
                Result.failure(Exception(userFriendlyMessage))
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_network)))
        }
    }

    /**
     * Helper function to get current date in yyyy-MM-dd format
     */
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
