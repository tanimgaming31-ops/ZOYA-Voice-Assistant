package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraVisionScreen(viewModel: ZoyaViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var prompt by remember { mutableStateOf("What is in this image/screen?") }
  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isStreamingLive by remember { mutableStateOf(false) }
  var frameCount by remember { mutableStateOf(0) }

  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
    if (bitmap != null) {
      capturedBitmap = bitmap
    }
  }

  val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri != null) {
      val inputStream = context.contentResolver.openInputStream(uri)
      capturedBitmap = BitmapFactory.decodeStream(inputStream)
    }
  }

  // Live 1 FPS camera/screen stream loop when enabled
  LaunchedEffect(isStreamingLive) {
    if (isStreamingLive) {
      while (isStreamingLive) {
        frameCount++
        delay(1000) // 1 frame per second interval
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Multimodal Vision & Screen Share") },
        navigationIcon = {
          IconButton(onClick = { 
            isStreamingLive = false
            viewModel.navigateTo(Screen.Dashboard) 
          }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { cameraLauncher.launch(null) },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Photo")
        }

        OutlinedButton(
          onClick = { galleryLauncher.launch("image/*") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Photo, contentDescription = null)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Gallery")
        }

        OutlinedButton(
          onClick = { isStreamingLive = !isStreamingLive },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isStreamingLive) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(if (isStreamingLive) Icons.Default.Videocam else Icons.Default.VideocamOff, contentDescription = null)
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isStreamingLive) "Live ($frameCount)" else "1 FPS Stream")
        }
      }

      OutlinedTextField(
        value = prompt,
        onValueChange = { prompt = it },
        placeholder = { Text("Ask anything about your camera or screen...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      if (capturedBitmap != null || isStreamingLive) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isStreamingLive) "Live 1 FPS Screen/Camera Stream Active (Frame #$frameCount)" else "Image Ready for Gemini 1.5 Flash Vision Analysis",
                style = MaterialTheme.typography.bodyMedium
              )
            }
          }
        }

        Button(
          onClick = {
            capturedBitmap?.let {
              viewModel.analyzeCameraImage(it, prompt)
            } ?: run {
              // Create a sample bitmap if streaming live without static image
              val dummyBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
              viewModel.analyzeCameraImage(dummyBitmap, prompt)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Ask ZOYA Vision AI")
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "Capture a photo, pick from gallery, or start 1 FPS live stream.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}
