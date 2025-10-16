package com.mpho.todoweatherapp.data.remote

import com.mpho.todoweatherapp.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for WeatherAPI.com
 * 
 * Defines endpoints for fetching weather data including current conditions
 * and astronomy data (sunrise/sunset times)
 */
interface WeatherApiService {
    
    /**
     * Get current weather and astronomy data for a location
     * 
     * @param apiKey API key for authentication
     * @param location Location query (can be city name, coordinates, IP address, etc.)
     * @param aqi Whether to return air quality data (0 = no, 1 = yes)
     * @return WeatherResponse containing current weather and astronomy data
     */
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: Int = 0
    ): Response<WeatherResponse>
    
    /**
     * Get current weather by coordinates
     * 
     * @param apiKey API key for authentication
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param aqi Whether to return air quality data (0 = no, 1 = yes)
     * @return WeatherResponse containing current weather and astronomy data
     */
    @GET("current.json")
    suspend fun getCurrentWeatherByCoordinates(
        @Query("key") apiKey: String,
        @Query("q") coordinates: String, // Format: "lat,lon" e.g., "48.8567,2.3508"
        @Query("aqi") aqi: Int = 0
    ): Response<WeatherResponse>
    
    /**
     * Get astronomy data (sunrise/sunset) for a location
     *
     * @param apiKey API key for authentication
     * @param location Location query
     * @param date Date for astronomy data (format: yyyy-MM-dd)
     * @return Astronomy data response
     */
    @GET("astronomy.json")
    suspend fun getAstronomyData(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("dt") date: String
    ): Response<com.mpho.todoweatherapp.data.model.AstronomyResponse>
    
    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1/"
        
        /**
         * Helper function to format coordinates for API call
         */
        fun formatCoordinates(latitude: Double, longitude: Double): String {
            return "$latitude,$longitude"
        }
    }
}
