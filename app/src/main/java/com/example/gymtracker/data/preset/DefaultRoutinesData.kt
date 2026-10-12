package com.example.gymtracker.data.preset

/**
 * Modelo para definir un ejercicio dentro de una rutina predefinida.
 */
data class RoutineExercisePreset(
    val order: Int,
    val name: String,
    val type: String, // "Compuesto", "Aislamiento", "Compuesto accesorio"
    val muscleGroup: String,
    val equipment: String,
    val technique: String
)

/**
 * Modelo para definir una rutina completa del plan día a día.
 */
data class RoutinePreset(
    val dayNumber: Int,
    val title: String,
    val focusDescription: String,
    val exercises: List<RoutineExercisePreset>
)

object DefaultRoutinesData {

    val allPresets: List<RoutinePreset> = listOf(
        // DÍA 1 — Empuje A (Pecho prioritario)
        RoutinePreset(
            dayNumber = 1,
            title = "Día 1 — Empuje A (Pecho prioritario)",
            focusDescription = "Pectoral prioritario. Enfoque en compuestos pesados primero y congestión de aislamiento al final.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    name = "Press de banca plano con barra",
                    type = "Compuesto",
                    muscleGroup = "Pectoral mayor",
                    equipment = "Barra",
                    technique = "Pirámide Invertida: Serie 1 pesada (6–8 reps) y 2 series de bajada (-10%)."
                ),
                RoutineExercisePreset(
                    order = 2,
                    name = "Press militar con mancuernas",
                    type = "Compuesto",
                    muscleGroup = "Deltoides anterior/lateral",
                    equipment = "Mancuerna",
                    technique = "Series Lineales: Mantén peso fijo para 8–10 reps con 2 min de descanso."
                ),
                RoutineExercisePreset(
                    order = 3,
                    name = "Press de banca agarre cerrado",
                    type = "Compuesto",
                    muscleGroup = "Tríceps braquial",
                    equipment = "Barra",
                    technique = "Pirámide Invertida: Serie 1 fuerte (8 reps) y Serie 2 con -10% (10 reps)."
                ),
                RoutineExercisePreset(
                    order = 4,
                    name = "Cruces en polea alta",
                    type = "Aislamiento",
                    muscleGroup = "Pectoral bajo/interno",
                    equipment = "Polea",
                    technique = "Drop Set en la última serie: En la serie 2, reduce el pin un 30% y saca 5 reps extra."
                ),
                RoutineExercisePreset(
                    order = 5,
                    name = "Elevaciones laterales",
                    type = "Aislamiento",
                    muscleGroup = "Deltoides lateral",
                    equipment = "Mancuerna",
                    technique = "Series Lineales: Carga moderada estricta (12–15 reps); no balancear el torso."
                ),
                RoutineExercisePreset(
                    order = 6,
                    name = "Extensión de tríceps en polea/máquina",
                    type = "Aislamiento",
                    muscleGroup = "Tríceps",
                    equipment = "Polea",
                    technique = "Drop Set en la última serie: Remate final al fallo, baja placas inmediatamente."
                )
            )
        ),

        // DÍA 2 — Tirón A (Espalda y bíceps)
        RoutinePreset(
            dayNumber = 2,
            title = "Día 2 — Tirón A (Espalda y bíceps)",
            focusDescription = "Espalda y bíceps. Trabajo compuesto de tracción vertical y horizontal seguido de trabajo directo de brazos y core.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    name = "Jalón al pecho prono",
                    type = "Compuesto",
                    muscleGroup = "Dorsal ancho",
                    equipment = "Polea",
                    technique = "Pirámide Invertida: Serie 1 pesada a 8 reps, series 2 y 3 bajando 1 placa (10–12 reps)."
                ),
                RoutineExercisePreset(
                    order = 2,
                    name = "Remo sentado en polea (agarre V)",
                    type = "Compuesto",
                    muscleGroup = "Espalda media / Romboides",
                    equipment = "Polea",
                    technique = "Series Lineales: Peso controlado para 8–10 reps pausando 1 segundo al estirar."
                ),
                RoutineExercisePreset(
                    order = 3,
                    name = "Curl en banco Scott con barra EZ",
                    type = "Aislamiento",
                    muscleGroup = "Bíceps (cabeza corta)",
                    equipment = "Barra",
                    technique = "Series Lineales: Peso fijo estricto (10–12 reps) sin despegar codos del cojín."
                ),
                RoutineExercisePreset(
                    order = 4,
                    name = "Curl martillo con mancuernas",
                    type = "Aislamiento",
                    muscleGroup = "Braquial y antebrazo",
                    equipment = "Mancuerna",
                    technique = "Drop Set en la última serie: Termina serie 2, suelta las mancuernas y toma otras un 30% más livianas."
                ),
                RoutineExercisePreset(
                    order = 5,
                    name = "Crunch abdominal en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Recto abdominal",
                    equipment = "Máquina",
                    technique = "Series Lineales: Controla la fase excéntrica (bajada lenta) a 12–15 reps."
                )
            )
        ),

        // DÍA 3 — Pierna A (Cuádriceps y cadena posterior)
        RoutinePreset(
            dayNumber = 3,
            title = "Día 3 — Pierna A (Cuádriceps y cadena posterior)",
            focusDescription = "Cuádriceps y cadena posterior. Fuerza pesada en máquina y estabilidad postural.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    name = "Sentadilla en Smith",
                    type = "Compuesto",
                    muscleGroup = "Cuádriceps y glúteo",
                    equipment = "Máquina",
                    technique = "Pirámide Invertida: Calienta bien. Serie 1 pesada (8 reps), series 2 y 3 con -10% de peso."
                ),
                RoutineExercisePreset(
                    order = 2,
                    name = "Prensa inclinada a 45°",
                    type = "Compuesto",
                    muscleGroup = "Cuádriceps y glúteo",
                    equipment = "Máquina",
                    technique = "Pirámide Invertida: Serie 1 a 10 reps, reduce 1 o 2 discos por lado para series 2 y 3."
                ),
                RoutineExercisePreset(
                    order = 3,
                    name = "Peso muerto rumano",
                    type = "Compuesto",
                    muscleGroup = "Isquiotibiales y glúteos",
                    equipment = "Barra",
                    technique = "Pirámide Creciente: Empieza con peso moderado y sube en la serie 2 para asegurar la postura lumbar."
                ),
                RoutineExercisePreset(
                    order = 4,
                    name = "Curl femoral tumbado",
                    type = "Aislamiento",
                    muscleGroup = "Isquiotibiales",
                    equipment = "Máquina",
                    technique = "Drop Set en la última serie: En la serie 3, quita 2 placas inmediatamente al fallar."
                ),
                RoutineExercisePreset(
                    order = 5,
                    name = "Elevaciones de gemelos",
                    type = "Aislamiento",
                    muscleGroup = "Pantorrillas",
                    equipment = "Máquina",
                    technique = "Series Lineales con pausa: 3 series con pausa de 2 segundos en el fondo del estiramiento."
                )
            )
        ),

        // DÍA 4 — Empuje B (Pecho, hombros y tríceps)
        RoutinePreset(
            dayNumber = 4,
            title = "Día 4 — Empuje B (Pecho, hombros y tríceps)",
            focusDescription = "Pecho superior, hombros y tríceps. Estímulo hipertrófico y congestión metabólica.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    name = "Press inclinado con mancuernas",
                    type = "Compuesto",
                    muscleGroup = "Pectoral superior",
                    equipment = "Mancuerna",
                    technique = "Pirámide Invertida: Serie 1 pesada a 8 reps; series 2 y 3 bajando 2 kg por mancuerna."
                ),
                RoutineExercisePreset(
                    order = 2,
                    name = "Press de pecho en máquina",
                    type = "Compuesto",
                    muscleGroup = "Pectoral medio",
                    equipment = "Máquina",
                    technique = "Series Lineales: 2 series de 10 reps pesadas y estables."
                ),
                RoutineExercisePreset(
                    order = 3,
                    name = "Press de hombros en máquina",
                    type = "Compuesto",
                    muscleGroup = "Deltoides",
                    equipment = "Máquina",
                    technique = "Series Lineales: 2 series de 10 reps controlando la bajada."
                ),
                RoutineExercisePreset(
                    order = 4,
                    name = "Fondos en máquina asistida",
                    type = "Compuesto",
                    muscleGroup = "Tríceps y pectoral bajo",
                    equipment = "Máquina",
                    technique = "Series Lineales: Ajusta contrapeso para 8–10 reps limpias."
                ),
                RoutineExercisePreset(
                    order = 5,
                    name = "Peck Deck en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Pectoral medio/interno",
                    equipment = "Máquina",
                    technique = "Drop Set en la última serie: En serie 2, reduce el pin y saca 4–6 reps adicionales."
                ),
                RoutineExercisePreset(
                    order = 6,
                    name = "Elevaciones laterales",
                    type = "Aislamiento",
                    muscleGroup = "Deltoides lateral",
                    equipment = "Mancuerna",
                    technique = "Series Lineales: 12–15 reps buscando quemazón local."
                ),
                RoutineExercisePreset(
                    order = 7,
                    name = "Extensión de tríceps en polea",
                    type = "Aislamiento",
                    muscleGroup = "Tríceps",
                    equipment = "Polea",
                    technique = "Drop Set en la última serie: Vacía el tríceps bajando placas sin parar."
                )
            )
        ),

        // DÍA 5 — Tirón B (Espalda y bíceps prioritario)
        RoutinePreset(
            dayNumber = 5,
            title = "Día 5 — Tirón B (Espalda y bíceps prioritario)",
            focusDescription = "Espalda media, dorsal bajo y aislamiento prioritario de bíceps.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    name = "Remo con apoyo en el pecho",
                    type = "Compuesto",
                    muscleGroup = "Espalda media",
                    equipment = "Máquina",
                    technique = "Pirámide Invertida: Serie 1 pesada a 8 reps; series 2 y 3 con peso menor a 10–12 reps."
                ),
                RoutineExercisePreset(
                    order = 2,
                    name = "Jalón al pecho agarre neutro",
                    type = "Compuesto",
                    muscleGroup = "Dorsal ancho (bajo)",
                    equipment = "Polea",
                    technique = "Series Lineales: 3 series de 8–10 reps apretando los dorsales abajo."
                ),
                RoutineExercisePreset(
                    order = 3,
                    name = "Peck Deck inverso (pájaros)",
                    type = "Aislamiento",
                    muscleGroup = "Deltoides posterior",
                    equipment = "Máquina",
                    technique = "Drop Set en la última serie: En serie 2, baja una placa y bombea repeticiones cortas."
                ),
                RoutineExercisePreset(
                    order = 4,
                    name = "Extensión lumbar en máquina",
                    type = "Compuesto accesorio",
                    muscleGroup = "Erectores lumbares",
                    equipment = "Máquina",
                    technique = "Series Lineales: Peso controlado (10–12 reps) sin arquear bruscamente."
                ),
                RoutineExercisePreset(
                    order = 5,
                    name = "Curl de bíceps en polea baja",
                    type = "Aislamiento",
                    muscleGroup = "Bíceps",
                    equipment = "Polea",
                    technique = "Drop Set en la última serie: En serie 3, reduce el pin para exprimir el bíceps al máximo."
                ),
                RoutineExercisePreset(
                    order = 6,
                    name = "Curl martillo con mancuernas",
                    type = "Aislamiento",
                    muscleGroup = "Braquial y antebrazo",
                    equipment = "Mancuerna",
                    technique = "Series Lineales: Movimiento alterno estricto a 10–12 reps."
                ),
                RoutineExercisePreset(
                    order = 7,
                    name = "Crunch abdominal en máquina",
                    type = "Aislamiento",
                    muscleGroup = "Recto abdominal",
                    equipment = "Máquina",
                    technique = "Series Lineales: 3 series de 12–15 reps con buena contracción."
                )
            )
        ),

        // DÍA 6 — Pierna B (Glúteos y complementos)
        RoutinePreset(
            dayNumber = 6,
            title = "Día 6 — Pierna B (Glúteos y complementos)",
            focusDescription = "Glúteo mayor/medio, isquiotibiales, aductores y gemelos.",
            exercises = listOf(
                RoutineExercisePreset(
                    order = 1,
                    name = "Prensa horizontal / sentada",
                    type = "Compuesto",
                    muscleGroup = "Cuádriceps y glúteos",
                    equipment = "Máquina",
                    technique = "Pirámide Invertida: Serie 1 pesada a 10 reps; series 2 y 3 bajando 1 placa."
                ),
                RoutineExercisePreset(
                    order = 2,
                    name = "Hip Thrust (máquina o barra)",
                    type = "Compuesto",
                    muscleGroup = "Glúteo mayor",
                    equipment = "Barra",
                    technique = "Pirámide Creciente: Serie 1 de tanteo técnico y Serie 2 con tu peso tope apretando arriba 2 seg."
                ),
                RoutineExercisePreset(
                    order = 3,
                    name = "Curl femoral sentado",
                    type = "Aislamiento",
                    muscleGroup = "Isquiotibiales",
                    equipment = "Máquina",
                    technique = "Drop Set en la última serie: En serie 3, baja carga y estira completo cada repetición."
                ),
                RoutineExercisePreset(
                    order = 4,
                    name = "Extensiones de cuádriceps",
                    type = "Aislamiento",
                    muscleGroup = "Cuádriceps",
                    equipment = "Máquina",
                    technique = "Drop Set en la última serie: En serie 2, reduce el pin un 30% para remate metabólico."
                ),
                RoutineExercisePreset(
                    order = 5,
                    name = "Máquina de abductores",
                    type = "Aislamiento",
                    muscleGroup = "Glúteo medio/menor",
                    equipment = "Máquina",
                    technique = "Drop Set en la última serie: En serie 2, saca 15 reps, baja el peso y saca 8 más."
                ),
                RoutineExercisePreset(
                    order = 6,
                    name = "Máquina de aductores",
                    type = "Aislamiento",
                    muscleGroup = "Aductores",
                    equipment = "Máquina",
                    technique = "Series Lineales: 12–15 reps con apertura controlada."
                ),
                RoutineExercisePreset(
                    order = 7,
                    name = "Elevaciones de gemelos",
                    type = "Aislamiento",
                    muscleGroup = "Pantorrillas",
                    equipment = "Máquina",
                    technique = "Series Lineales con pausa: 3 series de 15 reps con estiramiento profundo."
                )
            )
        )
    )

    /**
     * Busca la técnica recomendada para un ejercicio dado el título de la rutina (o workout) y el nombre del ejercicio.
     */
    fun findTechnique(routineTitle: String, exerciseName: String): String? {
        val preset = allPresets.firstOrNull { 
            routineTitle.contains(it.title, ignoreCase = true) || it.title.contains(routineTitle, ignoreCase = true)
        }
        if (preset != null) {
            val ex = preset.exercises.firstOrNull { 
                it.name.equals(exerciseName, ignoreCase = true) || exerciseName.contains(it.name, ignoreCase = true) || it.name.contains(exerciseName, ignoreCase = true)
            }
            if (ex != null) return ex.technique
        }

        // Búsqueda alternativa global en todos los presets si no coincide el título
        for (p in allPresets) {
            val ex = p.exercises.firstOrNull { it.name.equals(exerciseName, ignoreCase = true) }
            if (ex != null) return ex.technique
        }

        return null
    }

    /**
     * Genera un bloque de texto estructurado con todas las técnicas de la rutina para guardarlo en RoutineEntity.notes.
     */
    fun generateRoutineNotes(preset: RoutinePreset): String {
        val sb = StringBuilder()
        sb.append(preset.focusDescription).append("\n\n")
        sb.append("Técnicas recomendadas (Compuestos → Aislamiento):\n")
        preset.exercises.forEach { ex ->
            sb.append("${ex.order}. ${ex.name} [${ex.type} | ${ex.muscleGroup}]: ${ex.technique}\n")
        }
        return sb.toString().trim()
    }
}
