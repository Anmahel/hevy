package com.example.gymtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.dao.RoutineDao
import com.example.gymtracker.data.local.dao.WorkoutDao
import com.example.gymtracker.data.local.dao.WorkoutSetDao
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.entity.WorkoutEntity
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        WorkoutEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSetDao(): WorkoutSetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hevy_clone.db"
                )
                .fallbackToDestructiveMigration()
                // Se registra el callback para pre-cargar el JSON en el primer arranque
                .addCallback(ExerciseDatabaseCallback(context.applicationContext, coroutineScope))
                .build()

                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback de ciclo de vida de Room Database.
     * `onCreate` se dispara ÚNICAMENTE la primera vez que SQLite crea el archivo de la base de datos.
     */
    private class ExerciseDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                populateExercisesFromJson(context)
            }
        }

        private suspend fun populateExercisesFromJson(context: Context) {
            val database = INSTANCE ?: return
            val exerciseDao = database.exerciseDao()

            try {
                // 1. Abrir y leer el archivo ubicado en assets/exercises.json
                val inputStream = context.assets.open("exercises.json")
                val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

                // 2. Parsear el array JSON a una lista de ExerciseEntity
                val jsonArray = JSONArray(jsonString)
                val exercises = mutableListOf<ExerciseEntity>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val muscleGroupStr = jsonObject.getString("muscleGroup")
                    val primaryMuscleStr = jsonObject.optString("primaryMuscle", muscleGroupStr)
                    exercises.add(
                        ExerciseEntity(
                            name = jsonObject.getString("name"),
                            muscleGroup = muscleGroupStr,
                            equipment = jsonObject.optString("equipment", "Barra"),
                            isCustom = jsonObject.optBoolean("isCustom", false),
                            defaultRestSeconds = jsonObject.optInt("defaultRestSeconds", 90),
                            gifPathOrUrl = jsonObject.optString("gifPathOrUrl", ""),
                            thumbnailUrl = jsonObject.optString("thumbnailUrl", ""),
                            primaryMuscle = primaryMuscleStr,
                            secondaryMuscles = jsonObject.optString("secondaryMuscles", ""),
                            instructions = jsonObject.optString("instructions", "")
                        )
                    )
                }

                // 3. Inserción masiva en Room
                exerciseDao.insertExercises(exercises)

                // 4. Sembrado inicial de rutinas Día 1 a Día 6
                com.example.gymtracker.data.preset.RoutinePresetSeeder.seedDefaultRoutines(database, forceReload = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
