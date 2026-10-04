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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraVisionScreen(viewModel: ZoyaViewModel) {
  val context = LocalContext.current
  var prompt by remember { mutableStateOf("What is in this image?") }
  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

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

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Camera & Vision AI") },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Button(
          onClick = { cameraLauncher.launch(null) },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Take Photo")
        }

        OutlinedButton(
          onClick = { galleryLauncher.launch("image/*") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Photo, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Gallery")
        }
      }

      OutlinedTextField(
        value = prompt,
        onValueChange = { prompt = it },
        placeholder = { Text("Ask anything about the image...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      if (capturedBitmap != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Image captured successfully. Ready to analyze with Gemini.")
          }
        }

        Button(
          onClick = {
            capturedBitmap?.let {
              viewModel.analyzeCameraImage(it, prompt)
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
          Text(text = "No image selected yet. Take a photo or choose from gallery.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}
