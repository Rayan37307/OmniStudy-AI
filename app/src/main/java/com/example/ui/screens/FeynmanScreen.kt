package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ai.FeynmanEvaluation
import com.example.data.model.FeynmanSession
import com.example.ui.util.AppLanguage

@Composable
fun FeynmanScreen(
    sessions: List<FeynmanSession>,
    isActionLoading: Boolean,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onEvaluateExplanation: (concept: String, persona: String, explanation: String, onResult: (FeynmanEvaluation) -> Unit) -> Unit
) {
    val isBn = currentLanguage == AppLanguage.BENGALI
    var concept by remember { mutableStateOf("") }
    var selectedPersona by remember { mutableStateOf("5-Year-Old") }
    var explanation by remember { mutableStateOf("") }
    var currentEvaluation by remember { mutableStateOf<FeynmanEvaluation?>(null) }

    // Speech-To-Text Launcher (supports Bengali speech!)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = spoken?.firstOrNull()
            if (!text.isNullOrBlank()) {
                explanation = if (explanation.isBlank()) text else "$explanation $text"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = if (isBn) "Feynman Technique AI Coach" else "The Feynman Technique Coach",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBn) "যেকোনো কঠিন Topic সহজ ভাষায় Explain করো। AI তোমার Jargon ও Concept Gaps ধরিয়ে দেবে।" else "Teach a concept simply. Gemini AI identifies jargon, gaps & Socratic nudges.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Explanation Studio
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isBn) "1. Target Concept & Audience" else "1. Target Concept & Audience",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = {
                            Text(if (isBn) "যে Concept-টা explain করতে চাও (e.g. Gravity, Inflation)" else "Concept to explain (e.g. Gravity, Heuristics, Inflation)")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feynman_concept_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBn) "Target Audience (কাকে বোঝাবে):" else "Target Persona Audience:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "5-Year-Old" to (if (isBn) "🧒 5-Year-Old" else "🧒 5-Year-Old"),
                            "High School Peer" to (if (isBn) "🧑‍🎓 Peer / Friend" else "🧑‍🎓 Peer"),
                            "Strict Professor" to (if (isBn) "👨‍🏫 Professor" else "👨‍🏫 Professor")
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedPersona == key,
                                onClick = { selectedPersona = key },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "2. Your Simple Explanation" else "2. Your Simple Explanation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Voice STT button with Bengali voice support
                        FilledTonalIconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "নিজের ভাষায় সহজ করে বলো..." else "Explain the concept in your own words...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        label = {
                            Text(if (isBn) "Notes না দেখে নিজের ভাষায় সহজ করে লেখো বা Voice দিয়ে বলো..." else "Explain without checking notes. Avoid raw buzzwords...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("feynman_explanation_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onEvaluateExplanation(concept, selectedPersona, explanation) { result ->
                                currentEvaluation = result
                            }
                        },
                        enabled = concept.isNotBlank() && explanation.isNotBlank() && !isActionLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feynman_evaluate_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "AI Audit শুরু করো (Free)" else "Audit with Feynman AI (Free)")
                        }
                    }
                }
            }
        }

        // Live Evaluation Result Card
        if (currentEvaluation != null) {
            val eval = currentEvaluation!!
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "AI Feedback & Score" else "AI Cognitive Diagnosis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Audience: $selectedPersona",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${eval.accuracyScore}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = eval.critique,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Jargon Detected Badges
                        if (eval.jargonDetected.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isBn) "⚠️ এই Jargon / কঠিন শব্দগুলো আরও সহজ করা দরকার:" else "⚠️ Unexplained Jargon Detected:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                eval.jargonDetected.forEach { word ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFCC80)
                                    ) {
                                        Text(
                                            text = word,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Socratic Follow-Up Question
                        if (eval.socraticFollowUpQuestion.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isBn) "🤔 Socratic Nudge (Concept আরও Clear করতে):" else "🤔 Socratic Nudge (Fill the Gap):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = eval.socraticFollowUpQuestion,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Previous Sessions History
        item {
            Text(
                text = if (isBn) "Previous Audits (${sessions.size})" else "Previous Concept Audits (${sessions.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(sessions) { s ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = s.conceptName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${s.accuracyScore}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Persona: ${s.targetDifficulty}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.aiCritique,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
