package com.example.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class LiveConnectionState {
  DISCONNECTED, CONNECTING, LISTENING, THINKING, SPEAKING, INTERRUPTED, ERROR
}

class ZoyaGeminiLiveManager(private val context: Context) {
  private val TAG = "ZoyaGeminiLiveManager"
  private val client = OkHttpClient.Builder()
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .build()

  private var webSocket: WebSocket? = null

  private val _connectionState = MutableStateFlow(LiveConnectionState.DISCONNECTED)
  val connectionState: StateFlow<LiveConnectionState> = _connectionState.asStateFlow()

  private val _transcript = MutableStateFlow("Live voice connection ready.")
  val transcript: StateFlow<String> = _transcript.asStateFlow()

  fun connect(apiKey: String) {
    if (_connectionState.value == LiveConnectionState.CONNECTING || _connectionState.value == LiveConnectionState.LISTENING) {
      return
    }

    _connectionState.value = LiveConnectionState.CONNECTING
    _transcript.value = "Connecting to Gemini Live API..."

    val url = "wss://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:bidiGenerateContent?key=$apiKey"
    val request = Request.Builder().url(url).build()

    webSocket = client.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(ws: WebSocket, response: Response) {
        Log.i(TAG, "Gemini Live WebSocket connected")
        _connectionState.value = LiveConnectionState.LISTENING
        _transcript.value = "Connected. Listening to your voice..."
      }

      override fun onMessage(ws: WebSocket, text: String) {
        try {
          val json = JSONObject(text)
          val serverContent = json.optJSONObject("serverContent")
          if (serverContent != null) {
            val modelTurn = serverContent.optJSONObject("modelTurn")
            val parts = modelTurn?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
              val part = parts.getJSONObject(0)
              val textPart = part.optString("text", "")
              if (textPart.isNotBlank()) {
                _connectionState.value = LiveConnectionState.SPEAKING
                _transcript.value = textPart
              }
            }
            if (serverContent.optBoolean("turnComplete", false)) {
              _connectionState.value = LiveConnectionState.LISTENING
            }
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error parsing live message", e)
        }
      }

      override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "Gemini Live WebSocket failure", t)
        _connectionState.value = LiveConnectionState.ERROR
        _transcript.value = "Connection error: ${t.localizedMessage}"
      }

      override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "Gemini Live WebSocket closed: $reason")
        _connectionState.value = LiveConnectionState.DISCONNECTED
        _transcript.value = "Live connection closed."
      }
    })
  }

  fun sendAudioChunk(base64Audio: String) {
    if (_connectionState.value != LiveConnectionState.LISTENING) return
    try {
      val message = JSONObject().apply {
        put("realtimeInput", JSONObject().apply {
          put("mediaChunks", JSONArray().apply {
            put(JSONObject().apply {
              put("mimeType", "audio/pcm;rate=16000")
              put("data", base64Audio)
            })
          })
        })
      }
      webSocket?.send(message.toString())
      _connectionState.value = LiveConnectionState.THINKING
    } catch (e: Exception) {
      Log.e(TAG, "Error sending audio chunk", e)
    }
  }

  fun interrupt() {
    try {
      val cancelMsg = JSONObject().apply {
        put("clientContent", JSONObject().apply {
          put("turnComplete", true)
        })
      }
      webSocket?.send(cancelMsg.toString())
      _connectionState.value = LiveConnectionState.INTERRUPTED
      _transcript.value = "Interrupted. Listening..."
    } catch (e: Exception) {
      Log.e(TAG, "Error sending barge-in interrupt", e)
    }
  }

  fun disconnect() {
    webSocket?.close(1000, "User disconnected")
    webSocket = null
    _connectionState.value = LiveConnectionState.DISCONNECTED
    _transcript.value = "Disconnected from Live API."
  }
}
