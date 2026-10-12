package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.gymtracker.GymApplication
import com.example.gymtracker.ui.screens.main.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GymApplication
        val repository = app.repository
        val exerciseRepository = app.exerciseRepository
        val routineRepository = app.routineRepository
        val workoutRepository = app.workoutRepository

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        exerciseRepository = exerciseRepository,
                        routineRepository = routineRepository,
                        workoutRepository = workoutRepository,
                        gymRepository = repository
                    )
                }
            }
        }
    }
}
