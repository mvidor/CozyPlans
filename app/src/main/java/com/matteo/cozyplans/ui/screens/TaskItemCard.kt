package com.matteo.cozyplans.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskItemCard(
    task: Task,
    index: Int,
    compactMode: Boolean,
    isExpanded: Boolean,
    nowMillis: Long,
    formatter: DateTimeFormatter,
    zoneId: ZoneId,
    isEditing: Boolean,
    editingTitle: String,
    editingDescription: String,
    editingPhotoUri: String?,
    editingDueAtMillis: Long,
    editingRecurrence: TaskRecurrence,
    editingRecurrenceInterval: Int,
    editingPriority: TaskPriority,
    onEditingTitleChange: (String) -> Unit,
    onEditingDescriptionChange: (String) -> Unit,
    onEditingRecurrenceChange: (TaskRecurrence) -> Unit,
    onEditingRecurrenceIntervalChange: (Int) -> Unit,
    onEditingPriorityChange: (TaskPriority) -> Unit,
    onEditingDueAtMillisChange: (Long) -> Unit,
    onPickEditingPhoto: () -> Unit,
    onClearEditingPhoto: () -> Unit,
    onToggleExpand: () -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onToggleDone: () -> Unit,
    onRequestDelete: () -> Unit
) {
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
            if (task.photoUri != null && !isEditing) {
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
                                colors = listOf(Color(0x55000000), Color(0x22000000), Color(0x00000000))
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

                if (compactMode && !isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TaskStatusChip(task.isDone)
                        TextButton(onClick = onToggleExpand) {
                            Text(if (isExpanded) "Masquer" else "Details")
                        }
                    }
                } else {
                    TaskStatusChip(task.isDone)
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

            if (isEditing) {
                TaskItemEditSection(
                    editingTitle = editingTitle,
                    editingDescription = editingDescription,
                    editingPhotoUri = editingPhotoUri,
                    editingDueAtMillis = editingDueAtMillis,
                    editingRecurrence = editingRecurrence,
                    editingRecurrenceInterval = editingRecurrenceInterval,
                    editingPriority = editingPriority,
                    formatter = formatter,
                    zoneId = zoneId,
                    onEditingTitleChange = onEditingTitleChange,
                    onEditingDescriptionChange = onEditingDescriptionChange,
                    onEditingRecurrenceChange = onEditingRecurrenceChange,
                    onEditingRecurrenceIntervalChange = onEditingRecurrenceIntervalChange,
                    onEditingPriorityChange = onEditingPriorityChange,
                    onEditingDueAtMillisChange = onEditingDueAtMillisChange,
                    onPickEditingPhoto = onPickEditingPhoto,
                    onClearEditingPhoto = onClearEditingPhoto,
                    onSaveEdit = onSaveEdit,
                    onCancelEdit = onCancelEdit
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onStartEdit) { Text("Modifier") }
                    ElevatedButton(onClick = onToggleDone) {
                        Text(if (task.isDone) "Annuler realisee" else "Marquer realisee")
                    }
                    TextButton(onClick = onRequestDelete) {
                        Text(
                            text = "X",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (!compactMode || isExpanded || isEditing) {
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

@Composable
private fun TaskStatusChip(isDone: Boolean) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(if (isDone) "Faite" else "A faire") },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = if (isDone) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
            disabledLabelColor = if (isDone) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
        )
    )
}
