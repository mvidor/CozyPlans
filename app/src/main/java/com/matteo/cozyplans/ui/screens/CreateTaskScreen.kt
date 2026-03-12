package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun CreateTaskScreen(
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    photoUri: String?,
    onPhotoUriChange: (String?) -> Unit,
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
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onPhotoUriChange(uri?.toString())
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Nouvelle tache",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nom de la tache") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        label = { Text(if (photoUri == null) "Joindre une photo" else "Changer photo") }
                    )
                    if (photoUri != null) {
                        AssistChip(
                            onClick = { onPhotoUriChange(null) },
                            label = { Text("Retirer") }
                        )
                    }
                }
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Photo de la tache",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Echeance: ${selectedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
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
                    },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Date")
                    }

                    FilledTonalButton(
                        onClick = {
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
                    },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Heure")
                    }
                }
            }
        }

        Text(
            text = "Periodicite: ${recurrenceLabel(recurrence, recurrenceInterval)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            RecurrenceChip(
                label = "Aucune",
                selected = recurrence == TaskRecurrence.NONE,
                onClick = { onRecurrenceChange(TaskRecurrence.NONE) }
            )
            RecurrenceChip(
                label = "Jour",
                selected = recurrence == TaskRecurrence.DAILY,
                onClick = { onRecurrenceChange(TaskRecurrence.DAILY) }
            )
            RecurrenceChip(
                label = "Semaine",
                selected = recurrence == TaskRecurrence.WEEKLY,
                onClick = { onRecurrenceChange(TaskRecurrence.WEEKLY) }
            )
            RecurrenceChip(
                label = "Mois",
                selected = recurrence == TaskRecurrence.MONTHLY,
                onClick = { onRecurrenceChange(TaskRecurrence.MONTHLY) }
            )
        }

        if (recurrence != TaskRecurrence.NONE) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = {
                    onRecurrenceIntervalChange((recurrenceInterval - 1).coerceAtLeast(1))
                }, colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )) {
                    Text("-")
                }
                Text(
                    text = "Tous les $recurrenceInterval",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(100.dp)
                )
                FilledTonalButton(onClick = {
                    onRecurrenceIntervalChange((recurrenceInterval + 1).coerceAtMost(30))
                }, colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )) {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            PriorityChip(
                label = "Haute",
                selected = priority == TaskPriority.HIGH,
                onClick = { onPriorityChange(TaskPriority.HIGH) }
            )
            PriorityChip(
                label = "Moyenne",
                selected = priority == TaskPriority.MEDIUM,
                onClick = { onPriorityChange(TaskPriority.MEDIUM) }
            )
            PriorityChip(
                label = "Basse",
                selected = priority == TaskPriority.LOW,
                onClick = { onPriorityChange(TaskPriority.LOW) }
            )
        }

        ElevatedButton(
            onClick = onAddTask,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Ajouter la tache")
        }
    }
}

@Composable
private fun RecurrenceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun PriorityChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    )
}

@Composable
private fun RecurrenceButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        colors = if (selected) {
            ButtonDefaults.elevatedButtonColors()
        } else {
            ButtonDefaults.filledTonalButtonColors()
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
