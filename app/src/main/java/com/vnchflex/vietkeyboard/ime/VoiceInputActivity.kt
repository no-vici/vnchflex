package com.vnchflex.vietkeyboard.ime

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast

/**
 * Lightweight translucent host activity that launches Android's system Speech-to-Text
 * recognizer dialog and passes the recognized text back to [VietKeyboardService].
 */
class VoiceInputActivity : Activity() {

    companion object {
        const val EXTRA_LANG = "extra_lang"
        @Volatile
        var onVoiceResult: ((String) -> Unit)? = null
        private const val REQ_CODE_SPEECH = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lang = intent?.getStringExtra(EXTRA_LANG) ?: "vi-VN"
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (lang.startsWith("vi")) "Đang nghe (Tiếng Việt)..." else "Listening (English)..."
            )
        }
        try {
            startActivityForResult(speechIntent, REQ_CODE_SPEECH)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Thiết bị chưa cài đặt dịch vụ nhận diện giọng nói (Google Voice)",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_SPEECH && resultCode == RESULT_OK && data != null) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val best = matches?.firstOrNull { it.isNotBlank() }
            if (!best.isNullOrBlank()) {
                onVoiceResult?.invoke(best)
            }
        }
        finish()
    }
}
