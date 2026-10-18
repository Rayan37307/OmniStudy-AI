package com.example.data.repository

import com.example.data.dao.StudyDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class StudyRepository(private val dao: StudyDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allMaterials: Flow<List<StudyMaterial>> = dao.getAllMaterials()
    val allDecks: Flow<List<FlashcardDeck>> = dao.getAllDecks()
    val allCards: Flow<List<Flashcard>> = dao.getAllCards()
    val allExams: Flow<List<ExamMock>> = dao.getAllExams()
    val allTasks: Flow<List<StudyTask>> = dao.getAllTasks()
    val allPomodoroSessions: Flow<List<PomodoroSession>> = dao.getAllPomodoroSessions()
    val allFeynmanSessions: Flow<List<FeynmanSession>> = dao.getAllFeynmanSessions()

    fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>> = dao.getCardsByDeck(deckId)
    fun getDueCards(currentTime: Long): Flow<List<Flashcard>> = dao.getDueCards(currentTime)

    suspend fun insertOrUpdateProfile(profile: UserProfile) = dao.insertOrUpdateProfile(profile)
    suspend fun insertMaterial(material: StudyMaterial) = dao.insertMaterial(material)
    suspend fun deleteMaterial(id: Long) = dao.deleteMaterial(id)
    suspend fun getMaterialById(id: Long) = dao.getMaterialById(id)

    suspend fun insertDeck(deck: FlashcardDeck) = dao.insertDeck(deck)
    suspend fun deleteDeck(id: Long) = dao.deleteDeck(id)

    suspend fun insertCard(card: Flashcard) = dao.insertCard(card)
    suspend fun insertCards(cards: List<Flashcard>) = dao.insertCards(cards)
    suspend fun updateCard(card: Flashcard) = dao.updateCard(card)
    suspend fun deleteCard(id: Long) = dao.deleteCard(id)
    suspend fun getCardsByDeckOnce(deckId: Long) = dao.getCardsByDeckOnce(deckId)

    suspend fun insertExam(exam: ExamMock) = dao.insertExam(exam)
    suspend fun updateExam(exam: ExamMock) = dao.updateExam(exam)
    suspend fun getExamById(id: Long) = dao.getExamById(id)

    suspend fun insertPomodoroSession(session: PomodoroSession) = dao.insertPomodoroSession(session)

    suspend fun insertTask(task: StudyTask) = dao.insertTask(task)
    suspend fun updateTask(task: StudyTask) = dao.updateTask(task)
    suspend fun deleteTask(id: Long) = dao.deleteTask(id)

    suspend fun insertFeynmanSession(session: FeynmanSession) = dao.insertFeynmanSession(session)

    suspend fun addXp(amount: Int) {
        val current = dao.getUserProfile().firstOrNull() ?: UserProfile()
        dao.insertOrUpdateProfile(current.copy(xpPoints = current.xpPoints + amount))
    }

    suspend fun logStudyTime(minutes: Int) {
        val current = dao.getUserProfile().firstOrNull() ?: UserProfile()
        dao.insertOrUpdateProfile(
            current.copy(
                minutesStudiedToday = current.minutesStudiedToday + minutes,
                xpPoints = current.xpPoints + (minutes * 5)
            )
        )
    }

    suspend fun checkSeedData() {
        val existingProfile = dao.getUserProfile().firstOrNull()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    fullName = "তাহমিদ হাসান",
                    studyStreak = 7,
                    xpPoints = 520,
                    dailyGoalMinutes = 60,
                    minutesStudiedToday = 40
                )
            )

            // Seed Bengali study material (সালোকসংশ্লেষণ ও জৈব শক্তি রূপান্তর)
            val matId1 = dao.insertMaterial(
                StudyMaterial(
                    title = "সালোকসংশ্লেষণ ও জৈব শক্তি চক্র",
                    subject = "জীববিজ্ঞান (Biology)",
                    rawContent = "সালোকসংশ্লেষণ হলো এমন একটি জৈব রাসায়নিক প্রক্রিয়া যার মাধ্যমে সবুজ উদ্ভিদ সূর্যালোকের উপস্থিতিতে ক্লোরোফিলের সাহায্যে পানি ও কার্বন ডাই-অক্সাইড ব্যবহার করে শর্করা জাতীয় খাদ্য ও উপজাত হিসেবে অক্সিজেন প্রস্তুত করে। এটি দুটি প্রধান পর্যায়ে সম্পন্ন হয়: আলোক নির্ভর পর্যায় (থাইলাকয়েড ঝিল্লিতে ঘটে) এবং আলোক নিরপেক্ষ পর্যায় বা কেলভিন চক্র (স্ট্রোমাতে ঘটে)।",
                    cornellCues = "১. সালোকসংশ্লেষণের আলোক পর্যায়ে কী কী শক্তি অণু তৈরি হয়?;\n২. পানির ফোটোলাইসিস প্রক্রিয়া কী?;\n৩. কেলভিন চক্র ক্লোরোপ্লাস্টের কোথায় সম্পন্ন হয়?;\n৪. সালোকসংশ্লেষণে নির্গত অক্সিজেনের মূল উৎস কোনটি?",
                    cornellNotes = "• আলোক পর্যায়: থাইলাকয়েডে ক্লোরোফিল সৌরশক্তি গ্রহণ করে। পানির অণু বিশ্লিষ্ট হয়ে (Photolysis) H+, ইলেকট্রন এবং O2 গ্যাস উৎপন্ন করে। একইসাথে ফটোফসফোরাইলেশনের মাধ্যমে ATP এবং NADPH প্রস্তুত হয়।\n• অন্ধকার পর্যায় (কেলভিন চক্র): স্ট্রোমায় RuBisCO এনজাইমের সহযোগিতায় বায়ুর CO2 জৈব শর্করায় রূপান্তরিত হয়। এই ধাপে পূর্ববর্তী ধাপের ATP ও NADPH শক্তি সরবরাহ করে।\n• সামগ্রিক বিক্রিয়া: 6CO2 + 12H2O + আলো -> C6H12O6 + 6H2O + 6O2",
                    cornellSummary = "সালোকসংশ্লেষণ সৌরশক্তিকে রাসায়নিক শক্তিতে রূপান্তর করার সার্বজনীন প্রাকৃতিক সেতু, যা পৃথিবীর সকল জীবের খাদ্য ও অক্সিজেনের একমাত্র প্রধান প্রাকৃতিক উৎস।",
                    mindmapMermaid = "graph TD\n  সূর্যালোক --> থাইলাকয়েড_ঝিল্লি\n  পানি --> ফোটোলাইসিস\n  ফোটোলাইসিস --> অক্সিজেন_ও_শক্তি\n  অক্সিজেন_ও_শক্তি --> ATP_ও_NADPH\n  ATP_ও_NADPH --> কেলভিন_চক্র_স্ট্রোমা\n  বায়ুমণ্ডলের_CO2 --> কেলভিন_চক্র_স্ট্রোমা\n  কেলভিন_চক্র_স্ট্রোমা --> শর্করা_খাদ্য"
                )
            )

            // Seed Bengali Flashcard Deck
            val deckId1 = dao.insertDeck(
                FlashcardDeck(
                    title = "জীববিজ্ঞান ও আধুনিক বিজ্ঞান",
                    subject = "বিজ্ঞান (General Science)",
                    description = "উচ্চমাধ্যমিক জীববিজ্ঞান, কোষ তত্ত্ব ও পদার্থবিজ্ঞানের মৌলিক ধারণা।",
                    cardCount = 4
                )
            )

            dao.insertCards(
                listOf(
                    Flashcard(
                        deckId = deckId1,
                        front = "সালোকসংশ্লেষণের অন্ধকার পর্যায় বা কেলভিন চক্র ক্লোরোপ্লাস্টের কোথায় ঘটে?",
                        back = "স্ট্রোমাতে (Stroma)। এটি সম্পন্ন হতে সরাসরি সূর্যালোকের প্রয়োজন হয় না।",
                        hint = "ক্লোরোপ্লাস্টের ভেতরের তরল ধাত্র"
                    ),
                    Flashcard(
                        deckId = deckId1,
                        front = "উদ্ভিদ কোষে সূর্যালোকের সাহায্যে পানি বিশ্লিষ্ট হয়ে অক্সিজেন তৈরির প্রক্রিয়াকে [...] বলে।",
                        back = "উদ্ভিদ কোষে সূর্যালোকের সাহায্যে পানি বিশ্লিষ্ট হয়ে অক্সিজেন তৈরির প্রক্রিয়াকে [ফোটোলাইসিস (Photolysis)] বলে।",
                        hint = "ফোটন কণা দ্বারা পানির বিভাজন",
                        isCloze = true
                    ),
                    Flashcard(
                        deckId = deckId1,
                        front = "কোষের সার্বজনীন শক্তি মুদ্রা বা Energy Currency কাকে বলা হয়?",
                        back = "এটিপি (ATP - Adenosine Triphosphate)",
                        hint = "ফসফেট বন্ধনে শক্তি সঞ্চিত থাকে"
                    ),
                    Flashcard(
                        deckId = deckId1,
                        front = "নিউটনের মহাকর্ষ সূত্র অনুযায়ী দুটি বস্তুর মধ্যকার আকর্ষণ বল তাদের দূরত্বের [...] সমানুপাতিক।",
                        back = "নিউটনের মহাকর্ষ সূত্র অনুযায়ী দুটি বস্তুর মধ্যকার আকর্ষণ বল তাদের দূরত্বের [বর্গের ব্যস্তানুপাতিক (Inversely proportional to square of distance)]।",
                        hint = "F ∝ 1 / r²",
                        isCloze = true
                    )
                )
            )

            // Seed Bengali Study Tasks (Eisenhower Matrix)
            dao.insertTask(
                StudyTask(
                    title = "জীববিজ্ঞান: কেলভিন চক্রের কর্নাল কিউ রিভিশন",
                    quadrant = "urgent_important",
                    dueDate = "আজ বিকেল ৫:০০",
                    estimatedPomodoros = 2
                )
            )
            dao.insertTask(
                StudyTask(
                    title = "কোয়ান্টাম তত্ত্ব ফেয়ম্যান পদ্ধতিতে সহজ বাংলায় বুঝিয়ে বলা",
                    quadrant = "not_urgent_important",
                    dueDate = "আগামীকাল রাত ৯:০০",
                    estimatedPomodoros = 3
                )
            )
            dao.insertTask(
                StudyTask(
                    title = "পদার্থবিজ্ঞানের মৌলিক এককসমূহের ফ্ল্যাশকার্ড তৈরি",
                    quadrant = "urgent_not_important",
                    dueDate = "আজ রাত ৮:০০",
                    estimatedPomodoros = 1
                )
            )
            dao.insertTask(
                StudyTask(
                    title = "অপ্রয়োজনীয় পাঠ্যপুস্তক সূচিপত্র ব্রাউজিং",
                    quadrant = "neither",
                    dueDate = "ঐচ্ছিক",
                    estimatedPomodoros = 1
                )
            )
        }
    }
}
