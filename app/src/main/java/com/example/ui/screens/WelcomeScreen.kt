package com.example.ui.screens

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

@Composable
fun WelcomeScreen(viewModel: ZoyaViewModel) {
  val infiniteTransition = rememberInfiniteTransition(label = "orb")
  val scale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF090D16), Color(0xFF131C2E), Color(0xFF090D16))
        )
      )
      .padding(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Glowing Orb
      Box(
        modifier = Modifier
          .size(160.dp)
          .scale(scale)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFF090D16))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Mic,
          contentDescription = "ZOYA Orb",
          tint = Color.White,
          modifier = Modifier.size(64.dp)
        )
      }

      Spacer(modifier = Modifier.height(36.dp))

      Text(
        text = "ZOYA",
        style = MaterialTheme.typography.displayLarge.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 4.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.testTag("app_title")
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Your Personal AI Voice Assistant",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Action Buttons
      Button(
        onClick = { viewModel.navigateTo(Screen.Dashboard) },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("start_zoya_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(imageVector = Icons.Default.Mic, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Start Zoya", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        OutlinedButton(
          onClick = { viewModel.navigateTo(Screen.Chat) },
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("text_chat_button"),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(imageVector = Icons.Default.Chat, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Text Chat")
        }

        OutlinedButton(
          onClick = { viewModel.navigateTo(Screen.Settings) },
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("settings_button"),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(imageVector = Icons.Default.Settings, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Settings")
        }
      }

      Spacer(modifier = Modifier.height(48.dp))

      Text(
        text = "Developed by Tanim",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.testTag("developer_credit")
      )
    }
  }
}
