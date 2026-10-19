package com.example.gymtracker.domain.util

import androidx.compose.ui.graphics.Color
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.model.HypertrophyMuscle
import kotlin.math.max

/**
 * Niveles competitivos de Rango de Fuerza por ejercicio (basados en fuerza relativa 1RM / Peso Corporal).
 */
enum class StrengthRankTier(
    val displayName: String,
    val badgeIcon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val baseRatioThreshold: Double
) {
    BRONZE(
        displayName = "Bronce",
        badgeIcon = "🛡️",
        primaryColor = Color(0xFFCD7F32),
        secondaryColor = Color(0xFF8B5A2B),
        baseRatioThreshold = 0.0
    ),
    SILVER(
        displayName = "Plata",
        badgeIcon = "⚔️",
        primaryColor = Color(0xFFC0C8D8),
        secondaryColor = Color(0xFF7E8796),
        baseRatioThreshold = 0.75
    ),
    GOLD(
        displayName = "Oro",
        badgeIcon = "👑",
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFDAA520),
        baseRatioThreshold = 1.00
    ),
    PLATINUM(
        displayName = "Platino",
        badgeIcon = "💠",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF0097A7),
        baseRatioThreshold = 1.25
    ),
    DIAMOND(
        displayName = "Diamante",
        badgeIcon = "💎",
        primaryColor = Color(0xFFB388FF),
        secondaryColor = Color(0xFF7C4DFF),
        baseRatioThreshold = 1.55
    ),
    TITAN(
        displayName = "Titán",
        badgeIcon = "⚡",
        primaryColor = Color(0xFFFF3D00),
        secondaryColor = Color(0xFFD50000),
        baseRatioThreshold = 1.85
    );

    fun nextTier(): StrengthRankTier? {
        val idx = entries.indexOf(this)
        return if (idx in 0 until entries.lastIndex) entries[idx + 1] else null
    }
}

/**
 * Umbral individual de un rango para mostrar en la escalera competitiva de un ejercicio.
 */
data class TierThresholdItem(
    val tier: StrengthRankTier,
    val required1RmKg: Double,
    val isUnlocked: Boolean
)

/**
 * Resultado del cálculo de Rango de Fuerza Competitivo para un ejercicio.
 */
data class StrengthRankInfo(
    val exerciseId: Long = 0L,
    val exerciseName: String,
    val muscleGroup: String = "",
    val bestOneRepMaxKg: Double,
    val bodyweightKg: Double,
    val relativeStrengthRatio: Double,
    val currentTier: StrengthRankTier,
    val nextTier: StrengthRankTier?,
    val targetOneRepMaxForNextTierKg: Double?,
    val kgNeededForNextTier: Double,
    val progressToNextTier: Float,
    val allTierThresholds: List<TierThresholdItem> = emptyList()
)

/**
 * Representa un Récord Personal (PR) de 1RM batido en una sesión de entrenamiento.
 */
data class PersonalRecordAchievement(
    val exerciseId: Long,
    val exerciseName: String,
    val newBest1RmKg: Double,
    val previousBest1RmKg: Double,
    val bestSetWeightKg: Double,
    val bestSetReps: Int,
    val xpReward: Int = RpgGamificationEngine.XP_PER_PR_1RM
)

/**
 * Perfil de Nivel de Atleta y Experiencia (XP) estilo RPG.
 */
data class AthleteXpProfile(
    val level: Int = 1,
    val athleteTitle: String = "Iniciado del Hierro",
    val totalXp: Int = 0,
    val xpInCurrentLevel: Int = 0,
    val xpForNextLevel: Int = 500,
    val levelProgressFraction: Float = 0f,
    val completedSetsCount: Int = 0,
    val completedWorkoutsCount: Int = 0,
    val totalPrsCount: Int = 0,
    val nutritionAndWeightLogsCount: Int = 0,
    val setsXp: Int = 0,
    val workoutsXp: Int = 0,
    val prsXp: Int = 0,
    val nutritionLogsXp: Int = 0
)

object RpgGamificationEngine {

