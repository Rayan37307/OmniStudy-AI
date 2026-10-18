package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // Study Materials
    @Query("SELECT * FROM study_materials ORDER BY createdAt DESC")
    fun getAllMaterials(): Flow<List<StudyMaterial>>

    @Query("SELECT * FROM study_materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: Long): StudyMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterial): Long

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteMaterial(id: Long)

    // Flashcard Decks
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<FlashcardDeck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeck): Long

    @Query("DELETE FROM flashcard_decks WHERE id = :id")
    suspend fun deleteDeck(id: Long)

    // Flashcards
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    suspend fun getCardsByDeckOnce(deckId: Long): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentTime")
    fun getDueCards(currentTime: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards")
    fun getAllCards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Flashcard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Flashcard>)

    @Update
    suspend fun updateCard(card: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    // Exams
    @Query("SELECT * FROM exam_mocks ORDER BY createdAt DESC")
    fun getAllExams(): Flow<List<ExamMock>>

    @Query("SELECT * FROM exam_mocks WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): ExamMock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamMock): Long

    @Update
    suspend fun updateExam(exam: ExamMock)

    // Pomodoro Sessions
    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAt DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSession)

    // Study Tasks
    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask): Long

    @Update
    suspend fun updateTask(task: StudyTask)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // Feynman Sessions
    @Query("SELECT * FROM feynman_sessions ORDER BY createdAt DESC")
    fun getAllFeynmanSessions(): Flow<List<FeynmanSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeynmanSession(session: FeynmanSession): Long
}
