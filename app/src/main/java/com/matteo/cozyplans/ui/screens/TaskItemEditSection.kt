package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskItemEditSection(
    editingTitle: String,
    editingDescription: String,
    editingPhotoUri: String?,
    editingDueAtMillis: Long,
    editingRecurrence: TaskRecurrence,
    editingRecurrenceInterval: Int,
    editingPriority: TaskPriority,
    formatter: DateTimeFormatter,
    zoneId: ZoneId,
    onEditingTitleChange: (String) -> Unit,
    onEditingDescriptionChange: (String) -> Unit,
    onEditingRecurrenceChange: (TaskRecurrence) -> Unit,
    onEditingRecurrenceIntervalChange: (Int) -> Unit,
    onEditingPriorityChange: (TaskPriority) -> Unit,
    onEditingDueAtMillisChange: (Long) -> Unit,
    onPickEditingPhoto: () -> Unit,
    onClearEditingPhoto: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val context = LocalContext.current
    val editingDateTime = Instant.ofEpochMilli(editingDueAtMillis).atZone(zoneId).toLocalDateTime()

    OutlinedTextField(
        value = editingTitle,
        onValueChange = onEditingTitleChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Modifier la tache") }
    )
    OutlinedTextField(
        value = editingDescription,
        onValueChange = onEditingDescriptionChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Description") },
        minLines = 3,
        maxLines = 5
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = onPickEditingPhoto,
            label = { Text(if (editingPhotoUri == null) "Joindre photo" else "Changer photo") }
        )
        if (editingPhotoUri != null) {
            AssistChip(
                onClick = onClearEditingPhoto,
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
                    onEditingDueAtMillisChange(updated.atZone(zoneId).toInstant().toEpochMilli())
                },
                editingDateTime.year,
                editingDateTime.monthValue - 1,
                editingDateTime.dayOfMonth
            ).show()
        }) { Text("Date") }

        FilledTonalButton(onClick = {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val updated = LocalDateTime.of(
                        editingDateTime.toLocalDate(),
                        LocalTime.of(hourOfDay, minute)
                    )
                    onEditingDueAtMillisChange(updated.atZone(zoneId).toInstant().toEpochMilli())
                },
                editingDateTime.hour,
                editingDateTime.minute,
                true
            ).show()
        }) { Text("Heure") }
    }

    Text(
        text = "Periodicite: ${recurrenceLabel(editingRecurrence, editingRecurrenceInterval)}",
        style = MaterialTheme.typography.bodyMedium
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RecurrenceButton("Aucune", editingRecurrence == TaskRecurrence.NONE) {
            onEditingRecurrenceChange(TaskRecurrence.NONE)
        }
        RecurrenceButton("Jour", editingRecurrence == TaskRecurrence.DAILY) {
            onEditingRecurrenceChange(TaskRecurrence.DAILY)
        }
        RecurrenceButton("Semaine", editingRecurrence == TaskRecurrence.WEEKLY) {
            onEditingRecurrenceChange(TaskRecurrence.WEEKLY)
        }
        RecurrenceButton("Mois", editingRecurrence == TaskRecurrence.MONTHLY) {
            onEditingRecurrenceChange(TaskRecurrence.MONTHLY)
        }
    }

    if (editingRecurrence != TaskRecurrence.NONE) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = {
                onEditingRecurrenceIntervalChange((editingRecurrenceInterval - 1).coerceAtLeast(1))
            }) { Text("-") }
            Text(
                text = "Tous les $editingRecurrenceInterval",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(100.dp)
            )
            FilledTonalButton(onClick = {
                onEditingRecurrenceIntervalChange((editingRecurrenceInterval + 1).coerceAtMost(30))
            }) { Text("+") }
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
            onClick = { onEditingPriorityChange(TaskPriority.HIGH) },
            label = { Text("Haute") }
        )
        FilterChip(
            selected = editingPriority == TaskPriority.MEDIUM,
            onClick = { onEditingPriorityChange(TaskPriority.MEDIUM) },
            label = { Text("Moyenne") }
        )
        FilterChip(
            selected = editingPriority == TaskPriority.LOW,
            onClick = { onEditingPriorityChange(TaskPriority.LOW) },
            label = { Text("Basse") }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ElevatedButton(onClick = onSaveEdit) { Text("Enregistrer") }
        OutlinedButton(onClick = onCancelEdit) { Text("Annuler") }
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
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        }
    ) {
        Text(label)
    }
}

internal fun recurrenceLabel(
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
