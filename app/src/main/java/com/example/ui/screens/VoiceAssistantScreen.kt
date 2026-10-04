package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.network.AssistantVoiceState
import com.example.network.ZoyaVoiceManager
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(viewModel: ZoyaViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val language by viewModel.selectedLanguage.collectAsState()

  val voiceManager = remember {
    com.example.network.ZoyaVoiceManager(context, com.example.network.ZoyaAiService(context))
  }

  val voiceState by voiceManager.voiceState.collectAsState()
  val transcript by voiceManager.transcript.collectAsState()

  var hasPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasPermission = granted
    if (granted) {
      voiceManager.startListening(coroutineScope, language)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      voiceManager.release()
    }
  }

  // Glowing Halo Animations for Audio Visualizer
  val infiniteTransition = rememberInfiniteTransition(label = "audioVisualizer")
  
  val haloScale1 by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "haloScale1"
  )

  val haloScale2 by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "haloScale2"
  )

  val orbScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orbScale"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Live Audio Visualizer", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { 
            voiceManager.release()
            viewModel.navigateTo(Screen.Dashboard) 
          }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF090D16))
          )
        )
        .padding(paddingValues)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
      ) {
        // Status badge
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                  when (voiceState) {
                    AssistantVoiceState.LISTENING -> Color(0xFF10B981)
                    AssistantVoiceState.THINKING -> Color(0xFFF59E0B)
                    AssistantVoiceState.SPEAKING -> Color(0xFF8B5CF6)
                    AssistantVoiceState.INTERRUPTED -> Color(0xFFEF4444)
                    else -> Color(0xFF06B6D4)
                  }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = voiceState.name,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }

        // Central Glowing Audio Visualizer & Orb
        Box(
          modifier = Modifier
            .size(280.dp),
          contentAlignment = Alignment.Center
        ) {
          // Outer Halo Ring 2 (Active during Listening / Speaking)
          if (voiceState == AssistantVoiceState.LISTENING || voiceState == AssistantVoiceState.SPEAKING) {
            Box(
              modifier = Modifier
                .size(260.dp)
                .scale(haloScale2)
                .clip(CircleShape)
                .background(
                  Brush.radialGradient(
                    listOf(
                      if (voiceState == AssistantVoiceState.LISTENING) Color(0xFF10B981).copy(alpha = 0.25f)
                      else Color(0xFF8B5CF6).copy(alpha = 0.25f),
                      Color.Transparent
                    )
                  )
                )
            )

            // Outer Halo Ring 1
            Box(
              modifier = Modifier
                .size(220.dp)
                .scale(haloScale1)
                .clip(CircleShape)
                .background(
                  Brush.radialGradient(
                    listOf(
                      if (voiceState == AssistantVoiceState.LISTENING) Color(0xFF06B6D4).copy(alpha = 0.4f)
                      else Color(0xFFEC4899).copy(alpha = 0.4f),
                      Color.Transparent
                    )
                  )
                )
            )
          }

          // Central Dynamic Orb
          Box(
            modifier = Modifier
              .size(180.dp)
              .scale(if (voiceState == AssistantVoiceState.LISTENING || voiceState == AssistantVoiceState.SPEAKING) orbScale else 1f)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  when (voiceState) {
                    AssistantVoiceState.LISTENING -> listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF065F46))
                    AssistantVoiceState.THINKING -> listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFB45309))
                    AssistantVoiceState.SPEAKING -> listOf(Color(0xFFA78BFA), Color(0xFF7C3AED), Color(0xFF5B21B6))
                    else -> listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFF4C1D95))
                  }
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (voiceState) {
                AssistantVoiceState.LISTENING -> Icons.Default.Mic
                AssistantVoiceState.THINKING -> Icons.Default.HourglassEmpty
                AssistantVoiceState.SPEAKING -> Icons.Default.VolumeUp
                else -> Icons.Default.MicNone
              },
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(64.dp)
            )
          }
        }

        // Live Transcript / Feedback
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = transcript,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Barge-in enabled: Tap mic or speak to interrupt ZOYA.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
          )
        }

        // Bottom Controls (Barge-in & Mic)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { voiceManager.stopSpeaking() },
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Icon(Icons.Default.Stop, contentDescription = "Interrupt / Stop", tint = Color.White)
          }

          FloatingActionButton(
            onClick = {
              if (hasPermission) {
                voiceManager.startListening(coroutineScope, language)
              } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            },
            modifier = Modifier.size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
          ) {
            Icon(
              imageVector = if (voiceState == AssistantVoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = "Toggle Mic",
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }

          IconButton(
            onClick = { 
              voiceManager.release()
              viewModel.navigateTo(Screen.Chat) 
            },
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Icon(Icons.Default.Chat, contentDescription = "Switch to Text", tint = Color.White)
          }
        }
      }
    }
  }
}
