package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CreateTaskScreen(
    value: String,
    onValueChange: (String) -> Unit,
    dueAtMillis: Long,
    onDueAtChange: (Long) -> Unit,
    recurrence: TaskRecurrence,
    onRecurrenceChange: (TaskRecurrence) -> Unit,
    recurrenceInterval: Int,
    onRecurrenceIntervalChange: (Int) -> Unit,
    priority: TaskPriority,
    onPriorityChange: (TaskPriority) -> Unit,
    onAddTask: () -> Unit
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val selectedDateTime = Instant.ofEpochMilli(dueAtMillis).atZone(zoneId).toLocalDateTime()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Creer une nouvelle tache",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nom de la tache") },
            singleLine = true
        )

        Text(
            text = "Echeance: ${selectedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val updated = LocalDateTime.of(
                            LocalDate.of(year, month + 1, dayOfMonth),
                            selectedDateTime.toLocalTime()
                        )
                        onDueAtChange(updated.atZone(zoneId).toInstant().toEpochMilli())
                    },
                    selectedDateTime.year,
                    selectedDateTime.monthValue - 1,
                    selectedDateTime.dayOfMonth
                ).show()
            }) {
                Text("Choisir date")
            }

            Button(onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val updated = LocalDateTime.of(
                            selectedDateTime.toLocalDate(),
                            LocalTime.of(hourOfDay, minute)
                        )
                        onDueAtChange(updated.atZone(zoneId).toInstant().toEpochMilli())
                    },
                    selectedDateTime.hour,
                    selectedDateTime.minute,
                    true
                ).show()
            }) {
                Text("Choisir heure")
            }
        }

        Text(
            text = "Periodicite: ${recurrenceLabel(recurrence, recurrenceInterval)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecurrenceButton(
                label = "Aucune",
                selected = recurrence == TaskRecurrence.NONE,
                onClick = { onRecurrenceChange(TaskRecurrence.NONE) }
            )
            RecurrenceButton(
                label = "Jour",
                selected = recurrence == TaskRecurrence.DAILY,
                onClick = { onRecurrenceChange(TaskRecurrence.DAILY) }
            )
            RecurrenceButton(
                label = "Semaine",
                selected = recurrence == TaskRecurrence.WEEKLY,
                onClick = { onRecurrenceChange(TaskRecurrence.WEEKLY) }
            )
            RecurrenceButton(
                label = "Mois",
                selected = recurrence == TaskRecurrence.MONTHLY,
                onClick = { onRecurrenceChange(TaskRecurrence.MONTHLY) }
            )
        }

        if (recurrence != TaskRecurrence.NONE) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    onRecurrenceIntervalChange((recurrenceInterval - 1).coerceAtLeast(1))
                }) {
                    Text("-")
                }
                Text(
                    text = "Tous les $recurrenceInterval",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(100.dp)
                )
                Button(onClick = {
                    onRecurrenceIntervalChange((recurrenceInterval + 1).coerceAtMost(30))
                }) {
                    Text("+")
                }
            }
        }

        Text(
            text = "Priorite: ${
                when (priority) {
                    TaskPriority.HIGH -> "Haute"
                    TaskPriority.MEDIUM -> "Moyenne"
                    TaskPriority.LOW -> "Basse"
                }
            }",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onPriorityChange(TaskPriority.HIGH) }) { Text("Haute") }
            Button(onClick = { onPriorityChange(TaskPriority.MEDIUM) }) { Text("Moy.") }
            Button(onClick = { onPriorityChange(TaskPriority.LOW) }) { Text("Basse") }
        }

        Button(onClick = onAddTask) {
            Text("Ajouter la tache")
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
