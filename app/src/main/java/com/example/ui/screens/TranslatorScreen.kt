package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(viewModel: ZoyaViewModel) {
  var sourceText by remember { mutableStateOf("") }
  var targetLang by remember { mutableStateOf("Bengali") }
  var translatedText by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Multi-lingual Translator") },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = sourceText,
        onValueChange = { sourceText = it },
        placeholder = { Text("Enter text to translate...") },
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp),
        shape = RoundedCornerShape(16.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Bengali", "Hindi", "Arabic", "English").forEach { lang ->
          FilterChip(
            selected = targetLang == lang,
            onClick = { targetLang = lang },
            label = { Text(lang) }
          )
        }
      }

      Button(
        onClick = {
          if (sourceText.isNotBlank()) {
            translatedText = "[$targetLang Translation]: Translated version of '$sourceText' via ZOYA Gemini engine."
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Translate, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Translate Now")
      }

      if (translatedText.isNotBlank()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Result ($targetLang):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = translatedText, style = MaterialTheme.typography.bodyLarge)
          }
        }
      }
    }
  }
}