    // Reglas oficiales de XP del Módulo 4
    const val XP_PER_COMPLETED_SET = 10
    const val XP_PER_FINISHED_WORKOUT = 150
    const val XP_PER_PR_1RM = 300
    const val XP_PER_WEIGHT_OR_NUTRITION_LOG = 50
    const val XP_PER_LEVEL = 500

    /**
     * Calcula el 1RM estimado usando la fórmula de Epley: peso * (1 + reps / 30.0)
     */
    fun calculateOneRepMax(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        return if (reps == 1) weightKg else weightKg * (1.0 + reps / 30.0)
    }

    /**
     * Factor biomecánico según el tipo de ejercicio para que la fuerza relativa (1RM / Peso Corporal)
     * sea justa tanto en básicos pesados (Sentadilla/Peso Muerto), empujes/tirones (Banca/Dominadas)
     * como en analíticos (Bíceps/Hombros/Tríceps).
     */
    fun getBiomechanicMultiplier(exerciseName: String, muscleGroup: String = ""): Double {
        val text = "$exerciseName $muscleGroup".lowercase()
        return when {
            text.contains("peso muerto") || text.contains("sentadilla") ||
                text.contains("prensa") || text.contains("hip thrust") -> 1.25
            text.contains("banca") || text.contains("remo") ||
                text.contains("dominada") || text.contains("jalón") || text.contains("jalon") ||
                text.contains("fondos") || text.contains("pecho") || text.contains("espalda") -> 1.00
            text.contains("militar") || text.contains("hombro") ||
                text.contains("zancada") || text.contains("búlgara") || text.contains("bulgara") -> 0.65
            text.contains("curl") || text.contains("bíceps") || text.contains("biceps") ||
                text.contains("tríceps") || text.contains("triceps") ||
                text.contains("elevaciones") || text.contains("extensión") ||
                text.contains("extension") || text.contains("gemelo") || text.contains("abdomen") -> 0.45
            else -> 0.85
        }
    }

    /**
     * Calcula el Rango de Fuerza Competitivo (Bronce, Plata, Oro, Platino, Diamante, Titán)
     * comparando el mejor 1RM del ejercicio con el peso corporal actual del usuario.
     */
    fun calculateStrengthRank(
        exerciseId: Long = 0L,
        exerciseName: String,
        muscleGroup: String = "",
        bestOneRepMaxKg: Double,
        bodyweightKg: Double
    ): StrengthRankInfo {
        val safeBodyweight = if (bodyweightKg > 25.0) bodyweightKg else 75.0
        val safe1Rm = bestOneRepMaxKg.coerceAtLeast(0.0)
        val rawRatio = safe1Rm / safeBodyweight
        val multiplier = getBiomechanicMultiplier(exerciseName, muscleGroup)

        // Umbrales en kg de 1RM para cada rango en este ejercicio y peso corporal
        fun threshold1RmKg(tier: StrengthRankTier): Double {
            return tier.baseRatioThreshold * multiplier * safeBodyweight
        }

        val currentTier = StrengthRankTier.entries.lastOrNull { tier ->
            safe1Rm >= threshold1RmKg(tier)
        } ?: StrengthRankTier.BRONZE

        val nextTier = currentTier.nextTier()
        val currentFloorKg = threshold1RmKg(currentTier)
        val nextTargetKg = nextTier?.let { threshold1RmKg(it) }

        val kgNeeded = if (nextTargetKg != null) {
            max(0.0, nextTargetKg - safe1Rm)
        } else {
            0.0
        }

        val progress = if (nextTargetKg != null && nextTargetKg > currentFloorKg) {
            ((safe1Rm - currentFloorKg) / (nextTargetKg - currentFloorKg)).toFloat().coerceIn(0f, 1f)
        } else {
            1f
        }

        val allThresholds = StrengthRankTier.entries.map { tier ->
            val reqKg = threshold1RmKg(tier)
            TierThresholdItem(
                tier = tier,
                required1RmKg = reqKg,
                isUnlocked = safe1Rm >= reqKg && (tier == StrengthRankTier.BRONZE || safe1Rm > 0.0)
            )
        }

        return StrengthRankInfo(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            muscleGroup = muscleGroup,
            bestOneRepMaxKg = safe1Rm,
            bodyweightKg = safeBodyweight,
            relativeStrengthRatio = rawRatio,
            currentTier = currentTier,
            nextTier = nextTier,
            targetOneRepMaxForNextTierKg = nextTargetKg,
            kgNeededForNextTier = kgNeeded,
            progressToNextTier = progress,
            allTierThresholds = allThresholds
        )
    }

