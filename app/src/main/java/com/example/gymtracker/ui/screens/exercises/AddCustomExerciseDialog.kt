package com.example.gymtracker.ui.screens.exercises

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.ui.components.media.ExerciseGifPlayer
import java.io.File

/**
 * Copia la imagen o GIF seleccionado de la galería al almacenamiento interno de la app
 * para garantizar persistencia permanente sin depender de permisos temporales de URI.
 */
private fun persistPickedMediaUri(context: Context, uri: Uri): String {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
    return try {
        val mimeType = context.contentResolver.getType(uri)?.lowercase() ?: ""
        val extension = when {
            mimeType.contains("gif") || uri.toString().lowercase().endsWith(".gif") -> "gif"
            mimeType.contains("webp") -> "webp"
            mimeType.contains("png") -> "png"
            else -> "jpg"
        }
        val mediaDir = File(context.filesDir, "custom_exercise_media").apply {
            if (!exists()) mkdirs()
        }
        val destFile = File(mediaDir, "exercise_${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (destFile.exists() && destFile.length() > 0L) {
            "file://${destFile.absolutePath}"
        } else {
            uri.toString()
        }
    } catch (_: Exception) {
        uri.toString()
    }
}

/**
 * Normaliza el músculo primario inicial de un ejercicio existente hacia una de las 13 etiquetas oficiales.
 */
private fun resolveInitialPrimaryMuscleLabel(exercise: ExerciseEntity?): String {
    if (exercise == null) return HypertrophyMuscle.CATALOG_PRIMARY_MUSCLES.first()
    val rawPrimary = exercise.primaryMuscle.ifBlank { exercise.muscleGroup }
    val matchedEnum = HypertrophyMuscle.fromExerciseInfo(
        primaryMuscle = exercise.primaryMuscle,
        muscleGroup = exercise.muscleGroup,
        exerciseName = exercise.name
    )
    if (matchedEnum != null) {
        return matchedEnum.catalogLabel
    }
    return HypertrophyMuscle.CATALOG_PRIMARY_MUSCLES.firstOrNull {
        rawPrimary.contains(it, ignoreCase = true)
    } ?: HypertrophyMuscle.CATALOG_PRIMARY_MUSCLES.first()
}

/**
 * Normaliza la lista inicial de músculos secundarios de un ejercicio existente.
 */
private fun resolveInitialSecondaryMuscles(exercise: ExerciseEntity?, primaryLabel: String): Set<String> {
    if (exercise == null || exercise.secondaryMuscles.isBlank()) return emptySet()
    val parsedEnums = HypertrophyMuscle.fromSecondaryMuscles(exercise.secondaryMuscles)
    val labels = parsedEnums.map { it.catalogLabel }.filter { it != primaryLabel }.toSet()
    if (labels.isNotEmpty()) return labels
    return exercise.secondaryMuscles
        .split(",")
        .map { it.trim() }
        .filter { token ->
            token.isNotBlank() &&
                !token.equals(primaryLabel, ignoreCase = true) &&
                HypertrophyMuscle.CATALOG_PRIMARY_MUSCLES.any { it.equals(token, ignoreCase = true) }
        }
        .toSet()
}

/**
 * Normaliza el equipamiento inicial de un ejercicio existente hacia las 7 opciones oficiales.
 */
private fun resolveInitialEquipmentLabel(exercise: ExerciseEntity?): String {
    if (exercise == null || exercise.equipment.isBlank()) {
        return HypertrophyMuscle.CATALOG_EQUIPMENTS.first()
    }
    val raw = exercise.equipment.trim()
    val lower = raw.lowercase()
    return when {
        lower.contains("ez") || lower.contains("barra z") -> "Barra EZ"
        lower.contains("prensa") || lower.contains("smith") || lower.contains("multipower") || lower.contains("hack") -> "Prensa / Smith"
        lower.contains("mancuerna") -> "Mancuernas"
        lower.contains("polea") || lower.contains("cable") -> "Polea"
        lower.contains("peso corporal") || lower.contains("calistenia") || lower.contains("dominada") || lower.contains("fondos") -> "Peso Corporal"
        lower.contains("barra") -> "Barra"
        lower.contains("máquina") || lower.contains("maquina") -> "Máquina"
        else -> HypertrophyMuscle.CATALOG_EQUIPMENTS.firstOrNull {
            it.equals(raw, ignoreCase = true)
        } ?: "Máquina"
    }
}

