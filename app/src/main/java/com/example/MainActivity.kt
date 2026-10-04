package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: ZoyaViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val currentScreen by viewModel.currentScreen.collectAsState()

        when (currentScreen) {
          is Screen.Welcome -> WelcomeScreen(viewModel)
          is Screen.Dashboard -> DashboardScreen(viewModel)
          is Screen.VoiceAssistant -> VoiceAssistantScreen(viewModel)
          is Screen.Chat -> ChatScreen(viewModel)
          is Screen.Notes -> NotesScreen(viewModel)
          is Screen.Tasks -> TasksScreen(viewModel)
          is Screen.Reminders -> RemindersScreen(viewModel)
          is Screen.Translator -> TranslatorScreen(viewModel)
          is Screen.Tools -> ToolsScreen(viewModel)
          is Screen.CameraVision -> CameraVisionScreen(viewModel)
          is Screen.Settings -> SettingsScreen(viewModel)
        }
      }
    }
  }
}
