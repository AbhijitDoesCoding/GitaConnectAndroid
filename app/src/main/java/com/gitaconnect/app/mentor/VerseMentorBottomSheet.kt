package com.gitaconnect.app.mentor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gitaconnect.app.R
import com.gitaconnect.app.library.models.Verse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseMentorBottomSheet(
    verse: Verse,
    onDismiss: () -> Unit
) {
    val viewModel: VerseMentorViewModel = viewModel(
        key = "verse_mentor_${verse.chapter}_${verse.verseId}",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VerseMentorViewModel(verse) as T
            }
        }
    )

    val messages by viewModel.messages.collectAsState()
    val showQuestions by viewModel.showQuestions.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .clickable(enabled = false) {}, // Consume click within Card
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.bg_beige22),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Surface(
                        color = Color(0xFFFFFDF9), // Solid white/off-white top bar
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ask Gita Mentor",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkColor
                                )
                                Text(
                                    text = "Chapter ${verse.chapter}, Verse ${verse.verseId} (${currentLanguage})",
                                    fontSize = 13.sp,
                                    color = TextDarkColor.copy(alpha = 0.6f)
                                )
                            }

                            // Close Button
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDarkColor)
                            }

                            // Refresh Button
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextDarkColor)
                            }

                            // Language Selector
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.Translate, contentDescription = "Translate", tint = TextDarkColor)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    viewModel.languages.forEachIndexed { index, language ->
                                        DropdownMenuItem(
                                            text = { Text(language) },
                                            onClick = {
                                                viewModel.setLanguage(index)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE0D9CC), thickness = 1.dp)

                    // Chat Messages View
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(messages) { message ->
                            MessageBubble(message = message)
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // Pre-generated Questions Stack
                    if (showQuestions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            viewModel.preGeneratedQuestions.forEach { question ->
                                Card(
                                    onClick = { viewModel.sendMessage(question) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFFDF9)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = question,
                                        color = TextDarkColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input Box
                    ChatInputBar(onSendMessage = { text ->
                        viewModel.sendMessage(text)
                    })
                }
            }
        }
    }
}
