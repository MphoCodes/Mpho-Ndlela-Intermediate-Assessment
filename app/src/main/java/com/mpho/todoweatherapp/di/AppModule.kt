package com.mpho.todoweatherapp.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mpho.todoweatherapp.R
import com.mpho.todoweatherapp.data.local.TodoWeatherDatabase
import com.mpho.todoweatherapp.data.remote.WeatherApiService
import com.mpho.todoweatherapp.repository.TaskRepository
import com.mpho.todoweatherapp.repository.WeatherRepository
import com.mpho.todoweatherapp.repository.SavedCityRepository
import com.mpho.todoweatherapp.utils.LocationService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {
    
    @Volatile
    private var database: TodoWeatherDatabase? = null
    
    @Volatile
    private var weatherApiService: WeatherApiService? = null
    
    @Volatile
    private var taskRepository: TaskRepository? = null
    
    @Volatile
    private var weatherRepository: WeatherRepository? = null

    @Volatile
    private var savedCityRepository: SavedCityRepository? = null

    fun provideDatabase(context: Context): TodoWeatherDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                TodoWeatherDatabase::class.java,
                "todo_weather_database"
            ).build().also { database = it }
        }
    }
    
    fun provideTaskRepository(context: Context): TaskRepository {
        return taskRepository ?: synchronized(this) {
            taskRepository ?: TaskRepository(
                provideDatabase(context).taskDao()
            ).also { taskRepository = it }
        }
    }
    
    fun provideWeatherRepository(context: Context): WeatherRepository {
        return weatherRepository ?: synchronized(this) {
            weatherRepository ?: WeatherRepository(
                provideWeatherApiService(context),
                context
            ).also { weatherRepository = it }
        }
    }
    
    private fun provideWeatherApiService(context: Context): WeatherApiService {
        return weatherApiService ?: synchronized(this) {
            weatherApiService ?: createRetrofit(context).create(WeatherApiService::class.java)
                .also { weatherApiService = it }
        }
    }
    
    private fun createRetrofit(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.weather_base_url))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun provideLocationService(context: Context): LocationService {
        return LocationService(context)
    }

    fun provideSavedCityRepository(context: Context): SavedCityRepository {
        return savedCityRepository ?: synchronized(this) {
            savedCityRepository ?: SavedCityRepository(
                provideDatabase(context).savedCityDao()
            ).also { savedCityRepository = it }
        }
    }
}
