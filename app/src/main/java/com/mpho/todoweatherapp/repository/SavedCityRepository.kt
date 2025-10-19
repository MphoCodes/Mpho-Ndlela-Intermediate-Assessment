package com.mpho.todoweatherapp.repository

import com.mpho.todoweatherapp.data.local.SavedCityDao
import com.mpho.todoweatherapp.data.model.SavedCity
import kotlinx.coroutines.flow.Flow

class SavedCityRepository(private val savedCityDao: SavedCityDao) {
    
    val allSavedCities: Flow<List<SavedCity>> = savedCityDao.getAllSavedCities()
    
    suspend fun saveCity(cityName: String, country: String) {
        val existingCity = savedCityDao.getCityByName(cityName)
        if (existingCity == null) {
            savedCityDao.insertCity(SavedCity(cityName = cityName, country = country))
        }
    }
    
    suspend fun deleteCity(city: SavedCity) {
        savedCityDao.deleteCity(city)
    }
    
    suspend fun deleteAllCities() {
        savedCityDao.deleteAllCities()
    }
}

