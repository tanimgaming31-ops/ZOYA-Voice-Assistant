package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

data class DashboardFeature(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val screen: Screen,
  val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ZoyaViewModel) {
  val selectedLang by viewModel.selectedLanguage.collectAsState()

  val features = listOf(
    DashboardFeature("Voice Assistant", "Live AI Conversation", Icons.Default.Mic, Screen.VoiceAssistant, Color(0xFF8B5CF6)),
    DashboardFeature("Text Chat", "Gemini Real-time Chat", Icons.Default.Chat, Screen.Chat, Color(0xFF06B6D4)),
    DashboardFeature("Camera Vision", "Analyze Photos & Objects", Icons.Default.CameraAlt, Screen.CameraVision, Color(0xFFEC4899)),
    DashboardFeature("Notes", "Personal Voice & Text Notes", Icons.Default.Note, Screen.Notes, Color(0xFF10B981)),
    DashboardFeature("Tasks", "To-Do & Productivity", Icons.Default.CheckCircle, Screen.Tasks, Color(0xFFF59E0B)),
    DashboardFeature("Reminders", "Alarms & Notifications", Icons.Default.Alarm, Screen.Reminders, Color(0xFF6366F1)),
    DashboardFeature("Translator", "Multi-lingual Support", Icons.Default.Translate, Screen.Translator, Color(0xFF3B82F6)),
    DashboardFeature("Tools & Hub", "Weather, Search, Market", Icons.Default.Extension, Screen.Tools, Color(0xFF14B8A6))
  )

  val infiniteTransition = rememberInfiniteTransition(label = "dash")
  val orbScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orbScale"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "ZOYA Hub", fontWeight = FontWeight.Bold)
          }
        },
        actions = {
          TextButton(onClick = { 
            // Simple language toggle cycle
            val next = when (selectedLang) {
              "English" -> "Bengali"
              "Bengali" -> "Hindi"
              "Hindi" -> "Arabic"
              else -> "English"
            }
            viewModel.setLanguage(next)
          }) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = selectedLang, fontSize = 13.sp)
          }
          IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = { Icon(Icons.Default.Home, contentDescription = null) },
          label = { Text("Home") },
          selected = true,
          onClick = {}
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.Mic, contentDescription = null) },
          label = { Text("Voice") },
          selected = false,
          onClick = { viewModel.navigateTo(Screen.VoiceAssistant) }
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.Chat, contentDescription = null) },
          label = { Text("Chat") },
          selected = false,
          onClick = { viewModel.navigateTo(Screen.Chat) }
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.Settings, contentDescription = null) },
          label = { Text("Settings") },
          selected = false,
          onClick = { viewModel.navigateTo(Screen.Settings) }
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      // Hero Banner / Orb Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.navigateTo(Screen.VoiceAssistant) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .scale(orbScale)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Tap to speak with ZOYA",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Ready for live voice & intelligence",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Capabilities & Tools",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(features) { feature ->
          FeatureCard(feature = feature) {
            viewModel.navigateTo(feature.screen)
          }
        }
      }
    }
  }
}

@Composable
fun FeatureCard(feature: DashboardFeature, onClick: () -> Unit) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(feature.color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = feature.icon,
          contentDescription = null,
          tint = feature.color,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = feature.title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = feature.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2
      )
    }
  }
}
