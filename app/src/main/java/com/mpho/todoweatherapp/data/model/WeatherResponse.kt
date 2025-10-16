package com.mpho.todoweatherapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data classes for Weather API response from weatherapi.com
 *
 * Main response containing location and current weather data
 * Note: Astronomy data requires a separate API call to astronomy.json endpoint
 */
data class WeatherResponse(
    @SerializedName("location")
    val location: Location,
    @SerializedName("current")
    val current: Current
)

/**
 * Location information
 */
data class Location(
    @SerializedName("name")
    val name: String,
    @SerializedName("region")
    val region: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("tz_id")
    val timezoneId: String,
    @SerializedName("localtime")
    val localTime: String
)

/**
 * Current weather conditions
 */
data class Current(
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("temp_c")
    val temperatureCelsius: Double,
    @SerializedName("temp_f")
    val temperatureFahrenheit: Double,
    @SerializedName("is_day")
    val isDay: Int, // 1 for day, 0 for night
    @SerializedName("condition")
    val condition: Condition,
    @SerializedName("wind_mph")
    val windMph: Double,
    @SerializedName("wind_kph")
    val windKph: Double,
    @SerializedName("wind_degree")
    val windDegree: Int,
    @SerializedName("wind_dir")
    val windDirection: String,
    @SerializedName("pressure_mb")
    val pressureMb: Double,
    @SerializedName("pressure_in")
    val pressureIn: Double,
    @SerializedName("precip_mm")
    val precipitationMm: Double,
    @SerializedName("precip_in")
    val precipitationIn: Double,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("cloud")
    val cloudCover: Int,
    @SerializedName("feelslike_c")
    val feelsLikeCelsius: Double,
    @SerializedName("feelslike_f")
    val feelsLikeFahrenheit: Double,
    @SerializedName("vis_km")
    val visibilityKm: Double,
    @SerializedName("vis_miles")
    val visibilityMiles: Double,
    @SerializedName("uv")
    val uvIndex: Double,
    @SerializedName("gust_mph")
    val gustMph: Double,
    @SerializedName("gust_kph")
    val gustKph: Double
)

/**
 * Weather condition details
 */
data class Condition(
    @SerializedName("text")
    val text: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("code")
    val code: Int
)

/**
 * Astronomy API response for sunrise/sunset times
 */
data class AstronomyResponse(
    @SerializedName("location")
    val location: Location,
    @SerializedName("astronomy")
    val astronomy: Astronomy
)

/**
 * Astronomy data for sunrise/sunset times
 */
data class Astronomy(
    @SerializedName("astro")
    val astro: Astro
)

/**
 * Astronomical information
 */
data class Astro(
    @SerializedName("sunrise")
    val sunrise: String,
    @SerializedName("sunset")
    val sunset: String,
    @SerializedName("moonrise")
    val moonrise: String,
    @SerializedName("moonset")
    val moonset: String,
    @SerializedName("moon_phase")
    val moonPhase: String,
    @SerializedName("moon_illumination")
    val moonIllumination: String,
    @SerializedName("is_moon_up")
    val isMoonUp: Int,
    @SerializedName("is_sun_up")
    val isSunUp: Int
)
