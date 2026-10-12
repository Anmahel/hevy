package com.example.gymtracker

import android.app.Application
import com.example.gymtracker.data.local.AppDatabase
import com.example.gymtracker.data.repository.ExerciseRepositoryImpl
import com.example.gymtracker.data.repository.GymRepositoryImpl
import com.example.gymtracker.data.repository.RoutineRepositoryImpl
import com.example.gymtracker.data.repository.WorkoutRepositoryImpl
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.data.preset.RoutinePresetSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GymApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(database.exerciseDao())
    }

    val routineRepository: RoutineRepository by lazy {
        RoutineRepositoryImpl(database.routineDao(), database.exerciseDao())
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(database.workoutDao())
    }

    val repository: GymRepository by lazy {
        GymRepositoryImpl(
            exerciseDao = database.exerciseDao(),
            workoutDao = database.workoutDao(),
            workoutSetDao = database.workoutSetDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Precarga automática de las 6 rutinas sugeridas día a día si aún no existen
        applicationScope.launch {
            RoutinePresetSeeder.seedDefaultRoutines(database, forceReload = false)
        }
    }
}
