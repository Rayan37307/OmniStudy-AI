package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.audio.TtsManager
import com.example.data.model.StudyMaterial

@Composable
fun StudyHubScreen(
    materials: List<StudyMaterial>,
    selectedMaterial: StudyMaterial?,
    ttsManager: TtsManager,
    isActionLoading: Boolean,
    onSelectMaterial: (StudyMaterial?) -> Unit,
    onCreateMaterial: (title: String, subject: String, content: String) -> Unit,
    onImportWikipedia: (topic: String) -> Unit,
    onGenerateDeck: (StudyMaterial) -> Unit,
    onGenerateExam: (StudyMaterial) -> Unit,
    onDeleteMaterial: (Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showWikiDialog by remember { mutableStateOf(false) }
    var coverNotesForTesting by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) } // 0: Cornell Notes, 1: Mind Map, 2: Audio Player

    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val speechRate by ttsManager.speechRate.collectAsState()

    if (selectedMaterial != null) {
        // Detailed Cornell Notes & Mind Map View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onSelectMaterial(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to List")
                    }
                    Column {
                        Text(
                            text = selectedMaterial.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedMaterial.subject,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row {
                    IconButton(onClick = { onGenerateDeck(selectedMaterial) }) {
                        Icon(Icons.Default.Style, contentDescription = "Generate Cards", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onGenerateExam(selectedMaterial) }) {
                        Icon(Icons.Default.Quiz, contentDescription = "Create Exam", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab bar (Cornell Notes / Mind Map / Audio Study Guide)
            TabRow(
                selectedTabIndex = currentTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = { Text("Cornell Notes") }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = { Text("Mind Map") }
                )
                Tab(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    text = { Text("Audio Guide") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (currentTab) {
                0 -> {
                    // Cornell Notes View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (coverNotesForTesting) "Active Recall: Test Cues Mode" else "Study Mode",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (coverNotesForTesting) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // "Cover Notes & Test Cues" active recall toggle
                        FilterChip(
                            selected = coverNotesForTesting,
                            onClick = { coverNotesForTesting = !coverNotesForTesting },
                            label = { Text("Cover Notes & Self-Quiz") },
                            leadingIcon = {
                                Icon(
                                    if (coverNotesForTesting) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Column: Cue Questions / Recall Prompts
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "❓", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Recall Cues & Questions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val cuesList = selectedMaterial.cornellCues.split(";", "\n").filter { it.isNotBlank() }
                                    cuesList.forEachIndexed { i, cue ->
                                        Text(
                                            text = "${i + 1}. ${cue.trim()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Right Column: Detailed Structured Notes (with Cover option for active recall)
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "📝", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Detailed Structured Notes",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (coverNotesForTesting) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .clickable { coverNotesForTesting = false }
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Lock, contentDescription = "Hidden", tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Notes are hidden for Active Recall.\nAnswer the cues above, then tap here to reveal!",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = selectedMaterial.cornellNotes,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Panel: 2-Sentence Synthesis Summary
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "💡", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Executive Summary",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = selectedMaterial.cornellSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Mind Map Visual Hierarchy Viewer
                    MindMapViewer(
                        mermaid = selectedMaterial.mindmapMermaid,
                        title = selectedMaterial.title
                    )
                }

                2 -> {
                    // Audio Study Guide (Android TextToSpeech with rate controls)
                    AudioGuideViewer(
                        material = selectedMaterial,
                        ttsManager = ttsManager,
                        isSpeaking = isSpeaking,
                        speechRate = speechRate
                    )
                }
            }
        }
    } else {
        // Study Materials List & Ingestion View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Smart Study Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cornell Notes, Mind Maps & Multi-Source Ingestion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hub_add_notes_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ingest Notes")
                }

                FilledTonalButton(
                    onClick = { showWikiDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hub_wiki_import_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wiki Topic")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isActionLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Your Materials (${materials.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(materials) { material ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMaterial(material) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = material.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { onDeleteMaterial(material.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Material",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = material.subject,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = material.cornellSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onGenerateDeck(material) }) {
                                    Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Flashcards")
                                }
                                TextButton(onClick = { onGenerateExam(material) }) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Exam")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Ingest Notes Dialog
    if (showCreateDialog) {
        CreateMaterialModal(
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, subject, content ->
                onCreateMaterial(title, subject, content)
                showCreateDialog = false
            }
        )
    }

    // Wikipedia Topic Import Dialog
    if (showWikiDialog) {
        WikipediaImportModal(
            onDismiss = { showWikiDialog = false },
            onImport = { topic ->
                onImportWikipedia(topic)
                showWikiDialog = false
            }
        )
    }
}

@Composable
fun MindMapViewer(mermaid: String, title: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visual Concept Hierarchy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(onClick = {}, label = { Text("Mermaid.js") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph representation nodes
            val parsedNodes = remember(mermaid) {
                mermaid.lines()
                    .filter { it.contains("-->") || it.contains("->") }
                    .map { line ->
                        val parts = if (line.contains("-->")) line.split("-->") else line.split("->")
                        parts[0].trim().removePrefix("graph TD").trim() to parts.getOrElse(1) { "" }.trim()
                    }
                    .filter { it.first.isNotBlank() && it.second.isNotBlank() }
            }

            if (parsedNodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Core Concept Tree:\n\n$title\n  ↳ Core Mechanisms\n    ↳ Practical Applications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(parsedNodes) { (from, to) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = from,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Relates to",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = to,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioGuideViewer(
    material: StudyMaterial,
    ttsManager: TtsManager,
    isSpeaking: Boolean,
    speechRate: Float
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🎧", fontSize = 34.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Audio Study Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = material.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Play / Stop Button
            FilledIconButton(
                onClick = {
                    if (isSpeaking) {
                        ttsManager.stop()
                    } else {
                        val speechScript = "${material.title}. In ${material.subject}. Summary: ${material.cornellSummary}. Key concepts: ${material.cornellNotes}"
                        ttsManager.speak(speechScript)
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isSpeaking) "Stop Audio" else "Play Audio",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Speed Control
            Text(
                text = "Playback Speed: ${speechRate}x",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { rate ->
                    FilterChip(
                        selected = speechRate == rate,
                        onClick = { ttsManager.setRate(rate) },
                        label = { Text("${rate}x") }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateMaterialModal(onDismiss: () -> Unit, onSubmit: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Ingest Raw Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI will automatically structure Cornell cues, notes, and mind maps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic / Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Biology, Physics)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Paste raw notes or lecture transcript...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(title, subject, content) },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text("Transform with AI")
                    }
                }
            }
        }
    }
}

@Composable
fun WikipediaImportModal(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var topic by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Wikipedia REST Import",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fetch verified background context and auto-generate Cornell notes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic (e.g. Quantum Computing, Photosynthesis)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Quick Presets:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Entropy", "Photosynthesis", "Neural network").forEach { preset ->
                        SuggestionChip(
                            onClick = { topic = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onImport(topic) },
                        enabled = topic.isNotBlank()
                    ) {
                        Text("Fetch & Synthesize")
                    }
                }
            }
        }
    }
}
