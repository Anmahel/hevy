package com.example.gymtracker.data.preset

import android.content.Context
import com.example.gymtracker.data.local.dao.ExerciseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer

/**
 * Proveedor de imágenes anatómicas 3D (miniatura .jpg 180x180 y animación .gif en bucle)
 * e instrucciones en español sincronizadas desde `hasaneyldrm/exercises-dataset`,
 * con respaldo estático para modo sin conexión inicial.
 */
object ExerciseImageProvider {

    const val DATASET_BASE =
        "https://cdn.jsdelivr.net/gh/hasaneyldrm/exercises-dataset@7455efae41b330c265e7cd4b78dfa848e7ce5ebd"

    private const val GITHUB_RAW_BASE =
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

    private const val PREFS_NAME = "gym_tracker_prefs"
    private const val PREF_KEY_3D_SYNCED = "dataset_3d_gifs_linked_v1"

    data class DatasetMatchQuery(
        val preferredIds: List<String> = emptyList(),
        val englishCandidates: List<String>
    )

    data class RemoteDatasetExercise(
        val id: String,
        val nameLower: String,
        val imagePath: String,
        val gifPath: String,
        val instructionsEs: List<String>
    )

    /**
     * Diccionario de equivalencia Español -> Inglés + IDs del dataset `hasaneyldrm/exercises-dataset`
     * para vincular los 43 ejercicios del catálogo local y de las 6 rutinas.
     */
    private val SPANISH_TO_DATASET_MAP: Map<String, DatasetMatchQuery> = mapOf(
        // --- PECHO ---
        normalizeKey("Press de banca plano con barra") to DatasetMatchQuery(
            preferredIds = listOf("0025"),
            englishCandidates = listOf("Barbell Bench Press", "Barbell Bench Press - Medium Grip")
        ),
        normalizeKey("Press de Banca Plano") to DatasetMatchQuery(
            preferredIds = listOf("0025"),
            englishCandidates = listOf("Barbell Bench Press", "Barbell Bench Press - Medium Grip")
        ),
        normalizeKey("Press de Banca con Barra") to DatasetMatchQuery(
            preferredIds = listOf("0025"),
            englishCandidates = listOf("Barbell Bench Press", "Barbell Bench Press - Medium Grip")
        ),
        normalizeKey("Press inclinado con mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0314", "0313"),
            englishCandidates = listOf("Dumbbell Incline Bench Press", "Incline Dumbbell Press", "Dumbbell Incline Press")
        ),
        normalizeKey("Press Declinado") to DatasetMatchQuery(
            preferredIds = listOf("0033"),
            englishCandidates = listOf("Barbell Decline Bench Press", "Decline Barbell Bench Press")
        ),
        normalizeKey("Cruces en polea alta") to DatasetMatchQuery(
            preferredIds = listOf("0227", "0179"),
            englishCandidates = listOf("Cable Standing Fly", "Cable Crossover", "Cable Middle Fly", "Cable High Fly")
        ),
        normalizeKey("Aperturas en Polea (Crossover)") to DatasetMatchQuery(
            preferredIds = listOf("0227", "0179"),
            englishCandidates = listOf("Cable Standing Fly", "Cable Crossover", "Cable Middle Fly")
        ),
        normalizeKey("Fondos en Paralelas (Dips Pecho)") to DatasetMatchQuery(
            preferredIds = listOf("0251"),
            englishCandidates = listOf("Chest Dip", "Triceps Dip")
        ),
        normalizeKey("Fondos en Paralelas") to DatasetMatchQuery(
            preferredIds = listOf("0251"),
            englishCandidates = listOf("Chest Dip", "Triceps Dip")
        ),
        normalizeKey("Press de pecho en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0576", "0577"),
            englishCandidates = listOf("Lever Chest Press", "Lever Seated Chest Press")
        ),
        normalizeKey("Press de pecho sentado en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0576", "0577"),
            englishCandidates = listOf("Lever Chest Press", "Lever Seated Chest Press")
        ),
        normalizeKey("Press en Máquina de Pecho (Chest Press)") to DatasetMatchQuery(
            preferredIds = listOf("0576", "0577"),
            englishCandidates = listOf("Lever Chest Press", "Lever Seated Chest Press")
        ),
        normalizeKey("Peck Deck en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0596"),
            englishCandidates = listOf("Lever Seated Fly", "Lever Pec Deck Fly")
        ),
        normalizeKey("Flexiones de Brazos (Push-ups)") to DatasetMatchQuery(
            preferredIds = listOf("0662"),
            englishCandidates = listOf("Push-up", "Push Up")
        ),

        // --- ESPALDA ---
        normalizeKey("Peso Muerto Convencional") to DatasetMatchQuery(
            preferredIds = listOf("0032"),
            englishCandidates = listOf("Barbell Deadlift")
        ),
        normalizeKey("Dominadas Pronas (Pull-ups)") to DatasetMatchQuery(
            preferredIds = listOf("0652"),
            englishCandidates = listOf("Pull-up", "Pull Up")
        ),
        normalizeKey("Dominadas (Pull-ups)") to DatasetMatchQuery(
            preferredIds = listOf("0652"),
            englishCandidates = listOf("Pull-up", "Pull Up")
        ),
        normalizeKey("Remo con Barra (Pendlay)") to DatasetMatchQuery(
            preferredIds = listOf("0027"),
            englishCandidates = listOf("Barbell Bent Over Row", "Barbell Pendlay Row")
        ),
        normalizeKey("Remo con Barra") to DatasetMatchQuery(
            preferredIds = listOf("0027"),
            englishCandidates = listOf("Barbell Bent Over Row", "Barbell Pendlay Row")
        ),
        normalizeKey("Remo Unilateral con Mancuerna") to DatasetMatchQuery(
            preferredIds = listOf("0292"),
            englishCandidates = listOf("Dumbbell Bent-over Row", "Dumbbell One Arm Row", "Dumbbell Row")
        ),
        normalizeKey("Jalón al pecho con agarre prono") to DatasetMatchQuery(
            preferredIds = listOf("0150", "0197"),
            englishCandidates = listOf("Cable Lat Pulldown", "Cable Pulldown", "Cable Wide Grip Lat Pulldown")
        ),
        normalizeKey("Jalón al pecho prono") to DatasetMatchQuery(
            preferredIds = listOf("0150", "0197"),
            englishCandidates = listOf("Cable Lat Pulldown", "Cable Pulldown", "Cable Wide Grip Lat Pulldown")
        ),
        normalizeKey("Jalón al Pecho en Polea") to DatasetMatchQuery(
            preferredIds = listOf("0150", "0197"),
            englishCandidates = listOf("Cable Lat Pulldown", "Cable Pulldown")
        ),
        normalizeKey("Jalón al pecho agarre neutro") to DatasetMatchQuery(
            preferredIds = listOf("0150", "0197", "0159"),
            englishCandidates = listOf("Cable Close Grip Pulldown", "Cable Lat Pulldown", "Cable Underhand Pulldown")
        ),
        normalizeKey("Jalón al pecho con agarre neutro") to DatasetMatchQuery(
            preferredIds = listOf("0150", "0197", "0159"),
            englishCandidates = listOf("Cable Close Grip Pulldown", "Cable Lat Pulldown", "Cable Underhand Pulldown")
        ),
        normalizeKey("Remo sentado en polea con agarre en V") to DatasetMatchQuery(
            preferredIds = listOf("0861", "0239"),
            englishCandidates = listOf("Cable Seated Row", "Cable Straight Back Seated Row", "Cable Low Seated Row")
        ),
        normalizeKey("Remo sentado en polea (agarre V)") to DatasetMatchQuery(
            preferredIds = listOf("0861", "0239"),
            englishCandidates = listOf("Cable Seated Row", "Cable Straight Back Seated Row", "Cable Low Seated Row")
        ),
        normalizeKey("Remo en Polea Baja (Gironda)") to DatasetMatchQuery(
            preferredIds = listOf("0861", "0239"),
            englishCandidates = listOf("Cable Seated Row", "Cable Straight Back Seated Row", "Cable Low Seated Row")
        ),
        normalizeKey("Remo en Polea Baja") to DatasetMatchQuery(
            preferredIds = listOf("0861", "0239"),
            englishCandidates = listOf("Cable Seated Row", "Cable Straight Back Seated Row")
        ),
        normalizeKey("Remo con apoyo en el pecho") to DatasetMatchQuery(
            preferredIds = listOf("0575", "1350"),
            englishCandidates = listOf("Lever Chest Supported Row", "Dumbbell Incline Row", "Lever Seated Row")
        ),
        normalizeKey("Remo en Máquina con Apoyo de Pecho") to DatasetMatchQuery(
            preferredIds = listOf("0575", "1350"),
            englishCandidates = listOf("Lever Chest Supported Row", "Dumbbell Incline Row", "Lever Seated Row")
        ),
        normalizeKey("Extensión lumbar en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0573", "0489"),
            englishCandidates = listOf("Lever Back Extension", "Hyperextension")
        ),

        // --- HOMBROS ---
        normalizeKey("Press Militar de Pie (OHP)") to DatasetMatchQuery(
            preferredIds = listOf("0091"),
            englishCandidates = listOf("Barbell Standing Military Press", "Barbell Overhead Press", "Barbell Seated Overhead Press")
        ),
        normalizeKey("Press Militar con Barra") to DatasetMatchQuery(
            preferredIds = listOf("0091"),
            englishCandidates = listOf("Barbell Standing Military Press", "Barbell Overhead Press")
        ),
        normalizeKey("Press militar con mancuernas sentado") to DatasetMatchQuery(
            preferredIds = listOf("0405"),
            englishCandidates = listOf("Dumbbell Seated Shoulder Press", "Dumbbell Shoulder Press")
        ),
        normalizeKey("Press de Hombros Sentado con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0405"),
            englishCandidates = listOf("Dumbbell Seated Shoulder Press", "Dumbbell Shoulder Press")
        ),
        normalizeKey("Elevaciones laterales con mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0334"),
            englishCandidates = listOf("Dumbbell Lateral Raise")
        ),
        normalizeKey("Elevaciones Laterales en Polea") to DatasetMatchQuery(
            preferredIds = listOf("0178"),
            englishCandidates = listOf("Cable Lateral Raise", "Cable One Arm Lateral Raise")
        ),
        normalizeKey("Pájaros Posteriores con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0378", "0377"),
            englishCandidates = listOf("Dumbbell Rear Lateral Raise", "Dumbbell Seated Rear Lateral Raise", "Dumbbell Reverse Fly")
        ),
        normalizeKey("Peck Deck inverso (pájaros)") to DatasetMatchQuery(
            preferredIds = listOf("0602"),
            englishCandidates = listOf("Lever Seated Reverse Fly", "Lever Reverse Fly")
        ),
        normalizeKey("Peck Deck inverso (deltoides posterior)") to DatasetMatchQuery(
            preferredIds = listOf("0602"),
            englishCandidates = listOf("Lever Seated Reverse Fly", "Lever Reverse Fly")
        ),
        normalizeKey("Face Pulls en Polea") to DatasetMatchQuery(
            preferredIds = listOf("0203"),
            englishCandidates = listOf("Cable Rear Delt Row", "Cable Face Pull")
        ),
        normalizeKey("Press de hombros en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0603"),
            englishCandidates = listOf("Lever Shoulder Press", "Lever Seated Shoulder Press")
        ),
        normalizeKey("Press de Hombro en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0603"),
            englishCandidates = listOf("Lever Shoulder Press", "Lever Seated Shoulder Press")
        ),

        // --- BÍCEPS ---
        normalizeKey("Curl de Bíceps con Barra Z") to DatasetMatchQuery(
            preferredIds = listOf("0447", "0031"),
            englishCandidates = listOf("EZ Barbell Curl", "Barbell Curl")
        ),
        normalizeKey("Curl en banco Scott con barra EZ") to DatasetMatchQuery(
            preferredIds = listOf("0451", "0070"),
            englishCandidates = listOf("EZ Barbell Preacher Curl", "Barbell Preacher Curl")
        ),
        normalizeKey("Curl Predicador en Banco Scott") to DatasetMatchQuery(
            preferredIds = listOf("0451", "0591"),
            englishCandidates = listOf("EZ Barbell Preacher Curl", "Lever Preacher Curl", "Barbell Preacher Curl")
        ),
        normalizeKey("Curl martillo alterno con mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0313", "0312"),
            englishCandidates = listOf("Dumbbell Hammer Curl", "Dumbbell Alternate Hammer Curl")
        ),
        normalizeKey("Curl Martillo con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0313"),
            englishCandidates = listOf("Dumbbell Hammer Curl")
        ),
        normalizeKey("Curl Inclinado con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0315"),
            englishCandidates = listOf("Dumbbell Incline Curl", "Dumbbell Incline Biceps Curl")
        ),
        normalizeKey("Curl de bíceps en polea baja") to DatasetMatchQuery(
            preferredIds = listOf("0868", "0154"),
            englishCandidates = listOf("Cable Curl", "Cable Standing Curl", "Cable Biceps Curl")
        ),
        normalizeKey("Curl en Polea Baja") to DatasetMatchQuery(
            preferredIds = listOf("0868", "0154"),
            englishCandidates = listOf("Cable Curl", "Cable Standing Curl", "Cable Biceps Curl")
        ),
        normalizeKey("Dominadas Supinas (Chin-ups)") to DatasetMatchQuery(
            preferredIds = listOf("0253"),
            englishCandidates = listOf("Chin-up", "Chin Up")
        ),

        // --- TRÍCEPS ---
        normalizeKey("Press de banca con agarre cerrado") to DatasetMatchQuery(
            preferredIds = listOf("0030"),
            englishCandidates = listOf("Barbell Close-Grip Bench Press", "Barbell Close Grip Bench Press")
        ),
        normalizeKey("Press de Banca Agarre Cerrado") to DatasetMatchQuery(
            preferredIds = listOf("0030"),
            englishCandidates = listOf("Barbell Close-Grip Bench Press", "Barbell Close Grip Bench Press")
        ),
        normalizeKey("Extensión de tríceps en polea") to DatasetMatchQuery(
            preferredIds = listOf("0201", "0200"),
            englishCandidates = listOf("Cable Pushdown", "Cable Pushdown (with rope attachment)", "Cable Triceps Pushdown")
        ),
        normalizeKey("Extensión de tríceps en polea o máquina") to DatasetMatchQuery(
            preferredIds = listOf("0201", "0200"),
            englishCandidates = listOf("Cable Pushdown", "Cable Pushdown (with rope attachment)", "Cable Triceps Pushdown")
        ),
        normalizeKey("Extensiones de Tríceps en Polea Alta con Cuerda") to DatasetMatchQuery(
            preferredIds = listOf("0200", "0201"),
            englishCandidates = listOf("Cable Pushdown (with rope attachment)", "Cable Pushdown", "Cable Rope Triceps Pushdown")
        ),
        normalizeKey("Press Francés con Barra Z") to DatasetMatchQuery(
            preferredIds = listOf("0450", "0060"),
            englishCandidates = listOf("EZ Barbell Lying Triceps Extension", "Barbell Lying Triceps Extension")
        ),
        normalizeKey("Fondos en máquina asistida") to DatasetMatchQuery(
            preferredIds = listOf("0019", "0593"),
            englishCandidates = listOf("Assisted Triceps Dip", "Assisted Chest Dip", "Lever Seated Dip")
        ),
        normalizeKey("Fondos entre Bancos para Tríceps") to DatasetMatchQuery(
            preferredIds = listOf("0139"),
            englishCandidates = listOf("Bench Dip", "Bench Dips")
        ),
        normalizeKey("Extensión de Tríceps sobre la Cabeza con Mancuerna") to DatasetMatchQuery(
            preferredIds = listOf("0416", "0362"),
            englishCandidates = listOf("Dumbbell Standing Triceps Extension", "Dumbbell Seated Triceps Extension")
        ),
        normalizeKey("Extensión de Tríceps en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0609", "0593"),
            englishCandidates = listOf("Lever Triceps Extension", "Lever Seated Dip")
        ),

        // --- PIERNAS (CUÁDRICEPS, ISQUIOS, GLÚTEOS, GEMELOS) ---
        normalizeKey("Sentadilla en máquina Smith") to DatasetMatchQuery(
            preferredIds = listOf("0770"),
            englishCandidates = listOf("Smith Squat", "Smith Chair Squat")
        ),
        normalizeKey("Sentadilla en Smith") to DatasetMatchQuery(
            preferredIds = listOf("0770"),
            englishCandidates = listOf("Smith Squat", "Smith Chair Squat")
        ),
        normalizeKey("Sentadilla Trasera con Barra (Back Squat)") to DatasetMatchQuery(
            preferredIds = listOf("0043"),
            englishCandidates = listOf("Barbell Full Squat", "Barbell Squat", "Barbell High Bar Squat")
        ),
        normalizeKey("Sentadilla con Barra (Back Squat)") to DatasetMatchQuery(
            preferredIds = listOf("0043"),
            englishCandidates = listOf("Barbell Full Squat", "Barbell Squat")
        ),
        normalizeKey("Sentadilla Frontal") to DatasetMatchQuery(
            preferredIds = listOf("0042"),
            englishCandidates = listOf("Barbell Front Squat")
        ),
        normalizeKey("Prensa inclinada a 45°") to DatasetMatchQuery(
            preferredIds = listOf("0739"),
            englishCandidates = listOf("Sled 45° Leg Press", "Sled 45 Degrees Leg Press", "Sled Leg Press")
        ),
        normalizeKey("Prensa de Piernas 45°") to DatasetMatchQuery(
            preferredIds = listOf("0739"),
            englishCandidates = listOf("Sled 45° Leg Press", "Sled 45 Degrees Leg Press", "Sled Leg Press")
        ),
        normalizeKey("Prensa horizontal / sentada") to DatasetMatchQuery(
            preferredIds = listOf("0600", "0739"),
            englishCandidates = listOf("Lever Seated Leg Press", "Sled 45° Leg Press", "Sled Leg Press")
        ),
        normalizeKey("Extensión de Cuádriceps en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0585"),
            englishCandidates = listOf("Lever Leg Extension")
        ),
        normalizeKey("Extensiones de cuádriceps en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0585"),
            englishCandidates = listOf("Lever Leg Extension")
        ),
        normalizeKey("Extensiones de cuádriceps") to DatasetMatchQuery(
            preferredIds = listOf("0585"),
            englishCandidates = listOf("Lever Leg Extension")
        ),
        normalizeKey("Extensión de Cuádriceps") to DatasetMatchQuery(
            preferredIds = listOf("0585"),
            englishCandidates = listOf("Lever Leg Extension")
        ),
        normalizeKey("Sentadilla Búlgara con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0410"),
            englishCandidates = listOf("Dumbbell Single Leg Split Squat", "Dumbbell Bulgarian Split Squat", "Dumbbell Lunge")
        ),
        normalizeKey("Sentadilla Hack en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0743", "0046"),
            englishCandidates = listOf("Sled Hack Squat", "Barbell Hack Squat")
        ),
        normalizeKey("Zancadas Caminando con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0336"),
            englishCandidates = listOf("Dumbbell Lunge", "Dumbbell Walking Lunge")
        ),
        normalizeKey("Peso muerto rumano") to DatasetMatchQuery(
            preferredIds = listOf("0085"),
            englishCandidates = listOf("Barbell Romanian Deadlift")
        ),
        normalizeKey("Peso Muerto Rumano con Barra") to DatasetMatchQuery(
            preferredIds = listOf("0085"),
            englishCandidates = listOf("Barbell Romanian Deadlift")
        ),
        normalizeKey("Peso Muerto Rumano con Mancuernas") to DatasetMatchQuery(
            preferredIds = listOf("0380"),
            englishCandidates = listOf("Dumbbell Romanian Deadlift")
        ),
        normalizeKey("Curl femoral tumbado en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0586"),
            englishCandidates = listOf("Lever Lying Leg Curl")
        ),
        normalizeKey("Curl Femoral Tumbado") to DatasetMatchQuery(
            preferredIds = listOf("0586"),
            englishCandidates = listOf("Lever Lying Leg Curl")
        ),
        normalizeKey("Curl femoral sentado en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0599"),
            englishCandidates = listOf("Lever Seated Leg Curl")
        ),
        normalizeKey("Curl femoral sentado") to DatasetMatchQuery(
            preferredIds = listOf("0599"),
            englishCandidates = listOf("Lever Seated Leg Curl")
        ),
        normalizeKey("Curl Nórdico de Isquios") to DatasetMatchQuery(
            preferredIds = listOf("0586", "0466"),
            englishCandidates = listOf("Glute-Ham Raise", "Assisted Inverse Leg Curl", "Lever Lying Leg Curl")
        ),
        normalizeKey("Hip Thrust (máquina o barra)") to DatasetMatchQuery(
            preferredIds = listOf("0045", "1409"),
            englishCandidates = listOf("Barbell Glute Bridge", "Barbell Hip Thrust")
        ),
        normalizeKey("Hip Thrust (en máquina o con barra)") to DatasetMatchQuery(
            preferredIds = listOf("0045", "1409"),
            englishCandidates = listOf("Barbell Glute Bridge", "Barbell Hip Thrust")
        ),
        normalizeKey("Máquina de abductores") to DatasetMatchQuery(
            preferredIds = listOf("0597"),
            englishCandidates = listOf("Lever Seated Hip Abduction")
        ),
        normalizeKey("Máquina de aductores") to DatasetMatchQuery(
            preferredIds = listOf("0598"),
            englishCandidates = listOf("Lever Seated Hip Adduction")
        ),
        normalizeKey("Elevaciones de gemelos (pantorrilla)") to DatasetMatchQuery(
            preferredIds = listOf("0605", "0738"),
            englishCandidates = listOf("Lever Standing Calf Raise", "Sled Calf Press")
        ),
        normalizeKey("Elevación de Talones de Pie en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0605"),
            englishCandidates = listOf("Lever Standing Calf Raise")
        ),
        normalizeKey("Elevación de Gemelos en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0605"),
            englishCandidates = listOf("Lever Standing Calf Raise")
        ),
        normalizeKey("Elevación de Talones Sentado en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0594"),
            englishCandidates = listOf("Lever Seated Calf Raise")
        ),
        normalizeKey("Elevación de Gemelos en Prensa de Piernas") to DatasetMatchQuery(
            preferredIds = listOf("0738"),
            englishCandidates = listOf("Sled 45° Calf Press", "Sled Calf Press")
        ),
        normalizeKey("Elevación de Talones Unilateral con Mancuerna") to DatasetMatchQuery(
            preferredIds = listOf("0409", "0417"),
            englishCandidates = listOf("Dumbbell Single Leg Calf Raise", "Dumbbell Standing Calf Raise")
        ),

        // --- ABDOMEN / CORE ---
        normalizeKey("Crunch abdominal en máquina") to DatasetMatchQuery(
            preferredIds = listOf("0595", "0175"),
            englishCandidates = listOf("Lever Seated Crunch", "Cable Kneeling Crunch")
        ),
        normalizeKey("Crunches Abdominales en Máquina") to DatasetMatchQuery(
            preferredIds = listOf("0595", "0175"),
            englishCandidates = listOf("Lever Seated Crunch", "Cable Kneeling Crunch")
        ),
        normalizeKey("Crunch Abdominal en Polea Alta") to DatasetMatchQuery(
            preferredIds = listOf("0175"),
            englishCandidates = listOf("Cable Kneeling Crunch", "Cable Seated Crunch")
        ),
        normalizeKey("Elevaciones de Piernas Colgado en Barra") to DatasetMatchQuery(
            preferredIds = listOf("0472"),
            englishCandidates = listOf("Hanging Leg Raise")
        ),
        normalizeKey("Rueda Abdominal (Ab Wheel Rollout)") to DatasetMatchQuery(
            preferredIds = listOf("0857"),
            englishCandidates = listOf("Wheel Rollout", "Ab Wheel Rollout")
        ),
        normalizeKey("Plancha Abdominal Isométrica") to DatasetMatchQuery(
            preferredIds = listOf("0464"),
            englishCandidates = listOf("Front Plank", "Plank")
        ),
        normalizeKey("Giros Rusos (Russian Twists) con Mancuerna") to DatasetMatchQuery(
            preferredIds = listOf("0687"),
            englishCandidates = listOf("Russian Twist", "Weighted Russian Twist")
        )
    )

    /**
     * Descarga una sola vez en segundo plano `${DATASET_BASE}/data/exercises.json` y vincula
     * automáticamente los ejercicios existentes en Room con su miniatura `.jpg` (180x180),
     * su animación `.gif` 3D y sus instrucciones en español (`instruction_steps.es`),
     * respetando siempre cualquier imagen personalizada editada manualmente con el botón ✏️.
     */
    suspend fun syncExercisesWith3dDatasetIfNeeded(
        context: Context,
        exerciseDao: ExerciseDao
    ) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingExercises = exerciseDao.getAllExercisesList()
            if (existingExercises.isEmpty()) return@withContext

            val alreadySyncedFlag = prefs.getBoolean(PREF_KEY_3D_SYNCED, false)
            val hasUnlinkedDefaultExercises = existingExercises.any { ex ->
                !isUserCustomizedMedia(ex.gifPathOrUrl) && !ex.gifPathOrUrl.startsWith(DATASET_BASE)
            }

            if (alreadySyncedFlag && !hasUnlinkedDefaultExercises) {
                return@withContext
            }

            val remoteCatalog = fetchRemoteDatasetExercises()
            if (remoteCatalog.isEmpty()) return@withContext

            val byId = remoteCatalog.associateBy { it.id }
            val byExactName = remoteCatalog.associateBy { it.nameLower }

            val updatedEntities = mutableListOf<com.example.gymtracker.data.local.entity.ExerciseEntity>()

            for (exercise in existingExercises) {
                val match = findBestDatasetMatch(exercise.name, remoteCatalog, byId, byExactName) ?: continue

                val newThumbnailUrl = "$DATASET_BASE/${match.imagePath.removePrefix("/")}"
                val newGifUrl = "$DATASET_BASE/${match.gifPath.removePrefix("/")}"
                val newInstructions = if (match.instructionsEs.isNotEmpty()) {
                    match.instructionsEs.mapIndexed { idx, step ->
                        val cleaned = step.trim()
                        if (cleaned.matches(Regex("^\\d+[.)].*"))) cleaned else "${idx + 1}. $cleaned"
                    }.joinToString("\n")
                } else {
                    exercise.instructions
                }

                // Respetar siempre la imagen personalizada si el usuario la editó manualmente con el botón ✏️
                val keepUserCustomImage = isUserCustomizedMedia(exercise.gifPathOrUrl) ||
                    isUserCustomizedMedia(exercise.thumbnailUrl)

                val finalThumb = if (keepUserCustomImage) exercise.thumbnailUrl else newThumbnailUrl
                val finalGif = if (keepUserCustomImage) exercise.gifPathOrUrl else newGifUrl

                if (finalThumb != exercise.thumbnailUrl ||
                    finalGif != exercise.gifPathOrUrl ||
                    (newInstructions.isNotBlank() && newInstructions != exercise.instructions)
                ) {
                    updatedEntities.add(
                        exercise.copy(
                            thumbnailUrl = finalThumb,
                            gifPathOrUrl = finalGif,
                            instructions = if (newInstructions.isNotBlank()) newInstructions else exercise.instructions
                        )
                    )
                }
            }

            if (updatedEntities.isNotEmpty()) {
                exerciseDao.updateExercises(updatedEntities)
            }
            prefs.edit().putBoolean(PREF_KEY_3D_SYNCED, true).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Determina si la URL o ruta actual fue editada manualmente por el usuario (galería local o URL externa propia).
     */
    fun isUserCustomizedMedia(urlOrPath: String): Boolean {
        val trimmed = urlOrPath.trim()
        if (trimmed.isBlank()) return false
        if (trimmed.startsWith(GITHUB_RAW_BASE)) return false
        if (trimmed.startsWith(DATASET_BASE)) return false
        return true
    }

    private fun findBestDatasetMatch(
        spanishName: String,
        remoteCatalog: List<RemoteDatasetExercise>,
        byId: Map<String, RemoteDatasetExercise>,
        byExactName: Map<String, RemoteDatasetExercise>
    ): RemoteDatasetExercise? {
        val normKey = normalizeKey(spanishName)
        val query = SPANISH_TO_DATASET_MAP[normKey]
            ?: SPANISH_TO_DATASET_MAP.entries.firstOrNull {
                normKey.contains(it.key) || it.key.contains(normKey)
            }?.value
            ?: return null

        // 1. Coincidencia exacta por nombre en inglés
        for (candidate in query.englishCandidates) {
            val candLower = candidate.trim().lowercase()
            byExactName[candLower]?.let { return it }
        }

        // 2. Coincidencia por ID del dataset (ej. "0025", "0334")
        for (id in query.preferredIds) {
            byId[id]?.let { return it }
        }

        // 3. Coincidencia parcial por contención de palabras clave en inglés
        for (candidate in query.englishCandidates) {
            val candLower = candidate.trim().lowercase()
            remoteCatalog.firstOrNull {
                it.nameLower.contains(candLower) || candLower.contains(it.nameLower)
            }?.let { return it }

            val tokens = candLower.split(" ", "-", "(", ")")
                .map { it.trim() }
                .filter { it.length >= 3 }
            if (tokens.isNotEmpty()) {
                remoteCatalog.firstOrNull { remote ->
                    tokens.all { token -> remote.nameLower.contains(token) }
                }?.let { return it }
            }
        }

        return null
    }

    private fun fetchRemoteDatasetExercises(): List<RemoteDatasetExercise> {
        val endpoint = "$DATASET_BASE/data/exercises.json"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }

        return try {
            if (connection.responseCode !in 200..299) return emptyList()
            val rawJson = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val jsonArray = JSONArray(rawJson)
            val result = ArrayList<RemoteDatasetExercise>(jsonArray.length())

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val id = obj.optString("id", "").trim()
                val name = obj.optString("name", "").trim()
                val image = obj.optString("image", "").trim()
                val gifUrl = obj.optString("gif_url", "").ifBlank { obj.optString("gifUrl", "") }.trim()

                if (image.isBlank() && gifUrl.isBlank()) continue

                val stepsEs = mutableListOf<String>()
                val instructionStepsObj = obj.optJSONObject("instruction_steps")
                val esStepsArray = instructionStepsObj?.optJSONArray("es")
                if (esStepsArray != null) {
                    for (j in 0 until esStepsArray.length()) {
                        val step = esStepsArray.optString(j, "").trim()
                        if (step.isNotBlank()) stepsEs.add(step)
                    }
                } else {
                    val instructionsObj = obj.optJSONObject("instructions")
                    val esProse = instructionsObj?.optString("es", "")?.trim().orEmpty()
                    if (esProse.isNotBlank()) {
                        stepsEs.addAll(esProse.split("\n").map { it.trim() }.filter { it.isNotBlank() })
                    }
                }

                result.add(
                    RemoteDatasetExercise(
                        id = id,
                        nameLower = name.lowercase(),
                        imagePath = image.ifBlank { gifUrl },
                        gifPath = gifUrl.ifBlank { image },
                        instructionsEs = stepsEs
                    )
                )
            }
            result
        } finally {
            connection.disconnect()
        }
    }

    private fun normalizeKey(input: String): String {
        val decomposed = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    /**
     * Mapa de respaldo inmediato (antes de que termine el primer fetch o si se inicia sin red).
     */
    private val EXERCISE_IMAGE_PATHS: Map<String, String> = mapOf(
        "press de banca plano" to "Barbell_Bench_Press_-_Medium_Grip/0.jpg",
        "press de banca plano con barra" to "Barbell_Bench_Press_-_Medium_Grip/0.jpg",
        "press inclinado con mancuernas" to "Incline_Dumbbell_Press/0.jpg",
        "press declinado" to "Decline_Barbell_Bench_Press/0.jpg",
        "aperturas en polea (crossover)" to "Cable_Crossover/0.jpg",
        "cruces en polea alta" to "Cable_Crossover/0.jpg",
        "fondos en paralelas (dips pecho)" to "Dips_-_Chest_Version/0.jpg",
        "fondos en máquina asistida" to "Dips_-_Chest_Version/0.jpg",
        "press en máquina de pecho (chest press)" to "Leverage_Chest_Press/0.jpg",
        "flexiones de brazos (push-ups)" to "Pushups/0.jpg",
        "peck deck en máquina" to "Butterfly/0.jpg",
        "peso muerto convencional" to "Barbell_Deadlift/0.jpg",
        "dominadas pronas (pull-ups)" to "Pullups/0.jpg",
        "remo con barra (pendlay)" to "Bent_Over_Barbell_Row/0.jpg",
        "remo unilateral con mancuerna" to "One-Arm_Dumbbell_Row/0.jpg",
        "jalón al pecho en polea" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "jalón al pecho prono" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "jalón al pecho agarre neutro" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "jalón al pecho con agarre prono" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "remo en polea baja (gironda)" to "Seated_Cable_Rows/0.jpg",
        "remo sentado en polea (agarre v)" to "Seated_Cable_Rows/0.jpg",
        "remo sentado en polea con agarre en v" to "Seated_Cable_Rows/0.jpg",
        "remo en máquina con apoyo de pecho" to "Leverage_Incline_Chest_Press/0.jpg",
        "remo con apoyo en el pecho" to "Leverage_Incline_Chest_Press/0.jpg",
        "extensión lumbar en máquina" to "Hyperextensions_Back_Extensions/0.jpg",
        "press militar de pie (ohp)" to "Standing_Military_Press/0.jpg",
        "press militar con mancuernas sentado" to "Dumbbell_Shoulder_Press/0.jpg",
        "press de hombros sentado con mancuernas" to "Dumbbell_Shoulder_Press/0.jpg",
        "elevaciones laterales con mancuernas" to "Side_Lateral_Raise/0.jpg",
        "elevaciones laterales en polea" to "Cable_One_Arm_Lateral_Raise/0.jpg",
        "pájaros posteriores con mancuernas" to "Side_Lateral_Raise/0.jpg",
        "peck deck inverso (pájaros)" to "Side_Lateral_Raise/0.jpg",
        "face pulls en polea" to "Face_Pull/0.jpg",
        "press de hombro en máquina" to "Leverage_Shoulder_Press/0.jpg",
        "press de hombros en máquina" to "Leverage_Shoulder_Press/0.jpg",
        "curl de bíceps con barra z" to "Barbell_Curl/0.jpg",
        "curl en banco scott con barra ez" to "Preacher_Curl/0.jpg",
        "curl martillo con mancuernas" to "Hammer_Curls/0.jpg",
        "curl martillo alterno con mancuernas" to "Hammer_Curls/0.jpg",
        "curl inclinado con mancuernas" to "Incline_Dumbbell_Curl/0.jpg",
        "curl en polea baja" to "Cable_Curl/0.jpg",
        "curl de bíceps en polea baja" to "Cable_Curl/0.jpg",
        "curl predicador en banco scott" to "Preacher_Curl/0.jpg",
        "dominadas supinas (chin-ups)" to "Chin-Up/0.jpg",
        "extensiones de tríceps en polea alta con cuerda" to "Triceps_Pushdown/0.jpg",
        "extensión de tríceps en polea" to "Triceps_Pushdown/0.jpg",
        "extensión de tríceps en polea o máquina" to "Triceps_Pushdown/0.jpg",
        "press francés con barra z" to "Lying_Triceps_Press/0.jpg",
        "press de banca agarre cerrado" to "Close-Grip_Barbell_Bench_Press/0.jpg",
        "press de banca con agarre cerrado" to "Close-Grip_Barbell_Bench_Press/0.jpg",
        "fondos entre bancos para tríceps" to "Bench_Dips/0.jpg",
        "extensión de tríceps sobre la cabeza con mancuerna" to "Standing_Dumbbell_Triceps_Extension/0.jpg",
        "extensión de tríceps en máquina" to "Triceps_Pushdown/0.jpg",
        "sentadilla trasera con barra (back squat)" to "Barbell_Squat/0.jpg",
        "sentadilla frontal" to "Front_Barbell_Squat/0.jpg",
        "sentadilla en smith" to "Barbell_Squat/0.jpg",
        "sentadilla en máquina smith" to "Barbell_Squat/0.jpg",
        "prensa de piernas 45°" to "Leg_Press/0.jpg",
        "prensa inclinada a 45°" to "Leg_Press/0.jpg",
        "prensa horizontal / sentada" to "Leg_Press/0.jpg",
        "extensión de cuádriceps en máquina" to "Leg_Extensions/0.jpg",
        "extensiones de cuádriceps" to "Leg_Extensions/0.jpg",
        "sentadilla búlgara con mancuernas" to "Dumbbell_Lunges/0.jpg",
        "sentadilla hack en máquina" to "Barbell_Hack_Squat/0.jpg",
        "zancadas caminando con mancuernas" to "Dumbbell_Lunges/0.jpg",
        "hip thrust (máquina o barra)" to "Barbell_Hip_Thrust/0.jpg",
        "máquina de abductores" to "Thigh_Abductor/0.jpg",
        "máquina de aductores" to "Thigh_Adductor/0.jpg",
        "peso muerto rumano con barra" to "Romanian_Deadlift/0.jpg",
        "peso muerto rumano" to "Romanian_Deadlift/0.jpg",
        "peso muerto rumano con mancuernas" to "Romanian_Deadlift/0.jpg",
        "curl femoral tumbado en máquina" to "Lying_Leg_Curls/0.jpg",
        "curl femoral sentado en máquina" to "Seated_Leg_Curl/0.jpg",
        "curl femoral sentado" to "Seated_Leg_Curl/0.jpg",
        "curl nórdico de isquios" to "Lying_Leg_Curls/0.jpg",
        "elevación de talones de pie en máquina" to "Standing_Calf_Raises/0.jpg",
        "elevaciones de gemelos (pantorrilla)" to "Standing_Calf_Raises/0.jpg",
        "elevación de talones sentado en máquina" to "Seated_Calf_Raise/0.jpg",
        "elevación de gemelos en prensa de piernas" to "Calf_Press_On_The_Leg_Press_Machine/0.jpg",
        "elevación de talones unilateral con mancuerna" to "Standing_Calf_Raises/0.jpg",
        "press de pecho en máquina" to "Leverage_Chest_Press/0.jpg",
        "crunch abdominal en máquina" to "Cable_Crunch/0.jpg",
        "crunches abdominales en máquina" to "Cable_Crunch/0.jpg",
        "elevaciones de piernas colgado en barra" to "Hanging_Leg_Raise/0.jpg",
        "rueda abdominal (ab wheel rollout)" to "Ab_Roller/0.jpg",
        "crunch abdominal en polea alta" to "Cable_Crunch/0.jpg",
        "plancha abdominal isométrica" to "Plank/0.jpg",
        "giros rusos (russian twists) con mancuerna" to "Russian_Twist/0.jpg"
    )

    private val MUSCLE_GROUP_FALLBACKS: Map<String, String> = mapOf(
        "pecho" to "Barbell_Bench_Press_-_Medium_Grip/0.jpg",
        "espalda" to "Pullups/0.jpg",
        "hombros" to "Dumbbell_Shoulder_Press/0.jpg",
        "bíceps" to "Barbell_Curl/0.jpg",
        "tríceps" to "Triceps_Pushdown/0.jpg",
        "piernas" to "Barbell_Squat/0.jpg",
        "cuádriceps" to "Barbell_Squat/0.jpg",
        "isquiotibiales" to "Romanian_Deadlift/0.jpg",
        "core" to "Plank/0.jpg",
        "abdomen" to "Plank/0.jpg"
    )

    fun getImageUrlForExercise(name: String, muscleGroup: String = ""): String {
        val cleanName = name.trim().lowercase()
        val path = EXERCISE_IMAGE_PATHS[cleanName]
            ?: EXERCISE_IMAGE_PATHS.entries.firstOrNull { cleanName.contains(it.key) || it.key.contains(cleanName) }?.value
            ?: getFallbackForMuscle(muscleGroup)

        return "$GITHUB_RAW_BASE$path"
    }

    private fun getFallbackForMuscle(muscleGroup: String): String {
        val clean = muscleGroup.trim().lowercase()
        return MUSCLE_GROUP_FALLBACKS[clean]
            ?: MUSCLE_GROUP_FALLBACKS.entries.firstOrNull { clean.contains(it.key) }?.value
            ?: "Barbell_Bench_Press_-_Medium_Grip/0.jpg"
    }
}
