package com.matteo.cozyplans.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class TaskFilter {
    ALL,
    TODO,
    DONE
}

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onUpdateTask: (index: Int, updatedTitle: String, updatedDescription: String, updatedPhotoUri: String?, updatedDueAtMillis: Long, updatedRecurrence: TaskRecurrence, updatedRecurrenceInterval: Int, updatedPriority: TaskPriority) -> Unit,
    onToggleTaskDone: (index: Int) -> Unit,
    onDeleteTask: (index: Int) -> Unit,
    onPurgeCompleted: () -> Unit
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingValue by remember { mutableStateOf("") }
    var editingDescription by remember { mutableStateOf("") }
    var editingPhotoUri by remember { mutableStateOf<String?>(null) }
    var editingDueAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var editingRecurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var editingRecurrenceInterval by remember { mutableIntStateOf(1) }
    var editingPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var compactMode by remember { mutableStateOf(true) }
    val expandedTasks = remember { mutableStateMapOf<Int, Boolean>() }
    var pendingDeleteIndex by remember { mutableStateOf<Int?>(null) }

    var meteorTrigger by remember { mutableIntStateOf(0) }
    var showMeteor by remember { mutableStateOf(false) }
    val meteorProgress = remember { Animatable(0f) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Providers may refuse persistable grants.
            }
        }
        editingPhotoUri = uri?.toString()
    }

    val filteredTasks = tasks
        .mapIndexedNotNull { index, task ->
            val include = when (selectedFilter) {
                TaskFilter.ALL -> true
                TaskFilter.TODO -> !task.isDone
                TaskFilter.DONE -> task.isDone
            }
            if (include) index to task else null
        }
        .sortedWith(
            compareBy<Pair<Int, Task>>(
                { pair ->
                    when (pair.second.priority) {
                        TaskPriority.HIGH -> 0
                        TaskPriority.MEDIUM -> 1
                        TaskPriority.LOW -> 2
                    }
                },
                { it.second.dueAtMillis }
            )
        )

    val completedCount = tasks.count { it.isDone }
    val nowMillis = System.currentTimeMillis()
    val overdueCount = tasks.count { !it.isDone && it.dueAtMillis < nowMillis }

    LaunchedEffect(meteorTrigger) {
        if (meteorTrigger > 0) {
            showMeteor = true
            meteorProgress.snapTo(0f)
            meteorProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1700, easing = LinearEasing)
            )
            showMeteor = false
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Lister toutes les taches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OverdueAlertCard(overdueCount = overdueCount)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == TaskFilter.ALL,
                    onClick = { selectedFilter = TaskFilter.ALL },
                    label = { Text("Toutes") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FilterChip(
                    selected = selectedFilter == TaskFilter.TODO,
                    onClick = { selectedFilter = TaskFilter.TODO },
                    label = { Text("A faire") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FilterChip(
                    selected = selectedFilter == TaskFilter.DONE,
                    onClick = { selectedFilter = TaskFilter.DONE },
                    label = { Text("Realisees") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FilterChip(
                    selected = compactMode,
                    onClick = { compactMode = !compactMode },
                    label = { Text(if (compactMode) "Compact" else "Detaille") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            if (completedCount > 0) {
                OutlinedButton(onClick = onPurgeCompleted) {
                    Text("Effacer les taches effectuees ($completedCount)")
                }
            }

            if (filteredTasks.isEmpty()) {
                Text(
                    if (tasks.isEmpty()) "Aucune tache pour le moment."
                    else "Aucune tache pour ce filtre."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    itemsIndexed(filteredTasks) { _, item ->
                        val index = item.first
                        val task = item.second
                        val isExpanded = expandedTasks[index] ?: false

                        TaskItemCard(
                            task = task,
                            index = index,
                            compactMode = compactMode,
                            isExpanded = isExpanded,
                            nowMillis = nowMillis,
                            formatter = formatter,
                            zoneId = zoneId,
                            isEditing = editingIndex == index,
                            editingTitle = editingValue,
                            editingDescription = editingDescription,
                            editingPhotoUri = editingPhotoUri,
                            editingDueAtMillis = editingDueAtMillis,
                            editingRecurrence = editingRecurrence,
                            editingRecurrenceInterval = editingRecurrenceInterval,
                            editingPriority = editingPriority,
                            onEditingTitleChange = { editingValue = it },
                            onEditingDescriptionChange = { editingDescription = it },
                            onEditingRecurrenceChange = { editingRecurrence = it },
                            onEditingRecurrenceIntervalChange = { editingRecurrenceInterval = it },
                            onEditingPriorityChange = { editingPriority = it },
                            onEditingDueAtMillisChange = { editingDueAtMillis = it },
                            onPickEditingPhoto = { photoPickerLauncher.launch(arrayOf("image/*")) },
                            onClearEditingPhoto = { editingPhotoUri = null },
                            onToggleExpand = { expandedTasks[index] = !isExpanded },
                            onStartEdit = {
                                editingIndex = index
                                editingValue = task.title
                                editingDescription = task.description
                                editingPhotoUri = task.photoUri
                                editingDueAtMillis = task.dueAtMillis
                                editingRecurrence = task.recurrence
                                editingRecurrenceInterval = task.recurrenceInterval
                                editingPriority = task.priority
                            },
                            onCancelEdit = {
                                editingIndex = null
                                editingValue = ""
                                editingDescription = ""
                                editingPhotoUri = null
                            },
                            onSaveEdit = {
                                val updatedTitle = editingValue.trim()
                                if (updatedTitle.isNotEmpty()) {
                                    onUpdateTask(
                                        index,
                                        updatedTitle,
                                        editingDescription.trim(),
                                        editingPhotoUri,
                                        editingDueAtMillis,
                                        editingRecurrence,
                                        editingRecurrenceInterval,
                                        editingPriority
                                    )
                                    editingIndex = null
                                    editingValue = ""
                                    editingDescription = ""
                                    editingPhotoUri = null
                                }
                            },
                            onToggleDone = {
                                val wasDone = task.isDone
                                onToggleTaskDone(index)
                                if (!wasDone) meteorTrigger += 1
                            },
                            onRequestDelete = { pendingDeleteIndex = index }
                        )
                    }
                }
            }
        }

        MeteorCelebrationOverlay(
            show = showMeteor,
            progress = meteorProgress.value
        )

        DeleteTaskDialog(
            show = pendingDeleteIndex != null,
            onDismiss = { pendingDeleteIndex = null },
            onConfirm = {
                pendingDeleteIndex?.let(onDeleteTask)
                pendingDeleteIndex = null
            }
        )
    }
}
