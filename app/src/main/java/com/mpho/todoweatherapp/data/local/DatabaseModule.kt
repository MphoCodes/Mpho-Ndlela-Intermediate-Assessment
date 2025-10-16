package com.mpho.todoweatherapp.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies
 * 
 * This module tells Hilt how to provide instances of the database and DAO
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the Room database instance
     * 
     * @param context Application context
     * @return TodoWeatherDatabase instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TodoWeatherDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoWeatherDatabase::class.java,
            "todo_weather_database"
        )
            .fallbackToDestructiveMigration() // For development - recreates DB on schema changes
            .build()
    }
    
    /**
     * Provides the TaskDao instance
     * 
     * @param database The TodoWeatherDatabase instance
     * @return TaskDao instance
     */
    @Provides
    fun provideTaskDao(database: TodoWeatherDatabase): TaskDao {
        return database.taskDao()
    }
}
