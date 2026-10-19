package com.example.gymtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.dao.RoutineDao
import com.example.gymtracker.data.local.dao.WeightDao
import com.example.gymtracker.data.local.dao.WorkoutDao
import com.example.gymtracker.data.local.dao.WorkoutSetDao
import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
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
        WorkoutEntity::class,
        WeightLogEntity::class,
        DailyNutritionLogEntity::class,
        CustomMealBlockEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN armCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN chestCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN waistCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN thighCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN calfCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN photoUri TEXT")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN poseType TEXT")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_nutrition_logs` (
                        `dateKey` TEXT NOT NULL,
                        `consumedCalories` INTEGER NOT NULL DEFAULT 0,
                        `consumedProteinGrams` REAL NOT NULL DEFAULT 0.0,
                        `consumedCarbsGrams` REAL NOT NULL DEFAULT 0.0,
                        `consumedFatsGrams` REAL NOT NULL DEFAULT 0.0,
                        `creatineTaken` INTEGER NOT NULL DEFAULT 0,
                        `waterMl` INTEGER NOT NULL DEFAULT 0,
                        `tapHistoryJson` TEXT NOT NULL DEFAULT '[]',
                        PRIMARY KEY(`dateKey`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `custom_meal_blocks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `proteinGrams` REAL NOT NULL,
                        `carbsGrams` REAL NOT NULL,
                        `fatsGrams` REAL NOT NULL,
                        `calories` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE routines ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 99")
                db.execSQL("ALTER TABLE routine_exercises ADD COLUMN exerciseOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE routine_exercises ADD COLUMN targetSets INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE routine_exercises ADD COLUMN targetRepRange TEXT NOT NULL DEFAULT '8-12 reps'")
                db.execSQL("UPDATE routine_exercises SET exerciseOrder = orderIndex")
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN exerciseOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN targetRepRange TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN bicepsRelaxedCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN bicepsContractedCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN forearmCm REAL")
                db.execSQL("ALTER TABLE weight_logs ADD COLUMN glutesCm REAL")
                db.execSQL("UPDATE weight_logs SET bicepsContractedCm = armCm WHERE bicepsContractedCm IS NULL AND armCm IS NOT NULL")
            }
        }

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
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
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

                // 4. Sembrado inicial de rutinas Día 1 a Día 6 (v2)
                com.example.gymtracker.data.preset.RoutinePresetSeeder.seedDefaultRoutines(database, forceReload = true)
                val prefs = context.getSharedPreferences("gym_tracker_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_routines_seeded", true)
                    .putBoolean("preset_routines_initial_seed_done", true)
                    .putBoolean("routines_seed_v2", true)
                    .apply()

                // 5. Vinculación en segundo plano con hasaneyldrm/exercises-dataset
                com.example.gymtracker.data.preset.ExerciseImageProvider.syncExercisesWith3dDatasetIfNeeded(
                    context = context,
                    exerciseDao = exerciseDao
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
