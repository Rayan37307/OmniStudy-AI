package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.sm2.Sm2Rating
import com.example.data.model.Flashcard
import com.example.data.model.FlashcardDeck

@Composable
fun FlashcardsScreen(
    decks: List<FlashcardDeck>,
    activeDeck: FlashcardDeck?,
    studyCards: List<Flashcard>,
    currentCardIndex: Int,
    isCardFlipped: Boolean,
    isCramMode: Boolean,
    onStartSession: (deck: FlashcardDeck?, cramMode: Boolean) -> Unit,
    onFlipCard: () -> Unit,
    onGradeCard: (Sm2Rating) -> Unit,
    onEndSession: () -> Unit
) {
    if (studyCards.isNotEmpty() && currentCardIndex < studyCards.size) {
        // Active SM-2 Study Arena
        val currentCard = studyCards[currentCardIndex]
        var showHint by remember(currentCard.id) { mutableStateOf(false) }

        val rotation by animateFloatAsState(
            targetValue = if (isCardFlipped) 180f else 0f,
            animationSpec = tween(durationMillis = 400),
            label = "CardFlipRotation"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEndSession) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Arena")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isCramMode) "Cram Practice" else "SM-2 Spaced Repetition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Card ${currentCardIndex + 1} of ${studyCards.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                AssistChip(
                    onClick = { showHint = !showHint },
                    label = { Text("Hint") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            val progress = (currentCardIndex + 1).toFloat() / studyCards.size.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Flip Card Component
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCardFlipped) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 14f * density
                    }
                    .clickable { onFlipCard() }
                    .testTag("sm2_active_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // When flipped beyond 90 degrees, show the back side upright
                    if (rotation > 90f) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ANSWER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentCard.back,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )
                        }
                    } else {
                        // Front Side
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentCard.isCloze) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Cloze Deletion", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = "QUESTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = currentCard.front,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )

                            if (showHint && currentCard.hint.isNotBlank()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "💡 Hint: ${currentCard.hint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Tap card to flip",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SM-2 4-Rating Buttons (only visible after flipping or ready)
            if (isCardFlipped) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Rate Recall Quality (SuperMemo-2 Algorithm):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onGradeCard(Sm2Rating.AGAIN) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("grade_again"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Again", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("< 1d", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { onGradeCard(Sm2Rating.HARD) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("grade_hard"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("~1d", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { onGradeCard(Sm2Rating.GOOD) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("grade_good"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Good", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("~3d", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { onGradeCard(Sm2Rating.EASY) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("grade_easy"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Easy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("~6d+", fontSize = 10.sp)
                            }
                        }
                    }
                }
            } else {
                Button(
                    onClick = onFlipCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("flip_card_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.FlipToBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show Answer")
                }
            }
        }
    } else {
        // Decks List & Overview
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
                        text = "Spaced Repetition Decks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SuperMemo-2 (SM-2) Spaced Interval Algorithm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Today Quick Practice Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Due Today Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Review scheduled cards to cement long-term memory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Button(
                        onClick = { onStartSession(null, false) },
                        modifier = Modifier.testTag("start_due_session_button")
                    ) {
                        Text("Start Review")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your Decks (${decks.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(decks) { deck ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = deck.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${deck.cardCount} cards",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = deck.subject,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = deck.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onStartSession(deck, false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Study Due")
                                }
                                OutlinedButton(
                                    onClick = { onStartSession(deck, true) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cram All")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
