package com.example.network

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
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

class ZoyaAiService(context: Context) {
  private val TAG = "ZoyaAiService"
  private val client = OkHttpClient()

  private val apiKey: String
    get() {
      // Use the provided API key directly, or fallback to BuildConfig / placeholder
      val providedKey = "AQ.Ab8RN6KQK-9GJZasvBo68jA-5t3TWX3tij7UUiDZospsuPSmdQ"
      return if (providedKey.isNotBlank()) providedKey else "AIzaSyPlaceholder"
    }

  suspend fun generateChatResponse(prompt: String, history: List<Pair<String, String>> = emptyList()): String {
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
        val systemPrompt = "You are ZOYA, Your Personal AI Voice Assistant, developed by Tanim. " +
          "You are friendly, calm, helpful, concise, natural, and multilingual (English, Bengali, Hindi, Arabic). " +
          "Never claim an action was completed unless it actually was."
        partObj.put("text", "$systemPrompt\nUser: $prompt")
        currentParts.put(partObj)
        currentObj.put("parts", currentParts)
        contentsArray.put(currentObj)

        val bodyJson = JSONObject()
        bodyJson.put("contents", contentsArray)

        val requestBody = bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
          .url(url)
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
              return@withContext parts.getJSONObject(0).optString("text", "I'm here to help!")
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
        val request = Request.Builder().url(url).post(requestBody).build()

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
