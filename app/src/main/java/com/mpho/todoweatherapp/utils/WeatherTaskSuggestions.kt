package com.mpho.todoweatherapp.utils

import com.mpho.todoweatherapp.data.model.WeatherResponse

/**
 * Utility object for generating intelligent task suggestions based on current weather conditions.
 *
 * Analyzes weather data including temperature, conditions, humidity, and wind speed to provide
 * contextually relevant task suggestions across different categories (outdoor, indoor, exercise, etc.).
 */
object WeatherTaskSuggestions {
    
    /**
     * Represents a weather-based task suggestion.
     *
     * @property title The task title
     * @property description Detailed description of the task
     * @property category The category this task belongs to
     * @property priority The suggested priority level
     */
    data class TaskSuggestion(
        val title: String,
        val description: String,
        val category: TaskCategory,
        val priority: TaskPriority = TaskPriority.MEDIUM
    )
    
    enum class TaskCategory {
        OUTDOOR, INDOOR, EXERCISE, HOUSEHOLD, WORK, LEISURE
    }
    
    enum class TaskPriority {
        LOW, MEDIUM, HIGH
    }
    
    fun getSuggestionsForWeather(weather: WeatherResponse?): List<TaskSuggestion> {
        if (weather == null) return getDefaultSuggestions()
        
        val temperature = weather.current.temperatureCelsius
        val condition = weather.current.condition.text.lowercase()
        val humidity = weather.current.humidity
        val windSpeed = weather.current.windKph
        
        return buildList {
            when {
                condition.contains("rain") || condition.contains("drizzle") -> {
                    addAll(getRainySuggestions())
                }
                condition.contains("snow") -> {
                    addAll(getSnowySuggestions())
                }
                condition.contains("sunny") || condition.contains("clear") -> {
                    addAll(getSunnySuggestions(temperature))
                }
                condition.contains("cloud") -> {
                    addAll(getCloudySuggestions(temperature))
                }
                condition.contains("storm") || condition.contains("thunder") -> {
                    addAll(getStormySuggestions())
                }
                else -> {
                    addAll(getGeneralSuggestions(temperature, humidity, windSpeed))
                }
            }
            
            addAll(getTemperatureBasedSuggestions(temperature))
            addAll(getHumidityBasedSuggestions(humidity))
            addAll(getWindBasedSuggestions(windSpeed))
        }.distinctBy { it.title }.take(8)
    }
    
    private fun getRainySuggestions() = listOf(
        TaskSuggestion(
            "Organize indoor spaces",
            "Perfect rainy day activity - declutter and organize your home",
            TaskCategory.HOUSEHOLD,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Read a book",
            "Cozy up with a good book while listening to the rain",
            TaskCategory.LEISURE,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Indoor workout",
            "Stay active with yoga, stretching, or bodyweight exercises",
            TaskCategory.EXERCISE,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Cook a warm meal",
            "Perfect weather for soup, stew, or baking",
            TaskCategory.HOUSEHOLD,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Work on indoor projects",
            "Catch up on computer work, writing, or creative projects",
            TaskCategory.WORK,
            TaskPriority.HIGH
        )
    )
    
    private fun getSnowySuggestions() = listOf(
        TaskSuggestion(
            "Hot beverage preparation",
            "Make hot chocolate, tea, or coffee to warm up",
            TaskCategory.HOUSEHOLD,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Winter gear check",
            "Organize and check winter clothing and equipment",
            TaskCategory.HOUSEHOLD,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Indoor entertainment",
            "Movie marathon, board games, or video calls with friends",
            TaskCategory.LEISURE,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Plan warm meals",
            "Prepare hearty, warming foods for cold weather",
            TaskCategory.HOUSEHOLD,
            TaskPriority.MEDIUM
        )
    )
    
    private fun getSunnySuggestions(temperature: Double) = listOf(
        TaskSuggestion(
            "Outdoor exercise",
            "Perfect weather for running, cycling, or outdoor sports",
            TaskCategory.EXERCISE,
            TaskPriority.HIGH
        ),
        TaskSuggestion(
            "Garden maintenance",
            "Water plants, weed garden, or plant new flowers",
            TaskCategory.OUTDOOR,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Outdoor cleaning",
            "Wash car, clean outdoor furniture, or sweep patio",
            TaskCategory.OUTDOOR,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Nature walk",
            "Take a walk in the park or explore local trails",
            TaskCategory.LEISURE,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Outdoor social activities",
            "Meet friends for outdoor lunch or picnic",
            TaskCategory.LEISURE,
            TaskPriority.LOW
        )
    ).let { suggestions ->
        if (temperature > 25) {
            suggestions + TaskSuggestion(
                "Swimming or water activities",
                "Great weather for pool, beach, or water sports",
                TaskCategory.EXERCISE,
                TaskPriority.HIGH
            )
        } else suggestions
    }
    
