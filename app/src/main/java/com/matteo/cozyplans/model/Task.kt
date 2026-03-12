package com.matteo.cozyplans.model

data class Task(
    val title: String,
    val description: String = "",
    val photoUri: String? = null,
    val isDone: Boolean = false,
    val dueAtMillis: Long,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val recurrenceInterval: Int = 1,
    val priority: TaskPriority = TaskPriority.MEDIUM
)
