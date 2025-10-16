package com.mpho.todoweatherapp.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mpho.todoweatherapp.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies
 * 
 * This module provides Retrofit, OkHttpClient, and WeatherApiService instances
 * following best practices for network configuration
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * Provides Gson instance with custom configuration
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient() // Allow lenient JSON parsing
            .create()
    }
    
    /**
     * Provides HTTP logging interceptor for debugging
     * Only logs in debug builds for security
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Enable logging for debugging - can be controlled via build variants
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * Provides OkHttpClient with proper configuration
     * 
     * @param loggingInterceptor HTTP logging interceptor
     * @return Configured OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Provides base URL for the Weather API
     * 
     * @param context Application context to access string resources
     * @return Base URL string
     */
    @Provides
    @Singleton
    fun provideBaseUrl(@ApplicationContext context: Context): String {
        return context.getString(R.string.weather_base_url)
    }
    
    /**
     * Provides Retrofit instance with proper configuration
     * 
     * @param okHttpClient Configured OkHttpClient
     * @param gson Gson instance for JSON parsing
     * @param baseUrl Base URL for the API
     * @return Configured Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
        baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Provides WeatherApiService instance
     * 
     * @param retrofit Configured Retrofit instance
     * @return WeatherApiService implementation
     */
    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
    
    /**
     * Provides API key from string resources
     * 
     * @param context Application context to access string resources
     * @return API key string
     */
    @Provides
    @Singleton
    fun provideApiKey(@ApplicationContext context: Context): String {
        return context.getString(R.string.weather_api_key)
    }
}
