package com.matteo.cozyplans.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

        Button(onClick = onAddTask) {
            Text("Ajouter la tache")
        }
    }
}
