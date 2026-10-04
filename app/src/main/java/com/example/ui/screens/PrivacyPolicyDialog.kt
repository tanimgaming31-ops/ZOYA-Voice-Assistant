package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyDialog(
  onAccept: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
    title = { Text("Privacy Policy & Permissions Consent") },
    text = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "ZOYA AI Assistant (Developed by Tanim) requires access to your microphone and camera to provide real-time voice conversation and multimodal vision features.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          text = "• Audio and camera feeds are processed securely via encrypted APIs and are never sold or shared.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "• You can manage, view, or delete your persistent memory and preferences at any time in the Memory Manager settings.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onAccept,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Accept & Continue")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Decline")
      }
    }
  )
}