/**
 * Modal completo para CREAR un ejercicio nuevo o EDITAR cualquier ejercicio existente
 * (incluidos los 43 ejercicios por defecto) con control total sobre:
 * A) Nombre del Ejercicio (conservando historial y 1RM)
 * B) Imagen o GIF del Ejercicio (Galería del teléfono + URL externa)
 * C) Estímulo Muscular Primario (13 grupos conectados a la pestaña Estímulo), Secundarios (+0.5 series) y Equipamiento.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateOrEditExerciseDialog(
    initialExercise: ExerciseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        primaryMuscle: String,
        secondaryMuscles: String,
        equipment: String,
        mediaUrlOrPath: String,
        defaultRestSeconds: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditMode = initialExercise != null

    val availableMuscles = HypertrophyMuscle.CATALOG_PRIMARY_MUSCLES
    val availableEquipments = HypertrophyMuscle.CATALOG_EQUIPMENTS

    var name by remember(initialExercise) {
        mutableStateOf(initialExercise?.name ?: "")
    }
    var selectedPrimaryMuscle by remember(initialExercise) {
        mutableStateOf(resolveInitialPrimaryMuscleLabel(initialExercise))
    }
    var selectedSecondaryMuscles by remember(initialExercise) {
        mutableStateOf(resolveInitialSecondaryMuscles(initialExercise, resolveInitialPrimaryMuscleLabel(initialExercise)))
    }
    var selectedEquipment by remember(initialExercise) {
        mutableStateOf(resolveInitialEquipmentLabel(initialExercise))
    }
    var mediaSource by remember(initialExercise) {
        val currentMedia = initialExercise?.gifPathOrUrl?.ifBlank { initialExercise.thumbnailUrl } ?: ""
        mutableStateOf(currentMedia)
    }
    var restSecondsText by remember(initialExercise) {
        mutableStateOf((initialExercise?.defaultRestSeconds ?: 90).toString())
    }
    var isNameError by remember { mutableStateOf(false) }
    var galleryStatusText by remember { mutableStateOf<String?>(null) }

    // Selector oficial de Galería de Android (soporta Fotos, Capturas, PNG, JPG, WebP y GIFs animados sin permisos invasivos)
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = persistPickedMediaUri(context, uri)
            mediaSource = savedPath
            galleryStatusText = "✅ Imagen/GIF cargado desde tu galería"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0E131D),
            tonalElevation = 8.dp,
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        com.example.ui.theme.GymGreenLight.copy(alpha = 0.7f),
                        com.example.ui.theme.GymCyanSecondary.copy(alpha = 0.7f)
                    )
                )
            ),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 780.dp)
                .padding(vertical = 12.dp)
                .testTag("add_custom_exercise_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera del Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(com.example.ui.theme.GymGreenPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = com.example.ui.theme.GymGreenLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEditMode) "Editar Ejercicio" else "Nuevo Ejercicio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (isEditMode) {
                                    "Actualiza nombre, foto/GIF y estímulo muscular sin perder tu historial ni 1RM."
                                } else {
                                    "Configura nombre, imagen/GIF de galería y estímulo muscular."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.GymTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_exercise_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = com.example.ui.theme.GymTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = com.example.ui.theme.GymGlassBorder)

                // A) NOMBRE DEL EJERCICIO
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "A) NOMBRE DEL EJERCICIO",
                        style = com.example.ui.theme.TechnicalLabelStyle,
                        color = com.example.ui.theme.GymGreenLight
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) isNameError = false
                        },
                        label = { Text("Nombre del ejercicio") },
                        placeholder = { Text("Ej. Press Francés con Mancuernas") },
                        isError = isNameError,
                        supportingText = {
                            if (isNameError) {
                                Text("El nombre del ejercicio no puede estar vacío.")
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_exercise_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // B) IMAGEN O GIF DEL EJERCICIO (GALERÍA DEL TELÉFONO + URL)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141A26))
                        .border(1.dp, com.example.ui.theme.GymGlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "B) IMAGEN O GIF DEL EJERCICIO (GALERÍA + URL)",
                        style = com.example.ui.theme.TechnicalLabelStyle,
                        color = com.example.ui.theme.GymCyanSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Vista previa cuadrada/redondeada grande de la imagen o GIF actual
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    1.5.dp,
                                    com.example.ui.theme.GymCyanSecondary.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                )
                                .testTag("exercise_dialog_media_preview")
                        ) {
                            ExerciseGifPlayer(
                                gifSource = mediaSource,
                                exerciseName = name.ifBlank { initialExercise?.name ?: "Vista Previa" },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    pickVisualMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.ui.theme.GymCyanSecondary,
                                    contentColor = Color(0xFF04131A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pick_gallery_media_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "📁 Elegir de mi Galería",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Text(
                                text = galleryStatusText
                                    ?: "Sube cualquier foto, captura o GIF guardado en tu teléfono.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (galleryStatusText != null) com.example.ui.theme.GymGreenLight else com.example.ui.theme.GymTextSecondary
                            )
                        }
                    }

                    // Campo opcional para pegar URL directa de imagen o GIF
                    OutlinedTextField(
                        value = mediaSource,
                        onValueChange = {
                            mediaSource = it
                            galleryStatusText = null
                        },
                        label = { Text("🔗 O pegar URL de imagen/GIF") },
                        placeholder = { Text("https://ejemplo.com/ejercicio.gif") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = com.example.ui.theme.GymCyanSecondary
                            )
                        },
                        trailingIcon = {
                            if (mediaSource.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        mediaSource = ""
                                        galleryStatusText = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar imagen/URL"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_media_url_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // C) ESTÍMULO MUSCULAR Y EQUIPAMIENTO (CONECTADO A LA PESTAÑA "ESTÍMULO")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "C) MÚSCULO PRIMARIO (ESTÍMULO PRINCIPAL • 1.0 SERIE)",
                        style = com.example.ui.theme.TechnicalLabelStyle,
                        color = com.example.ui.theme.GymGreenLight
                    )
                    Text(
                        text = "Conectado en tiempo real a la pestaña 'Estímulo'. Al cambiarlo, todas las series de este ejercicio se contabilizan en este grupo muscular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.GymTextSecondary
                    )

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("primary_muscle_chips_container"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableMuscles.forEach { muscle ->
                            val isSelected = selectedPrimaryMuscle.equals(muscle, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPrimaryMuscle = muscle
                                    selectedSecondaryMuscles = selectedSecondaryMuscles - muscle
                                },
                                label = {
                                    Text(
                                        text = muscle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("primary_muscle_chip_$muscle")
                            )
                        }
                    }
                }

                // Selector opcional de Músculos Secundarios (+0.5 series)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "MÚSCULOS SECUNDARIOS (OPCIONAL • APORTAN +0.5 SERIES)",
                        style = com.example.ui.theme.TechnicalLabelStyle,
                        color = com.example.ui.theme.GymTextSecondary
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("secondary_muscle_chips_container"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableMuscles
                            .filterNot { it.equals(selectedPrimaryMuscle, ignoreCase = true) }
                            .forEach { secMuscle ->
                                val isSelected = selectedSecondaryMuscles.contains(secMuscle)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSecondaryMuscles = if (isSelected) {
                                            selectedSecondaryMuscles - secMuscle
                                        } else {
                                            selectedSecondaryMuscles + secMuscle
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isSelected) "✓ $secMuscle" else "+ $secMuscle",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = com.example.ui.theme.GymCyanSecondary.copy(alpha = 0.25f),
                                        selectedLabelColor = com.example.ui.theme.GymCyanSecondary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("secondary_muscle_chip_$secMuscle")
                                )
                            }
                    }
                }

                // Selector de Equipamiento
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "EQUIPAMIENTO",
                        style = com.example.ui.theme.TechnicalLabelStyle,
                        color = com.example.ui.theme.GymTextSecondary
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("equipment_chips_container"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableEquipments.forEach { equip ->
                            val isSelected = selectedEquipment.equals(equip, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEquipment = equip },
                                label = {
                                    Text(
                                        text = equip,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("equipment_chip_$equip")
                            )
                        }
                    }
                }

                // Tiempo de descanso predeterminado
                OutlinedTextField(
                    value = restSecondsText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*$"))) {
                            restSecondsText = input
                        }
                    },
                    label = { Text("Descanso predeterminado (segundos)") },
                    placeholder = { Text("90") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = com.example.ui.theme.GymTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                isNameError = true
                            } else {
                                val restSec = restSecondsText.toIntOrNull() ?: 90
                                val secondaryFormatted = selectedSecondaryMuscles.joinToString(", ")
                                onSave(
                                    name.trim(),
                                    selectedPrimaryMuscle,
                                    secondaryFormatted,
                                    selectedEquipment,
                                    mediaSource.trim(),
                                    restSec
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_custom_exercise_button")
                    ) {
                        Text(
                            text = if (isEditMode) "Guardar Cambios" else "Guardar Ejercicio",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wrapper de compatibilidad con llamadas anteriores a AddCustomExerciseDialog.
 */
@Composable
fun AddCustomExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, muscleGroup: String, equipment: String, defaultRestSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CreateOrEditExerciseDialog(
        initialExercise = null,
        onDismiss = onDismiss,
        onSave = { name, primaryMuscle, _, equipment, _, restSec ->
            onSave(name, primaryMuscle, equipment, restSec)
        },
        modifier = modifier
    )
}
