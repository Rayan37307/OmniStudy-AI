package com.example.core.sm2

import com.example.data.model.Flashcard
import kotlin.math.max
import kotlin.math.roundToInt

enum class Sm2Rating(val grade: Int, val label: String) {
    AGAIN(0, "Again"),
    HARD(3, "Hard"),
    GOOD(4, "Good"),
    EASY(5, "Easy")
}

object Sm2Engine {

    /**
     * Calculates the next review parameters using the SuperMemo-2 (SM-2) algorithm.
     *
     * @param card Current flashcard state
     * @param rating Rating grade given by the user (Again=0, Hard=3, Good=4, Easy=5)
     * @param currentTime Current timestamp in milliseconds
     * @return Updated flashcard with new repetition, interval, EF, and nextReviewDate
     */
    fun calculateNextReview(
        card: Flashcard,
        rating: Sm2Rating,
        currentTime: Long = System.currentTimeMillis()
    ): Flashcard {
        val q = rating.grade
        val currentEf = card.easinessFactor
        val currentRep = card.repetition
        val currentInterval = card.interval

        // Calculate new Easiness Factor (EF)
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val newEf = max(
            1.3f,
            currentEf + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        )

        val newRepetition: Int
        val newInterval: Int

        if (q < 3) {
            // Failed recall: reset repetitions to 0 and interval to 1 day
            newRepetition = 0
            newInterval = 1
        } else {
            // Successful recall
            if (currentRep == 0) {
                newInterval = 1
            } else if (currentRep == 1) {
                newInterval = 6
            } else {
                newInterval = (currentInterval * newEf).roundToInt()
            }
            newRepetition = currentRep + 1
        }

        val millisInDay = 24L * 60L * 60L * 1000L
        val nextReviewDate = currentTime + (newInterval * millisInDay)

        return card.copy(
            repetition = newRepetition,
            interval = newInterval,
            easinessFactor = newEf,
            nextReviewDate = nextReviewDate,
            lastReviewedAt = currentTime
        )
    }
}
