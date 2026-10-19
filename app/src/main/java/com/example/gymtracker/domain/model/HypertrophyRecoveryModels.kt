package com.example.gymtracker.domain.model

/**
 * Grupos musculares para análisis de estímulo e hipertrofia muscular,
 * conectados directamente con el Creador/Editor del Catálogo de Ejercicios:
 * Pecho, Espalda, Hombros, Bíceps, Tríceps, Cuádriceps, Isquiotibiales,
 * Glúteos, Aductores, Abductores, Pantorrillas, Abdomen / Core, Zona Lumbar.
 */
enum class HypertrophyMuscle(
    val id: String,
    val displayName: String,
    val catalogLabel: String,
    val targetOptimalMin: Int = 10,
    val targetOptimalMax: Int = 20
) {
    PECHO("pecho", "Pectoral / Pecho", "Pecho"),
    ESPALDA("espalda", "Dorsales / Espalda", "Espalda"),
    HOMBROS("hombros", "Deltoides / Hombros", "Hombros"),
    BICEPS("biceps", "Bíceps", "Bíceps"),
    TRICEPS("triceps", "Tríceps", "Tríceps"),
    CUADRICEPS("cuadriceps", "Cuádriceps", "Cuádriceps"),
    ISQUIOTIBIALES("isquiotibiales", "Isquiotibiales", "Isquiotibiales"),
    GLUTEOS("gluteos", "Glúteos", "Glúteos"),
    ADUCTORES("aductores", "Aductores", "Aductores"),
    ABDUCTORES("abductores", "Abductores", "Abductores"),
    GEMELOS("gemelos", "Pantorrillas / Gemelos", "Pantorrillas"),
    ABDOMEN("abdomen", "Abdomen / Core", "Abdomen / Core"),
    ZONA_LUMBAR("zona_lumbar", "Zona Lumbar", "Zona Lumbar");

    companion object {
        val ALL_ORDERED: List<HypertrophyMuscle> = listOf(
            PECHO,
            ESPALDA,
            HOMBROS,
            BICEPS,
            TRICEPS,
            CUADRICEPS,
            ISQUIOTIBIALES,
            GLUTEOS,
            ADUCTORES,
            ABDUCTORES,
            GEMELOS,
            ABDOMEN,
            ZONA_LUMBAR
        )

        val CATALOG_PRIMARY_MUSCLES: List<String> = listOf(
            "Pecho",
            "Espalda",
            "Hombros",
            "Bíceps",
            "Tríceps",
            "Cuádriceps",
            "Isquiotibiales",
            "Glúteos",
            "Aductores",
            "Abductores",
            "Pantorrillas",
            "Abdomen / Core",
            "Zona Lumbar"
        )

        val CATALOG_EQUIPMENTS: List<String> = listOf(
            "Máquina",
            "Barra",
            "Mancuernas",
            "Polea",
            "Barra EZ",
            "Prensa / Smith",
            "Peso Corporal"
        )

        /**
         * Analiza una cadena individual de músculo (ej. "Pecho", "Aductores", "Pantorrillas", "Zona Lumbar").
         */
        fun fromSingleMuscleText(rawText: String): HypertrophyMuscle? {
            val text = rawText.trim().lowercase()
            if (text.isBlank()) return null

            return when {
                text.contains("abductor") || text.contains("abducción") || text.contains("abduccion") -> ABDUCTORES
                text.contains("aductor") || text.contains("aducción") || text.contains("aduccion") -> ADUCTORES
                text.contains("lumbar") || text.contains("erector") || text.contains("espalda baja") -> ZONA_LUMBAR
                text.contains("pecho") || text.contains("pectoral") || text.contains("chest") -> PECHO
                text.contains("espalda") || text.contains("dorsal") || text.contains("trapecio") ||
                    text.contains("romboide") || text.contains("back") || text.contains("lats") -> ESPALDA
                text.contains("gemelo") || text.contains("pantorrilla") || text.contains("sóleo") ||
                    text.contains("soleo") || text.contains("calf") || text.contains("talón") -> GEMELOS
                text.contains("glúteo") || text.contains("gluteo") || text.contains("hip thrust") ||
                    text.contains("patada de glúteo") -> GLUTEOS
                text.contains("abdomen") || text.contains("abdominal") || text.contains("core") ||
                    text.contains("plancha") || text.contains("crunch") || text.contains("oblicuo") -> ABDOMEN
                text.contains("cuádricep") || text.contains("cuadricep") || text.contains("quad") -> CUADRICEPS
                text.contains("isquio") || text.contains("femoral") || text.contains("hamstring") -> ISQUIOTIBIALES
                text.contains("hombro") || text.contains("deltoide") || text.contains("deltoid") ||
                    text.contains("shoulder") -> HOMBROS
                text.contains("trícep") || text.contains("tricep") -> TRICEPS
                text.contains("bícep") || text.contains("bicep") || text.contains("braquial") -> BICEPS
                else -> null
            }
        }

        /**
         * Empareja un ejercicio con su grupo muscular de hipertrofia principal.
         * CRÍTICO: Prioriza estrictamente [primaryMuscle] y [muscleGroup] sobre [exerciseName]
         * para que al editar el músculo primario de cualquier ejercicio existente o nuevo,
         * la pestaña "Estímulo" contabilice automáticamente sus series en el nuevo músculo seleccionado.
         */
        fun fromExerciseInfo(primaryMuscle: String, muscleGroup: String, exerciseName: String): HypertrophyMuscle? {
            // 1. Prioridad absoluta al Músculo Primario configurado en el ejercicio
            fromSingleMuscleText(primaryMuscle)?.let { return it }

            // 2. Segunda prioridad al Grupo Muscular configurado en el ejercicio
            fromSingleMuscleText(muscleGroup)?.let { return it }

            // 3. Respaldo por palabras clave del nombre o combinación completa
            val combined = "$primaryMuscle $muscleGroup $exerciseName".lowercase()
            return when {
                combined.contains("abductor") || combined.contains("abducción") || combined.contains("abduccion") -> ABDUCTORES
                combined.contains("aductor") || combined.contains("aducción") || combined.contains("aduccion") -> ADUCTORES
                combined.contains("lumbar") || combined.contains("buenos días") || combined.contains("hiperextensión") -> ZONA_LUMBAR
                combined.contains("pecho") || combined.contains("pectoral") || combined.contains("chest") ||
                    combined.contains("banca") || combined.contains("aperturas") -> PECHO
                combined.contains("espalda") || combined.contains("dorsal") || combined.contains("trapecio") ||
                    combined.contains("remo") || combined.contains("jalón") || combined.contains("jalon") ||
                    combined.contains("dominada") || combined.contains("back") || combined.contains("lats") -> ESPALDA
                combined.contains("gemelo") || combined.contains("pantorrilla") || combined.contains("sóleo") ||
                    combined.contains("soleo") || combined.contains("calf") || combined.contains("talón") -> GEMELOS
                combined.contains("glúteo") || combined.contains("gluteo") || combined.contains("hip thrust") ||
                    combined.contains("patada de glúteo") -> GLUTEOS
                combined.contains("abdomen") || combined.contains("abdominal") || combined.contains("core") ||
                    combined.contains("plancha") || combined.contains("crunch") || combined.contains("oblicuo") -> ABDOMEN
                combined.contains("cuádricep") || combined.contains("cuadricep") || combined.contains("quad") ||
                    combined.contains("sentadilla") || combined.contains("prensa") || combined.contains("búlgara") ||
                    combined.contains("bulgara") || combined.contains("extensión de rodilla") ||
                    combined.contains("extension de rodilla") || combined.contains("sissy") -> CUADRICEPS
                combined.contains("isquio") || combined.contains("femoral") || combined.contains("hamstring") ||
                    combined.contains("peso muerto") || combined.contains("curl femoral") ||
                    combined.contains("puente") -> ISQUIOTIBIALES
                combined.contains("hombro") || combined.contains("deltoide") || combined.contains("deltoid") ||
                    combined.contains("shoulder") || combined.contains("militar") || combined.contains("elevaciones laterales") ||
                    combined.contains("elevacion lateral") || combined.contains("pájaro") || combined.contains("pajaro") -> HOMBROS
                combined.contains("bícep") || combined.contains("bicep") || (combined.contains("braquial") &&
                    !combined.contains("trícep")) || (combined.contains("curl") && !combined.contains("femoral")) -> BICEPS
                combined.contains("trícep") || combined.contains("tricep") || combined.contains("copa") ||
                    combined.contains("fondos") || combined.contains("press francés") ||
                    combined.contains("press frances") || combined.contains("agarre cerrado") -> TRICEPS
                combined.contains("pierna") || combined.contains("legs") -> CUADRICEPS
                combined.contains("brazo") || combined.contains("arms") -> BICEPS
                else -> null
            }
        }

        /**
         * Extrae la lista de músculos secundarios configurados en un ejercicio (aportan 0.5 series cada uno).
         */
        fun fromSecondaryMuscles(secondaryMusclesText: String, excludePrimary: HypertrophyMuscle? = null): List<HypertrophyMuscle> {
            if (secondaryMusclesText.isBlank()) return emptyList()
            return secondaryMusclesText
                .split(",", ";", "•")
                .mapNotNull { token -> fromSingleMuscleText(token.trim()) }
                .distinct()
                .filter { it != excludePrimary }
        }
    }
}

