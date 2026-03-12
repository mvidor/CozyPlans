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
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.model.TaskRecurrence
import com.matteo.cozyplans.ui.screens.CreateTaskScreen
import com.matteo.cozyplans.ui.screens.TaskListScreen
import com.matteo.cozyplans.ui.screens.WelcomeScreen
import com.matteo.cozyplans.ui.theme.CozyPlansTheme
import java.time.Instant
import java.time.ZoneId

@Composable
fun CozyPlansApp() {
    var currentPage by remember { mutableStateOf(AppPage.WELCOME) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDueAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var newTaskRecurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var newTaskRecurrenceInterval by remember { mutableStateOf(1) }
    var newTaskPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var rewardPoints by remember { mutableStateOf(0) }
    var completedTasksCount by remember { mutableStateOf(0) }
    var lastRewardMessage by remember { mutableStateOf("Termine une tache pour gagner des points") }
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
                            recurrence = newTaskRecurrence,
                            onRecurrenceChange = { newTaskRecurrence = it },
                            recurrenceInterval = newTaskRecurrenceInterval,
                            onRecurrenceIntervalChange = { newTaskRecurrenceInterval = it },
                            priority = newTaskPriority,
                            onPriorityChange = { newTaskPriority = it },
                            onAddTask = {
                                val trimmed = newTaskTitle.trim()
                                if (trimmed.isNotEmpty()) {
                                    tasks.add(
                                        Task(
                                            title = trimmed,
                                            dueAtMillis = newTaskDueAtMillis,
                                            recurrence = newTaskRecurrence,
                                            recurrenceInterval = newTaskRecurrenceInterval,
                                            priority = newTaskPriority
                                        )
                                    )
                                    newTaskTitle = ""
                                    newTaskDueAtMillis = System.currentTimeMillis()
                                    newTaskRecurrence = TaskRecurrence.NONE
                                    newTaskRecurrenceInterval = 1
                                    newTaskPriority = TaskPriority.MEDIUM
                                    currentPage = AppPage.LIST
                                }
                            }
                        )

                        AppPage.LIST -> TaskListScreen(
                            tasks = tasks,
                            rewardPoints = rewardPoints,
                            completedTasksCount = completedTasksCount,
                            lastRewardMessage = lastRewardMessage,
                            onUpdateTask = { index, updatedTitle, updatedDueAtMillis, updatedRecurrence, updatedRecurrenceInterval, updatedPriority ->
                                tasks[index] = tasks[index].copy(
                                    title = updatedTitle,
                                    dueAtMillis = updatedDueAtMillis,
                                    recurrence = updatedRecurrence,
                                    recurrenceInterval = updatedRecurrenceInterval,
                                    priority = updatedPriority
                                )
                            },
                            onToggleTaskDone = { index ->
                                val task = tasks[index]
                                val willBeDone = !task.isDone
                                tasks[index] = task.copy(isDone = willBeDone)
                                val rewardValue = rewardForTask(task)

                                if (willBeDone) {
                                    rewardPoints += rewardValue
                                    completedTasksCount += 1
                                    lastRewardMessage = "+$rewardValue points: ${task.title}"
                                } else {
                                    rewardPoints = (rewardPoints - rewardValue).coerceAtLeast(0)
                                    completedTasksCount = (completedTasksCount - 1).coerceAtLeast(0)
                                    lastRewardMessage = "-$rewardValue points: ${task.title}"
                                }

                                if (willBeDone && task.recurrence != TaskRecurrence.NONE) {
                                    val zoneId = ZoneId.systemDefault()
                                    val currentDue = Instant.ofEpochMilli(task.dueAtMillis).atZone(zoneId).toLocalDateTime()
                                    val interval = task.recurrenceInterval.coerceAtLeast(1).toLong()
                                    var nextDue = when (task.recurrence) {
                                        TaskRecurrence.DAILY -> currentDue.plusDays(interval)
                                        TaskRecurrence.WEEKLY -> currentDue.plusWeeks(interval)
                                        TaskRecurrence.MONTHLY -> currentDue.plusMonths(interval)
                                        TaskRecurrence.NONE -> currentDue
                                    }
                                    val now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDateTime()
                                    while (nextDue.isBefore(now)) {
                                        nextDue = when (task.recurrence) {
                                            TaskRecurrence.DAILY -> nextDue.plusDays(interval)
                                            TaskRecurrence.WEEKLY -> nextDue.plusWeeks(interval)
                                            TaskRecurrence.MONTHLY -> nextDue.plusMonths(interval)
                                            TaskRecurrence.NONE -> nextDue
                                        }
                                    }
                                    tasks.add(
                                        task.copy(
                                            isDone = false,
                                            dueAtMillis = nextDue.atZone(zoneId).toInstant().toEpochMilli()
                                        )
                                    )
                                }
                            },
                            onPurgeCompleted = {
                                tasks.removeAll { it.isDone }
                            }
                        )

                        AppPage.WELCOME -> Unit
                    }
                }
            }
        }
    }
}

private fun rewardForTask(task: Task): Int {
    val base = when (task.priority) {
        TaskPriority.HIGH -> 30
        TaskPriority.MEDIUM -> 20
        TaskPriority.LOW -> 10
    }
    val recurrenceBonus = if (task.recurrence != TaskRecurrence.NONE) 4 else 0
    val onTimeBonus = if (task.dueAtMillis >= System.currentTimeMillis()) 6 else 0
    return base + recurrenceBonus + onTimeBonus
}

@Preview(showBackground = true)
@Composable
private fun CozyPlansAppPreview() {
    CozyPlansTheme {
        CozyPlansApp()
    }
}
