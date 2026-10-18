package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Scholar",
    val studyStreak: Int = 3,
    val lastStudyDate: Long = System.currentTimeMillis(),
    val xpPoints: Int = 350,
    val dailyGoalMinutes: Int = 60,
    val minutesStudiedToday: Int = 25
)

@Entity(tableName = "study_materials")
data class StudyMaterial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val rawContent: String,
    val cornellCues: String, // JSON or newline separated cues
    val cornellNotes: String,
    val cornellSummary: String,
    val mindmapMermaid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcard_decks")
data class FlashcardDeck(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val description: String,
    val cardCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val hint: String = "",
    val repetition: Int = 0,
    val interval: Int = 1,
    val easinessFactor: Float = 2.5f,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long = 0,
    val isCloze: Boolean = false
)

@Entity(tableName = "exam_mocks")
data class ExamMock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val durationMinutes: Int,
    val questionsJson: String, // serialized List<ExamQuestion>
    val userAnswersJson: String = "{}", // map of index to answer
    val score: Int = 0,
    val totalMarks: Int = 100,
    val aiFeedbackJson: String = "{}",
    val isCompleted: Boolean = false,
    val completedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskName: String,
    val subject: String,
    val durationMinutes: Int,
    val ambientSoundUsed: String,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val quadrant: String, // urgent_important, not_urgent_important, urgent_not_important, neither
    val dueDate: String,
    val isCompleted: Boolean = false,
    val estimatedPomodoros: Int = 1
)

@Entity(tableName = "feynman_sessions")
data class FeynmanSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conceptName: String,
    val targetDifficulty: String, // eli5, high_school, university
    val userExplanation: String,
    val accuracyScore: Int,
    val missingConceptsJson: String,
    val aiCritique: String,
    val socraticQuestion: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