/**
 * Clasificación del estímulo semanal según volumen de series efectivas para hipertrofia:
 * - Menos de 6 series: "Mantenimiento / Estímulo bajo"
 * - 6 a 9 series: "Estímulo moderado / Progresando"
 * - 10 a 20 series: "Rango óptimo de hipertrofia" (Verde)
 * - Más de 22 series: "Límite de fatiga / Cuidado con sobreentrenamiento" (Amarillo/Rojo)
 */
enum class StimulusClassification(
    val label: String,
    val description: String,
    val isOptimal: Boolean,
    val isFatigueWarning: Boolean
) {
    LOW(
        label = "Mantenimiento / Estímulo bajo",
        description = "Menos de 6 series semanales. Mantiene adaptaciones pero no maximiza el crecimiento.",
        isOptimal = false,
        isFatigueWarning = false
    ),
    MODERATE(
        label = "Estímulo moderado",
        description = "Entre 6 y 9 series semanales. Estímulo positivo en progresión ascendente.",
        isOptimal = false,
        isFatigueWarning = false
    ),
    OPTIMAL(
        label = "Rango óptimo de hipertrofia",
        description = "Entre 10 y 20 series semanales. Máxima síntesis proteica miofibrilar y ganancia neta.",
        isOptimal = true,
        isFatigueWarning = false
    ),
    FATIGUE_LIMIT(
        label = "Límite de fatiga / Cuidado con sobreentrenamiento",
        description = "Más de 22 series semanales. Sobrecarga excesiva, riesgo de fatiga neuromuscular acumulada.",
        isOptimal = false,
        isFatigueWarning = true
    );

    companion object {
        fun fromWeeklySets(sets: Int): StimulusClassification {
            return when {
                sets < 6 -> LOW
                sets in 6..9 -> MODERATE
                sets in 10..20 -> OPTIMAL
                else -> FATIGUE_LIMIT
            }
        }
    }
}

