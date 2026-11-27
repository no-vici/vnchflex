package com.vnchflex.vietkeyboard.engine

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import org.json.JSONArray
import java.io.File
import java.nio.LongBuffer

/**
 * Bộ sửa lỗi dấu thanh & chính tả Tiếng Việt 100% offline chạy trên ONNX Runtime Android.
 * Tự động nạp `vi_corrector.onnx` + `vi_corrector_vocab.json` từ `filesDir/models/` hoặc `assets/` nếu có.
 */
class OnnxVietnameseCorrector(private val context: Context) {

    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var char2Id: Map<Char, Int> = emptyMap()
    private var id2Char: List<Char> = emptyList()

    @Volatile
    var isReady: Boolean = false
        private set

    fun initAsync(onLoaded: ((Boolean) -> Unit)? = null) {
        Thread {
            val ok = runCatching {
                val vocabRaw = loadVocabText() ?: return@runCatching false
                val arr = JSONArray(vocabRaw)
                val list = ArrayList<Char>(arr.length())
                val map = HashMap<Char, Int>(arr.length())
                for (i in 0 until arr.length()) {
                    val token = arr.getString(i)
                    val ch = if (token.length == 1) token[0] else '\u0000'
                    list.add(ch)
                    if (token.length == 1) map[ch] = i
                }
                id2Char = list
                char2Id = map

                val modelBytes = loadModelBytes() ?: return@runCatching false
                val ortEnv = OrtEnvironment.getEnvironment()
                val opts = OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(2)
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                }
                env = ortEnv
                session = ortEnv.createSession(modelBytes, opts)
                isReady = true
                true
            }.getOrDefault(false)
            onLoaded?.invoke(ok)
        }.start()
    }

    /**
     * Sửa lỗi dấu & chính tả cho một câu hoặc đoạn văn bản ngắn (tối đa 128 ký tự/lượt).
     */
    fun correctSentence(input: String): String {
        val ortEnv = env ?: return input
        val ortSession = session ?: return input
        if (!isReady || input.isBlank()) return input

        val trimmed = input.take(128)
        val seqLen = trimmed.length
        val inputIds = LongArray(seqLen) { idx ->
            (char2Id[trimmed[idx]] ?: 1).toLong() // 1 = <UNK>
        }

        val shape = longArrayOf(1L, seqLen.toLong())
        val tensor = OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(inputIds), shape)

        return tensor.use { inTensor ->
            ortSession.run(mapOf("input_ids" to inTensor)).use { outputs ->
                @Suppress("UNCHECKED_CAST")
                val logits = outputs[0].value as Array<Array<FloatArray>> // [1, seqLen, vocabSize]
                val sb = StringBuilder(seqLen)
                for (t in 0 until seqLen) {
                    val srcChar = trimmed[t]
                    if (!srcChar.isLetter()) {
                        sb.append(srcChar)
                        continue
                    }
                    val srcBase = DiacriticUtils.strip(srcChar.toString()).lowercase()
                    val scores = logits[0][t]
                    var bestId = -1
                    var bestScore = Float.NEGATIVE_INFINITY
                    for (v in 2 until scores.size) { // Bỏ qua <PAD>=0 và <UNK>=1
                        val candChar = id2Char.getOrNull(v) ?: continue
                        if (candChar == '\u0000' || !candChar.isLetter()) continue
                        val candBase = DiacriticUtils.strip(candChar.toString()).lowercase()
                        if (candBase != srcBase) continue
                        if (scores[v] > bestScore) {
                            bestScore = scores[v]
                            bestId = v
                        }
                    }
                    val outChar = if (bestId >= 0) id2Char[bestId] else srcChar
                    sb.append(
                        if (srcChar.isUpperCase()) outChar.uppercaseChar() else outChar.lowercaseChar()
                    )
                }
                sb.toString()
            }
        }
    }

    private fun loadModelBytes(): ByteArray? {
        val externalFile = File(context.filesDir, "models/vi_corrector.onnx")
        if (externalFile.exists()) return externalFile.readBytes()
        return runCatching {
            context.assets.open("vi_corrector.onnx").use { it.readBytes() }
        }.getOrNull()
    }

    private fun loadVocabText(): String? {
        val externalVocab = File(context.filesDir, "models/vi_corrector_vocab.json")
        if (externalVocab.exists()) return externalVocab.readText(Charsets.UTF_8)
        return runCatching {
            context.assets.open("vi_corrector_vocab.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull()
    }

    fun close() {
        isReady = false
        runCatching { session?.close() }
        session = null
    }
}
