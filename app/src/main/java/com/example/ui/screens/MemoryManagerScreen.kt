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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryManagerScreen(viewModel: ZoyaViewModel) {
  val memories by viewModel.memory.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var newKey by remember { mutableStateOf("") }
  var newValue by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Memory Manager") },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { viewModel.clearAllMemory() }) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All Memories")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Memory")
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      if (memories.isEmpty()) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(16.dp))
          Text(text = "No stored memories yet.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "ZOYA will automatically remember facts and preferences you share.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(memories) { memory ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = memory.key, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(text = memory.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { viewModel.deleteMemory(memory.key) }) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete Memory", tint = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }
    }

    if (showAddDialog) {
      AlertDialog(
        onDismissRequest = { showAddDialog = false },
        title = { Text("Add Memory Fact") },
        text = {
          Column {
            OutlinedTextField(
              value = newKey,
              onValueChange = { newKey = it },
              label = { Text("Fact / Key (e.g. Favorite Color)") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = newValue,
              onValueChange = { newValue = it },
              label = { Text("Value (e.g. Deep Blue)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              if (newKey.isNotBlank() && newValue.isNotBlank()) {
                viewModel.saveMemory(newKey.trim(), newValue.trim())
                newKey = ""
                newValue = ""
                showAddDialog = false
              }
            }
          ) {
            Text("Save")
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}
