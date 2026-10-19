package com.example.gymtracker.data.preset

/**
 * Modelo para definir un ejercicio dentro de una rutina predefinida con su orden inmutable,
 * cantidad de series pre-creadas y rango de repeticiones objetivo.
 */
data class RoutineExercisePreset(
    val order: Int,
    val exerciseOrder: Int = order - 1,
    val name: String,
    val type: String, // "Compuesto", "Aislamiento", "Compuesto accesorio"
    val muscleGroup: String,
    val equipment: String,
    val targetSets: Int,
    val targetRepRange: String, // ej. "6-8 reps"
    val technique: String
)

/**
 * Modelo para definir una rutina completa del plan día a día con sortOrder estricto (1 al 6).
 */
data class RoutinePreset(
    val dayNumber: Int,
    val sortOrder: Int = dayNumber,
    val title: String,
    val focusDescription: String,
    val exercises: List<RoutineExercisePreset>
)

object DefaultRoutinesData {

    val allPresets: List<RoutinePreset> = listOf(
        // --- DÍA 1 (sortOrder = 1) ---
        RoutinePreset(
            dayNumber = 1,
            sortOrder = 1,
            title = "Día 1 — Empuje A (Pecho, hombros y tríceps)",
            focusDescription = "Pecho, hombros y tríceps. Trabajo estructurado por grupo muscular con series y rango de repeticiones exacto.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    exerciseOrder = 0,
                    name = "Press de banca plano con barra",
                    type = "Compuesto",
                    muscleGroup = "Pecho",
                    equipment = "Barra",
                    targetSets = 3,
                    targetRepRange = "6-8 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 6 a 8 reps. Serie 1 pesada y 2 series de bajada (-10%)."
                ),
                RoutineExercisePreset(
                    order = 2,
                    exerciseOrder = 1,
                    name = "Cruces en polea alta",
                    type = "Aislamiento",
                    muscleGroup = "Pecho",
                    equipment = "Polea",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 10 a 15 reps. En la serie 2, reduce el pin un 30%."
                ),
                RoutineExercisePreset(
                    order = 3,
                    exerciseOrder = 2,
                    name = "Press militar con mancuernas sentado",
                    type = "Compuesto",
                    muscleGroup = "Hombros",
                    equipment = "Mancuerna",
                    targetSets = 2,
                    targetRepRange = "8-10 reps",
                    technique = "Series Lineales: 2 series | Meta: 8 a 10 reps con peso controlado y 2 min de descanso."
                ),
                RoutineExercisePreset(
                    order = 4,
                    exerciseOrder = 3,
                    name = "Elevaciones laterales con mancuernas",
                    type = "Aislamiento",
                    muscleGroup = "Hombros",
                    equipment = "Mancuerna",
                    targetSets = 2,
                    targetRepRange = "12-20 reps",
                    technique = "Series Lineales: 2 series | Meta: 12 a 20 reps estrictas; no balancear el torso."
                ),
                RoutineExercisePreset(
                    order = 5,
                    exerciseOrder = 4,
                    name = "Press de banca con agarre cerrado",
                    type = "Compuesto",
                    muscleGroup = "Tríceps",
                    equipment = "Barra",
                    targetSets = 2,
                    targetRepRange = "8-10 reps",
                    technique = "Pirámide Invertida: 2 series | Meta: 8 a 10 reps enfocando codos cerca del torso."
                ),
                RoutineExercisePreset(
                    order = 6,
                    exerciseOrder = 5,
                    name = "Extensión de tríceps en polea o máquina",
                    type = "Aislamiento",
                    muscleGroup = "Tríceps",
                    equipment = "Polea",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 10 a 15 reps al fallo técnico."
                )
            )
        ),

        // --- DÍA 2 (sortOrder = 2) ---
        RoutinePreset(
            dayNumber = 2,
            sortOrder = 2,
            title = "Día 2 — Tirón A (Espalda, bíceps y abdomen)",
            focusDescription = "Espalda, bíceps y abdomen. Tracción vertical y horizontal seguida de flexores de codo y core.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    exerciseOrder = 0,
                    name = "Jalón al pecho con agarre prono",
                    type = "Compuesto",
                    muscleGroup = "Espalda",
                    equipment = "Polea",
                    targetSets = 3,
                    targetRepRange = "8-12 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 8 a 12 reps controlando el estiramiento dorsal."
                ),
                RoutineExercisePreset(
                    order = 2,
                    exerciseOrder = 1,
                    name = "Remo sentado en polea con agarre en V",
                    type = "Compuesto",
                    muscleGroup = "Espalda",
                    equipment = "Polea",
                    targetSets = 3,
                    targetRepRange = "8-12 reps",
                    technique = "Series Lineales: 3 series | Meta: 8 a 12 reps pausando 1 segundo en contracción."
                ),
                RoutineExercisePreset(
                    order = 3,
                    exerciseOrder = 2,
                    name = "Curl en banco Scott con barra EZ",
                    type = "Aislamiento",
                    muscleGroup = "Bíceps",
                    equipment = "Barra",
                    targetSets = 3,
                    targetRepRange = "10-12 reps",
                    technique = "Series Lineales: 3 series | Meta: 10 a 12 reps sin despegar codos del cojín."
                ),
                RoutineExercisePreset(
                    order = 4,
                    exerciseOrder = 3,
                    name = "Curl martillo alterno con mancuernas",
                    type = "Aislamiento",
                    muscleGroup = "Bíceps",
                    equipment = "Mancuerna",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 10 a 15 reps para braquial y antebrazo."
                ),
                RoutineExercisePreset(
                    order = 5,
                    exerciseOrder = 4,
                    name = "Crunch abdominal en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Core",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "12-15 reps",
                    technique = "Series Lineales: 3 series | Meta: 12 a 15 reps controlando la fase excéntrica."
                )
            )
        ),

        // --- DÍA 3 (sortOrder = 3) ---
        RoutinePreset(
            dayNumber = 3,
            sortOrder = 3,
            title = "Día 3 — Pierna A (Cuádriceps y cadena posterior)",
            focusDescription = "Cuádriceps y cadena posterior. Fuerza pesada en máquina, bisagra de cadera y pantorrillas.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    exerciseOrder = 0,
                    name = "Sentadilla en máquina Smith",
                    type = "Compuesto",
                    muscleGroup = "Cuádriceps",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "8-10 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 8 a 10 reps profundas y controladas."
                ),
                RoutineExercisePreset(
                    order = 2,
                    exerciseOrder = 1,
                    name = "Prensa inclinada a 45°",
                    type = "Compuesto",
                    muscleGroup = "Cuádriceps",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "10-12 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 10 a 12 reps sin bloquear rodillas."
                ),
                RoutineExercisePreset(
                    order = 3,
                    exerciseOrder = 2,
                    name = "Peso muerto rumano",
                    type = "Compuesto",
                    muscleGroup = "Isquiotibiales y Glúteo",
                    equipment = "Barra",
                    targetSets = 2,
                    targetRepRange = "8-12 reps",
                    technique = "Series Lineales: 2 series | Meta: 8 a 12 reps manteniendo columna neutra."
                ),
                RoutineExercisePreset(
                    order = 4,
                    exerciseOrder = 3,
                    name = "Curl femoral tumbado en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Isquiotibiales",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 3 series | Meta: 10 a 15 reps pegando cadera al banco."
                ),
                RoutineExercisePreset(
                    order = 5,
                    exerciseOrder = 4,
                    name = "Elevaciones de gemelos (pantorrilla)",
                    type = "Aislamiento",
                    muscleGroup = "Pantorrillas",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "12-20 reps",
                    technique = "Series Lineales con pausa: 3 series | Meta: 12 a 20 reps con 2 seg en estiramiento."
                )
            )
        ),

        // --- DÍA 4 (sortOrder = 4) ---
        RoutinePreset(
            dayNumber = 4,
            sortOrder = 4,
            title = "Día 4 — Empuje B (Pecho, hombros y tríceps)",
            focusDescription = "Pecho superior, hombros y tríceps. Estímulo hipertrófico y congestión metabólica.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    exerciseOrder = 0,
                    name = "Press inclinado con mancuernas",
                    type = "Compuesto",
                    muscleGroup = "Pecho",
                    equipment = "Mancuerna",
                    targetSets = 3,
                    targetRepRange = "8-10 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 8 a 10 reps enfocando haz clavicular."
                ),
                RoutineExercisePreset(
                    order = 2,
                    exerciseOrder = 1,
                    name = "Press de pecho en máquina",
                    type = "Compuesto",
                    muscleGroup = "Pecho",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "8-12 reps",
                    technique = "Series Lineales: 2 series | Meta: 8 a 12 reps pesadas y estables."
                ),
                RoutineExercisePreset(
                    order = 3,
                    exerciseOrder = 2,
                    name = "Peck Deck en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Pecho",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 10 a 15 reps apretando 1 seg en el centro."
                ),
                RoutineExercisePreset(
                    order = 4,
                    exerciseOrder = 3,
                    name = "Press de hombros en máquina",
                    type = "Compuesto",
                    muscleGroup = "Hombros",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "8-10 reps",
                    technique = "Series Lineales: 2 series | Meta: 8 a 10 reps controlando la bajada."
                ),
                RoutineExercisePreset(
                    order = 5,
                    exerciseOrder = 4,
                    name = "Elevaciones laterales con mancuernas",
                    type = "Aislamiento",
                    muscleGroup = "Hombros",
                    equipment = "Mancuerna",
                    targetSets = 2,
                    targetRepRange = "12-20 reps",
                    technique = "Series Lineales: 2 series | Meta: 12 a 20 reps buscando congestión lateral."
                ),
                RoutineExercisePreset(
                    order = 6,
                    exerciseOrder = 5,
                    name = "Fondos en máquina asistida",
                    type = "Compuesto",
                    muscleGroup = "Tríceps",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "8-10 reps",
                    technique = "Series Lineales: 2 series | Meta: 8 a 10 reps limpias."
                ),
                RoutineExercisePreset(
                    order = 7,
                    exerciseOrder = 6,
                    name = "Extensión de tríceps en polea",
                    type = "Aislamiento",
                    muscleGroup = "Tríceps",
                    equipment = "Polea",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 10 a 15 reps vaciando el tríceps."
                )
            )
        ),

        // --- DÍA 5 (sortOrder = 5) ---
        RoutinePreset(
            dayNumber = 5,
            sortOrder = 5,
            title = "Día 5 — Tirón B (Espalda y bíceps prioritario)",
            focusDescription = "Espalda media, dorsal bajo y aislamiento prioritario de bíceps y abdomen.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    exerciseOrder = 0,
                    name = "Remo con apoyo en el pecho",
                    type = "Compuesto",
                    muscleGroup = "Espalda",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "8-12 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 8 a 12 reps retrayendo escápulas."
                ),
                RoutineExercisePreset(
                    order = 2,
                    exerciseOrder = 1,
                    name = "Jalón al pecho agarre neutro",
                    type = "Compuesto",
                    muscleGroup = "Espalda",
                    equipment = "Polea",
                    targetSets = 3,
                    targetRepRange = "8-12 reps",
                    technique = "Series Lineales: 3 series | Meta: 8 a 12 reps apretando dorsales abajo."
                ),
                RoutineExercisePreset(
                    order = 3,
                    exerciseOrder = 2,
                    name = "Peck Deck inverso (pájaros)",
                    type = "Aislamiento",
                    muscleGroup = "Hombros",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "12-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 12 a 15 reps para deltoides posterior."
                ),
                RoutineExercisePreset(
                    order = 4,
                    exerciseOrder = 3,
                    name = "Extensión lumbar en máquina",
                    type = "Compuesto accesorio",
                    muscleGroup = "Espalda",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Series Lineales: 2 series | Meta: 10 a 15 reps controladas sin hiperextender."
                ),
                RoutineExercisePreset(
                    order = 5,
                    exerciseOrder = 4,
                    name = "Curl de bíceps en polea baja",
                    type = "Aislamiento",
                    muscleGroup = "Bíceps",
                    equipment = "Polea",
                    targetSets = 3,
                    targetRepRange = "10-12 reps",
                    technique = "Drop Set en la última serie: 3 series | Meta: 10 a 12 reps en tensión constante."
                ),
                RoutineExercisePreset(
                    order = 6,
                    exerciseOrder = 5,
                    name = "Curl martillo alterno con mancuernas",
                    type = "Aislamiento",
                    muscleGroup = "Bíceps",
                    equipment = "Mancuerna",
                    targetSets = 2,
                    targetRepRange = "10-15 reps",
                    technique = "Series Lineales: 2 series | Meta: 10 a 15 reps estrictas."
                ),
                RoutineExercisePreset(
                    order = 7,
                    exerciseOrder = 6,
                    name = "Crunch abdominal en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Core",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "12-15 reps",
                    technique = "Series Lineales: 3 series | Meta: 12 a 15 reps con máxima contracción."
                )
            )
        ),

        // --- DÍA 6 (sortOrder = 6) ---
        RoutinePreset(
            dayNumber = 6,
            sortOrder = 6,
            title = "Día 6 — Pierna B (Glúteos y complementos)",
            focusDescription = "Glúteo mayor/medio, isquiotibiales, cuádriceps, aductores y pantorrillas.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    exerciseOrder = 0,
                    name = "Prensa horizontal / sentada",
                    type = "Compuesto",
                    muscleGroup = "Cuádriceps",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "10-12 reps",
                    technique = "Pirámide Invertida: 3 series | Meta: 10 a 12 reps profundas."
                ),
                RoutineExercisePreset(
                    order = 2,
                    exerciseOrder = 1,
                    name = "Hip Thrust (máquina o barra)",
                    type = "Compuesto",
                    muscleGroup = "Glúteo",
                    equipment = "Barra",
                    targetSets = 2,
                    targetRepRange = "8-12 reps",
                    technique = "Pirámide Creciente: 2 series | Meta: 8 a 12 reps apretando glúteo 2 seg arriba."
                ),
                RoutineExercisePreset(
                    order = 3,
                    exerciseOrder = 2,
                    name = "Curl femoral sentado",
                    type = "Aislamiento",
                    muscleGroup = "Isquiotibiales",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "10-15 reps",
                    technique = "Drop Set en la última serie: 3 series | Meta: 10 a 15 reps con rango completo."
                ),
                RoutineExercisePreset(
                    order = 4,
                    exerciseOrder = 3,
                    name = "Extensiones de cuádriceps",
                    type = "Aislamiento",
                    muscleGroup = "Cuádriceps",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "12-15 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 12 a 15 reps con pausa arriba."
                ),
                RoutineExercisePreset(
                    order = 5,
                    exerciseOrder = 4,
                    name = "Máquina de abductores",
                    type = "Aislamiento",
                    muscleGroup = "Glúteo",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "12-20 reps",
                    technique = "Drop Set en la última serie: 2 series | Meta: 12 a 20 reps para glúteo medio."
                ),
                RoutineExercisePreset(
                    order = 6,
                    exerciseOrder = 5,
                    name = "Máquina de aductores",
                    type = "Aislamiento",
                    muscleGroup = "Aductores",
                    equipment = "Máquina",
                    targetSets = 2,
                    targetRepRange = "12-15 reps",
                    technique = "Series Lineales: 2 series | Meta: 12 a 15 reps con apertura controlada."
                ),
                RoutineExercisePreset(
                    order = 7,
                    exerciseOrder = 6,
                    name = "Elevaciones de gemelos (pantorrilla)",
                    type = "Aislamiento",
                    muscleGroup = "Pantorrillas",
                    equipment = "Máquina",
                    targetSets = 3,
                    targetRepRange = "12-20 reps",
                    technique = "Series Lineales con pausa: 3 series | Meta: 12 a 20 reps con estiramiento profundo."
                )
            )
        )
    )

    /**
     * Busca la configuración preset (series, rango de repeticiones y técnica) para un ejercicio.
     */
    fun findExercisePreset(routineTitle: String, exerciseName: String): RoutineExercisePreset? {
        val preset = allPresets.firstOrNull {
            routineTitle.contains("Día ${it.dayNumber}", ignoreCase = true) ||
                routineTitle.contains(it.title, ignoreCase = true) ||
                it.title.contains(routineTitle, ignoreCase = true)
        }
        if (preset != null) {
            val ex = preset.exercises.firstOrNull {
                it.name.equals(exerciseName, ignoreCase = true) ||
                    exerciseName.contains(it.name, ignoreCase = true) ||
                    it.name.contains(exerciseName, ignoreCase = true)
            }
            if (ex != null) return ex
        }

        for (p in allPresets) {
            val ex = p.exercises.firstOrNull {
                it.name.equals(exerciseName, ignoreCase = true) ||
                    exerciseName.contains(it.name, ignoreCase = true) ||
                    it.name.contains(exerciseName, ignoreCase = true)
            }
            if (ex != null) return ex
        }

        return null
    }

    /**
     * Busca la técnica recomendada para un ejercicio dado el título de la rutina (o workout) y el nombre del ejercicio.
     */
    fun findTechnique(routineTitle: String, exerciseName: String): String? {
        return findExercisePreset(routineTitle, exerciseName)?.technique
    }

    /**
     * Genera un bloque de texto estructurado con todas las técnicas de la rutina para guardarlo en RoutineEntity.notes.
     */
    fun generateRoutineNotes(preset: RoutinePreset): String {
        val sb = StringBuilder()
        sb.append(preset.focusDescription).append("\n\n")
        sb.append("Orden estricto y metas por ejercicio:\n")
        preset.exercises.sortedBy { it.exerciseOrder }.forEach { ex ->
            sb.append("${ex.order}. ${ex.name} [${ex.muscleGroup} | ${ex.targetSets} series | Meta: ${ex.targetRepRange}]\n")
        }
        return sb.toString().trim()
    }
}
