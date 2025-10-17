package com.mpho.todoweatherapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mpho.todoweatherapp.di.AppModule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpho.todoweatherapp.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: WeatherViewModel = viewModel {
        WeatherViewModel(
            AppModule.provideWeatherRepository(context),
            AppModule.provideLocationService(context)
        )
    }

    // Permission launcher for location access
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, load location weather
            viewModel.loadCurrentLocationWeather()
        } else {
            // Permission denied, show error
            viewModel.setLocationPermissionDenied()
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weatherData by viewModel.weatherData.collectAsStateWithLifecycle()
    val astronomyData by viewModel.astronomyData.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    
    var showSearchDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentLocation,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Row {
                IconButton(onClick = {
                    if (viewModel.hasLocationPermission()) {
                        viewModel.loadCurrentLocationWeather()
                    } else {
                        // Request location permissions
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Use current location"
                    )
                }
                IconButton(onClick = { showSearchDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search location"
                    )
                }
                AnimatedRefreshButton(
                    onClick = { viewModel.refreshWeatherData() },
                    isLoading = uiState.isLoading
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
        ) {
            LoadingWeatherAnimation()
        }

        if (uiState.error != null) {
            ErrorState(
                error = uiState.error!!,
                onRetry = { viewModel.refreshWeatherData() },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!uiState.isLoading) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                weatherData?.let { weather ->
                    CurrentWeatherCard(
                        temperature = "${weather.current.temperatureCelsius.toInt()}°C",
                        condition = weather.current.condition.text,
                        feelsLike = "${weather.current.feelsLikeCelsius.toInt()}°C",
                        humidity = "${weather.current.humidity}%",
                        windSpeed = "${weather.current.windKph.toInt()} km/h",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                

                astronomyData?.let { astronomy ->
                    AstronomyCard(
                        sunrise = astronomy.astronomy.astro.sunrise,
                        sunset = astronomy.astronomy.astro.sunset,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                

                weatherData?.let { weather ->
                    WeatherDetailsCard(
                        pressure = "${weather.current.pressureMb.toInt()} mb",
                        visibility = "${weather.current.visibilityKm} km",
                        uvIndex = weather.current.uvIndex.toString(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    

    if (showSearchDialog) {
        SearchLocationDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { location ->
                viewModel.searchLocation(location)
                showSearchDialog = false
            }
        )
    }
}

@Composable
private fun CurrentWeatherCard(
    temperature: String,
    condition: String,
    feelsLike: String,
    humidity: String,
    windSpeed: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = temperature,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            

            Text(
                text = condition,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem("Feels like", feelsLike)
                WeatherInfoItem("Humidity", humidity)
                WeatherInfoItem("Wind", windSpeed)
            }
        }
    }
}

@Composable
private fun AstronomyCard(
    sunrise: String,
    sunset: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AstronomyItem(
                icon = "🌅",
                label = "Sunrise",
                time = sunrise
            )
            AstronomyItem(
                icon = "🌇",
                label = "Sunset",
                time = sunset
            )
        }
    }
}

@Composable
private fun WeatherDetailsCard(
    pressure: String,
    visibility: String,
    uvIndex: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherInfoItem("Pressure", pressure)
            WeatherInfoItem("Visibility", visibility)
            WeatherInfoItem("UV Index", uvIndex)
        }
    }
}

@Composable
private fun WeatherInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌧️",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Weather Unavailable",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
private fun SearchLocationDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Search Location")
        },
        text = {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Enter city name") },
                placeholder = { Text("e.g., London, New York") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (location.isNotBlank()) {
                        onSearch(location)
                    }
                },
                enabled = location.isNotBlank()
            ) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
private fun AstronomyItem(
    icon: String,
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun LoadingWeatherAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scaleAnimation by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scaleAnimation),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .rotate(rotationAngle),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Fetching weather data...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please wait a moment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AnimatedRefreshButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = if (isLoading) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(300)
        },
        label = "refresh_rotation"
    )

    IconButton(
        onClick = onClick,
        enabled = !isLoading
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh weather",
            modifier = Modifier.rotate(rotationAngle),
            tint = if (isLoading) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
