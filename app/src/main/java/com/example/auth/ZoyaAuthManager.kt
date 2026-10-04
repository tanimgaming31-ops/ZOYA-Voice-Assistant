package com.example.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ZoyaAuthManager(private val context: Context) {
  private val TAG = "ZoyaAuthManager"
  private val auth = FirebaseAuth.getInstance()

  private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  init {
    auth.addAuthStateListener { firebaseAuth ->
      _currentUser.value = firebaseAuth.currentUser
    }
  }

  fun signOut() {
    auth.signOut()
    _currentUser.value = null
    Log.i(TAG, "User signed out successfully.")
  }

  fun isUserAuthenticated(): Boolean {
    return auth.currentUser != null
  }
}
