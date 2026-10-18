package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.audio.SoundMode
import com.example.data.model.FlashcardDeck
import com.example.data.model.StudyMaterial
import com.example.data.model.UserProfile
import com.example.ui.AppScreen
import com.example.ui.util.AppLanguage
import com.example.ui.util.StudyStrings

@Composable
fun DashboardScreen(
    profile: UserProfile,
    materials: List<StudyMaterial>,
    decks: List<FlashcardDeck>,
    dueCardsCount: Int,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onNavigate: (AppScreen) -> Unit,
    onSelectMaterial: (StudyMaterial) -> Unit,
    onPlaySound: (SoundMode) -> Unit = {},
    onStopSound: () -> Unit = {},
    isSoundPlaying: Boolean = false
) {
    val isBn = currentLanguage == AppLanguage.BENGALI
    var isBreathingActive by remember { mutableStateOf(false) }

    // Gentle breathing pulse animation for Peace of Mind
    val infiniteTransition = rememberInfiniteTransition(label = "PeaceBreathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Aesthetic Minimalist Hero Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = StudyStrings.getWelcomeGreeting(profile.fullName, currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isBn) "✦ Calm Mind • Smart Study • 100% Free" else "✦ Clean Mindful Focus • 100% Free",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✦",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily Study Goal Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = StudyStrings.getDailyGoal(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${profile.minutesStudiedToday} / ${profile.dailyGoalMinutes} mins",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progress = (profile.minutesStudiedToday.toFloat() / profile.dailyGoalMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill(icon = "🔥", label = StudyStrings.getStreakLabel(currentLanguage), value = "${profile.studyStreak} Days")
                        MetricPill(icon = "⚡", label = StudyStrings.getXpLabel(currentLanguage), value = "${profile.xpPoints} XP")
                        MetricPill(icon = "🎴", label = StudyStrings.getDueCardsLabel(currentLanguage), value = "$dueCardsCount Due")
                    }
                }
            }
        }

        // Clean Aesthetic Peace of Mind & Mindful Focus Widget
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
                            Text(text = "✦", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "1-Min Breathing Break • Relax করো" else "Mindful Pause & Peace of Mind",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        AssistChip(
                            onClick = { isBreathingActive = !isBreathingActive },
                            label = { Text(if (isBreathingActive) (if (isBn) "Pause করো" else "Pause") else (if (isBn) "1-Min Breathe" else "1-Min Focus")) },
                            leadingIcon = {
                                Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isBreathingActive) {
                        // Visual Guided Breathing Circle
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                .size(110.dp)
                                .scale(breathScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBn) "ধীরে Inhale করো...\nRelaxed থাকো" else "Inhale...\nBe Centered",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isBn) "ধীরে শ্বাস নাও, Hold করো, তারপর Exhale করো। Mind একদম Fresh লাগবে।" else "Inhale deeply, pause gently, exhale slowly. Enter deep focus.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Serene Aesthetic Quote
                        Text(
                            text = if (isBn) "\"Mind শান্ত থাকলে পড়া অনেক সহজে মাথায় ঢোকে।\""
                            else "\"Simplicity and calm are the ultimate prerequisites for deep learning.\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1-Tap Quick Aesthetic Soundscapes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isSoundPlaying,
                            onClick = {
                                if (isSoundPlaying) onStopSound() else onPlaySound(SoundMode.FOREST_RAIN)
                            },
                            label = { Text("🌧️ Calm Rain") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isSoundPlaying,
                            onClick = {
                                if (isSoundPlaying) onStopSound() else onPlaySound(SoundMode.BINAURAL_40HZ)
                            },
                            label = { Text("🎧 40Hz Focus") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Clean & Simplified Study Workflows
        item {
            Text(
                text = if (isBn) "Study Modules" else "Essential Study Modules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkflowCard(
                        title = "Cornell Notes",
                        subtitle = if (isBn) "Smart Notes & Cues" else "Clean notes & cues",
                        icon = Icons.Default.AutoStories,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.STUDY_HUB) }
                    )
                    WorkflowCard(
                        title = "SM-2 Flashcards",
                        subtitle = "$dueCardsCount cards due",
                        icon = Icons.Default.Style,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.FLASHCARDS) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkflowCard(
                        title = "Mock Exams",
                        subtitle = if (isBn) "Quick Quiz Test" else "Adaptive quiz",
                        icon = Icons.Default.Quiz,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.EXAMS) }
                    )
                    WorkflowCard(
                        title = "Focus Studio",
                        subtitle = if (isBn) "Timer & Soundscapes" else "Nature & Timer",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.FOCUS) }
                    )
                }
            }
        }

        // Clean Interleaved Timetable
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = StudyStrings.getInterleavedTitle(currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = StudyStrings.getInterleavedSubtitle(currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InterleavedSlot(
                        time = "09:00 - 09:45 AM",
                        subject = if (isBn) "Biology (Photosynthesis)" else "Photosynthesis & Bio",
                        topic = if (isBn) "Calvin Cycle-এর সহজ Cues" else "Calvin Cycle Cues",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InterleavedSlot(
                        time = "10:00 - 10:45 AM",
                        subject = if (isBn) "Physics Mechanics" else "Physics Mechanics",
                        topic = if (isBn) "Newton's Gravity Formulas" else "Newtonian Gravity",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Recent Materials
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "Recent Lessons" else "Recent Lessons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(AppScreen.STUDY_HUB) }) {
                    Text(if (isBn) "View All" else "View All")
                }
            }
        }

        if (materials.isEmpty()) {
            item {
                Text(
                    text = if (isBn) "এখনো কোনো Notes নেই। নতুন Note বানাতে 'Cornell Notes'-এ tap করো।" else "No study materials yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(materials.take(2)) { material ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMaterial(material) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = material.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = material.subject,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricPill(icon: String, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkflowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InterleavedSlot(
    time: String,
    subject: String,
    topic: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$subject • $topic",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
