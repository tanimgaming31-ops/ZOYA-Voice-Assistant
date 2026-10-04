package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: ZoyaViewModel) {
  val reminders by viewModel.reminders.collectAsState(initial = emptyList())
  var showDialog by remember { mutableStateOf(false) }
  var title by remember { mutableStateOf("") }
  var dateTime by remember { mutableStateOf("Today at 5:00 PM") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Reminders") },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = MaterialTheme.colorScheme.primary
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Reminder", tint = Color.White)
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      if (reminders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = "No active reminders.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(reminders) { rem ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = rem.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = rem.dateTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.deleteReminder(rem.id) }) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
              }
            }
          }
        }
      }

      if (showDialog) {
        AlertDialog(
          onDismissRequest = { showDialog = false },
          title = { Text("Set Reminder") },
          text = {
            Column {
              OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Reminder title") },
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = dateTime,
                onValueChange = { dateTime = it },
                placeholder = { Text("Date & Time") },
                modifier = Modifier.fillMaxWidth()
              )
            }
          },
          confirmButton = {
            TextButton(onClick = {
              if (title.isNotBlank()) {
                viewModel.addReminder(title, dateTime)
                title = ""
                showDialog = false
              }
            }) {
              Text("Set")
            }
          },
          dismissButton = {
            TextButton(onClick = { showDialog = false }) {
              Text("Cancel")
            }
          }
        )
      }
    }
  }
}
