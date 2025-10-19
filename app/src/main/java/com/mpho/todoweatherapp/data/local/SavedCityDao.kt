package com.mpho.todoweatherapp.data.local

import androidx.room.*
import com.mpho.todoweatherapp.data.model.SavedCity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCityDao {
    
    @Query("SELECT * FROM saved_cities ORDER BY savedAt DESC")
    fun getAllSavedCities(): Flow<List<SavedCity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCity)
    
    @Delete
    suspend fun deleteCity(city: SavedCity)
    
    @Query("SELECT * FROM saved_cities WHERE cityName = :cityName LIMIT 1")
    suspend fun getCityByName(cityName: String): SavedCity?
    
    @Query("DELETE FROM saved_cities")
    suspend fun deleteAllCities()
}

