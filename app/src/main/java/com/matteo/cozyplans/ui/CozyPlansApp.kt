package com.matteo.cozyplans.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.matteo.cozyplans.data.local.TaskStorage
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import com.matteo.cozyplans.ui.screens.CreateTaskScreen
import com.matteo.cozyplans.ui.screens.ProfileScreen
import com.matteo.cozyplans.ui.screens.TaskListScreen
import com.matteo.cozyplans.ui.screens.WelcomeScreen
import com.matteo.cozyplans.ui.theme.CozyPlansTheme
import java.time.Instant
import java.time.ZoneId

@Composable
fun CozyPlansApp() {
    var currentPage by remember { mutableStateOf(AppPage.WELCOME) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskPhotoUri by remember { mutableStateOf<String?>(null) }
    var newTaskDueAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var newTaskRecurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var newTaskRecurrenceInterval by remember { mutableStateOf(1) }
    var newTaskPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var rewardPoints by remember { mutableStateOf(0) }
    var completedTasksCount by remember { mutableStateOf(0) }
    var lastRewardMessage by remember { mutableStateOf("Termine une tache pour gagner des points") }
    var didLoadPersistedState by remember { mutableStateOf(false) }
    val tasks = remember { mutableStateListOf<Task>() }
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("cozyplans_local_store", Context.MODE_PRIVATE)
    }

    LaunchedEffect(Unit) {
        val persisted = TaskStorage.load(prefs)
        tasks.clear()
        tasks.addAll(persisted.tasks)
        rewardPoints = persisted.rewardPoints
        completedTasksCount = persisted.completedTasksCount
        lastRewardMessage = persisted.lastRewardMessage
        didLoadPersistedState = true
    }

    LaunchedEffect(
        didLoadPersistedState,
        tasks.toList(),
        rewardPoints,
        completedTasksCount,
        lastRewardMessage
    ) {
        if (!didLoadPersistedState) return@LaunchedEffect
        TaskStorage.save(
            prefs = prefs,
            tasks = tasks.toList(),
            rewardPoints = rewardPoints,
            completedTasksCount = completedTasksCount,
            lastRewardMessage = lastRewardMessage
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            when (currentPage) {
                AppPage.WELCOME -> WelcomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    onContinue = { currentPage = AppPage.CREATE }
                )

                AppPage.CREATE, AppPage.LIST, AppPage.PROFILE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "CozyPlans",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Planifie, execute, evolue.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PageChip(
                                label = "Creer",
                                selected = currentPage == AppPage.CREATE,
                                onClick = { currentPage = AppPage.CREATE },
                                modifier = Modifier.weight(1f)
                            )
                            PageChip(
                                label = "Lister",
                                selected = currentPage == AppPage.LIST,
                                onClick = { currentPage = AppPage.LIST },
                                modifier = Modifier.weight(1f)
                            )
                            PageChip(
                                label = "Profil",
                                selected = currentPage == AppPage.PROFILE,
                                onClick = { currentPage = AppPage.PROFILE },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            tonalElevation = 8.dp,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                when (currentPage) {
                                    AppPage.CREATE -> CreateTaskScreen(
                                        value = newTaskTitle,
                                        onValueChange = { newTaskTitle = it },
                                        description = newTaskDescription,
                                        onDescriptionChange = { newTaskDescription = it },
                                        photoUri = newTaskPhotoUri,
                                        onPhotoUriChange = { newTaskPhotoUri = it },
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
                                                        description = newTaskDescription.trim(),
                                                        photoUri = newTaskPhotoUri,
                                                        dueAtMillis = newTaskDueAtMillis,
                                                        recurrence = newTaskRecurrence,
                                                        recurrenceInterval = newTaskRecurrenceInterval,
                                                        priority = newTaskPriority
                                                    )
                                                )
                                                newTaskTitle = ""
                                                newTaskDescription = ""
                                                newTaskPhotoUri = null
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
                                        onUpdateTask = { index, updatedTitle, updatedDescription, updatedPhotoUri, updatedDueAtMillis, updatedRecurrence, updatedRecurrenceInterval, updatedPriority ->
                                            tasks[index] = tasks[index].copy(
                                                title = updatedTitle,
                                                description = updatedDescription,
                                                photoUri = updatedPhotoUri,
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
                                        onDeleteTask = { index ->
                                            tasks.removeAt(index)
                                        },
                                        onPurgeCompleted = {
                                            tasks.removeAll { it.isDone }
                                        }
                                    )

                                    AppPage.PROFILE -> ProfileScreen(
                                        rewardPoints = rewardPoints,
                                        completedTasksCount = completedTasksCount,
                                        pendingTasksCount = tasks.count { !it.isDone },
                                        overdueTasksCount = tasks.count { !it.isDone && it.dueAtMillis < System.currentTimeMillis() },
                                        lastRewardMessage = lastRewardMessage
                                    )

                                    AppPage.WELCOME -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
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
