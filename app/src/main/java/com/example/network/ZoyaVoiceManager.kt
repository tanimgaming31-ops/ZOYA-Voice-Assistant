package com.example.network

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class AssistantVoiceState {
  IDLE, LISTENING, THINKING, SPEAKING, INTERRUPTED, ERROR
}

class ZoyaVoiceManager(private val context: Context, private val aiService: ZoyaAiService) : TextToSpeech.OnInitListener {
  private val TAG = "ZoyaVoiceManager"

  private val _voiceState = MutableStateFlow(AssistantVoiceState.IDLE)
  val voiceState: StateFlow<AssistantVoiceState> = _voiceState.asStateFlow()

  private val _transcript = MutableStateFlow("Tap the microphone and speak to ZOYA.")
  val transcript: StateFlow<String> = _transcript.asStateFlow()

  private var tts: TextToSpeech? = null
  private var isTtsInitialized = false
  private var isRecording = false
  private var recordingJob: Job? = null

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  init {
    tts = TextToSpeech(context, this)
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = tts?.setLanguage(Locale.US)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e(TAG, "TTS Language not supported")
      } else {
        isTtsInitialized = true
        Log.i(TAG, "TTS initialized successfully")
      }
    } else {
      Log.e(TAG, "TTS initialization failed")
    }
  }

  fun startListening(scope: CoroutineScope, language: String = "English") {
    if (_voiceState.value == AssistantVoiceState.LISTENING || _voiceState.value == AssistantVoiceState.THINKING) {
      return
    }

    stopSpeaking()

    _voiceState.value = AssistantVoiceState.LISTENING
    _transcript.value = "Listening..."

    isRecording = true
    recordingJob = scope.launch(Dispatchers.IO) {
      recordAndSendAudio(language)
    }
  }

  private suspend fun recordAndSendAudio(language: String) {
    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

    var audioRecord: AudioRecord? = null
    try {
      audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
      )

      if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
        throw IllegalStateException("AudioRecord initialization failed.")
      }

      audioRecord.startRecording()
      val pcmData = java.io.ByteArrayOutputStream()
      val buffer = ByteArray(bufferSize)
      val startTime = System.currentTimeMillis()

      while (isRecording && (System.currentTimeMillis() - startTime < 4000)) {
        val read = audioRecord.read(buffer, 0, buffer.size)
        if (read > 0) {
          pcmData.write(buffer, 0, read)
        }
        delay(50)
      }

      audioRecord.stop()
      audioRecord.release()
      audioRecord = null

      val bytes = pcmData.toByteArray()
      if (bytes.isNotEmpty()) {
        _voiceState.value = AssistantVoiceState.THINKING
        _transcript.value = "Thinking..."
        sendAudioToGeminiRest(bytes, language)
      } else {
        _voiceState.value = AssistantVoiceState.IDLE
        _transcript.value = "No audio captured. Tap mic to try again."
      }

    } catch (e: Exception) {
      Log.e(TAG, "Error in audio recording", e)
      audioRecord?.release()
      _voiceState.value = AssistantVoiceState.ERROR
      _transcript.value = "Microphone error: ${e.localizedMessage}"
    }
  }

  private fun sendAudioToGeminiRest(pcmBytes: ByteArray, language: String) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val base64Audio = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
        val apiKey = "AQ.Ab8RN6KQK-9GJZasvBo68jA-5t3TWX3tij7UUiDZospsuPSmdQ"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
          put("contents", JSONArray().apply {
            put(JSONObject().apply {
              put("parts", JSONArray().apply {
                put(JSONObject().apply {
                  put("text", "Listen to this audio prompt from the user. Respond conversationally and concisely in $language as ZOYA, a helpful AI assistant developed by Tanim.")
                })
                put(JSONObject().apply {
                  put("inlineData", JSONObject().apply {
                    put("mimeType", "audio/pcm;rate=16000")
                    put("data", base64Audio)
                  })
                })
              })
            })
          })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: "Unknown error"
            throw Exception("Gemini API error (${response.code}): $errBody")
          }

          val responseString = response.body?.string() ?: ""
          val jsonResp = JSONObject(responseString)
          val candidates = jsonResp.optJSONArray("candidates")
          var replyText = "I heard you, but received no response."

          if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
              replyText = parts.getJSONObject(0).optString("text", replyText)
            }
          }

          _transcript.value = replyText
          speakResponse(replyText)
        }

      } catch (e: Exception) {
        Log.e(TAG, "Error sending audio to Gemini REST", e)
        _voiceState.value = AssistantVoiceState.ERROR
        _transcript.value = "AI request failed: ${e.localizedMessage}"
      }
    }
  }

  private fun speakResponse(text: String) {
    _voiceState.value = AssistantVoiceState.SPEAKING
    if (isTtsInitialized && tts != null) {
      tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ZOYA_TTS")
    } else {
      Log.w(TAG, "TTS not initialized, displaying text only")
      _voiceState.value = AssistantVoiceState.IDLE
    }
  }

  fun stopSpeaking() {
    isRecording = false
    recordingJob?.cancel()
    if (tts?.isSpeaking == true) {
      tts?.stop()
    }
    _voiceState.value = AssistantVoiceState.INTERRUPTED
    _transcript.value = "Interrupted. Tap mic to speak."
  }

  fun release() {
    shutdown()
  }

  fun shutdown() {
    stopSpeaking()
    tts?.shutdown()
    tts = null
  }
}
