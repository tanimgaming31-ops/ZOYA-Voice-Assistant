package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MemoryEntity
import com.example.data.NoteEntity
import com.example.data.ReminderEntity
import com.example.data.TaskEntity
import com.example.data.ZoyaDatabase
import com.example.network.ZoyaAiService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class Screen {
  object Welcome : Screen()
  object Dashboard : Screen()
  object VoiceAssistant : Screen()
  object Chat : Screen()
  object Notes : Screen()
  object Tasks : Screen()
  object Reminders : Screen()
  object Translator : Screen()
  object Tools : Screen()
  object CameraVision : Screen()
  object Settings : Screen()
  object MemoryManager : Screen()
}

enum class VoiceState {
  IDLE, CONNECTING, LISTENING, THINKING, SPEAKING, INTERRUPTED, RECONNECTING, ERROR
}

data class ChatMessage(
  val id: Long = System.currentTimeMillis(),
  val text: String,
  val isUser: Boolean,
  val timestamp: Long = System.currentTimeMillis()
)

class ZoyaViewModel(application: Application) : AndroidViewModel(application) {
  private val database = ZoyaDatabase.getDatabase(application)
  private val aiService = ZoyaAiService(application)

  private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.IDLE)
  val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

  private val _selectedLanguage = MutableStateFlow<String>("English")
  val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(text = "Hello! I am ZOYA, your personal AI voice assistant developed by Tanim. How can I help you today?", isUser = false)
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  val notes: StateFlow<List<NoteEntity>> = database.noteDao().getAllNotes()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val tasks: StateFlow<List<TaskEntity>> = database.taskDao().getAllTasks()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val reminders: StateFlow<List<ReminderEntity>> = database.reminderDao().getAllReminders()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val memory: StateFlow<List<MemoryEntity>> = database.memoryDao().getAllMemory()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun navigateTo(screen: Screen) {
    _currentScreen.value = screen
  }

  fun setLanguage(lang: String) {
    _selectedLanguage.value = lang
  }

  fun setVoiceState(state: VoiceState) {
    _voiceState.value = state
  }

  fun sendTextMessage(text: String) {
    if (text.isBlank()) return
    val userMsg = ChatMessage(text = text, isUser = true)
    _chatMessages.value = _chatMessages.value + userMsg

    viewModelScope.launch {
      _isLoading.value = true
      _voiceState.value = VoiceState.THINKING

      val lower = text.lowercase()
      val responseText = when {
        lower.contains("weather in") -> {
          val city = text.substringAfter("weather in").trim().removeSuffix("?")
          aiService.fetchWeather(city.ifEmpty { "London" })
        }
        lower.contains("create note") || lower.contains("remember that") -> {
          val content = text.replace("create note", "").replace("remember that", "").trim()
          database.noteDao().insertNote(NoteEntity(title = "Voice Note", content = content))
          "Note successfully created and saved to your personal memory database."
        }
        lower.contains("create task") || lower.contains("add task") -> {
          val taskTitle = text.replace("create task", "").replace("add task", "").trim()
          database.taskDao().insertTask(TaskEntity(title = taskTitle))
          "Task successfully created and scheduled."
        }
        else -> {
          val history = _chatMessages.value.takeLast(6).map { 
            (if (it.isUser) "user" else "model") to it.text 
          }
          aiService.generateChatResponse(text, history, memory.value)
        }
      }

      val assistantMsg = ChatMessage(text = responseText, isUser = false)
      _chatMessages.value = _chatMessages.value + assistantMsg
      _isLoading.value = false
      _voiceState.value = VoiceState.SPEAKING
    }
  }

  fun analyzeCameraImage(bitmap: Bitmap, prompt: String) {
    viewModelScope.launch {
      _isLoading.value = true
      _voiceState.value = VoiceState.THINKING
      val result = aiService.analyzeImage(bitmap, prompt)
      val msg = ChatMessage(text = "[Vision Analysis]: $result", isUser = false)
      _chatMessages.value = _chatMessages.value + msg
      _isLoading.value = false
      _voiceState.value = VoiceState.IDLE
      navigateTo(Screen.Chat)
    }
  }

  fun addNote(title: String, content: String, category: String = "General") {
    viewModelScope.launch {
      database.noteDao().insertNote(NoteEntity(title = title, content = content, category = category))
    }
  }

  fun deleteNote(id: Long) {
    viewModelScope.launch {
      database.noteDao().deleteNote(id)
    }
  }

  fun addTask(title: String, description: String, dueDate: String, priority: String) {
    viewModelScope.launch {
      database.taskDao().insertTask(TaskEntity(title = title, description = description, dueDate = dueDate, priority = priority))
    }
  }

  fun updateTaskCompletion(task: TaskEntity, isCompleted: Boolean) {
    viewModelScope.launch {
      database.taskDao().updateTask(task.copy(isCompleted = isCompleted))
    }
  }

  fun deleteTask(id: Long) {
    viewModelScope.launch {
      database.taskDao().deleteTask(id)
    }
  }

  fun addReminder(title: String, dateTime: String) {
    viewModelScope.launch {
      database.reminderDao().insertReminder(ReminderEntity(title = title, dateTime = dateTime))
    }
  }

  fun deleteReminder(id: Long) {
    viewModelScope.launch {
      database.reminderDao().deleteReminder(id)
    }
  }

  fun saveMemory(key: String, value: String) {
    viewModelScope.launch {
      database.memoryDao().setMemory(MemoryEntity(key = key, value = value))
    }
  }

  fun deleteMemory(key: String) {
    viewModelScope.launch {
      database.memoryDao().deleteMemory(key)
    }
  }

  fun clearAllMemory() {
    viewModelScope.launch {
      database.memoryDao().clearAllMemory()
    }
  }

  fun clearChat() {
    _chatMessages.value = listOf(
      ChatMessage(text = "Chat cleared. How else can I assist you?", isUser = false)
    )
  }
}
