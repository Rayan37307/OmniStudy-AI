package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.audio.SoundMode
import com.example.data.model.StudyTask
import com.example.ui.PomodoroState

@Composable
fun FocusScreen(
    pomodoroState: PomodoroState,
    tasks: List<StudyTask>,
    onToggleTimer: () -> Unit,
    onSetDuration: (workMin: Int, breakMin: Int, name: String) -> Unit,
    onSelectSound: (SoundMode) -> Unit,
    onSetVolume: (Float) -> Unit,
    onAddTask: (title: String, quadrant: String, dueDate: String, pomodoros: Int) -> Unit,
    onToggleTaskComplete: (StudyTask) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDismissMicroQuiz: () -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedQuadrantFilter by remember { mutableStateOf<String?>(null) }

    val minutes = pomodoroState.timeRemainingSeconds / 60
    val seconds = pomodoroState.timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val progress = 1f - (pomodoroState.timeRemainingSeconds.toFloat() / pomodoroState.totalSeconds.coerceAtLeast(1).toFloat())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pomodoro Timer Circular Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pomodoroState.isBreak) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pomodoro_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (pomodoroState.isBreak) "Restorative Break" else pomodoroState.sessionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pomodoroState.isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Completed Sessions: ${pomodoroState.completedSessionsCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular Countdown Display
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(170.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 10.dp,
                            color = if (pomodoroState.isBreak) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = if (pomodoroState.isRunning) "FOCUSING" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer Play / Pause Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = onToggleTimer,
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("toggle_pomodoro_button")
                        ) {
                            Icon(
                                imageVector = if (pomodoroState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (pomodoroState.isRunning) "Pause" else "Play",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Durations
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = pomodoroState.totalSeconds == 25 * 60 && !pomodoroState.isBreak,
                            onClick = { onSetDuration(25, 5, "Classic Pomodoro") },
                            label = { Text("25 / 5m") }
                        )
                        FilterChip(
                            selected = pomodoroState.totalSeconds == 50 * 60,
                            onClick = { onSetDuration(50, 10, "Extended Deep Work") },
                            label = { Text("50 / 10m") }
                        )
                        FilterChip(
                            selected = pomodoroState.totalSeconds == 90 * 60,
                            onClick = { onSetDuration(90, 20, "Ultradian Rhythm") },
                            label = { Text("90 / 20m") }
                        )
                    }
                }
            }
        }

        // Procedural Audio Soundscape Synthesizer
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎵", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Procedural Audio Synthesizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (pomodoroState.selectedSound != SoundMode.NONE) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Live AudioTrack") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Real-time mathematical audio synthesis. No external audio files or internet required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sound Mode Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SoundMode.values().forEach { mode ->
                            FilterChip(
                                selected = pomodoroState.selectedSound == mode,
                                onClick = { onSelectSound(mode) },
                                label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    if (pomodoroState.selectedSound != SoundMode.NONE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            Slider(
                                value = pomodoroState.soundVolume,
                                onValueChange = onSetVolume,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Eisenhower Task Matrix
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Eisenhower Task Matrix",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prioritize tasks by urgency and importance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Quadrant Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedQuadrantFilter == null,
                    onClick = { selectedQuadrantFilter = null },
                    label = { Text("All (${tasks.size})") }
                )
                FilterChip(
                    selected = selectedQuadrantFilter == "urgent_important",
                    onClick = { selectedQuadrantFilter = "urgent_important" },
                    label = { Text("Do First") }
                )
                FilterChip(
                    selected = selectedQuadrantFilter == "not_urgent_important",
                    onClick = { selectedQuadrantFilter = "not_urgent_important" },
                    label = { Text("Schedule") }
                )
            }
        }

        // Task Items
        val filteredTasks = if (selectedQuadrantFilter == null) tasks else tasks.filter { it.quadrant == selectedQuadrantFilter }
        if (filteredTasks.isEmpty()) {
            item {
                Text(
                    text = "No tasks in this quadrant. Tap '+' to create a study milestone!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(filteredTasks) { task ->
                val quadrantColor = when (task.quadrant) {
                    "urgent_important" -> Color(0xFFE57373)
                    "not_urgent_important" -> Color(0xFF64B5F6)
                    "urgent_not_important" -> Color(0xFFFFB74D)
                    else -> Color(0xFFB0BEC5)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTaskComplete(task) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(quadrantColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Due: ${task.dueDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "🍅 ${task.estimatedPomodoros} blocks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteTask(task.id) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Post-Focus 1-Question Active Recall Micro-Quiz Dialog
    if (pomodoroState.triggerMicroQuiz) {
        MicroQuizModal(onDismiss = onDismissMicroQuiz)
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskModal(
            onDismiss = { showAddTaskDialog = false },
            onSubmit = { title, quadrant, dueDate, pomodoros ->
                onAddTask(title, quadrant, dueDate, pomodoros)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun MicroQuizModal(onDismiss: () -> Unit) {
    var selectedOpt by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🎯 Post-Focus Active Recall",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1-question micro-quiz to reinforce cognitive retention before your break starts!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Where does the Krebs Cycle occur in eukaryotic cells?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                val options = listOf("Mitochondrial Matrix", "Cytosol", "Nuclear Envelope", "Ribosome")
                options.forEach { opt ->
                    val isCorrect = opt == "Mitochondrial Matrix"
                    val isSelected = selectedOpt == opt

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            submitted && isCorrect -> Color(0xFFC8E6C9)
                            submitted && isSelected -> Color(0xFFFFCDD2)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !submitted) { selectedOpt = opt }
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!submitted) {
                        Button(
                            onClick = { submitted = true },
                            enabled = selectedOpt != null
                        ) {
                            Text("Check Answer")
                        }
                    } else {
                        Button(onClick = onDismiss) {
                            Text("Begin Break")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskModal(onDismiss: () -> Unit, onSubmit: (String, String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var quadrant by remember { mutableStateOf("urgent_important") }
    var dueDate by remember { mutableStateOf("Today, 6:00 PM") }
    var pomodoros by remember { mutableIntStateOf(2) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Study Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Priority Quadrant:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "urgent_important" to "Do First",
                        "not_urgent_important" to "Schedule",
                        "urgent_not_important" to "Delegate"
                    ).forEach { (q, label) ->
                        FilterChip(
                            selected = quadrant == q,
                            onClick = { quadrant = q },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Time") },
                    modifier = Modifier.fillMaxWidth(),
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
                        onClick = { onSubmit(title, quadrant, dueDate, pomodoros) },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}
