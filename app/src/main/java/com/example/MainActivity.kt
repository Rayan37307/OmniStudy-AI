package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudyMaterial
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.OmniBottomNavigation
import com.example.ui.components.OmniTopAppBar
import com.example.ui.components.QuickLookupModal
import com.example.ui.screens.*
import com.example.ui.theme.OmniStudyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val materials by viewModel.allMaterials.collectAsStateWithLifecycle()
            val decks by viewModel.allDecks.collectAsStateWithLifecycle()
            val cards by viewModel.allCards.collectAsStateWithLifecycle()
            val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
            val exams by viewModel.allExams.collectAsStateWithLifecycle()
            val feynmanSessions by viewModel.allFeynmanSessions.collectAsStateWithLifecycle()

            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

            // Flashcards Session State
            val activeDeck by viewModel.activeDeck.collectAsStateWithLifecycle()
            val studyCards by viewModel.studyCards.collectAsStateWithLifecycle()
            val currentCardIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()
            val isCardFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()
            val isCramMode by viewModel.isCramMode.collectAsStateWithLifecycle()

            // Exam Session State
            val activeExamState by viewModel.activeExamState.collectAsStateWithLifecycle()

            // Pomodoro State
            val pomodoroState by viewModel.pomodoroState.collectAsStateWithLifecycle()

            // Lookup Drawer State
            val isLookupOpen by viewModel.isLookupOpen.collectAsStateWithLifecycle()
            val lookupQuery by viewModel.lookupQuery.collectAsStateWithLifecycle()
            val lookupLoading by viewModel.lookupLoading.collectAsStateWithLifecycle()
            val lookupDictionaryResult by viewModel.lookupDictionaryResult.collectAsStateWithLifecycle()
            val lookupWikiResult by viewModel.lookupWikiResult.collectAsStateWithLifecycle()

            // Loading & Status
            val isActionLoading by viewModel.actionLoading.collectAsStateWithLifecycle()
            val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

            var selectedMaterial by remember { mutableStateOf<StudyMaterial?>(null) }
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(statusMessage) {
                statusMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearStatusMessage()
                }
            }

            // Back handler support
            BackHandler(enabled = !hasCompletedOnboarding || selectedMaterial != null || studyCards.isNotEmpty() || activeExamState.exam != null || currentScreen != AppScreen.DASHBOARD) {
                when {
                    !hasCompletedOnboarding -> { /* stay on onboarding */ }
                    selectedMaterial != null -> selectedMaterial = null
                    studyCards.isNotEmpty() -> viewModel.startStudySession(null, false)
                    activeExamState.exam != null -> viewModel.goToExamQuestion(0)
                    else -> viewModel.setScreen(AppScreen.DASHBOARD)
                }
            }

            OmniStudyTheme(darkTheme = isDarkMode) {
                if (!hasCompletedOnboarding) {
                    OnboardingScreen(
                        currentLanguage = currentLanguage,
                        onComplete = { lang, goal, name ->
                            viewModel.completeOnboarding(lang, goal, name)
                        },
                        onSkip = {
                            viewModel.completeOnboarding(currentLanguage, 45, null)
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            OmniTopAppBar(
                                profile = userProfile,
                                isDarkMode = isDarkMode,
                                currentLanguage = currentLanguage,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onOpenLookup = { viewModel.openLookup() },
                                onRestartOnboarding = { viewModel.restartOnboarding() }
                            )
                        },
                    bottomBar = {
                        OmniBottomNavigation(
                            currentScreen = currentScreen,
                            currentLanguage = currentLanguage,
                            onScreenSelected = { screen ->
                                selectedMaterial = null
                                viewModel.setScreen(screen)
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val dueCardsCount = cards.count { it.nextReviewDate <= System.currentTimeMillis() }

                        when (currentScreen) {
                            AppScreen.DASHBOARD -> {
                                DashboardScreen(
                                    profile = userProfile,
                                    materials = materials,
                                    decks = decks,
                                    dueCardsCount = dueCardsCount,
                                    currentLanguage = currentLanguage,
                                    onNavigate = { screen -> viewModel.setScreen(screen) },
                                    onSelectMaterial = { mat ->
                                        selectedMaterial = mat
                                        viewModel.setScreen(AppScreen.STUDY_HUB)
                                    },
                                    onPlaySound = { mode ->
                                        viewModel.setSoundMode(mode)
                                        viewModel.soundscape.start(mode)
                                    },
                                    onStopSound = {
                                        viewModel.soundscape.stop()
                                    },
                                    isSoundPlaying = viewModel.soundscape.isPlaying()
                                )
                            }
                            AppScreen.STUDY_HUB -> {
                                StudyHubScreen(
                                    materials = materials,
                                    selectedMaterial = selectedMaterial,
                                    ttsManager = viewModel.ttsManager,
                                    isActionLoading = isActionLoading,
                                    onSelectMaterial = { mat -> selectedMaterial = mat },
                                    onCreateMaterial = { title, subject, content ->
                                        viewModel.createMaterialFromNotes(title, subject, content)
                                    },
                                    onImportWikipedia = { topic ->
                                        viewModel.importWikipediaTopic(topic)
                                    },
                                    onGenerateDeck = { mat ->
                                        viewModel.generateDeckFromMaterial(mat)
                                        viewModel.setScreen(AppScreen.FLASHCARDS)
                                    },
                                    onGenerateExam = { mat ->
                                        viewModel.createAndStartExam(mat.title, mat.subject, count = 5)
                                        viewModel.setScreen(AppScreen.EXAMS)
                                    },
                                    onDeleteMaterial = { id -> viewModel.deleteMaterial(id) }
                                )
                            }
                            AppScreen.FLASHCARDS -> {
                                FlashcardsScreen(
                                    decks = decks,
                                    activeDeck = activeDeck,
                                    studyCards = studyCards,
                                    currentCardIndex = currentCardIndex,
                                    isCardFlipped = isCardFlipped,
                                    isCramMode = isCramMode,
                                    onStartSession = { deck, cram -> viewModel.startStudySession(deck, cram) },
                                    onFlipCard = { viewModel.flipCard() },
                                    onGradeCard = { rating -> viewModel.gradeCard(rating) },
                                    onEndSession = { viewModel.startStudySession(null, false) }
                                )
                            }
                            AppScreen.EXAMS -> {
                                ExamsScreen(
                                    exams = exams,
                                    activeExamState = activeExamState,
                                    isActionLoading = isActionLoading,
                                    onCreateExam = { topic, subject, count, diff ->
                                        viewModel.createAndStartExam(topic, subject, count, diff)
                                    },
                                    onStartExam = { exam, qs -> viewModel.startExamSession(exam, qs) },
                                    onSelectAnswer = { qId, ans -> viewModel.selectExamAnswer(qId, ans) },
                                    onToggleReview = { qId -> viewModel.toggleMarkForReview(qId) },
                                    onNavigateQuestion = { idx -> viewModel.goToExamQuestion(idx) },
                                    onSubmitExam = { viewModel.submitActiveExam() },
                                    onConvertMistakes = {
                                        viewModel.convertMistakesToFlashcards()
                                        viewModel.setScreen(AppScreen.FLASHCARDS)
                                    },
                                    onExitExam = { viewModel.submitActiveExam() }
                                )
                            }
                            AppScreen.FOCUS -> {
                                FocusScreen(
                                    pomodoroState = pomodoroState,
                                    tasks = tasks,
                                    onToggleTimer = { viewModel.togglePomodoroTimer() },
                                    onSetDuration = { work, brk, name -> viewModel.setPomodoroDuration(work, brk, name) },
                                    onSelectSound = { mode -> viewModel.setSoundMode(mode) },
                                    onSetVolume = { vol -> viewModel.setSoundVolume(vol) },
                                    onAddTask = { title, quad, due, pomo -> viewModel.addTask(title, quad, due, pomo) },
                                    onToggleTaskComplete = { task -> viewModel.toggleTaskComplete(task) },
                                    onDeleteTask = { id -> viewModel.deleteTask(id) },
                                    onDismissMicroQuiz = { viewModel.dismissMicroQuiz() }
                                )
                            }
                            AppScreen.FEYNMAN -> {
                                FeynmanScreen(
                                    sessions = feynmanSessions,
                                    isActionLoading = isActionLoading,
                                    currentLanguage = currentLanguage,
                                    onEvaluateExplanation = { concept, persona, explanation, callback ->
                                        viewModel.evaluateFeynmanExplanation(concept, persona, explanation, callback)
                                    }
                                )
                            }
                        }

                        // Floating Quick Lookup Modal (Free Dictionary & Wikipedia REST APIs)
                        QuickLookupModal(
                            isOpen = isLookupOpen,
                            query = lookupQuery,
                            isLoading = lookupLoading,
                            currentLanguage = currentLanguage,
                            dictionaryResult = lookupDictionaryResult,
                            wikiResult = lookupWikiResult,
                            onQueryChange = { q -> /* text change */ },
                            onSearch = { q -> viewModel.performLookup(q) },
                            onSpeakWord = { word -> viewModel.ttsManager.speak(word, isBengali = currentLanguage == com.example.ui.util.AppLanguage.BENGALI || word.any { it in '\u0980'..'\u09FF' }) },
                            onImportToNotes = { topic, summary ->
                                viewModel.importWikipediaTopic(topic)
                                viewModel.setScreen(AppScreen.STUDY_HUB)
                            },
                            onClose = { viewModel.closeLookup() }
                        )
                    }
                }
            }
        }
    }
}
}
