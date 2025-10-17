package com.mpho.todoweatherapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mpho.todoweatherapp.ui.screens.SplashScreen
import com.mpho.todoweatherapp.ui.theme.TodoWeatherAppTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TodoWeatherAppTheme {
                SplashScreen(
                    onSplashFinished = {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
