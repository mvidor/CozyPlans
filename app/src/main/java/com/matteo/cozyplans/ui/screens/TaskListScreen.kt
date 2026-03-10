package com.matteo.cozyplans.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.model.Task

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onUpdateTask: (index: Int, updatedTitle: String) -> Unit,
    onToggleTaskDone: (index: Int) -> Unit
) {
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingValue by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Lister toutes les taches",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (tasks.isEmpty()) {
            Text("Aucune tache pour le moment.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(tasks) { index, task ->
                    val taskCardColor =
                        if (task.isDone) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.surface
                    val taskTextColor =
                        if (task.isDone) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

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

                            Spacer(modifier = Modifier.height(8.dp))

                            if (editingIndex == index) {
                                OutlinedTextField(
                                    value = editingValue,
                                    onValueChange = { editingValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Modifier la tache") }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val updated = editingValue.trim()
                                            if (updated.isNotEmpty()) {
                                                onUpdateTask(index, updated)
                                                editingIndex = null
                                                editingValue = ""
                                            }
                                        }
                                    ) {
                                        Text("Enregistrer")
                                    }

                                    Button(
                                        onClick = {
                                            editingIndex = null
                                            editingValue = ""
                                        }
                                    ) {
                                        Text("Annuler")
                                    }
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            editingIndex = index
                                            editingValue = task.title
                                        }
                                    ) {
                                        Text("Modifier")
                                    }

                                    Button(onClick = { onToggleTaskDone(index) }) {
                                        Text(if (task.isDone) "Annuler realisee" else "Marquer realisee")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
