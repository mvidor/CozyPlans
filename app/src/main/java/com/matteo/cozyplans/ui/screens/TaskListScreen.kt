package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    onUpdateTask: (index: Int, updatedTitle: String, updatedDueAtMillis: Long) -> Unit,
    onToggleTaskDone: (index: Int) -> Unit
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingValue by remember { mutableStateOf("") }
    var editingDueAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }

    val filteredTasks = tasks.mapIndexedNotNull { index, task ->
        val include = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.TODO -> !task.isDone
            TaskFilter.DONE -> task.isDone
        }
        if (include) index to task else null
    }
    val nowMillis = System.currentTimeMillis()
    val overdueCount = tasks.count { !it.isDone && it.dueAtMillis < nowMillis }

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

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        val updated = editingValue.trim()
                                        if (updated.isNotEmpty()) {
                                            onUpdateTask(index, updated, editingDueAtMillis)
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
                                    }) {
                                        Text("Modifier")
                                    }

                                    Button(onClick = { onToggleTaskDone(index) }) {
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
                        }
                    }
                }
            }
        }
    }
}
