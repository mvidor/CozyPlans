package com.matteo.cozyplans.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OverdueAlertCard(overdueCount: Int) {
    if (overdueCount <= 0) return
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = "ALERTE: $overdueCount tache(s) en retard",
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeleteTaskDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la tache ?") },
        text = { Text("Cette action est definitive.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Oui, supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun MeteorCelebrationOverlay(
    show: Boolean,
    progress: Float
) {
    if (!show) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress
        val launchX = size.width * 0.50f
        val launchY = size.height * 0.96f
        val explodeX = size.width * 0.50f
        val explodeY = size.height * 0.40f
        val ascentPhase = 0.42f

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x8021337D),
                    Color(0x66312A88),
                    Color(0x00000000)
                )
            )
        )

        if (p < ascentPhase) {
            val t = p / ascentPhase
            val rocketY = launchY + ((explodeY - launchY) * t)
            drawLine(Color(0xAA8E6FFF), Offset(launchX, launchY), Offset(launchX, rocketY), 14f)
            drawLine(Color(0xCC7ED4FF), Offset(launchX, launchY), Offset(launchX, rocketY), 8f)
            drawCircle(Color(0xFFD3BEFF), 18f, Offset(launchX, rocketY))
            drawCircle(Color(0xFF8FE3FF), 11f, Offset(launchX, rocketY))
            drawCircle(Color(0x66FFFFFF), 26f, Offset(launchX, rocketY))
        } else {
            val t = ((p - ascentPhase) / (1f - ascentPhase)).coerceIn(0f, 1f)
            val burstRadius = size.minDimension * 0.50f * t
            val alpha = (1f - t).coerceAtLeast(0f)

            drawLine(
                color = Color(0x557DA7FF).copy(alpha = alpha),
                start = Offset(launchX, launchY),
                end = Offset(explodeX, explodeY),
                strokeWidth = 6f
            )
            drawCircle(Color(0x88C7B4FF).copy(alpha = alpha), 36f + (42f * t), Offset(explodeX, explodeY))
            drawCircle(Color(0xFF8F6DFF).copy(alpha = alpha), 20f + (18f * t), Offset(explodeX, explodeY))

            val count = 90
            for (i in 0 until count) {
                val angle = (2.0 * PI * i / count) + (0.35 * sin(i.toFloat()))
                val speed = 0.52f + (i % 9) * 0.10f
                val dist = burstRadius * speed
                val x = explodeX + (cos(angle).toFloat() * dist)
                val y = explodeY + (sin(angle).toFloat() * dist)
                val color = when (i % 4) {
                    0 -> Color(0xFF6DC6FF)
                    1 -> Color(0xFF5C8DFF)
                    2 -> Color(0xFF8E63FF)
                    else -> Color(0xFFB07CFF)
                }
                drawCircle(color.copy(alpha = alpha), 3.5f + ((i % 5) * 1.8f), Offset(x, y))
            }

            val spikes = 18
            for (i in 0 until spikes) {
                val angle = (2.0 * PI * i / spikes)
                val ex = explodeX + (cos(angle).toFloat() * burstRadius * 1.22f)
                val ey = explodeY + (sin(angle).toFloat() * burstRadius * 1.22f)
                drawLine(
                    color = if (i % 2 == 0) Color(0xAA78CFFF) else Color(0xAA8D6FFF),
                    start = Offset(explodeX, explodeY),
                    end = Offset(ex, ey),
                    strokeWidth = 5f
                )
            }
        }
    }
}
