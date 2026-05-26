package com.gitaconnect.app.mentor

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gitaconnect.app.R
import kotlinx.coroutines.launch

val BackgroundCream = Color(0xFFF2ECE0)
val AIBubbleColor = Color(0xFFE3C683)
val UserBubbleColor = Color(0xFFDCDCDC)
val TextDarkColor = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorScreen(modifier: Modifier = Modifier, viewModel: MentorViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_beige22),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Gita Mentor",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkColor
                            )
                            Text(
                                text = "Seek Wisdom Now",
                                fontSize = 14.sp,
                                color = TextDarkColor.copy(alpha = 0.7f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextDarkColor)
                        }
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFDF9))
                )
                HorizontalDivider(color = Color(0xFFE0D9CC), thickness = 1.dp)
            }
        },
        bottomBar = {
            ChatInputBar(onSendMessage = { text ->
                viewModel.sendMessage(text)
            })
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(messages) { message ->
                MessageBubble(message = message)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
    }
}

@Composable
fun MessageBubble(message: Message) {
    if (message.isThinking) {
        AIThinkingBubble()
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isFromUser) {
            Image(
                painter = painterResource(id = R.drawable.ai_pandit),
                contentDescription = "AI Pandit",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (message.isFromUser) UserBubbleColor else AIBubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = TextDarkColor,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun AIThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.ai_pandit),
            contentDescription = "AI Pandit",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 100.dp)
                .background(
                    color = AIBubbleColor,
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DotAnimation(delay = 0)
                DotAnimation(delay = 150)
                DotAnimation(delay = 300)
            }
        }
    }
}

@Composable
fun DotAnimation(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotAnimation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color = TextDarkColor.copy(alpha = alpha), shape = CircleShape)
    )
}

@Composable
fun ChatInputBar(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFDF9), shape = RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, Color(0xFFE8DFC8)), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textStyle = TextStyle(color = TextDarkColor, fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                }),
                cursorBrush = SolidColor(TextDarkColor),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Ask Questions to AI Pandit", color = Color.Gray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )

            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Mic",
                tint = TextDarkColor,
                modifier = Modifier.padding(end = 12.dp).size(24.dp)
            )

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = TextDarkColor,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        if (text.isNotBlank()) {
                            onSendMessage(text)
                            text = ""
                        }
                    }
            )
        }
    }
}
