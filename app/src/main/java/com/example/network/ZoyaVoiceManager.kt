package com.example.network

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class AssistantVoiceState {
  IDLE, LISTENING, THINKING, SPEAKING, INTERRUPTED, ERROR
}

class ZoyaVoiceManager(private val context: Context, private val aiService: ZoyaAiService) {
  private val TAG = "ZoyaVoiceManager"

  private val _voiceState = MutableStateFlow(AssistantVoiceState.IDLE)
  val voiceState: StateFlow<AssistantVoiceState> = _voiceState.asStateFlow()

  private val _transcript = MutableStateFlow("Tap microphone to start voice conversation with ZOYA.")
  val transcript: StateFlow<String> = _transcript.asStateFlow()

  private var tts: TextToSpeech? = null
  private var isTtsInitialized = false

  init {
    tts = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        tts?.language = Locale.US
        isTtsInitialized = true
      }
    }
  }

  fun startListening(scope: CoroutineScope, language: String) {
    // Barge-in: stop any ongoing speech immediately
    stopSpeaking()

    _voiceState.value = AssistantVoiceState.LISTENING
    _transcript.value = "Listening..."

    scope.launch(Dispatchers.IO) {
      try {
        // Simulate speech capture or process voice command
        kotlinx.coroutines.delay(2000)
        _voiceState.value = AssistantVoiceState.THINKING
        _transcript.value = "Thinking..."

        val prompt = "Hello ZOYA, assist me with my daily tasks."
        val response = aiService.generateChatResponse(prompt)

        _transcript.value = response
        speakResponse(response)
      } catch (e: Exception) {
        Log.e(TAG, "Voice assistant error", e)
        _voiceState.value = AssistantVoiceState.ERROR
        _transcript.value = "An error occurred during voice processing."
      }
    }
  }

  fun speakResponse(text: String) {
    _voiceState.value = AssistantVoiceState.SPEAKING
    if (isTtsInitialized) {
      tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "zoya_utterance")
    }
  }

  fun stopSpeaking() {
    if (isTtsInitialized) {
      tts?.stop()
    }
    if (_voiceState.value == AssistantVoiceState.SPEAKING) {
      _voiceState.value = AssistantVoiceState.INTERRUPTED
      _transcript.value = "Interrupted. Listening..."
    }
  }

  fun release() {
    tts?.stop()
    tts?.shutdown()
  }
}
