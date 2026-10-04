package com.example.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ZoyaCloudSyncManager {
  private val TAG = "ZoyaCloudSyncManager"
  private val firestore = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

  suspend fun syncUserMemoryToCloud(key: String, value: String) {
    val userId = auth.currentUser?.uid ?: return
    try {
      val data = mapOf(
        "key" to key,
        "value" to value,
        "updatedAt" to System.currentTimeMillis()
      )
      firestore.collection("users")
        .document(userId)
        .collection("memory")
        .document(key)
        .set(data)
        .await()
      Log.i(TAG, "Successfully synced memory '$key' to Firestore.")
    } catch (e: Exception) {
      Log.e(TAG, "Error syncing memory to Firestore", e)
    }
  }

  suspend fun syncUserSettingsToCloud(settingsMap: Map<String, Any>) {
    val userId = auth.currentUser?.uid ?: return
    try {
      firestore.collection("users")
        .document(userId)
        .collection("settings")
        .document("preferences")
        .set(settingsMap)
        .await()
      Log.i(TAG, "Successfully synced user settings to Firestore.")
    } catch (e: Exception) {
      Log.e(TAG, "Error syncing settings to Firestore", e)
    }
  }
}
