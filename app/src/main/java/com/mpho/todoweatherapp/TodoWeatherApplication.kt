package com.mpho.todoweatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the TodoWeather app
 * 
 * This class is required for Hilt dependency injection
 * The @HiltAndroidApp annotation triggers Hilt's code generation
 */
@HiltAndroidApp
class TodoWeatherApplication : Application()
