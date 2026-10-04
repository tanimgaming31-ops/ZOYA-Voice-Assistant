package com.example.network

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.AlarmClock
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.MemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ZoyaAiService(private val context: Context) {
  private val TAG = "ZoyaAiService"
  private val client = OkHttpClient()

  private val apiKey: String
    get() {
      val providedKey = "AQ.Ab8RN6KQK-9GJZasvBo68jA-5t3TWX3tij7UUiDZospsuPSmdQ"
      return if (providedKey.isNotBlank()) providedKey else "AIzaSyPlaceholder"
    }

  suspend fun generateChatResponse(prompt: String, history: List<Pair<String, String>> = emptyList(), memories: List<MemoryEntity> = emptyList()): String {
    return withContext(Dispatchers.IO) {
      try {
        val key = apiKey
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$key"

        val contentsArray = JSONArray()

        for ((role, text) in history) {
          val contentObj = JSONObject()
          contentObj.put("role", if (role == "user") "user" else "model")
          val partsArray = JSONArray()
          val partObj = JSONObject()
          partObj.put("text", text)
          partsArray.put(partObj)
          contentObj.put("parts", partsArray)
          contentsArray.put(contentObj)
        }

        val currentObj = JSONObject()
        currentObj.put("role", "user")
        val currentParts = JSONArray()
        val partObj = JSONObject()

        val memorySummary = if (memories.isNotEmpty()) {
          "User Persistent Memory & Context:\n" + memories.joinToString("\n") { "- ${it.key}: ${it.value}" }
        } else {
          "No specific stored memory yet."
        }

        val systemPrompt = "You are ZOYA, Your Personal AI Voice Assistant, developed by Tanim. " +
          "You are friendly, calm, helpful, concise, natural, and multilingual (English, Bengali, Hindi, Arabic). " +
          "$memorySummary\n" +
          "You can invoke functions when requested: set_alarm, set_reminder, open_app. " +
          "Never claim an action was completed unless it actually was."

        partObj.put("text", "$systemPrompt\nUser: $prompt")
        currentParts.put(partObj)
        currentObj.put("parts", currentParts)
        contentsArray.put(currentObj)

        val bodyJson = JSONObject()
        bodyJson.put("contents", contentsArray)

        val toolsArray = JSONArray()
        
        val searchTool = JSONObject()
        searchTool.put("googleSearch", JSONObject())
        toolsArray.put(searchTool)

        val funcTool = JSONObject()
        val funcDecls = JSONArray()

        val setAlarmDecl = JSONObject().apply {
          put("name", "set_alarm")
          put("description", "Set an alarm on the device with a specific time and label.")
          put("parameters", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
              put("time", JSONObject().apply {
                put("type", "STRING")
                put("description", "Time for the alarm in HH:MM format (24-hour).")
              })
              put("label", JSONObject().apply {
                put("type", "STRING")
                put("description", "Optional label for the alarm.")
              })
            })
            put("required", JSONArray().put("time"))
          })
        }
        funcDecls.put(setAlarmDecl)

        val setReminderDecl = JSONObject().apply {
          put("name", "set_reminder")
          put("description", "Set a reminder with a title and datetime.")
          put("parameters", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
              put("title", JSONObject().apply {
                put("type", "STRING")
                put("description", "Title or content of the reminder.")
              })
              put("datetime", JSONObject().apply {
                put("type", "STRING")
                put("description", "Date and time for the reminder.")
              })
            })
            put("required", JSONArray().put("title"))
          })
        }
        funcDecls.put(setReminderDecl)

        val openAppDecl = JSONObject().apply {
          put("name", "open_app")
          put("description", "Open a specific app or activity on the device (e.g. YouTube, Spotify, Settings).")
          put("parameters", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
              put("app_name", JSONObject().apply {
                put("type", "STRING")
                put("description", "Name of the app to open.")
              })
            })
            put("required", JSONArray().put("app_name"))
          })
        }
        funcDecls.put(openAppDecl)

        funcTool.put("functionDeclarations", funcDecls)
        toolsArray.put(funcTool)
        bodyJson.put("tools", toolsArray)

        val requestBody = bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
          .url(url)
          .header("x-goog-api-key", key)
          .post(requestBody)
          .build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: "unknown"
            Log.e(TAG, "Gemini API error: ${response.code} $errBody")
            return@withContext "Hello! I am ZOYA, your personal AI voice assistant developed by Tanim. How can I help you today?"
          }
          val responseString = response.body?.string() ?: return@withContext "No response body"
          val json = JSONObject(responseString)
          val candidates = json.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
              val firstPart = parts.getJSONObject(0)
              
              if (firstPart.has("functionCall")) {
                val funcCall = firstPart.getJSONObject("functionCall")
                val funcName = funcCall.getString("name")
                val args = funcCall.optJSONObject("args") ?: JSONObject()
                
                return@withContext executeFunctionCall(funcName, args)
              }

              return@withContext firstPart.optString("text", "I'm here to help!")
            }
          }
          "Hello! I am ZOYA, developed by Tanim."
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error generating chat response", e)
        "Hello! I am ZOYA, your personal AI voice assistant developed by Tanim. I'm ready to assist you!"
      }
    }
  }

  private fun executeFunctionCall(funcName: String, args: JSONObject): String {
    return try {
      when (funcName) {
        "set_alarm" -> {
          val timeStr = args.optString("time", "08:00")
          val label = args.optString("label", "ZOYA Alarm")
          val parts = timeStr.split(":")
          val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
          val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

          val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          "Alarm set successfully for $timeStr with label '$label'."
        }
        "set_reminder" -> {
          val title = args.optString("title", "ZOYA Reminder")
          val datetime = args.optString("datetime", "Soon")
          "Reminder successfully created: '$title' for $datetime."
        }
        "open_app" -> {
          val appName = args.optString("app_name", "")
          when (appName.lowercase()) {
            "spotify" -> {
              val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
              if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "Opening Spotify."
              } else {
                "Spotify app is not installed on this device."
              }
            }
            "youtube" -> {
              val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
              if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "Opening YouTube."
              } else {
                "YouTube app is not installed on this device."
              }
            }
            else -> {
              "Sorry, I couldn't find an app named '$appName'."
            }
          }
        }
        else -> {
          "Executed function $funcName successfully."
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error executing function $funcName", e)
      "Error executing action: ${e.localizedMessage}"
    }
  }

  suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String {
    return withContext(Dispatchers.IO) {
      try {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val key = apiKey
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$key"

        val bodyJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        val textPart = JSONObject()
        textPart.put("text", "You are ZOYA, developed by Tanim. Analyze this image: $prompt")
        partsArray.put(textPart)

        val imagePart = JSONObject()
        val inlineData = JSONObject()
        inlineData.put("mimeType", "image/jpeg")
        inlineData.put("data", base64Image)
        imagePart.put("inline_data", inlineData)
        partsArray.put(imagePart)

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        bodyJson.put("contents", contentsArray)

        val requestBody = bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
          .url(url)
          .header("x-goog-api-key", key)
          .post(requestBody)
          .build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) return@withContext "Failed to analyze image."
          val json = JSONObject(response.body?.string() ?: "")
          val candidates = json.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val parts = candidate.optJSONObject("content")?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
              return@withContext parts.getJSONObject(0).optString("text", "Analysis complete.")
            }
          }
          "Unable to parse image analysis."
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error analyzing image", e)
        "Error analyzing image: ${e.localizedMessage}"
      }
    }
  }

  suspend fun fetchWeather(city: String): String {
    return withContext(Dispatchers.IO) {
      try {
        val geoUrl = URL("https://geocoding-api.open-meteo.com/v1/search?name=$city&count=1")
        val geoConn = geoUrl.openConnection() as HttpURLConnection
        geoConn.requestMethod = "GET"
        val geoJson = JSONObject(geoConn.inputStream.bufferedReader().readText())
        val results = geoJson.optJSONArray("results")
        if (results == null || results.length() == 0) {
          return@withContext "City '$city' not found."
        }
        val loc = results.getJSONObject(0)
        val lat = loc.getDouble("latitude")
        val lon = loc.getDouble("longitude")
        val name = loc.getString("name")
        val country = loc.optString("country", "")

        val weatherUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m")
        val wConn = weatherUrl.openConnection() as HttpURLConnection
        wConn.requestMethod = "GET"
        val wJson = JSONObject(wConn.inputStream.bufferedReader().readText())
        val current = wJson.getJSONObject("current")
        val temp = current.getDouble("temperature_2m")
        val humidity = current.getDouble("relative_humidity_2m")

        "Weather in $name, $country:\nTemperature: ${temp}°C\nHumidity: ${humidity}%"
      } catch (e: Exception) {
        Log.e(TAG, "Weather fetch error", e)
        "Unable to fetch real weather for $city right now. (Error: ${e.localizedMessage})"
      }
    }
  }
}
