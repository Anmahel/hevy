package com.example.gymtracker.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gymtracker.domain.util.BackupJsonManager
import com.example.gymtracker.domain.util.LocalStorageStats
import java.io.File

/**
 * MÓDULO 5 DE EXPANSIÓN: Panel de Gestión y Seguridad de Datos (Backup & Restore Local .JSON).
 * Incluye:
 * 1. Estadísticas de Almacenamiento Local (Rutinas, Sesiones guardadas, Ejercicios, Registros biométricos).
 * 2. Exportación completa en un clic (.JSON) a archivo `gymtracker_backup_fecha.json` y portapapeles.
 * 3. Importación y Restauración Segura desde archivo `.json` o texto pegado con validación previa.
 */
@Composable
fun BackupRestoreDialog(
    storageStats: LocalStorageStats,
    onGenerateBackupJson: () -> String,
    onRestoreFromJson: (rawJson: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var jsonInputText by remember { mutableStateOf("") }
    var exportedPreviewJson by remember { mutableStateOf<String?>(null) }
    var lastSuggestedFileName by remember {
        mutableStateOf(BackupJsonManager.buildDefaultBackupFileName())
    }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    // Launcher SAF para guardar el archivo `gymtracker_backup_fecha.json` en el dispositivo
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val jsonContent = exportedPreviewJson ?: onGenerateBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonContent.toByteArray(Charsets.UTF_8))
                }
                isStatusError = false
                statusMessage = "✅ Archivo '$lastSuggestedFileName' guardado correctamente en tu dispositivo."
            } catch (e: Exception) {
                isStatusError = true
                statusMessage = "Error al escribir archivo: ${e.localizedMessage ?: "desconocido"}"
            }
        }
    }

    // Launcher SAF para abrir e importar un archivo `.json` previamente guardado
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                } ?: ""
                if (content.isNotBlank()) {
                    jsonInputText = content
                    isStatusError = false
                    statusMessage = "📂 Archivo .json cargado (${content.length} bytes). Pulsa 'Importar / Restaurar Backup' para validar y restaurar."
                } else {
                    isStatusError = true
                    statusMessage = "⚠️ El archivo seleccionado está vacío."
                }
            } catch (e: Exception) {
                isStatusError = true
                statusMessage = "Error al leer el archivo: ${e.localizedMessage ?: "desconocido"}"
            }
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
                        Color(0xFF00E676).copy(alpha = 0.7f),
                        Color(0xFF00E5FF).copy(alpha = 0.7f)
                    )
                )
            ),
            modifier = modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp)
                .testTag("backup_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cabecera del Panel de Gestión y Seguridad de Datos
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Datos y Copia de Seguridad",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Backup & Restore local (.JSON) 100% íntegro",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_backup_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }

                // 4. ESTADÍSTICAS DE ALMACENAMIENTO LOCAL
                Surface(
                    color = Color(0xFF151C28),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF242F44)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("storage_stats_section")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ESTADÍSTICAS DE ALMACENAMIENTO LOCAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E5FF)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StorageStatItemCard(
                                label = "Rutinas",
                                value = "${storageStats.routinesCount}",
                                subLabel = "configuradas",
                                icon = Icons.Default.ViewAgenda,
                                accentColor = Color(0xFF00E676),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stat_routines_count")
                            )
                            StorageStatItemCard(
                                label = "Sesiones",
                                value = "${storageStats.savedSessionsCount}",
                                subLabel = "${storageStats.totalSetsCount} series",
                                icon = Icons.Default.History,
                                accentColor = Color(0xFF00E5FF),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stat_sessions_count")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StorageStatItemCard(
                                label = "Ejercicios",
                                value = "${storageStats.exercisesCount}",
                                subLabel = "${storageStats.customExercisesCount} propios",
                                icon = Icons.Default.FitnessCenter,
                                accentColor = Color(0xFFB388FF),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stat_exercises_count")
                            )
                            StorageStatItemCard(
                                label = "Biométricos",
                                value = "${storageStats.totalBiometricRecordsCount}",
                                subLabel = "${storageStats.biometricWeightLogsCount} peso • ${storageStats.nutritionDaysCount} nutr.",
                                icon = Icons.Default.MonitorWeight,
                                accentColor = Color(0xFFFFD700),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stat_biometrics_count")
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1C2536))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥 Racha actual: ${storageStats.currentStreakDays}d (Mejor: ${storageStats.bestStreakDays}d)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE5E7EB),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🏆 ${storageStats.totalPersonalRecords} Récords 1RM",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Banner de retroalimentación de exportación / validación / restauración
                AnimatedVisibility(visible = statusMessage != null) {
                    statusMessage?.let { msg ->
                        Surface(
                            color = if (isStatusError) {
                                Color(0xFFEF4444).copy(alpha = 0.16f)
                            } else {
                                Color(0xFF00E676).copy(alpha = 0.16f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isStatusError) Color(0xFFEF4444) else Color(0xFF00E676)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_status_message_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isStatusError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isStatusError) Color(0xFFEF4444) else Color(0xFF00E676),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 2. EXPORTACIÓN COMPLETA EN UN CLIC (.JSON)
                Surface(
                    color = Color(0xFF151C28),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "EXPORTACIÓN COMPLETA EN 1 CLIC (.JSON)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "Empaqueta tus rutinas personalizadas, ejercicios creados, historial de entrenamientos, récords, pesos, medidas, comidas rápidas y racha en '$lastSuggestedFileName'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )

                        Button(
                            onClick = {
                                val json = onGenerateBackupJson()
                                val fileName = BackupJsonManager.buildDefaultBackupFileName()
                                exportedPreviewJson = json
                                lastSuggestedFileName = fileName

                                // Guardar copia local inmediata en el directorio de backups de la app + copiar al portapapeles por seguridad
                                try {
                                    val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
                                    val localFile = File(backupDir, fileName)
                                    localFile.writeText(json, Charsets.UTF_8)
                                    copyToClipboard(context, json)
                                    isStatusError = false
                                    statusMessage = "✅ Backup '$fileName' generado, guardado localmente y copiado al portapapeles."
                                } catch (e: Exception) {
                                    copyToClipboard(context, json)
                                    isStatusError = false
                                    statusMessage = "✅ Backup '$fileName' generado y copiado al portapapeles."
                                }

                                // Lanzar selector del sistema para guardar archivo donde el usuario elija
                                try {
                                    createDocumentLauncher.launch(fileName)
                                } catch (_: Exception) {
                                    // Si el entorno no tiene gestor de archivos externo, ya quedó guardado y copiado
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("export_backup_json_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exportar Copia de Seguridad (.json)",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val json = onGenerateBackupJson()
                                val fileName = BackupJsonManager.buildDefaultBackupFileName()
                                exportedPreviewJson = json
                                lastSuggestedFileName = fileName
                                copyToClipboard(context, json)
                                isStatusError = false
                                statusMessage = "✅ JSON de '$fileName' copiado al portapapeles (${json.length} caracteres)."
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("copy_backup_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copiar JSON al Portapapeles",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Vista previa compacta si ya se exportó
                        exportedPreviewJson?.let { preview ->
                            Surface(
                                color = Color(0xFF0B0E17),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF263147)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Archivo: $lastSuggestedFileName",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E676)
                                        )
                                        TextButton(
                                            onClick = { jsonInputText = preview },
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = "Usar en restaurador ↓",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF00E5FF)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preview.take(360) + if (preview.length > 360) "\n..." else "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        color = Color(0xFFD1D5DB),
                                        maxLines = 6,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. IMPORTACIÓN Y RESTAURACIÓN SEGURA (.JSON)
                Surface(
                    color = Color(0xFF151C28),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "IMPORTACIÓN Y RESTAURACIÓN SEGURA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "Selecciona un archivo .json guardado o pega su contenido. El sistema validará la estructura antes de restaurar tus datos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                    } catch (e: Exception) {
                                        isStatusError = true
                                        statusMessage = "No se pudo abrir el selector de archivos: ${e.localizedMessage}"
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_backup_file_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Archivo .json",
                                    color = Color(0xFF00E5FF),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val pasted = pasteFromClipboard(context)
                                    if (!pasted.isNullOrBlank()) {
                                        jsonInputText = pasted
                                        isStatusError = false
                                        statusMessage = "📋 Contenido JSON pegado desde el portapapeles."
                                    } else {
                                        isStatusError = true
                                        statusMessage = "⚠️ El portapapeles está vacío."
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF9CA3AF).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("paste_backup_clipboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pegar JSON",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = jsonInputText,
                            onValueChange = { jsonInputText = it },
                            placeholder = {
                                Text(
                                    text = "Pega aquí el contenido de tu archivo gymtracker_backup_fecha.json...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF283246),
                                focusedContainerColor = Color(0xFF0B0E17),
                                unfocusedContainerColor = Color(0xFF0B0E17)
                            ),
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_backup_json_input")
                        )

                        Button(
                            onClick = {
                                onRestoreFromJson(jsonInputText) { success, message ->
                                    isStatusError = !success
                                    statusMessage = if (success) "✅ $message" else "⚠️ $message"
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("restore_backup_json_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Importar / Restaurar Backup",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageStatItemCard(
    label: String,
    value: String,
    subLabel: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2434)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BackupAccessBannerCard(
    storageStats: LocalStorageStats,
    onOpenBackupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141B27)),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.55f),
                    Color(0xFF00E676).copy(alpha = 0.55f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("backup_access_banner_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Datos y Backup Local (.JSON)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${storageStats.routinesCount} rutinas • ${storageStats.savedSessionsCount} sesiones • ${storageStats.exercisesCount} ejerc. • ${storageStats.totalBiometricRecordsCount} biom.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onOpenBackupClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.7f)),
                modifier = Modifier.testTag("open_backup_from_banner_button")
            ) {
                Text(
                    text = "Gestionar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("gymtracker_backup_json", text)
        clipboard?.setPrimaryClip(clip)
    } catch (_: Exception) {
    }
}

private fun pasteFromClipboard(context: Context): String? {
    return try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val item = clipboard?.primaryClip?.getItemAt(0)
        item?.text?.toString()
    } catch (_: Exception) {
        null
    }
}
