package com.matteo.cozyplans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.ui.screens.CreateTaskScreen
import com.matteo.cozyplans.ui.screens.TaskListScreen
import com.matteo.cozyplans.ui.screens.WelcomeScreen
import com.matteo.cozyplans.ui.theme.CozyPlansTheme

@Composable
fun CozyPlansApp() {
    var currentPage by remember { mutableStateOf(AppPage.WELCOME) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDueAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val tasks = remember { mutableStateListOf<Task>() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentPage) {
            AppPage.WELCOME -> WelcomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onContinue = { currentPage = AppPage.CREATE }
            )

            AppPage.CREATE, AppPage.LIST -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "CozyPlans",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { currentPage = AppPage.CREATE },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Creer")
                        }
                        Button(
                            onClick = { currentPage = AppPage.LIST },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lister")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (currentPage) {
                        AppPage.CREATE -> CreateTaskScreen(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            dueAtMillis = newTaskDueAtMillis,
                            onDueAtChange = { newTaskDueAtMillis = it },
                            onAddTask = {
                                val trimmed = newTaskTitle.trim()
                                if (trimmed.isNotEmpty()) {
                                    tasks.add(Task(title = trimmed, dueAtMillis = newTaskDueAtMillis))
                                    newTaskTitle = ""
                                    newTaskDueAtMillis = System.currentTimeMillis()
                                    currentPage = AppPage.LIST
                                }
                            }
                        )

                        AppPage.LIST -> TaskListScreen(
                            tasks = tasks,
                            onUpdateTask = { index, updatedTitle, updatedDueAtMillis ->
                                tasks[index] = tasks[index].copy(
                                    title = updatedTitle,
                                    dueAtMillis = updatedDueAtMillis
                                )
                            },
                            onToggleTaskDone = { index ->
                                tasks[index] = tasks[index].copy(isDone = !tasks[index].isDone)
                            }
                        )

                        AppPage.WELCOME -> Unit
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CozyPlansAppPreview() {
    CozyPlansTheme {
        CozyPlansApp()
    }
}
