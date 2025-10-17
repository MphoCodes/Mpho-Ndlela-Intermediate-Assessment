package com.mpho.todoweatherapp.repository

import android.content.Context
import com.mpho.todoweatherapp.R
import com.mpho.todoweatherapp.data.model.WeatherResponse
import com.mpho.todoweatherapp.data.model.AstronomyResponse
import com.mpho.todoweatherapp.data.remote.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val context: Context
) {
    
    private val apiKey: String by lazy {
        context.getString(R.string.weather_api_key)
    }
    

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
    

    suspend fun getCurrentLocationWeather(): Result<WeatherResponse> {

        return getCurrentWeather("Johannesburg")
    }
    

    suspend fun refreshWeatherData(location: String): Result<WeatherResponse> {
        return getCurrentWeather(location)
    }
    

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


    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