    /**
     * Calcula los Rangos de Fuerza para una lista de ejercicios a partir del historial de sesiones y el peso corporal actual.
     */
    fun calculateExerciseStrengthRanks(
        exercises: List<ExerciseEntity>,
        sessions: List<WorkoutSessionWithDetails>,
        bodyweightKg: Double
    ): Map<Long, StrengthRankInfo> {
        val best1RmByExercise = mutableMapOf<Long, Double>()
        sessions.forEach { sessionDetail ->
            sessionDetail.sets
                .filter { it.set.isCompleted }
                .forEach { setWithEx ->
                    val est1Rm = calculateOneRepMax(setWithEx.set.weightKg, setWithEx.set.reps)
                    val prev = best1RmByExercise[setWithEx.set.exerciseId] ?: 0.0
                    if (est1Rm > prev) {
                        best1RmByExercise[setWithEx.set.exerciseId] = est1Rm
                    }
                }
        }

        return exercises.associate { exercise ->
            val best1Rm = best1RmByExercise[exercise.id] ?: 0.0
            exercise.id to calculateStrengthRank(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                muscleGroup = exercise.primaryMuscle.ifBlank { exercise.muscleGroup },
                bestOneRepMaxKg = best1Rm,
                bodyweightKg = bodyweightKg
            )
        }
    }

    /**
     * Calcula el perfil RPG completo del atleta (Nivel, Título, XP Total y desglose)
     * a partir del historial de sesiones, registros de peso y nutrición diaria.
     */
    fun calculateAthleteXpProfile(
        sessions: List<WorkoutSessionWithDetails>,
        weightLogsCount: Int,
        hasLoggedNutritionToday: Boolean = false
    ): AthleteXpProfile {
        val sortedSessions = sessions.sortedBy { it.session.startTime }

        var totalCompletedSets = 0
        var totalPrsCount = 0
        var completedWorkoutsCount = 0
        val best1RmByExercise = mutableMapOf<Long, Double>()

        sortedSessions.forEach { sessionDetail ->
            if (sessionDetail.session.endTime != null) {
                completedWorkoutsCount += 1
            }
            val completedSetsInSession = sessionDetail.sets.filter { it.set.isCompleted }
            totalCompletedSets += completedSetsInSession.size

            // Agrupar por ejercicio dentro de la sesión para detectar nuevos PRs de 1RM
            val setsByExercise = completedSetsInSession.groupBy { it.set.exerciseId }
            setsByExercise.forEach { (exerciseId, setList) ->
                val sessionBest1Rm = setList.maxOfOrNull {
                    calculateOneRepMax(it.set.weightKg, it.set.reps)
                } ?: 0.0

                if (sessionBest1Rm > 0.0) {
                    val previousBest = best1RmByExercise[exerciseId] ?: 0.0
                    // Si supera el récord anterior (o establece su primer récord oficial > 0)
                    if (sessionBest1Rm > previousBest + 0.1) {
                        totalPrsCount += 1
                        best1RmByExercise[exerciseId] = sessionBest1Rm
                    }
                }
            }
        }

        val totalNutritionAndWeightLogs = weightLogsCount + (if (hasLoggedNutritionToday) 1 else 0)

        val setsXp = totalCompletedSets * XP_PER_COMPLETED_SET
        val workoutsXp = completedWorkoutsCount * XP_PER_FINISHED_WORKOUT
        val prsXp = totalPrsCount * XP_PER_PR_1RM
        val nutritionXp = totalNutritionAndWeightLogs * XP_PER_WEIGHT_OR_NUTRITION_LOG

        val totalXp = setsXp + workoutsXp + prsXp + nutritionXp
        val level = (totalXp / XP_PER_LEVEL) + 1
        val xpInCurrent = totalXp % XP_PER_LEVEL
        val progress = (xpInCurrent.toFloat() / XP_PER_LEVEL.toFloat()).coerceIn(0f, 1f)

        return AthleteXpProfile(
            level = level,
            athleteTitle = resolveAthleteTitle(level),
            totalXp = totalXp,
            xpInCurrentLevel = xpInCurrent,
            xpForNextLevel = XP_PER_LEVEL,
            levelProgressFraction = progress,
            completedSetsCount = totalCompletedSets,
            completedWorkoutsCount = completedWorkoutsCount,
            totalPrsCount = totalPrsCount,
            nutritionAndWeightLogsCount = totalNutritionAndWeightLogs,
            setsXp = setsXp,
            workoutsXp = workoutsXp,
            prsXp = prsXp,
            nutritionLogsXp = nutritionXp
        )
    }

