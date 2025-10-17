package com.mpho.todoweatherapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.mpho.todoweatherapp.data.model.Task
import com.mpho.todoweatherapp.data.model.Converters


@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoWeatherDatabase : RoomDatabase() {
    
    /**
     * Get the TaskDao for database operations
     */
    abstract fun taskDao(): TaskDao
    
    companion object {
        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: TodoWeatherDatabase? = null
        
        /**
         * Get database instance using singleton pattern
         * This ensures only one instance of the database exists
         */
        fun getDatabase(context: Context): TodoWeatherDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoWeatherDatabase::class.java,
                    "todo_weather_database"
                )
                    .fallbackToDestructiveMigration() // For development - recreates DB on schema changes
                    .build()
                INSTANCE = instance
                // Return instance
                instance
            }
        }
        
        /**
         * For testing purposes - allows setting a test database instance
         */
        internal fun setTestInstance(database: TodoWeatherDatabase?) {
            INSTANCE = database
        }
    }
}
