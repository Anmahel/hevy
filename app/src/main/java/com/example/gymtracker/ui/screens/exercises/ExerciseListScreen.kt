package com.example.gymtracker.ui.screens.exercises

import com.example.ui.theme.safeHeaderTopPadding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.util.StrengthRankInfo
import com.example.gymtracker.ui.components.StrengthRankBadgeChip
import com.example.gymtracker.ui.components.media.ExerciseThumbnail

import com.example.gymtracker.ui.components.GlobalTopHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    viewModel: ExerciseListViewModel,
    onExerciseClick: (Long) -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    athleteLevel: Int = 1,
    onOpenRpgProfile: () -> Unit = {},
    waterGlassesToday: Int = 0,
    isWaterGoalMetToday: Boolean = false,
    creatineTakenToday: Boolean = false,
    onQuickAddWaterGlass: () -> Unit = {},
    onQuickTakeCreatine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val muscleGroupFilters = HypertrophyMuscle.CATALOG_PRIMARY_MUSCLES

    Scaffold(
        topBar = {
            GlobalTopHeader(
                title = "Ejercicios",
                waterGlassesToday = waterGlassesToday,
                isWaterGoalMetToday = isWaterGoalMetToday,
                creatineTakenToday = creatineTakenToday,
                athleteLevel = athleteLevel,
                onQuickAddWaterGlass = onQuickAddWaterGlass,
                onQuickTakeCreatine = onQuickTakeCreatine,
                onOpenRpgProfile = onOpenRpgProfile,
                onNavigateToBackup = onNavigateToBackup
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("add_custom_exercise_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nuevo ejercicio"
                )
            }
        },
        modifier = modifier.testTag("exercise_list_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Barra de Búsqueda de Material 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onSearch = { viewModel.onSearchBarActiveChanged(false) },
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text("Buscar por nombre, músculo o equipo...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar búsqueda"
                                        )
                                    }
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_search_bar")
                ) {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Fila deslizable horizontal con filtros de grupo muscular (FilterChip)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("muscle_group_filter_row")
            ) {
                item {
                    val isAllSelected = uiState.selectedMuscleGroup == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { viewModel.onMuscleGroupSelected(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                items(muscleGroupFilters) { group ->
                    val isSelected = uiState.selectedMuscleGroup.equals(group, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onMuscleGroupSelected(group) },
                        label = { Text(group) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. LazyColumn con la lista de ejercicios
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No se encontraron ejercicios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty())
                                    "No hay resultados para '${uiState.searchQuery}'."
                                else "Usa el botón '+' para crear tu propio ejercicio personalizado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 112.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.filteredExercises.size} ejercicios disponibles",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(uiState.filteredExercises, key = { it.id }) { exercise ->
                        ExerciseCatalogItem(
                            exercise = exercise,
                            strengthRankInfo = uiState.strengthRanksByExerciseId[exercise.id],
                            onClick = { onExerciseClick(exercise.id) },
                            onEditClick = { viewModel.openEditDialog(exercise) }
                        )
                    }
                }
            }
        }
    }

    // Modal para crear ejercicio nuevo o editar cualquier ejercicio existente
    if (uiState.isAddDialogOpen) {
        CreateOrEditExerciseDialog(
            initialExercise = uiState.editingExercise,
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { name, primaryMuscle, secondaryMuscles, equip, mediaUrl, rest ->
                viewModel.saveOrUpdateExercise(
                    name = name,
                    primaryMuscle = primaryMuscle,
                    secondaryMuscles = secondaryMuscles,
                    equipment = equip,
                    mediaUrlOrPath = mediaUrl,
                    defaultRestSeconds = rest
                )
            }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ExerciseCatalogItem(
    exercise: ExerciseEntity,
    strengthRankInfo: StrengthRankInfo? = null,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryMuscleDisplay = exercise.primaryMuscle.ifBlank { exercise.muscleGroup }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = modifier
            .fillMaxWidth()
            .testTag("exercise_item_${exercise.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseThumbnail(
                    imageSource = exercise.thumbnailUrl.ifEmpty { exercise.gifPathOrUrl },
                    size = 46.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = primaryMuscleDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = exercise.equipment,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        if (exercise.isCustom) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Personalizado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Botón de Editar (✏️ Icons.Default.Edit) al lado del icono circular de mancuerna
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                            .testTag("edit_exercise_button_${exercise.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar ejercicio ${exercise.name}",
                            tint = com.example.ui.theme.GymCyanSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Insignia Competitiva de Rango de Fuerza (Bronce, Plata, Oro, Platino, Diamante, Titán)
            strengthRankInfo?.let { rankInfo ->
                StrengthRankBadgeChip(rankInfo = rankInfo)
            }
        }
    }
}
