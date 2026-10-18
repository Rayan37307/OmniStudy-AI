package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ai.*
import com.example.core.api.AuxiliaryApis
import com.example.core.api.DictionaryResult
import com.example.core.api.WikiSummary
import com.example.core.audio.SoundMode
import com.example.core.audio.SoundscapeSynthesizer
import com.example.core.audio.TtsManager
import com.example.core.sm2.Sm2Engine
import com.example.core.sm2.Sm2Rating
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

import com.example.ui.util.AppLanguage

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    STUDY_HUB("Study Hub"),
    FLASHCARDS("Flashcards"),
    EXAMS("Mock Exams"),
    FOCUS("Focus Studio"),
    FEYNMAN("Feynman AI")
}

data class PomodoroState(
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val timeRemainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val sessionName: String = "Active Deep Work",
    val completedSessionsCount: Int = 0,
    val selectedSound: SoundMode = SoundMode.NONE,
    val soundVolume: Float = 0.6f,
    val triggerMicroQuiz: Boolean = false
)

data class ActiveExamState(
    val exam: ExamMock? = null,
    val questions: List<ExamQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(),
    val markedForReview: Set<Int> = emptySet(),
    val timeRemainingSeconds: Int = 15 * 60,
    val isSubmitted: Boolean = false,
    val gradingResult: ExamGradingResult? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    val geminiService = GeminiStudyService()
    val auxiliaryApis = AuxiliaryApis()
    val soundscape = SoundscapeSynthesizer()
    val ttsManager = TtsManager(application)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Dark Mode Toggle State
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val prefs = application.getSharedPreferences("omnistudy_prefs", android.content.Context.MODE_PRIVATE)

    // Onboarding Flow State (shown on first launch)
    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean("has_completed_onboarding", false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    // Current Language State (Bengali default as requested)
    private val _currentLanguage = MutableStateFlow(
        if (prefs.getString("app_language", "bn") == "en") AppLanguage.ENGLISH else AppLanguage.BENGALI
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun completeOnboarding(language: AppLanguage, dailyGoalMinutes: Int, userName: String?) {
        prefs.edit()
            .putBoolean("has_completed_onboarding", true)
            .putString("app_language", language.code)
            .apply()
        _hasCompletedOnboarding.value = true
        _currentLanguage.value = language
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                fullName = if (!userName.isNullOrBlank()) userName.trim() else current.fullName,
                dailyGoalMinutes = dailyGoalMinutes
            )
            repository.insertOrUpdateProfile(updated)
        }
    }

    fun restartOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", false).apply()
        _hasCompletedOnboarding.value = false
    }

    // Data from Repository
    val userProfile: StateFlow<UserProfile>
    val allMaterials: StateFlow<List<StudyMaterial>>
    val allDecks: StateFlow<List<FlashcardDeck>>
    val allTasks: StateFlow<List<StudyTask>>
    val allExams: StateFlow<List<ExamMock>>
    val allFeynmanSessions: StateFlow<List<FeynmanSession>>
    val allCards: StateFlow<List<Flashcard>>

    // Floating Lookup Drawer State
    private val _isLookupOpen = MutableStateFlow(false)
    val isLookupOpen: StateFlow<Boolean> = _isLookupOpen.asStateFlow()

    private val _lookupQuery = MutableStateFlow("")
    val lookupQuery: StateFlow<String> = _lookupQuery.asStateFlow()

    private val _lookupLoading = MutableStateFlow(false)
    val lookupLoading: StateFlow<Boolean> = _lookupLoading.asStateFlow()

    private val _lookupDictionaryResult = MutableStateFlow<DictionaryResult?>(null)
    val lookupDictionaryResult: StateFlow<DictionaryResult?> = _lookupDictionaryResult.asStateFlow()

    private val _lookupWikiResult = MutableStateFlow<WikiSummary?>(null)
    val lookupWikiResult: StateFlow<WikiSummary?> = _lookupWikiResult.asStateFlow()

    // Flashcard Study Session State
    private val _activeDeck = MutableStateFlow<FlashcardDeck?>(null)
    val activeDeck: StateFlow<FlashcardDeck?> = _activeDeck.asStateFlow()

    private val _studyCards = MutableStateFlow<List<Flashcard>>(emptyList())
    val studyCards: StateFlow<List<Flashcard>> = _studyCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    private val _isCramMode = MutableStateFlow(false)
    val isCramMode: StateFlow<Boolean> = _isCramMode.asStateFlow()

    // Active Exam State
    private val _activeExamState = MutableStateFlow(ActiveExamState())
    val activeExamState: StateFlow<ActiveExamState> = _activeExamState.asStateFlow()
    private var examTimerJob: Job? = null

    // Pomodoro Timer State
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var pomodoroTimerJob: Job? = null

    // UI Loading & Snackbars
    private val _actionLoading = MutableStateFlow(false)
    val actionLoading: StateFlow<Boolean> = _actionLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudyRepository(database.studyDao())

        userProfile = repository.userProfile
            .map { it ?: UserProfile() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

        allMaterials = repository.allMaterials
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        allDecks = repository.allDecks
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        allTasks = repository.allTasks
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        allExams = repository.allExams
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        allFeynmanSessions = repository.allFeynmanSessions
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        allCards = repository.allCards
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        viewModelScope.launch {
            repository.checkSeedData()
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.BENGALI) AppLanguage.ENGLISH else AppLanguage.BENGALI
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // --- QUICK LOOKUP DRAWER ---
    fun openLookup(query: String = "") {
        _isLookupOpen.value = true
        if (query.isNotBlank()) {
            _lookupQuery.value = query
            performLookup(query)
        }
    }

    fun closeLookup() {
        _isLookupOpen.value = false
    }

    fun performLookup(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        _lookupQuery.value = q
        _lookupLoading.value = true
        _lookupDictionaryResult.value = null
        _lookupWikiResult.value = null

        viewModelScope.launch {
            val isBn = _currentLanguage.value == AppLanguage.BENGALI || q.any { it in '\u0980'..'\u09FF' }
            val dictDeferred = auxiliaryApis.fetchDictionaryDefinition(q)
            val wikiDeferred = auxiliaryApis.fetchWikipediaSummary(q, preferBengali = isBn)

            _lookupDictionaryResult.value = dictDeferred.getOrNull()
            _lookupWikiResult.value = wikiDeferred.getOrNull()
            _lookupLoading.value = false
        }
    }

    // --- STUDY HUB & MATERIAL ACTIONS ---
    fun createMaterialFromNotes(title: String, subject: String, content: String) {
        if (content.isBlank() || title.isBlank()) return
        viewModelScope.launch {
            _actionLoading.value = true
            _statusMessage.value = "AI analyzing and structuring Cornell Notes..."
            try {
                val cornell = geminiService.generateCornellNotes(content, subject)
                val id = repository.insertMaterial(
                    StudyMaterial(
                        title = title,
                        subject = subject,
                        rawContent = content,
                        cornellCues = cornell.cues,
                        cornellNotes = cornell.notes,
                        cornellSummary = cornell.summary,
                        mindmapMermaid = cornell.mermaid
                    )
                )
                repository.addXp(50)
                _statusMessage.value = "Cornell Notes & Mind Map generated (+50 XP)!"
            } catch (e: Exception) {
                _statusMessage.value = "Created: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun importWikipediaTopic(topic: String, subject: String = "বিজ্ঞান (General Science)") {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _actionLoading.value = true
            val isBn = _currentLanguage.value == AppLanguage.BENGALI || topic.any { it in '\u0980'..'\u09FF' }
            _statusMessage.value = if (isBn) "উইকিপিডিয়া থেকে তথ্য সংগ্রহ করা হচ্ছে..." else "Fetching verified Wikipedia context..."
            try {
                val summaryRes = auxiliaryApis.fetchWikipediaSummary(topic, preferBengali = isBn)
                val summary = summaryRes.getOrThrow()
                _statusMessage.value = if (isBn) "এআই দিয়ে কর্নাল নোট ও মাইন্ড ম্যাপ তৈরি হচ্ছে..." else "Synthesizing Cornell breakdown with Gemini AI..."
                val cornell = geminiService.generateCornellNotes(summary.extract, subject)
                repository.insertMaterial(
                    StudyMaterial(
                        title = summary.title,
                        subject = if (summary.description.isNotBlank()) summary.description else subject,
                        rawContent = summary.extract,
                        cornellCues = cornell.cues,
                        cornellNotes = cornell.notes,
                        cornellSummary = cornell.summary,
                        mindmapMermaid = cornell.mermaid
                    )
                )
                repository.addXp(60)
                _statusMessage.value = if (isBn) "'${summary.title}' সফলভাবে যুক্ত হয়েছে (+60 XP)!" else "Imported '${summary.title}' from Wikipedia (+60 XP)!"
            } catch (e: Exception) {
                _statusMessage.value = "Failed: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun generateDeckFromMaterial(material: StudyMaterial) {
        viewModelScope.launch {
            _actionLoading.value = true
            _statusMessage.value = "Generating high-yield flashcards..."
            try {
                val generated = geminiService.generateFlashcards(material.rawContent, material.subject, count = 8)
                val deckId = repository.insertDeck(
                    FlashcardDeck(
                        title = "${material.title} Cards",
                        subject = material.subject,
                        description = "AI Spaced Repetition deck extracted from ${material.title}.",
                        cardCount = generated.size
                    )
                )
                val cards = generated.map {
                    Flashcard(
                        deckId = deckId,
                        front = it.front,
                        back = it.back,
                        hint = it.hint,
                        isCloze = it.isCloze
                    )
                }
                repository.insertCards(cards)
                repository.addXp(75)
                _statusMessage.value = "Generated ${cards.size} Flashcards with SM-2 (+75 XP)!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun deleteMaterial(id: Long) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
        }
    }

    // --- FLASHCARDS & SM-2 ARENA ---
    fun startStudySession(deck: FlashcardDeck?, cramMode: Boolean = false) {
        _activeDeck.value = deck
        _isCramMode.value = cramMode
        _currentCardIndex.value = 0
        _isCardFlipped.value = false

        viewModelScope.launch {
            val list = if (deck != null) {
                repository.getCardsByDeckOnce(deck.id)
            } else {
                repository.allCards.first()
            }

            val filtered = if (cramMode) {
                list
            } else {
                val now = System.currentTimeMillis()
                val due = list.filter { it.nextReviewDate <= now }
                if (due.isEmpty()) list else due
            }

            _studyCards.value = filtered
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun gradeCard(rating: Sm2Rating) {
        val cards = _studyCards.value
        val index = _currentCardIndex.value
        if (index >= cards.size) return

        val currentCard = cards[index]
        val updated = Sm2Engine.calculateNextReview(currentCard, rating)

        viewModelScope.launch {
            repository.updateCard(updated)
            repository.addXp(10)

            _isCardFlipped.value = false
            if (index + 1 < cards.size) {
                _currentCardIndex.value = index + 1
            } else {
                // Session finished!
                _statusMessage.value = "Study session complete! (+${cards.size * 10} XP)"
                _studyCards.value = emptyList()
            }
        }
    }

    // --- EXAMS & MOCK SIMULATOR ---
    fun createAndStartExam(topic: String, subject: String, count: Int = 5, difficulty: String = "Standard") {
        viewModelScope.launch {
            _actionLoading.value = true
            _statusMessage.value = "Creating adaptive $difficulty exam for '$topic'..."
            try {
                val questions = geminiService.generateMockExam(topic, subject, count, difficulty)
                val duration = count * 2 // 2 minutes per question
                val exam = ExamMock(
                    title = "$topic ($difficulty)",
                    subject = subject,
                    durationMinutes = duration,
                    questionsJson = serializeQuestions(questions)
                )
                val examId = repository.insertExam(exam)
                val insertedExam = exam.copy(id = examId)

                startExamSession(insertedExam, questions)
                _statusMessage.value = "Exam ready! Good luck."
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun startExamSession(exam: ExamMock, questions: List<ExamQuestion>) {
        examTimerJob?.cancel()
        _activeExamState.value = ActiveExamState(
            exam = exam,
            questions = questions,
            currentQuestionIndex = 0,
            userAnswers = emptyMap(),
            markedForReview = emptySet(),
            timeRemainingSeconds = exam.durationMinutes * 60,
            isSubmitted = false,
            gradingResult = null
        )

        examTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeExamState.value
                if (current.isSubmitted) break
                if (current.timeRemainingSeconds <= 1) {
                    submitActiveExam()
                    break
                }
                _activeExamState.value = current.copy(
                    timeRemainingSeconds = current.timeRemainingSeconds - 1
                )
            }
        }
    }

    fun selectExamAnswer(questionId: Int, answer: String) {
        val current = _activeExamState.value
        val updated = current.userAnswers.toMutableMap()
        updated[questionId] = answer
        _activeExamState.value = current.copy(userAnswers = updated)
    }

    fun toggleMarkForReview(questionId: Int) {
        val current = _activeExamState.value
        val updated = current.markedForReview.toMutableSet()
        if (updated.contains(questionId)) updated.remove(questionId) else updated.add(questionId)
        _activeExamState.value = current.copy(markedForReview = updated)
    }

    fun goToExamQuestion(index: Int) {
        val current = _activeExamState.value
        if (index in current.questions.indices) {
            _activeExamState.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun submitActiveExam() {
        examTimerJob?.cancel()
        val current = _activeExamState.value
        if (current.isSubmitted) return

        viewModelScope.launch {
            _actionLoading.value = true
            _statusMessage.value = "AI grading responses and generating diagnostic report..."
            try {
                val grading = geminiService.gradeExam(current.questions, current.userAnswers)
                val updatedExam = current.exam?.copy(
                    score = grading.score,
                    totalMarks = grading.totalMarks,
                    userAnswersJson = serializeAnswers(current.userAnswers),
                    aiFeedbackJson = grading.overallFeedback,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
                if (updatedExam != null) {
                    repository.updateExam(updatedExam)
                }
                repository.addXp(grading.score)
                repository.logStudyTime(current.exam?.durationMinutes ?: 15)

                _activeExamState.value = current.copy(
                    isSubmitted = true,
                    gradingResult = grading
                )
                _statusMessage.value = "Exam Completed! Score: ${grading.score}% (+${grading.score} XP)"
            } catch (e: Exception) {
                _statusMessage.value = "Grading error: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun convertMistakesToFlashcards() {
        val current = _activeExamState.value
        val grading = current.gradingResult ?: return
        val incorrectIds = grading.questionAnalysis.filter { !it.isCorrect }.map { it.questionId }
        val incorrectQuestions = current.questions.filter { incorrectIds.contains(it.id) }

        if (incorrectQuestions.isEmpty()) {
            _statusMessage.value = "No mistakes to convert! Perfect score!"
            return
        }

        viewModelScope.launch {
            _actionLoading.value = true
            val deckId = repository.insertDeck(
                FlashcardDeck(
                    title = "Remedial: ${current.exam?.title ?: "Exam"} Mistakes",
                    subject = current.exam?.subject ?: "Exam Review",
                    description = "Automated flashcards generated from incorrectly answered exam questions.",
                    cardCount = incorrectQuestions.size
                )
            )
            val cards = incorrectQuestions.map {
                Flashcard(
                    deckId = deckId,
                    front = it.question,
                    back = "Correct Answer: ${it.correctAnswer}\n\nExplanation: ${it.explanation}",
                    hint = "Mistake from ${current.exam?.title}"
                )
            }
            repository.insertCards(cards)
            repository.addXp(40)
            _statusMessage.value = "Converted ${cards.size} mistakes into remedial flashcards!"
            _actionLoading.value = false
        }
    }

    // --- POMODORO FOCUS STUDIO & SOUNDSCAPE ---
    fun setPomodoroDuration(workMinutes: Int, breakMinutes: Int = 5, name: String = "Deep Work") {
        pomodoroTimerJob?.cancel()
        _pomodoroState.value = PomodoroState(
            isRunning = false,
            isBreak = false,
            timeRemainingSeconds = workMinutes * 60,
            totalSeconds = workMinutes * 60,
            sessionName = name,
            selectedSound = _pomodoroState.value.selectedSound,
            soundVolume = _pomodoroState.value.soundVolume
        )
    }

    fun togglePomodoroTimer() {
        val current = _pomodoroState.value
        if (current.isRunning) {
            pomodoroTimerJob?.cancel()
            soundscape.stop()
            _pomodoroState.value = current.copy(isRunning = false)
        } else {
            if (current.selectedSound != SoundMode.NONE) {
                soundscape.start(current.selectedSound)
            }
            _pomodoroState.value = current.copy(isRunning = true)
            pomodoroTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    val s = _pomodoroState.value
                    if (!s.isRunning) break
                    if (s.timeRemainingSeconds <= 1) {
                        onPomodoroBlockComplete()
                        break
                    }
                    _pomodoroState.value = s.copy(timeRemainingSeconds = s.timeRemainingSeconds - 1)
                }
            }
        }
    }

    private fun onPomodoroBlockComplete() {
        val current = _pomodoroState.value
        soundscape.stop()

        viewModelScope.launch {
            if (!current.isBreak) {
                val durationMin = current.totalSeconds / 60
                repository.insertPomodoroSession(
                    PomodoroSession(
                        taskName = current.sessionName,
                        subject = "Focus Block",
                        durationMinutes = durationMin,
                        ambientSoundUsed = current.selectedSound.displayName
                    )
                )
                repository.logStudyTime(durationMin)
                _statusMessage.value = "Focus block complete! Time for a short restorative break."
                _pomodoroState.value = current.copy(
                    isRunning = false,
                    isBreak = true,
                    timeRemainingSeconds = 5 * 60,
                    totalSeconds = 5 * 60,
                    completedSessionsCount = current.completedSessionsCount + 1,
                    triggerMicroQuiz = true
                )
            } else {
                _statusMessage.value = "Break finished! Ready to resume focus?"
                _pomodoroState.value = current.copy(
                    isRunning = false,
                    isBreak = false,
                    timeRemainingSeconds = 25 * 60,
                    totalSeconds = 25 * 60
                )
            }
        }
    }

    fun dismissMicroQuiz() {
        _pomodoroState.value = _pomodoroState.value.copy(triggerMicroQuiz = false)
    }

    fun setSoundMode(mode: SoundMode) {
        _pomodoroState.value = _pomodoroState.value.copy(selectedSound = mode)
        if (_pomodoroState.value.isRunning) {
            soundscape.start(mode)
        }
    }

    fun setSoundVolume(volume: Float) {
        _pomodoroState.value = _pomodoroState.value.copy(soundVolume = volume)
        soundscape.volume = volume
    }

    // --- EISENHOWER TASK MATRIX ---
    fun addTask(title: String, quadrant: String, dueDate: String, pomodoros: Int = 1) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                StudyTask(
                    title = title,
                    quadrant = quadrant,
                    dueDate = dueDate,
                    estimatedPomodoros = pomodoros
                )
            )
        }
    }

    fun toggleTaskComplete(task: StudyTask) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            if (updated.isCompleted) {
                repository.addXp(15)
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // --- FEYNMAN AI TECHNIQUE ---
    fun evaluateFeynmanExplanation(concept: String, persona: String, explanation: String, onResult: (FeynmanEvaluation) -> Unit) {
        if (explanation.isBlank() || concept.isBlank()) return
        viewModelScope.launch {
            _actionLoading.value = true
            _statusMessage.value = "Gemini AI auditing explanation with Feynman rubric..."
            try {
                val eval = geminiService.evaluateFeynman(concept, persona, explanation)
                repository.insertFeynmanSession(
                    FeynmanSession(
                        conceptName = concept,
                        targetDifficulty = persona,
                        userExplanation = explanation,
                        accuracyScore = eval.accuracyScore,
                        missingConceptsJson = JSONArray(eval.missingCorePillars).toString(),
                        aiCritique = eval.critique,
                        socraticQuestion = eval.socraticFollowUpQuestion
                    )
                )
                repository.addXp(eval.accuracyScore)
                _statusMessage.value = "Evaluated! Mastery Score: ${eval.accuracyScore}/100 (+${eval.accuracyScore} XP)"
                onResult(eval)
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    // Serializers
    fun serializeQuestions(list: List<ExamQuestion>): String {
        val arr = JSONArray()
        list.forEach { q ->
            arr.put(JSONObject().apply {
                put("id", q.id)
                put("question", q.question)
                put("type", q.type)
                put("options", JSONArray(q.options))
                put("correctAnswer", q.correctAnswer)
                put("explanation", q.explanation)
            })
        }
        return arr.toString()
    }

    fun deserializeQuestions(json: String): List<ExamQuestion> {
        val list = mutableListOf<ExamQuestion>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val opts = mutableListOf<String>()
                val optsArr = obj.optJSONArray("options")
                if (optsArr != null) {
                    for (j in 0 until optsArr.length()) opts.add(optsArr.getString(j))
                }
                list.add(
                    ExamQuestion(
                        id = obj.optInt("id", i + 1),
                        question = obj.optString("question"),
                        type = obj.optString("type", "MCQ"),
                        options = opts,
                        correctAnswer = obj.optString("correctAnswer"),
                        explanation = obj.optString("explanation")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeAnswers(answers: Map<Int, String>): String {
        val obj = JSONObject()
        answers.forEach { (k, v) -> obj.put(k.toString(), v) }
        return obj.toString()
    }

    override fun onCleared() {
        super.onCleared()
        soundscape.release()
        ttsManager.shutdown()
        pomodoroTimerJob?.cancel()
        examTimerJob?.cancel()
    }
}
