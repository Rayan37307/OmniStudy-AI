package com.example.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class SoundMode(val displayName: String, val description: String) {
    NONE("নীরব (Off)", "Silence"),
    FOREST_RAIN("শান্ত বৃষ্টি (Calm Rain)", "Soft gentle rain ambience for steady focus"),
    BAMBOO_STREAM("শান্ত ধারা (Stream Flow)", "Gentle water flow and soothing acoustic ripples"),
    BROWN_NOISE("ব্রাউন নয়েজ (Brown Noise)", "Warm deep frequency for profound concentration"),
    BINAURAL_40HZ("৪০Hz বাইনোরাল (40Hz Focus)", "Stereo gamma waves for mental clarity and active recall")
}

class SoundscapeSynthesizer {

    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var currentMode: SoundMode = SoundMode.NONE
        private set

    var volume: Float = 0.6f
        set(value) {
            field = value.coerceIn(0f, 1f)
            audioTrack?.setVolume(field)
        }

    fun isPlaying(): Boolean = currentMode != SoundMode.NONE

    fun start(mode: SoundMode) {
        stop()
        if (mode == SoundMode.NONE) return

        currentMode = mode
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.setVolume(volume)
        track.play()

        synthesisJob = scope.launch {
            val random = Random()
            val chunkSamples = 2048
            val buffer = ShortArray(chunkSamples * 2)

            var brownL = 0.0
            var brownR = 0.0

            var b0L = 0.0; var b1L = 0.0; var b2L = 0.0
            var b0R = 0.0; var b1R = 0.0; var b2R = 0.0

            var streamL = 0.0
            var streamR = 0.0

            var phaseL = 0.0
            var phaseR = 0.0
            val freqL = 200.0
            val freqR = 240.0 // 40Hz difference for Gamma Brainwave Binding
            val phaseIncL = (2.0 * PI * freqL) / sampleRate
            val phaseIncR = (2.0 * PI * freqR) / sampleRate

            try {
                while (isActive) {
                    var i = 0
                    while (i < buffer.size) {
                        when (currentMode) {
                            SoundMode.FOREST_RAIN -> {
                                // Organic rain filtering: soft pink noise modulated with gentle raindrop sprinkles
                                val whiteL = random.nextDouble() * 2.0 - 1.0
                                val whiteR = random.nextDouble() * 2.0 - 1.0
                                b0L = 0.99886 * b0L + whiteL * 0.0555179
                                b1L = 0.99332 * b1L + whiteL * 0.0750759
                                b2L = 0.96900 * b2L + whiteL * 0.1538520
                                val rainL = (b0L + b1L + b2L + whiteL * 0.5362) * 0.12

                                b0R = 0.99886 * b0R + whiteR * 0.0555179
                                b1R = 0.99332 * b1R + whiteR * 0.0750759
                                b2R = 0.96900 * b2R + whiteR * 0.1538520
                                val rainR = (b0R + b1R + b2R + whiteR * 0.5362) * 0.12

                                buffer[i] = (rainL.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                                buffer[i + 1] = (rainR.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                            }
                            SoundMode.BAMBOO_STREAM -> {
                                // Flowing gentle stream: low pass modulated water wave
                                val whiteL = random.nextDouble() * 2.0 - 1.0
                                val whiteR = random.nextDouble() * 2.0 - 1.0
                                streamL = streamL * 0.92 + whiteL * 0.08
                                streamR = streamR * 0.92 + whiteR * 0.08
                                val modL = (streamL * 2.5).coerceIn(-1.0, 1.0)
                                val modR = (streamR * 2.5).coerceIn(-1.0, 1.0)
                                buffer[i] = (modL * 32767.0).toInt().toShort()
                                buffer[i + 1] = (modR * 32767.0).toInt().toShort()
                            }
                            SoundMode.BROWN_NOISE -> {
                                val whiteL = (random.nextDouble() * 2.0 - 1.0)
                                val whiteR = (random.nextDouble() * 2.0 - 1.0)
                                brownL = (brownL + (0.02 * whiteL)) / 1.02
                                brownR = (brownR + (0.02 * whiteR)) / 1.02
                                val sampleL = (brownL * 3.5).coerceIn(-1.0, 1.0)
                                val sampleR = (brownR * 3.5).coerceIn(-1.0, 1.0)
                                buffer[i] = (sampleL * 32767.0).toInt().toShort()
                                buffer[i + 1] = (sampleR * 32767.0).toInt().toShort()
                            }
                            SoundMode.BINAURAL_40HZ -> {
                                val sL = sin(phaseL) * 0.35
                                val sR = sin(phaseR) * 0.35
                                phaseL = (phaseL + phaseIncL) % (2.0 * PI)
                                phaseR = (phaseR + phaseIncR) % (2.0 * PI)
                                buffer[i] = (sL * 32767.0).toInt().toShort()
                                buffer[i + 1] = (sR * 32767.0).toInt().toShort()
                            }
                            SoundMode.NONE -> {
                                buffer[i] = 0
                                buffer[i + 1] = 0
                            }
                        }
                        i += 2
                    }

                    if (isActive && track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.write(buffer, 0, buffer.size)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        currentMode = SoundMode.NONE
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
