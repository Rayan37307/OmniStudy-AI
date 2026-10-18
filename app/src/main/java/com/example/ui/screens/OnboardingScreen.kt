package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.AppLanguage
import com.example.ui.util.StudyStrings
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    currentLanguage: AppLanguage,
    onComplete: (language: AppLanguage, dailyGoalMinutes: Int, userName: String?) -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var selectedGoalMinutes by remember { mutableIntStateOf(45) }
    var userNameInput by remember { mutableStateOf("") }

    val isBn = selectedLanguage == AppLanguage.BENGALI

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = StudyStrings.getAppName(selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = StudyStrings.getOnboardingSkip(selectedLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Interactive bottom controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button or spacer
                    if (pagerState.currentPage > 0) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // Pager Indicators (Pill morph)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 28.dp else 8.dp,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                label = "dot_width"
                            )
                            val color by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                label = "dot_color"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }

                    // Next / Finish Button
                    val isLastPage = pagerState.currentPage == 3
                    Button(
                        onClick = {
                            if (isLastPage) {
                                onComplete(selectedLanguage, selectedGoalMinutes, userNameInput)
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier.testTag(
                            if (isLastPage) "onboarding_start_button" else "onboarding_next_button"
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isLastPage) StudyStrings.getOnboardingGetStarted(selectedLanguage)
                                else StudyStrings.getOnboardingNext(selectedLanguage),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                imageVector = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> OnboardingWelcomePage(isBn = isBn)
                1 -> OnboardingMethodsPage(isBn = isBn)
                2 -> OnboardingPreferencesPage(
                    isBn = isBn,
                    selectedLanguage = selectedLanguage,
                    onSelectLanguage = { selectedLanguage = it },
                    selectedGoal = selectedGoalMinutes,
                    onSelectGoal = { selectedGoalMinutes = it },
                    userName = userNameInput,
                    onUserNameChange = { userNameInput = it }
                )
                3 -> OnboardingPeaceOfMindPage(isBn = isBn)
            }
        }
    }
}

@Composable
private fun OnboardingWelcomePage(isBn: Boolean) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Minimalist Geometric Aesthetic Graphic
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isBn) "Clean & Aesthetic\nSmart Study Space" else "Clean, Aesthetic\nMindful Learning",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isBn) "কোনো Distraction ছাড়া Calm Focus, সহজে Active Recall আর Relaxed ভাবে পড়াশোনা করার App।"
            else "A distraction-free, zero-cost study studio designed for calm focus, cognitive clarity, and effortless mastery.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Aesthetic highlight points
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureHighlightPill(
                icon = "✦",
                title = if (isBn) "100% Free Forever" else "100% Free Forever",
                subtitle = if (isBn) "কোনো Subscription ফি বা Hidden Cost নেই" else "Zero subscriptions, paywalls, or fees"
            )
            FeatureHighlightPill(
                icon = "⚡",
                title = if (isBn) "Free AI Study Coach" else "High-Speed AI Studio",
                subtitle = if (isBn) "Auto-generated Cornell Notes, Quizzes আর instant ব্যাখ্যা" else "Auto-generated Cornell cues & instant quizzes"
            )
            FeatureHighlightPill(
                icon = "🎧",
                title = if (isBn) "Offline Focus Soundscapes" else "Procedural Soundscapes",
                subtitle = if (isBn) "40Hz Gamma & Deep Brown Noise দিয়ে Calm Ambience" else "40Hz Gamma & deep brown noise synthesis"
            )
        }
    }
}

@Composable
private fun OnboardingMethodsPage(isBn: Boolean) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isBn) "Smart Study Techniques" else "Cognitive Learning Flow",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isBn) "Neuroscience-ভিত্তিক এই ৩টি Smart Method দিয়ে সহজে শেখো:"
            else "Built on neuroscience: Active recall, spaced repetition, and conceptual simplification.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MethodCard(
                icon = Icons.Default.AutoStories,
                title = if (isBn) "1. Cornell Notes & Cues" else "1. Cornell Notes & Cues",
                description = if (isBn) "বড় Text থেকে Summary ও Active Recall Cues বানিয়ে নিজে নিজে Practice করো।"
                else "Convert raw material into summary, active recall cues, and mindmap structures."
            )
            MethodCard(
                icon = Icons.Default.Style,
                title = if (isBn) "2. SM-2 Flashcards" else "2. SM-2 Spaced Repetition",
                description = if (isBn) "Spaced Repetition দিয়ে Review করো, যাতে পড়া Long-term স্মৃতিতে গেঁথে যায়।"
                else "Optimized review intervals prevent forgetting curves before they happen."
            )
            MethodCard(
                icon = Icons.Default.RecordVoiceOver,
                title = if (isBn) "3. AI Feynman Coach" else "3. AI Feynman Technique",
                description = if (isBn) "যেকোনো Topic নিজের ভাষায় Explain করো; AI তোমার Gaps ও Jargon ধরিয়ে দেবে।"
                else "Explain complex concepts in simple terms to spot conceptual gaps immediately."
            )
        }
    }
}

@Composable
private fun OnboardingPreferencesPage(
    isBn: Boolean,
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    selectedGoal: Int,
    onSelectGoal: (Int) -> Unit,
    userName: String,
    onUserNameChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isBn) "তোমার Study Routine" else "Your Study Rhythms",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isBn) "Language আর Daily Focus Target সেট করো (পরেও Change করতে পারবে)"
            else "Select your preferred language and daily focus target.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Language Choice Cards
        Text(
            text = if (isBn) "Language বেছে নাও" else "Select Language",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageSelectCard(
                flag = "🇧🇩",
                title = "বাংলা",
                subtitle = "Mixed (Banglish)",
                isSelected = selectedLanguage == AppLanguage.BENGALI,
                onClick = { onSelectLanguage(AppLanguage.BENGALI) },
                modifier = Modifier.weight(1f)
            )
            LanguageSelectCard(
                flag = "🇬🇧",
                title = "English",
                subtitle = "English",
                isSelected = selectedLanguage == AppLanguage.ENGLISH,
                onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Goal Selector
        Text(
            text = if (isBn) "Daily Focus Target" else "Daily Target Minutes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val goals = listOf(25, 45, 60, 90)
            goals.forEach { mins ->
                val isSelected = selectedGoal == mins
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectGoal(mins) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$mins",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "mins" else "mins",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Optional Name Input
        Text(
            text = if (isBn) "তোমার Name (Optional)" else "Your Name (Optional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            placeholder = { Text(if (isBn) "যেমন: Rafi বা Tanvir" else "e.g. Scholar") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OnboardingPeaceOfMindPage(isBn: Boolean) {
    val scrollState = rememberScrollState()

    // Gentle rhythmic pulse animation for peace of mind
    val infiniteTransition = rememberInfiniteTransition(label = "PeaceRing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBn) "Peace of Mind" else "Peace of Mind",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = if (isBn) "Calm Mind & Deep Focus" else "Peace of Mind & Focus",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isBn) "কোনো Ads নেই, Subscription নেই, কোনো Tracking নেই। Clean & Minimal Design যাতে তোমার পড়াশোনায় Peace of Mind বজায় থাকে।"
            else "Zero ads, zero subscriptions, zero interruptions. Clean design built to bring peace of mind to your daily studies.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBn) "\"Clean Mind & Deep Focus দিয়েই সবচেয়ে ভালো Study হয়।\""
                    else "\"Simplicity is the prerequisite for great focus and understanding.\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightPill(
    icon: String,
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
private fun MethodCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectCard(
    flag: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = flag, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
