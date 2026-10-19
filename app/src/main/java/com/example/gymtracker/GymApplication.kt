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
import android.content.Context
import com.example.gymtracker.data.preset.RoutinePresetSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

class GymApplication : Application(), ImageLoaderFactory {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("exercise_image_cache"))
                    .maxSizeBytes(128L * 1024 * 1024) // 128 MB persistente offline
                    .build()
            }
            .crossfade(true)
            .build()
    }

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

    val weightRepository: com.example.gymtracker.domain.repository.WeightRepository by lazy {
        com.example.gymtracker.data.repository.WeightRepositoryImpl(database.weightDao())
    }

    override fun onCreate() {
        super.onCreate()
        com.example.gymtracker.data.local.ActiveWorkoutSessionStore.init(this)
        val prefs = getSharedPreferences("gym_tracker_prefs", Context.MODE_PRIVATE)
        val isRoutinesSeededV2 = prefs.getBoolean("routines_seed_v2", false)

        // Inicialización/actualización a routines_seed_v2 y vinculación única en segundo plano con hasaneyldrm/exercises-dataset
        applicationScope.launch {
            if (!isRoutinesSeededV2) {
                RoutinePresetSeeder.seedDefaultRoutines(database, forceReload = true)
                prefs.edit()
                    .putBoolean("is_routines_seeded", true)
                    .putBoolean("preset_routines_initial_seed_done", true)
                    .putBoolean("routines_seed_v2", true)
                    .apply()
            }

            try {
                val dao = database.exerciseDao()
                val existing = dao.getAllExercisesList()
                val toUpdate = existing.filter { it.gifPathOrUrl.isBlank() || it.thumbnailUrl.isBlank() }
                if (toUpdate.isNotEmpty()) {
                    val updated = toUpdate.map { ex ->
                        val url = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise(ex.name, ex.muscleGroup)
                        ex.copy(
                            gifPathOrUrl = ex.gifPathOrUrl.ifBlank { url },
                            thumbnailUrl = ex.thumbnailUrl.ifBlank { url }
                        )
                    }
                    dao.updateExercises(updated)
                }

                // Vincular automáticamente los 43 ejercicios con el dataset 3D (miniatura .jpg, animación .gif e instrucciones en español)
                com.example.gymtracker.data.preset.ExerciseImageProvider.syncExercisesWith3dDatasetIfNeeded(
                    context = this@GymApplication,
                    exerciseDao = dao
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
