package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

private enum class TaskFilter {
    ALL,
    TODO,
    DONE
}

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onUpdateTask: (index: Int, updatedTitle: String, updatedDueAtMillis: Long, updatedRecurrence: TaskRecurrence, updatedRecurrenceInterval: Int, updatedPriority: TaskPriority) -> Unit,
    onToggleTaskDone: (index: Int) -> Unit,
    onPurgeCompleted: () -> Unit
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingValue by remember { mutableStateOf("") }
    var editingDueAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var editingRecurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var editingRecurrenceInterval by remember { mutableIntStateOf(1) }
    var editingPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var meteorTrigger by remember { mutableIntStateOf(0) }
    var showMeteor by remember { mutableStateOf(false) }
    val meteorProgress = remember { Animatable(0f) }

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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Lister toutes les taches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (overdueCount > 0) {
                Card(
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
                Button(onClick = { selectedFilter = TaskFilter.ALL }) { Text("Toutes") }
                Button(onClick = { selectedFilter = TaskFilter.TODO }) { Text("A faire") }
                Button(onClick = { selectedFilter = TaskFilter.DONE }) { Text("Realisees") }
            }
            if (completedCount > 0) {
                Button(onClick = onPurgeCompleted) {
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(filteredTasks) { _, item ->
                        val index = item.first
                        val task = item.second
                        val isOverdue = !task.isDone && task.dueAtMillis < nowMillis
                        val taskCardColor = if (task.isDone) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.surface
                        val taskTextColor = if (task.isDone) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        val taskDueDateTime = Instant.ofEpochMilli(task.dueAtMillis).atZone(zoneId).toLocalDateTime()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = taskCardColor)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${index + 1}. ${task.title}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                                    color = taskTextColor
                                )
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

                                    Text(
                                        text = "Echeance: ${editingDateTime.format(formatter)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
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

                                        Button(onClick = {
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
                                            Button(onClick = {
                                                editingRecurrenceInterval = (editingRecurrenceInterval - 1).coerceAtLeast(1)
                                            }) {
                                                Text("-")
                                            }
                                            Text(
                                                text = "Tous les $editingRecurrenceInterval",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.width(100.dp)
                                            )
                                            Button(onClick = {
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
                                        Button(onClick = { editingPriority = TaskPriority.HIGH }) { Text("Haute") }
                                        Button(onClick = { editingPriority = TaskPriority.MEDIUM }) { Text("Moy.") }
                                        Button(onClick = { editingPriority = TaskPriority.LOW }) { Text("Basse") }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            val updated = editingValue.trim()
                                            if (updated.isNotEmpty()) {
                                                onUpdateTask(index, updated, editingDueAtMillis, editingRecurrence, editingRecurrenceInterval, editingPriority)
                                                editingIndex = null
                                                editingValue = ""
                                            }
                                        }) {
                                            Text("Enregistrer")
                                        }

                                        Button(onClick = {
                                            editingIndex = null
                                            editingValue = ""
                                        }) {
                                            Text("Annuler")
                                        }
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            editingIndex = index
                                            editingValue = task.title
                                            editingDueAtMillis = task.dueAtMillis
                                            editingRecurrence = task.recurrence
                                            editingRecurrenceInterval = task.recurrenceInterval
                                            editingPriority = task.priority
                                        }) {
                                            Text("Modifier")
                                        }

                                        Button(onClick = {
                                            val wasDone = task.isDone
                                            onToggleTaskDone(index)
                                            if (!wasDone) {
                                                meteorTrigger += 1
                                            }
                                        }) {
                                            Text(if (task.isDone) "Annuler realisee" else "Marquer realisee")
                                        }
                                    }
                                }

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
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
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
