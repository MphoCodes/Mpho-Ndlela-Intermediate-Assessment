package com.mpho.todoweatherapp

import org.junit.Test
import org.junit.Assert.*

class LocationValidationTest {

    @Test
    fun isValidLocationFormat_withValidCityName_returnsTrue() {
        val result = isValidLocationFormat("London")
        assertTrue(result)
    }

    @Test
    fun isValidLocationFormat_withValidCityWithSpaces_returnsTrue() {
        val result = isValidLocationFormat("New York")
        assertTrue(result)
    }

    @Test
    fun isValidLocationFormat_withValidCoordinates_returnsTrue() {
        val result = isValidLocationFormat("51.5074,-0.1278")
        assertTrue(result)
    }

    @Test
    fun isValidLocationFormat_withInvalidNumbers_returnsFalse() {
        val result = isValidLocationFormat("123456")
        assertFalse(result)
    }

    @Test
    fun isValidLocationFormat_withSpecialCharacters_returnsFalse() {
        val result = isValidLocationFormat("@#$%")
        assertFalse(result)
    }

    @Test
    fun isValidLocationFormat_withEmptyString_returnsFalse() {
        val result = isValidLocationFormat("")
        assertFalse(result)
    }

    @Test
    fun validateLocationInput_withShortInput_returnsFalse() {
        val result = validateLocationInput("a")
        assertFalse(result)
    }

    @Test
    fun validateLocationInput_withValidInput_returnsTrue() {
        val result = validateLocationInput("London")
        assertTrue(result)
    }

    private fun isValidLocationFormat(location: String): Boolean {
        if (location.isBlank()) return false

        val coordinatePattern = Regex("^-?\\d+\\.?\\d*,-?\\d+\\.?\\d*$")
        if (coordinatePattern.matches(location)) {
            return true
        }

        val cityPattern = Regex("^[a-zA-Z\\s,.-]+$")
        return cityPattern.matches(location) && location.any { it.isLetter() }
    }

    private fun validateLocationInput(location: String): Boolean {
        if (location.isBlank()) return false
        if (location.trim().length < 2) return false
        return isValidLocationFormat(location.trim())
    }
}