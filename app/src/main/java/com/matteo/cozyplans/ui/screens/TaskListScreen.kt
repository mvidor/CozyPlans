package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import coil.compose.AsyncImage

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
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

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
                // Ignore devices/providers that do not support persistable permissions.
            }
        }
        editingPhotoUri = uri?.toString()
    }

    val filteredTasks = tasks.mapIndexedNotNull { index, task ->
        val include = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.TODO -> !task.isDone
            TaskFilter.DONE -> task.isDone
        }
        if (include) index to task else null
    }.sortedWith(
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Lister toutes les taches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (overdueCount > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "ALERTE: $overdueCount tache(s) en retard",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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
                val emptyMessage = if (tasks.isEmpty()) {
                    "Aucune tache pour le moment."
                } else {
                    "Aucune tache pour ce filtre."
                }
                Text(emptyMessage)
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
                        val isOverdue = !task.isDone && task.dueAtMillis < nowMillis
                        val taskCardColor = if (task.isDone) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.surface
                        val taskTextColor = if (task.isDone) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        val taskDueDateTime = Instant.ofEpochMilli(task.dueAtMillis).atZone(zoneId).toLocalDateTime()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = taskCardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (task.photoUri != null && editingIndex != index) {
                                    AsyncImage(
                                        model = task.photoUri,
                                        contentDescription = "Photo de la tache",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(96.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(96.dp)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0x55000000),
                                                        Color(0x22000000),
                                                        Color(0x00000000)
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${index + 1}. ${task.title}",
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                                        color = taskTextColor
                                    )
                                    if (compactMode && editingIndex != index) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AssistChip(
                                                onClick = {},
                                                enabled = false,
                                                label = { Text(if (task.isDone) "Faite" else "A faire") },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    disabledContainerColor = if (task.isDone) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                                    disabledLabelColor = if (task.isDone) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            )
                                            TextButton(onClick = { expandedTasks[index] = !isExpanded }) {
                                                Text(if (isExpanded) "Masquer" else "Details")
                                            }
                                        }
                                    } else {
                                        AssistChip(
                                            onClick = {},
                                            enabled = false,
                                            label = { Text(if (task.isDone) "Faite" else "A faire") },
                                            colors = AssistChipDefaults.assistChipColors(
                                                disabledContainerColor = if (task.isDone) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                                disabledLabelColor = if (task.isDone) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = when (task.priority) {
                                        TaskPriority.HIGH -> "Priorite: Haute"
                                        TaskPriority.MEDIUM -> "Priorite: Moyenne"
                                        TaskPriority.LOW -> "Priorite: Basse"
                                    },
                                    color = when (task.priority) {
                                        TaskPriority.HIGH -> Color(0xFFD81B60)
                                        TaskPriority.MEDIUM -> taskTextColor
                                        TaskPriority.LOW -> Color(0xFF43A047)
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (isOverdue) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "En retard",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (task.description.isNotBlank()) {
                                    Text(
                                        text = task.description,
                                        maxLines = if (compactMode && !isExpanded) 1 else 3,
                                        overflow = TextOverflow.Ellipsis,
                                        color = taskTextColor.copy(alpha = 0.92f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (editingIndex == index) {
                                    val editingDateTime = Instant.ofEpochMilli(editingDueAtMillis).atZone(zoneId).toLocalDateTime()

                                    OutlinedTextField(
                                        value = editingValue,
                                        onValueChange = { editingValue = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text("Modifier la tache") }
                                    )
                                    OutlinedTextField(
                                        value = editingDescription,
                                        onValueChange = { editingDescription = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Description") },
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AssistChip(
                                            onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                                            label = { Text(if (editingPhotoUri == null) "Joindre photo" else "Changer photo") }
                                        )
                                        if (editingPhotoUri != null) {
                                            AssistChip(
                                                onClick = { editingPhotoUri = null },
                                                label = { Text("Retirer") }
                                            )
                                        }
                                    }
                                    if (editingPhotoUri != null) {
                                        AsyncImage(
                                            model = editingPhotoUri,
                                            contentDescription = "Photo de la tache",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Text(
                                        text = "Echeance: ${editingDateTime.format(formatter)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilledTonalButton(onClick = {
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    val updated = LocalDateTime.of(
                                                        LocalDate.of(year, month + 1, dayOfMonth),
                                                        editingDateTime.toLocalTime()
                                                    )
                                                    editingDueAtMillis = updated.atZone(zoneId).toInstant().toEpochMilli()
                                                },
                                                editingDateTime.year,
                                                editingDateTime.monthValue - 1,
                                                editingDateTime.dayOfMonth
                                            ).show()
                                        }) {
                                            Text("Date")
                                        }

                                        FilledTonalButton(onClick = {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    val updated = LocalDateTime.of(
                                                        editingDateTime.toLocalDate(),
                                                        LocalTime.of(hourOfDay, minute)
                                                    )
                                                    editingDueAtMillis = updated.atZone(zoneId).toInstant().toEpochMilli()
                                                },
                                                editingDateTime.hour,
                                                editingDateTime.minute,
                                                true
                                            ).show()
                                        }) {
                                            Text("Heure")
                                        }
                                    }

                                    Text(
                                        text = "Periodicite: ${recurrenceLabel(editingRecurrence, editingRecurrenceInterval)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        RecurrenceButton(
                                            label = "Aucune",
                                            selected = editingRecurrence == TaskRecurrence.NONE,
                                            onClick = { editingRecurrence = TaskRecurrence.NONE }
                                        )
                                        RecurrenceButton(
                                            label = "Jour",
                                            selected = editingRecurrence == TaskRecurrence.DAILY,
                                            onClick = { editingRecurrence = TaskRecurrence.DAILY }
                                        )
                                        RecurrenceButton(
                                            label = "Semaine",
                                            selected = editingRecurrence == TaskRecurrence.WEEKLY,
                                            onClick = { editingRecurrence = TaskRecurrence.WEEKLY }
                                        )
                                        RecurrenceButton(
                                            label = "Mois",
                                            selected = editingRecurrence == TaskRecurrence.MONTHLY,
                                            onClick = { editingRecurrence = TaskRecurrence.MONTHLY }
                                        )
                                    }
                                    if (editingRecurrence != TaskRecurrence.NONE) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FilledTonalButton(onClick = {
                                                editingRecurrenceInterval = (editingRecurrenceInterval - 1).coerceAtLeast(1)
                                            }) {
                                                Text("-")
                                            }
                                            Text(
                                                text = "Tous les $editingRecurrenceInterval",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.width(100.dp)
                                            )
                                            FilledTonalButton(onClick = {
                                                editingRecurrenceInterval = (editingRecurrenceInterval + 1).coerceAtMost(30)
                                            }) {
                                                Text("+")
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Priorite: ${
                                            when (editingPriority) {
                                                TaskPriority.HIGH -> "Haute"
                                                TaskPriority.MEDIUM -> "Moyenne"
                                                TaskPriority.LOW -> "Basse"
                                            }
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = editingPriority == TaskPriority.HIGH,
                                            onClick = { editingPriority = TaskPriority.HIGH },
                                            label = { Text("Haute") }
                                        )
                                        FilterChip(
                                            selected = editingPriority == TaskPriority.MEDIUM,
                                            onClick = { editingPriority = TaskPriority.MEDIUM },
                                            label = { Text("Moyenne") }
                                        )
                                        FilterChip(
                                            selected = editingPriority == TaskPriority.LOW,
                                            onClick = { editingPriority = TaskPriority.LOW },
                                            label = { Text("Basse") }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ElevatedButton(onClick = {
                                            val updated = editingValue.trim()
                                            if (updated.isNotEmpty()) {
                                                onUpdateTask(
                                                    index,
                                                    updated,
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
                                        }, colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )) {
                                            Text("Enregistrer")
                                        }

                                        OutlinedButton(onClick = {
                                            editingIndex = null
                                            editingValue = ""
                                            editingDescription = ""
                                            editingPhotoUri = null
                                        }) {
                                            Text("Annuler")
                                        }
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = {
                                            editingIndex = index
                                            editingValue = task.title
                                            editingDescription = task.description
                                            editingPhotoUri = task.photoUri
                                            editingDueAtMillis = task.dueAtMillis
                                            editingRecurrence = task.recurrence
                                            editingRecurrenceInterval = task.recurrenceInterval
                                            editingPriority = task.priority
                                        }) {
                                            Text("Modifier")
                                        }

                                        ElevatedButton(onClick = {
                                            val wasDone = task.isDone
                                            onToggleTaskDone(index)
                                            if (!wasDone) {
                                                meteorTrigger += 1
                                            }
                                        }, colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )) {
                                            Text(if (task.isDone) "Annuler realisee" else "Marquer realisee")
                                        }

                                        TextButton(onClick = { pendingDeleteIndex = index }) {
                                            Text(
                                                text = "X",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                if (!compactMode || isExpanded || editingIndex == index) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Pour le ${taskDueDateTime.format(formatter)}",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        color = taskTextColor
                                    )
                                    Text(
                                        text = "Periodicite: ${recurrenceLabel(task.recurrence, task.recurrenceInterval).lowercase()}",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        color = taskTextColor
                                    )
                                } else {
                                    Text(
                                        text = "${taskDueDateTime.format(formatter)} - ${recurrenceLabel(task.recurrence, task.recurrenceInterval).lowercase()}",
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = taskTextColor.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            }
                        }
                    }
                }
            }
        }

        if (showMeteor) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = meteorProgress.value
                val launchX = size.width * 0.50f
                val launchY = size.height * 0.96f
                val explodeX = size.width * 0.50f
                val explodeY = size.height * 0.40f
                val ascentPhase = 0.42f

                // Night tint while animation is active.
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x8021337D),
                            Color(0x66312A88),
                            Color(0x00000000)
                        )
                    )
                )

                if (p < ascentPhase) {
                    val t = p / ascentPhase
                    val rocketY = launchY + ((explodeY - launchY) * t)
                    drawLine(
                        color = Color(0xAA8E6FFF),
                        start = Offset(launchX, launchY),
                        end = Offset(launchX, rocketY),
                        strokeWidth = 14f
                    )
                    drawLine(
                        color = Color(0xCC7ED4FF),
                        start = Offset(launchX, launchY),
                        end = Offset(launchX, rocketY),
                        strokeWidth = 8f
                    )
                    drawCircle(
                        color = Color(0xFFD3BEFF),
                        radius = 18f,
                        center = Offset(launchX, rocketY)
                    )
                    drawCircle(
                        color = Color(0xFF8FE3FF),
                        radius = 11f,
                        center = Offset(launchX, rocketY)
                    )
                    drawCircle(
                        color = Color(0x66FFFFFF),
                        radius = 26f,
                        center = Offset(launchX, rocketY)
                    )
                } else {
                    val t = ((p - ascentPhase) / (1f - ascentPhase)).coerceIn(0f, 1f)
                    val burstRadius = (size.minDimension * 0.50f * t)
                    val alpha = (1f - t).coerceAtLeast(0f)

                    // Fading trail from the launch.
                    drawLine(
                        color = Color(0x557DA7FF).copy(alpha = alpha),
                        start = Offset(launchX, launchY),
                        end = Offset(explodeX, explodeY),
                        strokeWidth = 6f
                    )

                    // Explosion core.
                    drawCircle(
                        color = Color(0x88C7B4FF).copy(alpha = alpha),
                        radius = 36f + (42f * t),
                        center = Offset(explodeX, explodeY)
                    )
                    drawCircle(
                        color = Color(0xFF8F6DFF).copy(alpha = alpha),
                        radius = 20f + (18f * t),
                        center = Offset(explodeX, explodeY)
                    )

                    // Blue/violet shards.
                    val count = 90
                    for (i in 0 until count) {
                        val angle = (2.0 * PI * i / count) + (0.35 * sin(i.toFloat()))
                        val speed = 0.52f + (i % 9) * 0.10f
                        val dist = burstRadius * speed
                        val x = explodeX + (cos(angle).toFloat() * dist)
                        val y = explodeY + (sin(angle).toFloat() * dist)
                        val color = when (i % 4) {
                            0 -> Color(0xFF6DC6FF)
                            1 -> Color(0xFF5C8DFF)
                            2 -> Color(0xFF8E63FF)
                            else -> Color(0xFFB07CFF)
                        }
                        val r = 3.5f + ((i % 5) * 1.8f)
                        drawCircle(
                            color = color.copy(alpha = alpha),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }

                    // Longer spikes for "boom".
                    val spikes = 18
                    for (i in 0 until spikes) {
                        val angle = (2.0 * PI * i / spikes)
                        val ex = explodeX + (cos(angle).toFloat() * burstRadius * 1.22f)
                        val ey = explodeY + (sin(angle).toFloat() * burstRadius * 1.22f)
                        drawLine(
                            color = if (i % 2 == 0) Color(0xAA78CFFF) else Color(0xAA8D6FFF),
                            start = Offset(explodeX, explodeY),
                            end = Offset(ex, ey),
                            strokeWidth = 5f
                        )
                    }
                }
            }
        }

        if (pendingDeleteIndex != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteIndex = null },
                title = { Text("Supprimer la tache ?") },
                text = { Text("Cette action est definitive.") },
                confirmButton = {
                    TextButton(onClick = {
                        val idx = pendingDeleteIndex
                        if (idx != null) {
                            onDeleteTask(idx)
                        }
                        pendingDeleteIndex = null
                    }) {
                        Text("Oui, supprimer", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteIndex = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
@Composable
private fun RecurrenceButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (selected) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Text(label)
    }
}

private fun recurrenceLabel(
    recurrence: TaskRecurrence,
    interval: Int
): String {
    val safeInterval = interval.coerceAtLeast(1)
    return when (recurrence) {
        TaskRecurrence.NONE -> "Aucune"
        TaskRecurrence.DAILY -> if (safeInterval == 1) "Tous les jours" else "Tous les $safeInterval jours"
        TaskRecurrence.WEEKLY -> if (safeInterval == 1) "Toutes les semaines" else "Toutes les $safeInterval semaines"
        TaskRecurrence.MONTHLY -> if (safeInterval == 1) "Tous les mois" else "Tous les $safeInterval mois"
    }
}
