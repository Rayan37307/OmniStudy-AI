package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.AppScreen
import com.example.ui.util.AppLanguage
import com.example.ui.util.StudyStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniTopAppBar(
    profile: UserProfile,
    isDarkMode: Boolean,
    currentLanguage: AppLanguage,
    onToggleDarkMode: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenLookup: () -> Unit,
    onRestartOnboarding: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = StudyStrings.getAppName(currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = StudyStrings.getFreeTierNotice(currentLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        },
        actions = {
            // Language Switcher Button (বাংলা / English)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleLanguage() }
                    .padding(end = 4.dp)
                    .testTag("toggle_language_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = currentLanguage.flag, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentLanguage.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Quick Dictionary / Wikipedia Lookup button
            IconButton(
                onClick = onOpenLookup,
                modifier = Modifier.testTag("open_lookup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Quick Lookup",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Onboarding Guide / Info button
            IconButton(
                onClick = onRestartOnboarding,
                modifier = Modifier.testTag("restart_onboarding_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Guide & Onboarding",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dark/Light Mode toggle
            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier.testTag("toggle_dark_mode")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme"
                )
            }
        }
    )
}

@Composable
fun OmniBottomNavigation(
    currentScreen: AppScreen,
    currentLanguage: AppLanguage,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onScreenSelected(AppScreen.DASHBOARD) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text(StudyStrings.getDashboardTab(currentLanguage)) },
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.STUDY_HUB,
            onClick = { onScreenSelected(AppScreen.STUDY_HUB) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.STUDY_HUB) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Study Hub"
                )
            },
            label = { Text(StudyStrings.getHubTab(currentLanguage)) },
            modifier = Modifier.testTag("nav_study_hub")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.FLASHCARDS,
            onClick = { onScreenSelected(AppScreen.FLASHCARDS) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.FLASHCARDS) Icons.Filled.Style else Icons.Outlined.Style,
                    contentDescription = "Flashcards"
                )
            },
            label = { Text(StudyStrings.getCardsTab(currentLanguage)) },
            modifier = Modifier.testTag("nav_flashcards")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.EXAMS,
            onClick = { onScreenSelected(AppScreen.EXAMS) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.EXAMS) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                    contentDescription = "Mock Exams"
                )
            },
            label = { Text(StudyStrings.getExamsTab(currentLanguage)) },
            modifier = Modifier.testTag("nav_exams")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.FOCUS,
            onClick = { onScreenSelected(AppScreen.FOCUS) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.FOCUS) Icons.Filled.Timer else Icons.Outlined.Timer,
                    contentDescription = "Focus Studio"
                )
            },
            label = { Text(StudyStrings.getFocusTab(currentLanguage)) },
            modifier = Modifier.testTag("nav_focus")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.FEYNMAN,
            onClick = { onScreenSelected(AppScreen.FEYNMAN) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.FEYNMAN) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                    contentDescription = "Feynman AI"
                )
            },
            label = { Text(StudyStrings.getFeynmanTab(currentLanguage)) },
            modifier = Modifier.testTag("nav_feynman")
        )
    }
}
