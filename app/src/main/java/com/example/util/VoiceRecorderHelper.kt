package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceRecorderHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready to record voice call/audio")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _statusMessage.value = "Speech recognition unavailable on this device. You can paste audio transcript text directly."
            return
        }

        try {
            stopListening()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isRecording.value = true
                        _statusMessage.value = "Listening... Speak clearly into the microphone."
                    }

                    override fun onBeginningOfSpeech() {
                        _statusMessage.value = "Capturing speech audio..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isRecording.value = false
                        _statusMessage.value = "Processing audio transcript..."
                    }

                    override fun onError(error: Int) {
                        _isRecording.value = false
                        _statusMessage.value = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected in recording."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Audio recognition notice (code $error)."
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isRecording.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _transcript.value = matches[0]
                            _statusMessage.value = "Speech transcript captured successfully."
                        } else {
                            _statusMessage.value = "No clear transcript extracted from recording."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _transcript.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            _isRecording.value = false
            _statusMessage.value = "Recording error: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        try {
            _isRecording.value = false
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            speechRecognizer = null
        }
    }

    fun clear() {
        stopListening()
        _transcript.value = ""
        _statusMessage.value = "Ready to record voice call/audio"
    }
}
