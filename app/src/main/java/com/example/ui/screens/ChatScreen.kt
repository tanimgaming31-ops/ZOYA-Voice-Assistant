package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.Screen
import com.example.viewmodel.ZoyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ZoyaViewModel) {
  val messages by viewModel.chatMessages.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  var inputQuery by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("ZOYA Text Chat", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { viewModel.clearChat() }) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    bottomBar = {
      Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputQuery,
            onValueChange = { inputQuery = it },
            placeholder = { Text("Ask ZOYA anything...") },
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input"),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3
          )

          Spacer(modifier = Modifier.width(8.dp))

          FloatingActionButton(
            onClick = {
              if (inputQuery.isNotBlank()) {
                viewModel.sendTextMessage(inputQuery)
                inputQuery = ""
              }
            },
            modifier = Modifier
              .size(50.dp)
              .testTag("send_button"),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
          ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
          }
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
      ) {
        items(messages) { msg ->
          ChatBubble(message = msg)
        }

        if (isLoading) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start
            ) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(text = "ZOYA is thinking...", fontSize = 14.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ChatBubble(message: ChatMessage) {
  val alignment = if (message.isUser) Alignment.End else Alignment.Start
  val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = alignment
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (message.isUser) 16.dp else 4.dp,
        bottomEnd = if (message.isUser) 4.dp else 16.dp
      ),
      color = bubbleColor,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Text(
        text = message.text,
        color = textColor,
        modifier = Modifier.padding(12.dp),
        fontSize = 15.sp
      )
    }
  }
}
