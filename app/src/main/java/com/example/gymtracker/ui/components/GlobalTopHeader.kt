package com.example.gymtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymError
import com.example.ui.theme.GymGlassBorder
import com.example.ui.theme.GymGreenPrimary
import com.example.ui.theme.GymStickyHeaderBg
import com.example.ui.theme.safeHeaderTopPadding

/**
 * Barra superior global fija para todas las pestañas principales ("Mis Rutinas", "Historial",
 * "Estímulo", "Nutrición" y "Ejercicios").
 *
 * Mantiene exactamente la misma posición, tamaño y alineación a la derecha de los 4 iconos de
 * estado rápido (💧/💦 Agua, 🧪 Creatina, 🏆 Nv.X Perfil RPG y 🛡️ Backup) al cambiar de pestaña,
 * con el título compacto de la pestaña actual a la izquierda.
 */
@Composable
fun GlobalTopHeader(
    title: String,
    waterGlassesToday: Int = 0,
    isWaterGoalMetToday: Boolean = false,
    creatineTakenToday: Boolean = false,
    athleteLevel: Int = 1,
    onQuickAddWaterGlass: () -> Unit = {},
    onQuickTakeCreatine: () -> Unit = {},
    onOpenRpgProfile: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GymStickyHeaderBg)
            .safeHeaderTopPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Título compacto a la izquierda (nunca empuja ni mueve a los iconos de la derecha)
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            // Iconos fijos alineados a la derecha en el mismo lugar en todas las pestañas
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1) Botón rápido de Gota de Agua (💧 -> 💦 al cumplir meta diaria, +750 ml 1 Shaker)
                val waterAccent = if (isWaterGoalMetToday) {
                    GymCyanSecondary
                } else {
                    Color(0xFF0EA5E9)
                }
                Surface(
                    onClick = onQuickAddWaterGlass,
                    color = waterAccent.copy(alpha = if (isWaterGoalMetToday) 0.22f else 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        waterAccent.copy(alpha = if (isWaterGoalMetToday) 0.75f else 0.45f)
                    ),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("main_header_water_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (isWaterGoalMetToday) "💦" else "💧",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "$waterGlassesToday",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.Black,
                            color = waterAccent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // 2) Botón rápido de Creatina 5g (Rojo pendiente -> Verde tomada, bloqueado por el resto del día)
                val creatineAccent = if (creatineTakenToday) {
                    GymGreenPrimary
                } else {
                    GymError
                }
                Surface(
                    onClick = {
                        if (!creatineTakenToday) {
                            onQuickTakeCreatine()
                        }
                    },
                    enabled = !creatineTakenToday,
                    color = creatineAccent.copy(alpha = if (creatineTakenToday) 0.20f else 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        creatineAccent.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("main_header_creatine_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = if (creatineTakenToday) "Creatina tomada hoy" else "Creatina pendiente hoy",
                            tint = creatineAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (creatineTakenToday) "✓" else "5g",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.Black,
                            color = creatineAccent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // 3) Botón de Trofeo + Nivel actual para abrir el Perfil RPG de Atleta
                Surface(
                    onClick = onOpenRpgProfile,
                    color = Color(0xFFFFD700).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFFFFD700).copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .testTag("main_header_rpg_profile_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Perfil RPG de Atleta",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Nv.$athleteLevel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // 4) Icono de Escudo para abrir Datos y Backup (.JSON)
                IconButton(
                    onClick = onNavigateToBackup,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("main_header_backup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Datos y Backup",
                        tint = GymCyanSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = GymGlassBorder
        )
    }
}
