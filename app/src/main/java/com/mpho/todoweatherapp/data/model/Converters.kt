package com.mpho.todoweatherapp.data.model

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room type converters for handling Date objects in the database
 */
class Converters {
    
    /**
     * Convert Date to Long timestamp for database storage
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convert Long timestamp back to Date object
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
