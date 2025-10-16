package com.mpho.todoweatherapp.utils

import android.util.Log
import com.mpho.todoweatherapp.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for testing API integration
 * This can be used to verify that our weather API is working correctly
 */
@Singleton
class ApiTestHelper @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    
    private val tag = "ApiTestHelper"
    
    /**
     * Test the weather API with a sample location
     */
    fun testWeatherApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(tag, "Testing Weather API...")
                
                // Test current weather
                val weatherResult = weatherRepository.getCurrentWeather("Johannesburg")
                if (weatherResult.isSuccess) {
                    val weather = weatherResult.getOrNull()
                    Log.d(tag, "✅ Weather API Success!")
                    Log.d(tag, "Location: ${weather?.location?.name}, ${weather?.location?.country}")
                    Log.d(tag, "Temperature: ${weather?.current?.temperatureCelsius}°C")
                    Log.d(tag, "Condition: ${weather?.current?.condition?.text}")
                } else {
                    Log.e(tag, "❌ Weather API Failed: ${weatherResult.exceptionOrNull()?.message}")
                }
                
                // Test astronomy data
                val astronomyResult = weatherRepository.getAstronomyData("Johannesburg")
                if (astronomyResult.isSuccess) {
                    val astronomy = astronomyResult.getOrNull()
                    Log.d(tag, "✅ Astronomy API Success!")
                    Log.d(tag, "Sunrise: ${astronomy?.astronomy?.astro?.sunrise}")
                    Log.d(tag, "Sunset: ${astronomy?.astronomy?.astro?.sunset}")
                } else {
                    Log.e(tag, "❌ Astronomy API Failed: ${astronomyResult.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e(tag, "❌ API Test Exception: ${e.message}", e)
            }
        }
    }
}