    private fun getCloudySuggestions(temperature: Double) = listOf(
        TaskSuggestion(
            "Light outdoor activities",
            "Perfect for walking, light gardening, or outdoor errands",
            TaskCategory.OUTDOOR,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Photography",
            "Great lighting for outdoor photography",
            TaskCategory.LEISURE,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Moderate exercise",
            "Good weather for jogging or cycling",
            TaskCategory.EXERCISE,
            TaskPriority.MEDIUM
        )
    )
    
    private fun getStormySuggestions() = listOf(
        TaskSuggestion(
            "Emergency preparedness",
            "Check flashlights, batteries, and emergency supplies",
            TaskCategory.HOUSEHOLD,
            TaskPriority.HIGH
        ),
        TaskSuggestion(
            "Stay indoors safely",
            "Focus on indoor activities and avoid going outside",
            TaskCategory.INDOOR,
            TaskPriority.HIGH
        ),
        TaskSuggestion(
            "Backup important data",
            "Ensure important files are backed up in case of power outage",
            TaskCategory.WORK,
            TaskPriority.HIGH
        )
    )
    
    private fun getGeneralSuggestions(temperature: Double, humidity: Int, windSpeed: Double) = listOf(
        TaskSuggestion(
            "Check weather updates",
            "Stay informed about changing weather conditions",
            TaskCategory.WORK,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Plan appropriate clothing",
            "Choose outfit based on current weather conditions",
            TaskCategory.HOUSEHOLD,
            TaskPriority.LOW
        )
    )
    
    private fun getTemperatureBasedSuggestions(temperature: Double) = when {
        temperature < 0 -> listOf(
            TaskSuggestion(
                "Heating system check",
                "Ensure heating is working properly",
                TaskCategory.HOUSEHOLD,
                TaskPriority.HIGH
            )
        )
        temperature < 10 -> listOf(
            TaskSuggestion(
                "Warm clothing prep",
                "Get out warm clothes and blankets",
                TaskCategory.HOUSEHOLD,
                TaskPriority.MEDIUM
            )
        )
        temperature > 30 -> listOf(
            TaskSuggestion(
                "Stay hydrated",
                "Drink plenty of water and stay cool",
                TaskCategory.HOUSEHOLD,
                TaskPriority.HIGH
            ),
            TaskSuggestion(
                "Air conditioning check",
                "Ensure cooling systems are working",
                TaskCategory.HOUSEHOLD,
                TaskPriority.MEDIUM
            )
        )
        else -> emptyList()
    }
    
    private fun getHumidityBasedSuggestions(humidity: Int) = when {
        humidity > 80 -> listOf(
            TaskSuggestion(
                "Dehumidify spaces",
                "Use fans or dehumidifiers to reduce moisture",
                TaskCategory.HOUSEHOLD,
                TaskPriority.MEDIUM
            )
        )
        humidity < 30 -> listOf(
            TaskSuggestion(
                "Add moisture to air",
                "Use humidifier or place water bowls around house",
                TaskCategory.HOUSEHOLD,
                TaskPriority.LOW
            )
        )
        else -> emptyList()
    }
    
    private fun getWindBasedSuggestions(windSpeed: Double) = when {
        windSpeed > 25 -> listOf(
            TaskSuggestion(
                "Secure outdoor items",
                "Bring in or secure loose outdoor furniture and decorations",
                TaskCategory.OUTDOOR,
                TaskPriority.HIGH
            )
        )
        windSpeed > 15 -> listOf(
            TaskSuggestion(
                "Check outdoor setup",
                "Ensure outdoor items are properly secured",
                TaskCategory.OUTDOOR,
                TaskPriority.MEDIUM
            )
        )
        else -> emptyList()
    }
    
    private fun getDefaultSuggestions() = listOf(
        TaskSuggestion(
            "Plan your day",
            "Review your schedule and prioritize tasks",
            TaskCategory.WORK,
            TaskPriority.MEDIUM
        ),
        TaskSuggestion(
            "Stay organized",
            "Tidy up your workspace and living areas",
            TaskCategory.HOUSEHOLD,
            TaskPriority.LOW
        ),
        TaskSuggestion(
            "Take breaks",
            "Remember to rest and recharge throughout the day",
            TaskCategory.LEISURE,
            TaskPriority.LOW
        )
    )
}
