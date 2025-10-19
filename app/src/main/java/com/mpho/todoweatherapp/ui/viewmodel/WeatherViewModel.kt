package com.mpho.todoweatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpho.todoweatherapp.data.model.WeatherResponse
import com.mpho.todoweatherapp.data.model.AstronomyResponse
import com.mpho.todoweatherapp.data.model.SavedCity
import com.mpho.todoweatherapp.repository.WeatherRepository
import com.mpho.todoweatherapp.repository.SavedCityRepository
import com.mpho.todoweatherapp.utils.LocationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val astronomyData: AstronomyResponse? = null,
    val currentLocation: String = "Johannesburg",
    val error: String? = null
)

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationService: LocationService,
    private val savedCityRepository: SavedCityRepository
) : ViewModel() {
    

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()
    

    private val _astronomyData = MutableStateFlow<AstronomyResponse?>(null)
    val astronomyData: StateFlow<AstronomyResponse?> = _astronomyData.asStateFlow()
    

    private val _currentLocation = MutableStateFlow("Johannesburg")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    val savedCities: StateFlow<List<SavedCity>> = savedCityRepository.allSavedCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadWeatherData()
    }
    

    fun loadWeatherData(location: String = _currentLocation.value) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                _currentLocation.value = location
                

                val weatherResult = weatherRepository.getCurrentWeather(location)
                if (weatherResult.isSuccess) {
                    _weatherData.value = weatherResult.getOrNull()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = weatherResult.exceptionOrNull()?.message ?: "Failed to load weather data"
                    )
                }
                

                val astronomyResult = weatherRepository.getAstronomyData(location)
                if (astronomyResult.isSuccess) {
                    _astronomyData.value = astronomyResult.getOrNull()
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load weather data: ${e.message}"
                )
            }
        }
    }
    

    fun refreshWeatherData() {
        loadWeatherData(_currentLocation.value)
    }
    

    fun loadWeatherDataByCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val weatherResult = weatherRepository.getCurrentWeatherByCoordinates(latitude, longitude)
                if (weatherResult.isSuccess) {
                    _weatherData.value = weatherResult.getOrNull()
                    _currentLocation.value = weatherResult.getOrNull()?.location?.name ?: "Current Location"
                    

                    val coordinates = "$latitude,$longitude"
                    val astronomyResult = weatherRepository.getAstronomyData(coordinates)
                    if (astronomyResult.isSuccess) {
                        _astronomyData.value = astronomyResult.getOrNull()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = weatherResult.exceptionOrNull()?.message ?: "Failed to load weather data"
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load weather data: ${e.message}"
                )
            }
        }
    }
    

    fun searchLocation(location: String) {
        if (location.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a location")
            return
        }

        val trimmedLocation = location.trim()

        if (trimmedLocation.length < 2) {
            _uiState.value = _uiState.value.copy(error = "Location must be at least 2 characters")
            return
        }

        if (!isValidLocationFormat(trimmedLocation)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid city name or coordinates")
            return
        }

        loadWeatherData(trimmedLocation)
    }

    private fun isValidLocationFormat(location: String): Boolean {
        val coordinatePattern = Regex("^-?\\d+\\.?\\d*,-?\\d+\\.?\\d*$")
        if (coordinatePattern.matches(location)) {
            return true
        }

        val cityPattern = Regex("^[a-zA-Z\\s,.-]+$")
        return cityPattern.matches(location) && location.any { it.isLetter() }
    }
    

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    

    fun getFormattedTemperature(): String {
        return _weatherData.value?.current?.temperatureCelsius?.let { temp ->
            "${temp.toInt()}°C"
        } ?: "--°C"
    }
    

    fun getWeatherCondition(): String {
        return _weatherData.value?.current?.condition?.text ?: "Unknown"
    }
    

    fun getSunriseTime(): String {
        return _astronomyData.value?.astronomy?.astro?.sunrise ?: "--:--"
    }
    

    fun getSunsetTime(): String {
        return _astronomyData.value?.astronomy?.astro?.sunset ?: "--:--"
    }

    fun hasLocationPermission(): Boolean {
        return locationService.hasLocationPermission()
    }

    fun setLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Location permission is required to get weather for your current location. Please grant permission or search for a city manually."
        )
    }

    fun loadCurrentLocationWeather() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                if (!locationService.hasLocationPermission()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location permission required"
                    )
                    return@launch
                }

                val location = locationService.getCurrentLocation()
                if (location != null) {
                    val locationString = locationService.formatLocationForApi(location)
                    loadWeatherData(locationString)
                } else {
                    loadWeatherData("Johannesburg")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get location: ${e.message}"
                )
            }
        }
    }

    fun saveCurrentCity() {
        viewModelScope.launch {
            _weatherData.value?.let { weather ->
                savedCityRepository.saveCity(
                    cityName = weather.location.name,
                    country = weather.location.country
                )
            }
        }
    }

    fun deleteCity(city: SavedCity) {
        viewModelScope.launch {
            savedCityRepository.deleteCity(city)
        }
    }

    fun loadCityWeather(cityName: String) {
        loadWeatherData(cityName)
    }
}
