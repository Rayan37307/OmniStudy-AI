package com.example

import com.example.core.sm2.Sm2Engine
import com.example.core.sm2.Sm2Rating
import com.example.data.model.Flashcard
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun sm2_initialCardGradedEasy_incrementsRepetitionAndCalculatesInterval() {
    val card = Flashcard(
      deckId = 1L,
      front = "What is ATP?",
      back = "Adenosine Triphosphate",
      repetition = 0,
      interval = 1,
      easinessFactor = 2.5f
    )

    val updated = Sm2Engine.calculateNextReview(card, Sm2Rating.EASY)
    assertEquals(1, updated.repetition)
    assertEquals(1, updated.interval)
    assertTrue("EF should increase for Easy rating", updated.easinessFactor >= 2.5f)
  }

  @Test
  fun sm2_cardGradedAgain_resetsRepetitionAndInterval() {
    val card = Flashcard(
      deckId = 1L,
      front = "Question",
      back = "Answer",
      repetition = 4,
      interval = 15,
      easinessFactor = 2.3f
    )

    val updated = Sm2Engine.calculateNextReview(card, Sm2Rating.AGAIN)
    assertEquals(0, updated.repetition)
    assertEquals(1, updated.interval)
  }

  @Test
  fun sm2_easinessFactorNeverDropsBelowMinimum() {
    var card = Flashcard(
      deckId = 1L,
      front = "Hard Question",
      back = "Difficult Answer",
      repetition = 0,
      interval = 1,
      easinessFactor = 1.35f
    )

    // Grade AGAIN multiple times
    repeat(5) {
      card = Sm2Engine.calculateNextReview(card, Sm2Rating.AGAIN)
    }

    assertTrue("EF should be at least 1.3", card.easinessFactor >= 1.3f)
  }
}
