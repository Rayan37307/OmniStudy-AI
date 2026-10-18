package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.Dialog
import com.example.core.ai.ExamQuestion
import com.example.data.model.ExamMock
import com.example.ui.ActiveExamState

@Composable
fun ExamsScreen(
    exams: List<ExamMock>,
    activeExamState: ActiveExamState,
    isActionLoading: Boolean,
    onCreateExam: (topic: String, subject: String, count: Int, difficulty: String) -> Unit,
    onStartExam: (ExamMock, List<ExamQuestion>) -> Unit,
    onSelectAnswer: (questionId: Int, answer: String) -> Unit,
    onToggleReview: (questionId: Int) -> Unit,
    onNavigateQuestion: (index: Int) -> Unit,
    onSubmitExam: () -> Unit,
    onConvertMistakes: () -> Unit,
    onExitExam: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (activeExamState.exam != null) {
        if (activeExamState.isSubmitted && activeExamState.gradingResult != null) {
            // Post-Exam Diagnostic Report
            val grading = activeExamState.gradingResult
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diagnostic Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onExitExam) {
                            Icon(Icons.Default.Close, contentDescription = "Close Report")
                        }
                    }
                }

                // Score Card
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${grading.score}%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = activeExamState.exam.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = grading.overallFeedback,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Convert Mistakes to Flashcards button
                            val hasMistakes = grading.questionAnalysis.any { !it.isCorrect }
                            if (hasMistakes) {
                                Button(
                                    onClick = onConvertMistakes,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("convert_mistakes_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Style, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Convert Mistakes to Flashcards")
                                }
                            }
                        }
                    }
                }

                // Question by question breakdown
                item {
                    Text(
                        text = "Detailed Question Rationales",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(activeExamState.questions) { q ->
                    val analysis = grading.questionAnalysis.find { it.questionId == q.id }
                    val userAns = activeExamState.userAnswers[q.id] ?: "(No answer)"
                    val isCorrect = analysis?.isCorrect == true

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Question ${q.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCorrect) Color(0xFF81C784) else Color(0xFFE57373)
                                ) {
                                    Text(
                                        text = if (isCorrect) "✓ Correct" else "✗ Needs Review",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = q.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Your Answer: $userAns",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                            if (!isCorrect) {
                                Text(
                                    text = "Correct Answer: ${q.correctAnswer}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Explanation: ${q.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // Live Exam Simulator
            val currentQ = activeExamState.questions.getOrNull(activeExamState.currentQuestionIndex)
            val minutes = activeExamState.timeRemainingSeconds / 60
            val seconds = activeExamState.timeRemainingSeconds % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Timer & Controls Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeExamState.exam.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Question ${activeExamState.currentQuestionIndex + 1} of ${activeExamState.questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Countdown Timer Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (activeExamState.timeRemainingSeconds < 120) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (activeExamState.timeRemainingSeconds < 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (activeExamState.timeRemainingSeconds < 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question Palette (Answered, Unanswered, Marked for Review)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(activeExamState.questions) { idx, q ->
                        val isAnswered = activeExamState.userAnswers.containsKey(q.id)
                        val isMarked = activeExamState.markedForReview.contains(q.id)
                        val isCurrent = idx == activeExamState.currentQuestionIndex

                        val bgColor = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isMarked -> MaterialTheme.colorScheme.tertiary
                            isAnswered -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            isCurrent -> MaterialTheme.colorScheme.onPrimary
                            isMarked -> MaterialTheme.colorScheme.onTertiary
                            isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable { onNavigateQuestion(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card & Options
                if (currentQ != null) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(currentQ.type.replace("_", " ")) }
                                    )
                                    IconButton(onClick = { onToggleReview(currentQ.id) }) {
                                        Icon(
                                            if (activeExamState.markedForReview.contains(currentQ.id)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Mark for Review",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentQ.question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 26.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Options selector
                            if (currentQ.options.isNotEmpty()) {
                                items(currentQ.options) { option ->
                                    val isSelected = activeExamState.userAnswers[currentQ.id] == option
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectAnswer(currentQ.id, option) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelectAnswer(currentQ.id, option) }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Short Answer input field
                                item {
                                    var textAnswer by remember(currentQ.id) {
                                        mutableStateOf(activeExamState.userAnswers[currentQ.id] ?: "")
                                    }
                                    OutlinedTextField(
                                        value = textAnswer,
                                        onValueChange = {
                                            textAnswer = it
                                            onSelectAnswer(currentQ.id, it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Your response...") },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Navigation & Submit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNavigateQuestion(activeExamState.currentQuestionIndex - 1) },
                        enabled = activeExamState.currentQuestionIndex > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Previous")
                    }

                    if (activeExamState.currentQuestionIndex < activeExamState.questions.size - 1) {
                        Button(
                            onClick = { onNavigateQuestion(activeExamState.currentQuestionIndex + 1) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = onSubmitExam,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("submit_exam_button")
                        ) {
                            Text("Submit Exam")
                        }
                    }
                }
            }
        }
    } else {
        // Exams List & Creator View
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
                        text = "Adaptive Mock Exams",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MCQ, True/False & Short Answer with AI Rubrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_mock_exam_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Build New Custom Exam")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isActionLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Previous Exams (${exams.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(exams) { exam ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = exam.subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (exam.isCompleted) {
                                    Text(
                                        text = "Completed • Score: ${exam.score}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (exam.score >= 70) Color(0xFF81C784) else Color(0xFFFFB74D),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Duration: ${exam.durationMinutes} mins",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val qs = com.example.ui.MainViewModel(android.app.Application()).deserializeQuestions(exam.questionsJson)
                                    onStartExam(exam, qs)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (exam.isCompleted) "Review" else "Start")
                            }
                        }
                    }
                }
            }
        }
    }

    // Exam Creator Dialog
    if (showCreateDialog) {
        CreateExamModal(
            onDismiss = { showCreateDialog = false },
            onSubmit = { topic, subject, count, difficulty ->
                onCreateExam(topic, subject, count, difficulty)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateExamModal(onDismiss: () -> Unit, onSubmit: (String, String, Int, String) -> Unit) {
    var topic by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var count by remember { mutableIntStateOf(5) }
    var difficulty by remember { mutableStateOf("Standard") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configure Mock Exam",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic (e.g. Thermodynamics, Cell Division)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Physics, Biology)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Question Count: $count", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 8, 10).forEach { c ->
                        FilterChip(
                            selected = count == c,
                            onClick = { count = c },
                            label = { Text("$c Qs") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Difficulty Tier:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Foundational", "Standard", "Olympiad").forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff) }
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
                        onClick = { onSubmit(topic, subject, count, difficulty) },
                        enabled = topic.isNotBlank() && subject.isNotBlank()
                    ) {
                        Text("Generate Exam")
                    }
                }
            }
        }
    }
}
