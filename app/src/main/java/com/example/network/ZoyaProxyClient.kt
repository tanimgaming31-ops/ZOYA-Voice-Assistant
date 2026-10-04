package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ZoyaProxyClient {
  private val TAG = "ZoyaProxyClient"
  private val client = OkHttpClient()
  private val backendUrl = "https://zoya-backend-proxy.onrender.com" // Configurable backend proxy URL

  suspend fun fetchEphemeralSessionToken(): String? {
    return withContext(Dispatchers.IO) {
      try {
        val request = Request.Builder()
          .url("$backendUrl/api/token")
          .post("{}".toRequestBody())
          .build()

        client.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val tokenObj = json.optJSONObject("token")
            return@withContext tokenObj?.optString("sessionId")
          }
        }
        null
      } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch ephemeral token from backend proxy", e)
        null
      }
    }
  }
}