    /**
     * Evalúa qué ejercicios han batido un Récord Personal (PR) de 1RM en la sesión actual
     * comparándolos con todas las sesiones previas del historial.
     */
    fun detectSessionPersonalRecords(
        currentWorkoutId: Long,
        currentExerciseSets: List<Triple<Long, String, List<Pair<Double, Int>>>>, // exerciseId, name, list of (weightKg, reps)
        previousSessions: List<WorkoutSessionWithDetails>
    ): List<PersonalRecordAchievement> {
        // 1. Calcular el mejor 1RM histórico previo (excluyendo la sesión actual y sesiones posteriores)
        val historicalBest1Rm = mutableMapOf<Long, Double>()
        previousSessions
            .filter { it.session.id != currentWorkoutId && it.session.endTime != null }
            .forEach { sessionDetail ->
                sessionDetail.sets
                    .filter { it.set.isCompleted }
                    .forEach { setWithEx ->
                        val est1Rm = calculateOneRepMax(setWithEx.set.weightKg, setWithEx.set.reps)
                        val prev = historicalBest1Rm[setWithEx.set.exerciseId] ?: 0.0
                        if (est1Rm > prev) {
                            historicalBest1Rm[setWithEx.set.exerciseId] = est1Rm
                        }
                    }
            }

        // 2. Comparar cada ejercicio de la sesión actual contra su máximo histórico
        val achievements = mutableListOf<PersonalRecordAchievement>()
        currentExerciseSets.forEach { (exerciseId, exerciseName, completedWeightReps) ->
            if (completedWeightReps.isNotEmpty()) {
                var bestSetWeight = 0.0
                var bestSetReps = 0
                var bestSession1Rm = 0.0

                completedWeightReps.forEach { (w, r) ->
                    val est = calculateOneRepMax(w, r)
                    if (est > bestSession1Rm) {
                        bestSession1Rm = est
                        bestSetWeight = w
                        bestSetReps = r
                    }
                }

                val prevBest = historicalBest1Rm[exerciseId] ?: 0.0
                if (bestSession1Rm > 0.0 && bestSession1Rm > prevBest + 0.1) {
                    achievements.add(
                        PersonalRecordAchievement(
                            exerciseId = exerciseId,
                            exerciseName = exerciseName,
                            newBest1RmKg = bestSession1Rm,
                            previousBest1RmKg = prevBest,
                            bestSetWeightKg = bestSetWeight,
                            bestSetReps = bestSetReps,
                            xpReward = XP_PER_PR_1RM
                        )
                    )
                }
            }
        }
        return achievements
    }

    /**
     * Identifica los grupos musculares de hipertrofia trabajados en una lista de ejercicios.
     */
    fun resolveWorkedMuscles(
        exercises: List<Triple<String, String, String>> // (primaryMuscle, muscleGroup, exerciseName)
    ): Set<HypertrophyMuscle> {
        return exercises.mapNotNull { (primary, group, name) ->
            HypertrophyMuscle.fromExerciseInfo(
                primaryMuscle = primary,
                muscleGroup = group,
                exerciseName = name
            )
        }.toSet()
    }

    fun resolveAthleteTitle(level: Int): String {
        return when {
            level <= 2 -> "Iniciado del Hierro"
            level in 3..5 -> "Atleta de Fuerza"
            level in 6..9 -> "Guerrero de Hipertrofia"
            level in 10..14 -> "Élite de Acero"
            level in 15..20 -> "Maestro Olímpico"
            else -> "Titán Legendario"
        }
    }
}
