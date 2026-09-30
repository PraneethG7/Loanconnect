package com.example.ui.screens.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.LoanSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ChatMessage
import java.util.Locale

data class ModelOption(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val color: Color
)

private val availableModels = listOf(
    ModelOption("gemini-3.5-flash", "Gemini 3.5 Flash", "General + Search", "General tasks with Google Search Grounding", LoanPrimary),
    ModelOption("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Deep Audit", "Complex reasoning, financial audit & calculations", WarningAmber),
    ModelOption("gemini-3.1-flash-lite-preview", "Gemini 3.1 Lite", "Ultra Fast", "Fast low-latency Q&A and summaries", InfoCyan),
    ModelOption("gemini-3.8-live", "Gemini 3.8 Live", "Voice API", "Real-time interactive voice conversations", Color(0xFF10B981))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanConnectAiScreen(
    user: UserEntity,
    messages: List<ChatMessage>,
    selectedLanguage: String,
    selectedModel: String = "gemini-3.5-flash",
    isSearchGroundingEnabled: Boolean = true,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectModel: (String) -> Unit = {},
    onToggleSearchGrounding: (Boolean) -> Unit = {},
    onClearChat: () -> Unit = {}
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Live Voice Mode State
    var showLiveVoiceModal by remember { mutableStateOf(false) }
    var voiceTranscript by remember { mutableStateOf("Tap the microphone and speak your question...") }
    var voiceAiReply by remember { mutableStateOf("") }
    var isVoiceSpeaking by remember { mutableStateOf(false) }

    // TTS instance
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val languages = listOf("English", "Hindi", "Tamil", "Telugu", "Kannada", "Malayalam", "Bengali", "Marathi", "Gujarati")

    val quickSuggestions = if (user.role == UserRole.FINANCIER) {
        listOf("Who has not paid?", "Show overdue loans", "How much did I collect today?", "What are current RBI peer lending rules?", "Show commission yield")
    } else {
        listOf("How much do I still owe?", "When is my next payment?", "What is current inflation in India?", "Explain my loan agreement", "How to enable AutoPay?")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .testTag("ai_assistant_screen")
    ) {
        // Header with Live Voice button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = LoanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LoanConnect AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Role: ${if (user.role == UserRole.FINANCIER) "Financier Risk Analyst" else "Borrower Financial Coach"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Voice Live API Button
                FilledTonalButton(
                    onClick = {
                        onSelectModel("gemini-3.8-live")
                        showLiveVoiceModal = true
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voice Live", color = Color(0xFF059669), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onClearChat) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Model Selector Row (gemini-3.1-pro-preview, gemini-3.5-flash, gemini-3.1-flash-lite-preview, gemini-3.8-live)
        Text(
            text = "Gemini Model Selection:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableModels) { opt ->
                val isSelected = selectedModel == opt.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectModel(opt.id)
                        if (opt.id == "gemini-3.8-live") {
                            showLiveVoiceModal = true
                        }
                    },
                    label = { Text(opt.badge, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Search Grounding & Language Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google Search Grounding toggle
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleSearchGrounding(!isSearchGroundingEnabled) },
                color = if (isSearchGroundingEnabled) Color(0xFF3B82F6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        tint = if (isSearchGroundingEnabled) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSearchGroundingEnabled) "Google Search Grounding ON" else "Search Grounding OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSearchGroundingEnabled) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Language Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.widthIn(max = 160.dp)
            ) {
                items(languages) { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { onSelectLanguage(lang) },
                        label = { Text(lang.take(3), fontSize = 10.sp) },
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Suggestion Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickSuggestions) { chipText ->
                SuggestionChip(
                    onClick = { onSendMessage(chipText) },
                    label = { Text(chipText, fontSize = 11.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-Turn Chat Messages List (Maintains Conversation History)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "ai"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    if (isAi) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LoanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = LoanPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        modifier = Modifier.widthIn(max = 300.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAi) 4.dp else 16.dp,
                            bottomEnd = if (isAi) 16.dp else 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAi) MaterialTheme.colorScheme.surfaceVariant else LoanPrimary
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                color = if (isAi) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            if (isAi) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, "msg-${msg.id}")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.VolumeUp,
                                            contentDescription = "Read Aloud",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = LoanPrimary
                        )
                        Text(
                            text = "Analyzing with $selectedModel...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask about loans, EMI, RBI rates...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showLiveVoiceModal = true
                        }
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Live Voice Conversation",
                            tint = Color(0xFF10B981)
                        )
                    }
                }
            )

            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText.trim()
                        inputText = ""
                        onSendMessage(text)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_send_button"),
                shape = CircleShape,
                containerColor = LoanPrimary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // LIVE VOICE CONVERSATION DIALOG (Model gemini-3.8-live)
    if (showLiveVoiceModal) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isVoiceSpeaking) 1.25f else 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Dialog(onDismissRequest = {
            tts?.stop()
            showLiveVoiceModal = false
        }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF022C22))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "GEMINI 3.8 LIVE",
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            IconButton(onClick = {
                                tts?.stop()
                                showLiveVoiceModal = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            }
                        }

                        Text(
                            text = "Live Voice Conversation",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time speech dialogue with Gemini Live API",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        // Animated Pulse Microphone Circle
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF065F46))
                                    )
                                )
                                .clickable {
                                    // Trigger quick voice dialogue prompt
                                    voiceTranscript = "What is my total outstanding loan amount and due date?"
                                    isVoiceSpeaking = true
                                    onSendMessage(voiceTranscript)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Live Voice",
                                tint = Color.White,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        // Status Transcript Box
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Spoken Query:",
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = voiceTranscript,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                val latestAiMsg = messages.lastOrNull { it.sender == "ai" }
                                if (latestAiMsg != null && isVoiceSpeaking) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Live API Voice Output:",
                                        color = Color(0xFFFDE047),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = latestAiMsg.text,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Quick voice prompts
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    voiceTranscript = "Show my active loans and next installment due date."
                                    isVoiceSpeaking = true
                                    onSendMessage(voiceTranscript)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Check Due Date", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    voiceTranscript = "Explain my loan agreement and interest rate breakdown."
                                    isVoiceSpeaking = true
                                    onSendMessage(voiceTranscript)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Explain Terms", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