/**
 * Estado del Chip de recuperación muscular para la UI.
 */
enum class RecoveryChipState(val displayName: String) {
    IN_RECOVERY("En recuperación"),
    OPTIMAL("Óptimo"),
    READY("Listo para entrenar")
}

/**
 * Representa el análisis completo de estímulo y recuperación de un músculo específico.
 */
data class MuscleRecoveryItem(
    val muscle: HypertrophyMuscle,
    val weeklySets: Int,
    val previousWeekSets: Int,
    val weeklyVolumeKg: Double,
    val previousWeekVolumeKg: Double,
    val stimulus: StimulusClassification,
    val hoursSinceLastTrained: Long?,
    val recoveryProgress: Float, // 0.0f a 1.0f (llenándose durante 0 a 48h)
    val recoveryMessage: String,
    val chipState: RecoveryChipState,
    val lastTrainedDateFormatted: String
)

/**
 * Barra diaria para visualización gráfica del volumen en los últimos 7 días.
 */
data class DailyVolumeBar(
    val dayLabel: String, // ej. "Lun", "Mar", "Mié"
    val dateFormatted: String, // ej. "14 Oct"
    val timestamp: Long,
    val volumeKg: Double,
    val setsCount: Int,
    val isToday: Boolean
)

/**
 * Resumen de sobrecarga progresiva comparando los últimos 7 días con la semana previa.
 */
data class ProgressiveOverloadSummary(
    val currentWeekVolumeKg: Double,
    val previousWeekVolumeKg: Double,
    val currentWeekSets: Int,
    val previousWeekSets: Int,
    val percentageDiff: Double,
    val isOverloadAchieved: Boolean,
    val dailyBars: List<DailyVolumeBar>
)
