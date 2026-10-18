package com.example.core.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeneratedCornell(
    val cues: String,
    val notes: String,
    val summary: String,
    val mermaid: String
)

data class GeneratedCard(
    val front: String,
    val back: String,
    val hint: String = "",
    val isCloze: Boolean = false
)

data class ExamQuestion(
    val id: Int,
    val question: String,
    val type: String, // "MCQ", "TRUE_FALSE", "SHORT_ANSWER", "ESSAY"
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String
)

data class ExamGradingResult(
    val score: Int,
    val totalMarks: Int,
    val overallFeedback: String,
    val questionAnalysis: List<QuestionFeedback>
)

data class QuestionFeedback(
    val questionId: Int,
    val isCorrect: Boolean,
    val awardedMarks: Int,
    val rationale: String
)

data class FeynmanEvaluation(
    val accuracyScore: Int,
    val jargonDetected: List<String>,
    val missingCorePillars: List<String>,
    val factualErrors: List<String>,
    val critique: String,
    val socraticFollowUpQuestion: String
)

class GeminiStudyService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    // Helper to detect Bengali script
    private fun isBengaliText(text: String): Boolean {
        return text.any { it in '\u0980'..'\u09FF' }
    }

    /**
     * Calls Gemini free tier (gemini-2.5-flash / gemini-3.5-flash).
     * Zero cost on Google AI Studio Free Tier.
     */
    private suspend fun callGemini(prompt: String, jsonFormat: Boolean = false): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("NO_API_KEY")
        }

        val requestBodyObj = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                put(JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", partsArr)
                })
            }
            put("contents", contentsArr)

            if (jsonFormat) {
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }
        }

        // Using free-tier high-speed model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBodyObj.toString().toRequestBody(mediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errMessage = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}"
            }
            throw Exception(errMessage)
        }

        val resJson = JSONObject(responseBody)
        val candidates = resJson.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text") ?: ""
        text
    }

    suspend fun generateCornellNotes(content: String, subject: String): GeneratedCornell = withContext(Dispatchers.IO) {
        val isBn = isBengaliText(content) || isBengaliText(subject)
        try {
            val langInstruction = if (isBn) {
                "The user is studying in Bengali (বাংলা). Output all cues, structured notes, and summary strictly in fluent, natural Bengali (বাংলা ভাষা)."
            } else {
                "Output all cues, notes, and summary in the same language as the input."
            }

            val prompt = """
                You are a world-class cognitive learning specialist. Convert the following study material into structured Cornell Notes format.
                $langInstruction
                Subject: $subject
                Material:
                $content

                Respond ONLY with a JSON object with this exact structure:
                {
                  "cues": "Question 1?; Question 2?; Question 3?",
                  "notes": "1. Main Point\n  - Sub-point\n2. Key Mechanism\n  - Detail",
                  "summary": "Concise 2-sentence synthesis capturing the central thesis and core takeaway.",
                  "mermaid": "graph TD\n  ConceptA --> ConceptB\n  ConceptB --> ConceptC"
                }
            """.trimIndent()

            val raw = callGemini(prompt, jsonFormat = true)
            val json = JSONObject(cleanJson(raw))
            GeneratedCornell(
                cues = json.optString("cues", if (isBn) "মূল তত্ত্বটি কী?; এটি কীভাবে কাজ করে?" else "What are the core fundamentals?; How does this mechanism function?"),
                notes = json.optString("notes", content),
                summary = json.optString("summary", if (isBn) "$subject সম্পর্কিত মূল ধারণার সংক্ষিপ্তসার।" else "Summary of $subject concepts."),
                mermaid = json.optString("mermaid", "graph TD\n  Start --> Next")
            )
        } catch (e: Exception) {
            // Free offline pedagogical generator
            generateOfflineCornell(content, subject, isBn)
        }
    }

    suspend fun generateFlashcards(content: String, subject: String, count: Int = 6): List<GeneratedCard> = withContext(Dispatchers.IO) {
        val isBn = isBengaliText(content) || isBengaliText(subject)
        try {
            val langInstruction = if (isBn) {
                "The study material is in Bengali (বাংলা). Generate high-yield flashcards in clear, correct Bengali (বাংলা ভাষা)."
            } else {
                "Generate flashcards in the language of the provided text."
            }

            val prompt = """
                Extract $count high-yield flashcards from this study material on '$subject'.
                $langInstruction
                Ensure at least 1-2 cards use Cloze Deletion formatting where key terms are enclosed in square brackets like [...].
                Material:
                $content

                Respond ONLY with a JSON array of objects with keys: "front", "back", "hint", "isCloze" (boolean).
            """.trimIndent()

            val raw = callGemini(prompt, jsonFormat = true)
            val arr = JSONArray(cleanJson(raw))
            val cards = mutableListOf<GeneratedCard>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                cards.add(
                    GeneratedCard(
                        front = item.optString("front"),
                        back = item.optString("back"),
                        hint = item.optString("hint", ""),
                        isCloze = item.optBoolean("isCloze", false)
                    )
                )
            }
            if (cards.isNotEmpty()) cards else generateOfflineFlashcards(content, subject, isBn)
        } catch (e: Exception) {
            generateOfflineFlashcards(content, subject, isBn)
        }
    }

    suspend fun generateMockExam(topic: String, subject: String, count: Int = 5, difficulty: String = "Standard"): List<ExamQuestion> = withContext(Dispatchers.IO) {
        val isBn = isBengaliText(topic) || isBengaliText(subject)
        try {
            val langInstruction = if (isBn) {
                "The exam is for a Bengali-medium student. Generate all questions, options, answers, and explanations strictly in standard Bengali (বাংলা ভাষা)."
            } else {
                "Generate all questions, options, and explanations in standard academic English."
            }

            val prompt = """
                Generate a $count-question academic mock exam on '$topic' ($subject) with difficulty '$difficulty'.
                $langInstruction
                Include a mix of Multiple Choice (MCQ), True/False (TRUE_FALSE), and Short Answer (SHORT_ANSWER).
                For MCQ, include 4 options in 'options'.
                For TRUE_FALSE, include options (e.g. ["সত্য", "মিথ্যা"] in Bengali or ["True", "False"]).
                For SHORT_ANSWER, options can be empty.
                Always provide 'correctAnswer' and an in-depth 'explanation'.

                Respond ONLY with a JSON array of objects:
                [
                  {
                    "id": 1,
                    "question": "Question text...",
                    "type": "MCQ",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctAnswer": "Option A",
                    "explanation": "Why Option A is correct..."
                  }
                ]
            """.trimIndent()

            val raw = callGemini(prompt, jsonFormat = true)
            val arr = JSONArray(cleanJson(raw))
            val list = mutableListOf<ExamQuestion>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val optsArr = obj.optJSONArray("options")
                val opts = mutableListOf<String>()
                if (optsArr != null) {
                    for (j in 0 until optsArr.length()) {
                        opts.add(optsArr.getString(j))
                    }
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
            if (list.isNotEmpty()) list else generateOfflineExam(topic, subject, isBn)
        } catch (e: Exception) {
            generateOfflineExam(topic, subject, isBn)
        }
    }

    suspend fun gradeExam(questions: List<ExamQuestion>, userAnswers: Map<Int, String>): ExamGradingResult = withContext(Dispatchers.IO) {
        var correctCount = 0
        val feedbackList = mutableListOf<QuestionFeedback>()
        val isBn = questions.any { isBengaliText(it.question) }

        for (q in questions) {
            val userAns = userAnswers[q.id]?.trim() ?: ""
            val isCorrect: Boolean
            val awardedMarks: Int

            if (q.type == "MCQ" || q.type == "TRUE_FALSE") {
                isCorrect = userAns.equals(q.correctAnswer.trim(), ignoreCase = true)
                awardedMarks = if (isCorrect) 100 / questions.size else 0
            } else {
                val normalizedUser = userAns.lowercase()
                val normalizedTarget = q.correctAnswer.lowercase()
                val match = normalizedUser.isNotBlank() && (
                    normalizedUser.contains(normalizedTarget) ||
                    normalizedTarget.contains(normalizedUser) ||
                    levenshteinSimilarity(normalizedUser, normalizedTarget) > 0.65
                )
                isCorrect = match
                awardedMarks = if (isCorrect) 100 / questions.size else (100 / questions.size / 2).coerceAtLeast(0)
            }

            if (isCorrect) correctCount++
            val rationaleText = if (isBn) {
                if (isCorrect) "সঠিক উত্তর! ${q.explanation}" else "ভুল উত্তর। সঠিক উত্তর ছিল: '${q.correctAnswer}'। ${q.explanation}"
            } else {
                if (isCorrect) "Correct! ${q.explanation}" else "Incorrect. Expected: '${q.correctAnswer}'. ${q.explanation}"
            }

            feedbackList.add(
                QuestionFeedback(
                    questionId = q.id,
                    isCorrect = isCorrect,
                    awardedMarks = awardedMarks,
                    rationale = rationaleText
                )
            )
        }

        val totalScore = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0
        val overallFeedback = if (isBn) {
            when {
                totalScore >= 80 -> "দারুণ Performance! তুমি Topic-টা খুব ভালো বুঝেছো।"
                totalScore >= 60 -> "ভালো প্রস্তুতি! কিছু জায়গায় ভুল হয়েছে, সেগুলো Flashcards দিয়ে Review করে নাও।"
                else -> "আরেকটু Review করা দরকার। ভুল উত্তরগুলো Flashcards-এ নিয়ে আবার Practice করো।"
            }
        } else {
            when {
                totalScore >= 80 -> "Outstanding mastery! You demonstrated thorough conceptual understanding."
                totalScore >= 60 -> "Solid grasp with a few knowledge gaps to reinforce with spaced repetition."
                else -> "Needs revision. Convert incorrect questions to flashcards for active recall practice."
            }
        }

        ExamGradingResult(
            score = totalScore,
            totalMarks = 100,
            overallFeedback = overallFeedback,
            questionAnalysis = feedbackList
        )
    }

    suspend fun evaluateFeynman(concept: String, persona: String, explanation: String): FeynmanEvaluation = withContext(Dispatchers.IO) {
        val isBn = isBengaliText(concept) || isBengaliText(explanation)
        try {
            val langInstruction = if (isBn) {
                "The student explained in code-mixed Bengali & English (Banglish style: modern natural Bengali mixed with standard English terms like Topic, Concept, Example, Jargon). Deliver all critique, jargon detection, and the Socratic question in a friendly, conversational mixed Bengali + English style, addressing the student casually as 'তুমি' (tumi), never use formal 'আপনি'."
            } else {
                "Deliver all analysis in the user's input language."
            }

            val prompt = """
                Evaluate this student's explanation using the Feynman Technique.
                $langInstruction
                Target Concept: '$concept'
                Target Audience Persona: '$persona' (e.g. 5-Year-Old, Peer, University Professor).
                Student Explanation:
                "$explanation"

                Analyze if the student used unexplained jargon, missed core foundational principles, or stated any factual errors.
                Generate a constructive critique and a Socratic follow-up question that gently exposes gaps in understanding.
                Remember to address the student as 'তুমি' (tumi) in Bengali.

                Respond ONLY with JSON:
                {
                  "accuracyScore": 85,
                  "jargonDetected": ["জটিল শব্দ ১", "জটিল শব্দ ২"],
                  "missingCorePillars": ["মূল স্তম্ভ ১"],
                  "factualErrors": [],
                  "critique": "গঠনমূলক ফিডব্যাক...",
                  "socraticFollowUpQuestion": "একটি সহজ প্রশ্ন..."
                }
            """.trimIndent()

            val raw = callGemini(prompt, jsonFormat = true)
            val json = JSONObject(cleanJson(raw))
            val jargon = json.optJSONArray("jargonDetected")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList()
            val missing = json.optJSONArray("missingCorePillars")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList()
            val errors = json.optJSONArray("factualErrors")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList()

            FeynmanEvaluation(
                accuracyScore = json.optInt("accuracyScore", 80),
                jargonDetected = jargon,
                missingCorePillars = missing,
                factualErrors = errors,
                critique = json.optString("critique", if (isBn) "Explanation-টা overall দারুণ হয়েছে! সহজ Real-life Example দিয়ে এটা আরও স্পষ্ট করো।" else "Clear explanation overall. Work on simplifying technical terms."),
                socraticFollowUpQuestion = json.optString("socraticFollowUpQuestion", if (isBn) "বাস্তব জীবনের কোন সাধারণ Example দিয়ে তুমি এটি বোঝাবে?" else "What physical constraint prevents this from happening spontaneously?")
            )
        } catch (e: Exception) {
            generateOfflineFeynman(concept, persona, explanation, isBn)
        }
    }

    private fun cleanJson(text: String): String {
        return text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun levenshteinSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1
        if (longer.isEmpty()) return 1.0
        val dist = levenshteinDistance(longer, shorter)
        return (longer.length - dist).toDouble() / longer.length.toDouble()
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1)
        for (j in costs.indices) costs[j] = j
        for (i in 1..s1.length) {
            costs[0] = i
            var nw = i - 1
            for (j in 1..s2.length) {
                val cj = 1 + minOf(costs[j], costs[j - 1])
                val replace = nw + if (s1[i - 1] == s2[j - 1]) 0 else 1
                val cost = minOf(cj, replace)
                nw = costs[j]
                costs[j] = cost
            }
        }
        return costs[s2.length]
    }

    // High quality offline fallback generators with zero cost
    private fun generateOfflineCornell(content: String, subject: String, isBn: Boolean): GeneratedCornell {
        val lines = content.split("\n", "।", ".").filter { it.trim().length > 10 }
        if (isBn) {
            val cues = "১. ${subject}-এর মূল সংজ্ঞা ও তাৎপর্য কী?;\n২. এই প্রক্রিয়ার ধাপসমূহ কী কী?;\n৩. বাস্তব ক্ষেত্রে এর প্রয়োগ কোথায়?"
            val notes = "• মূল তত্ত্ব: ${lines.getOrNull(0)?.trim() ?: content}\n" +
                    "• কার্যপদ্ধতি: ${lines.getOrNull(1)?.trim() ?: "নিয়ন্ত্রিত শারীরবৃত্তীয় বা তাত্ত্বিক প্রক্রিয়া।"}\n" +
                    "• প্রায়োগিক গুরুত্ব: ${lines.getOrNull(2)?.trim() ?: "দৈনন্দিন জীবন ও বৈজ্ঞানিক গবেষণায় বিশেষ ভূমিকা পালন করে।"}"
            val summary = "$subject সম্পর্কিত আলোচনায় দেখা যায় যে এটি একটি সুনির্দিষ্ট চক্রাকার প্রক্রিয়া যা শক্তির রূপান্তর ও ভারসাম্যে সহায়ক।"
            val mermaid = "graph TD\n  বিষয় --> মূল_তত্ত্ব\n  মূল_তত্ত্ব --> কার্যপদ্ধতি\n  কার্যপদ্ধতি --> প্রয়োগ"
            return GeneratedCornell(cues, notes, summary, mermaid)
        } else {
            val cues = if (lines.size >= 3) {
                "What is the definition of ${lines[0].take(30)}...?; How does this relate to $subject?; What are the critical implications?"
            } else {
                "What is the central concept?; Why does this matter?; How is it applied?"
            }
            val notes = "• Core Concept: ${lines.getOrNull(0)?.trim() ?: content}\n" +
                    "• Key Mechanism: ${lines.getOrNull(1)?.trim() ?: "Primary operational pathway."}\n" +
                    "• Application: ${lines.getOrNull(2)?.trim() ?: "Key academic or practical relevance."}"
            val summary = "In $subject, this topic highlights foundational principles that govern systemic behavior and practical applications."
            val mermaid = "graph TD\n  $subject --> CoreTheory\n  CoreTheory --> KeyMechanism\n  KeyMechanism --> Application"
            return GeneratedCornell(cues, notes, summary, mermaid)
        }
    }

    private fun generateOfflineFlashcards(content: String, subject: String, isBn: Boolean): List<GeneratedCard> {
        if (isBn) {
            return listOf(
                GeneratedCard(
                    front = "$subject-এর প্রধান বৈশিষ্ট্য বা সূত্রটি কী?",
                    back = content.take(160),
                    hint = "সংজ্ঞা মনে করার চেষ্টা করো"
                ),
                GeneratedCard(
                    front = "শূন্যস্থান পূরণ করো: $subject প্রক্রিয়ায় [...] গুরুত্বপূর্ণ ভূমিকা পালন করে।",
                    back = "$subject প্রক্রিয়ায় [শক্তির রূপান্তর ও রাসায়নিক ভারসাম্য] গুরুত্বপূর্ণ ভূমিকা পালন করে।",
                    hint = "শক্তি ও ভারসাম্য",
                    isCloze = true
                ),
                GeneratedCard(
                    front = "এই ধারণাটির সাথে প্রথাগত নিয়মের মূল পার্থক্য কোথায়?",
                    back = "এটি একটি স্বতঃস্ফূর্ত ও গতিশীল নিয়ামক দ্বারা পরিচালিত হয়।",
                    hint = "গতিশীল নিয়ম"
                )
            )
        } else {
            return listOf(
                GeneratedCard(
                    front = "What is the primary governing principle of $subject discussed here?",
                    back = content.take(160),
                    hint = "Core definition"
                ),
                GeneratedCard(
                    front = "Fill in the blank: In $subject, [...] plays a foundational role in operational efficiency.",
                    back = "In $subject, [systemic regulation and feedback] plays a foundational role in operational efficiency.",
                    hint = "Regulation term",
                    isCloze = true
                ),
                GeneratedCard(
                    front = "How does this concept differ from standard foundational models?",
                    back = "It integrates multi-variable interactions and adaptive feedback loops.",
                    hint = "Think about dynamic systems"
                )
            )
        }
    }

    private fun generateOfflineExam(topic: String, subject: String, isBn: Boolean): List<ExamQuestion> {
        if (isBn) {
            return listOf(
                ExamQuestion(
                    id = 1,
                    question = "$subject বিষয়ে '$topic'-এর মূল কাজ বা তাৎপর্য নিচের কোনটি?",
                    type = "MCQ",
                    options = listOf(
                        "এটি নিয়ন্ত্রিত ধাপে শক্তির রূপান্তর ও ভারসাম্য বজায় রাখে।",
                        "এটি কোনো শক্তি ব্যয় বা রূপান্তর ছাড়াই স্থির থাকে।",
                        "এটি সম্পূর্ণ বিশৃঙ্খল ও নিয়মহীনভাবে ঘটে।",
                        "এটি কেবল চরম শূন্যস্থানেই কার্যকর হয়।"
                    ),
                    correctAnswer = "এটি নিয়ন্ত্রিত ধাপে শক্তির রূপান্তর ও ভারসাম্য বজায় রাখে।",
                    explanation = "$subject বিষয়ে $topic একটি সুশৃঙ্খল প্রক্রিয়া যা নিয়মতান্ত্রিক রূপান্তরের মাধ্যমে সম্পন্ন হয়।"
                ),
                ExamQuestion(
                    id = 2,
                    question = "সত্য নাকি মিথ্যা: $topic প্রাকৃতিক শক্তির নিত্যতা সূত্র মেনে চলে।",
                    type = "TRUE_FALSE",
                    options = listOf("সত্য", "মিথ্যা"),
                    correctAnswer = "সত্য",
                    explanation = "প্রকৃতির সকল ভৌতিক ও জৈব রাসায়নিক প্রক্রিয়া শক্তির সংরক্ষণশীলতা নীতি মেনে চলে।"
                ),
                ExamQuestion(
                    id = 3,
                    question = "$topic প্রক্রিয়ার প্রধান উৎপাদ কী?",
                    type = "SHORT_ANSWER",
                    options = emptyList(),
                    correctAnswer = "শক্তি",
                    explanation = "শক্তি (ATP বা তাপীয় শক্তি) হলো এই সামগ্রিক রূপান্তরের প্রধান চালিকাশক্তি।"
                )
            )
        } else {
            return listOf(
                ExamQuestion(
                    id = 1,
                    question = "Which statement best characterizes the primary mechanism of $topic in $subject?",
                    type = "MCQ",
                    options = listOf(
                        "It converts fundamental inputs into stable systemic outputs through regulated pathways.",
                        "It acts entirely as a passive inert medium without energy transformation.",
                        "It operates randomly without predictable physical or logical laws.",
                        "It solely occurs under extreme laboratory vacuum conditions."
                    ),
                    correctAnswer = "It converts fundamental inputs into stable systemic outputs through regulated pathways.",
                    explanation = "In $subject, $topic represents an active, structured process converting inputs via regulated cycles."
                ),
                ExamQuestion(
                    id = 2,
                    question = "True or False: $topic operates independently of environmental energy constraints.",
                    type = "TRUE_FALSE",
                    options = listOf("True", "False"),
                    correctAnswer = "False",
                    explanation = "All natural and scientific systems are subject to conservation laws and thermodynamic constraints."
                ),
                ExamQuestion(
                    id = 3,
                    question = "What is the key functional output generated by $topic?",
                    type = "SHORT_ANSWER",
                    options = emptyList(),
                    correctAnswer = "Energy",
                    explanation = "Energy (or functional equilibrium) is the universal driver and primary outcome."
                )
            )
        }
    }

    private fun generateOfflineFeynman(concept: String, persona: String, explanation: String, isBn: Boolean): FeynmanEvaluation {
        val wordCount = explanation.split("\\s+".toRegex()).size
        val score = when {
            wordCount > 30 -> 88
            wordCount > 15 -> 76
            else -> 62
        }

        if (isBn) {
            val detectedJargon = if (explanation.contains("মেকানিজম", ignoreCase = true) || explanation.contains("সিস্টেম", ignoreCase = true)) {
                listOf("মেকানিজম", "সিস্টেম")
            } else {
                emptyList()
            }
            return FeynmanEvaluation(
                accuracyScore = score,
                jargonDetected = detectedJargon,
                missingCorePillars = listOf("Real-life Example", "Boundary Conditions"),
                factualErrors = emptyList(),
                critique = "তুমি Concept-টির মূল ভাব ভালো বুঝেছো। $persona-এর জন্য কঠিন শব্দের বদলে Real-life Example দিলে Concept Clear হওয়া অনেক সহজ হবে।",
                socraticFollowUpQuestion = "ঘরের বা ক্লাসের কোনো সাধারণ Example দিয়ে তুমি এই ঘটনাটি কীভাবে বোঝাবে?"
            )
        } else {
            val detectedJargon = if (explanation.contains("mechanism", ignoreCase = true) || explanation.contains("system", ignoreCase = true)) {
                listOf("mechanism", "equilibrium")
            } else {
                emptyList()
            }
            return FeynmanEvaluation(
                accuracyScore = score,
                jargonDetected = detectedJargon,
                missingCorePillars = listOf("Real-world intuitive analogy", "Constraint boundary conditions"),
                factualErrors = emptyList(),
                critique = "You clearly understand the central gist for a $persona. To make it unforgettable, anchor it with an everyday physical analogy.",
                socraticFollowUpQuestion = "If you had to demonstrate this concept to a child using only objects found in a kitchen, how would you model it?"
            )
        }
    }
}
