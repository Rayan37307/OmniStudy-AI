package com.example.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Bengali Locale first
                val bnLocale = Locale.forLanguageTag("bn-BD")
                val result = tts?.setLanguage(bnLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val inBnLocale = Locale.forLanguageTag("bn-IN")
                    val inResult = tts?.setLanguage(inBnLocale)
                    if (inResult == TextToSpeech.LANG_MISSING_DATA || inResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.language = Locale.US
                    }
                }
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun setLanguage(languageCode: String) {
        if (!isInitialized) return
        val targetLocale = if (languageCode.equals("bn", ignoreCase = true)) {
            Locale.forLanguageTag("bn-BD")
        } else {
            Locale.US
        }
        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.US
        }
    }

    fun setRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.5f)
        _speechRate.value = clamped
        tts?.setSpeechRate(clamped)
    }

    fun speak(text: String, isBengali: Boolean = false) {
        if (!isInitialized) return
        stop()
        if (isBengali) {
            setLanguage("bn")
        }
        tts?.setSpeechRate(_speechRate.value)
        val utteranceId = "OmniStudy_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        _isSpeaking.value = true
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
