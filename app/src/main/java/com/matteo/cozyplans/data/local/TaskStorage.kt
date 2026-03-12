package com.matteo.cozyplans.data.local

import android.content.SharedPreferences
import com.matteo.cozyplans.model.Task
import com.matteo.cozyplans.model.TaskPriority
import com.matteo.cozyplans.model.TaskRecurrence
import org.json.JSONArray
import org.json.JSONObject

data class PersistedAppState(
    val tasks: List<Task>,
    val rewardPoints: Int,
    val completedTasksCount: Int,
    val lastRewardMessage: String
)

object TaskStorage {
    private const val KEY_PAYLOAD = "cozyplans_payload_v1"

    fun load(prefs: SharedPreferences): PersistedAppState {
        val raw = prefs.getString(KEY_PAYLOAD, null) ?: return PersistedAppState(
            tasks = emptyList(),
            rewardPoints = 0,
            completedTasksCount = 0,
            lastRewardMessage = "Termine une tache pour gagner des points"
        )

        return try {
            val root = JSONObject(raw)
            val tasksJson = root.optJSONArray("tasks") ?: JSONArray()
            val tasks = buildList {
                for (i in 0 until tasksJson.length()) {
                    val item = tasksJson.optJSONObject(i) ?: continue
                    add(
                        Task(
                            title = item.optString("title", ""),
                            description = item.optString("description", ""),
                            photoUri = item.optString("photoUri", "").ifBlank { null },
                            isDone = item.optBoolean("isDone", false),
                            dueAtMillis = item.optLong("dueAtMillis", System.currentTimeMillis()),
                            recurrence = parseRecurrence(item.optString("recurrence", "NONE")),
                            recurrenceInterval = item.optInt("recurrenceInterval", 1).coerceAtLeast(1),
                            priority = parsePriority(item.optString("priority", "MEDIUM"))
                        )
                    )
                }
            }

            PersistedAppState(
                tasks = tasks,
                rewardPoints = root.optInt("rewardPoints", 0),
                completedTasksCount = root.optInt("completedTasksCount", 0),
                lastRewardMessage = root.optString("lastRewardMessage", "Termine une tache pour gagner des points")
            )
        } catch (_: Exception) {
            PersistedAppState(
                tasks = emptyList(),
                rewardPoints = 0,
                completedTasksCount = 0,
                lastRewardMessage = "Termine une tache pour gagner des points"
            )
        }
    }

    fun save(
        prefs: SharedPreferences,
        tasks: List<Task>,
        rewardPoints: Int,
        completedTasksCount: Int,
        lastRewardMessage: String
    ) {
        val tasksJson = JSONArray()
        tasks.forEach { task ->
            tasksJson.put(
                JSONObject()
                    .put("title", task.title)
                    .put("description", task.description)
                    .put("photoUri", task.photoUri ?: "")
                    .put("isDone", task.isDone)
                    .put("dueAtMillis", task.dueAtMillis)
                    .put("recurrence", task.recurrence.name)
                    .put("recurrenceInterval", task.recurrenceInterval)
                    .put("priority", task.priority.name)
            )
        }

        val root = JSONObject()
            .put("tasks", tasksJson)
            .put("rewardPoints", rewardPoints)
            .put("completedTasksCount", completedTasksCount)
            .put("lastRewardMessage", lastRewardMessage)

        prefs.edit().putString(KEY_PAYLOAD, root.toString()).apply()
    }

    private fun parseRecurrence(raw: String): TaskRecurrence {
        return TaskRecurrence.entries.firstOrNull { it.name == raw } ?: TaskRecurrence.NONE
    }

    private fun parsePriority(raw: String): TaskPriority {
        return TaskPriority.entries.firstOrNull { it.name == raw } ?: TaskPriority.MEDIUM
    }
}
