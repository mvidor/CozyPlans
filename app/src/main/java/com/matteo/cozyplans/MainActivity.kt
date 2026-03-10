package com.matteo.cozyplans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matteo.cozyplans.ui.theme.CozyPlansTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CozyPlansTheme {
                CozyPlansApp()
            }
        }
    }
}

private enum class AppPage {
    WELCOME,
    CREATE,
    LIST
}

@Composable
private fun CozyPlansApp() {
    var currentPage by remember { mutableStateOf(AppPage.WELCOME) }
    var newTaskTitle by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf<String>() }

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
                        AppPage.CREATE -> CreateTaskPage(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            onAddTask = {
                                val trimmed = newTaskTitle.trim()
                                if (trimmed.isNotEmpty()) {
                                    tasks.add(trimmed)
                                    newTaskTitle = ""
                                    currentPage = AppPage.LIST
                                }
                            }
                        )

                        AppPage.LIST -> TaskListPage(tasks = tasks)
                        AppPage.WELCOME -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onContinue() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo CozyPlans",
            modifier = Modifier.fillMaxWidth(0.8f),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "CozyPlans",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Touche pour continuer",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CreateTaskPage(
    value: String,
    onValueChange: (String) -> Unit,
    onAddTask: () -> Unit
) {
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

        Button(onClick = onAddTask) {
            Text("Ajouter la tache")
        }
    }
}

@Composable
private fun TaskListPage(tasks: List<String>) {
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${index + 1}. $task",
                            modifier = Modifier.padding(12.dp)
                        )
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
