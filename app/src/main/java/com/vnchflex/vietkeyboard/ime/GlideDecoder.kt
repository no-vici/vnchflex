package com.vnchflex.vietkeyboard.ime

import com.vnchflex.vietkeyboard.engine.DiacriticUtils
import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine

/**
 * Lightweight Vietnamese + English Glide Typing Decoder:
 * - Normalizes consecutive duplicate letters (`collapseRepeats`: `"letter"` -> `"leter"`, `"book"` -> `"bok"`)
 *   so words with double letters get the full subsequence bonus and zero edit-distance penalty when swiped.
 * - Tolerates start/end key keyboard-neighbor misses (`areKeysAdjacent`) with a slight penalty instead of hard-dropping.
 * - Combines geometric path similarity, unigram frequency weight, and active Trigram `(prevWord2, prevWord1)` context boosts.
 */
object GlideDecoder {
    internal val KEY_NEIGHBORS: Map<Char, String> = mapOf(
        'q' to "wa", 'w' to "qeas", 'e' to "wrsd", 'r' to "etdf", 't' to "ryfg",
        'y' to "tugh", 'u' to "yihj", 'i' to "uojk", 'o' to "ipkl", 'p' to "ol",
        'a' to "qwsz", 's' to "weadzx", 'd' to "ersfxc", 'f' to "rtgvcd", 'g' to "tyhbvf",
        'h' to "yujnbg", 'j' to "uikmnh", 'k' to "AEiojl,m".lowercase(), 'l' to "opk",
        'z' to "asx", 'x' to "zsdc", 'c' to "xdfv", 'v' to "cfgb", 'b' to "vghn",
        'n' to "bhjm", 'm' to "njk"
    )

    private val KEY_POSITIONS: Map<Char, Pair<Float, Float>> = mapOf(
        'q' to (0.05f to 0.16f), 'w' to (0.15f to 0.16f), 'e' to (0.25f to 0.16f), 'r' to (0.35f to 0.16f), 't' to (0.45f to 0.16f),
        'y' to (0.55f to 0.16f), 'u' to (0.65f to 0.16f), 'i' to (0.75f to 0.16f), 'o' to (0.85f to 0.16f), 'p' to (0.95f to 0.16f),
        'a' to (0.10f to 0.50f), 's' to (0.20f to 0.50f), 'd' to (0.30f to 0.50f), 'f' to (0.40f to 0.50f), 'g' to (0.50f to 0.50f),
        'h' to (0.60f to 0.50f), 'j' to (0.70f to 0.50f), 'k' to (0.80f to 0.50f), 'l' to (0.90f to 0.50f),
        'z' to (0.20f to 0.84f), 'x' to (0.30f to 0.84f), 'c' to (0.40f to 0.84f), 'v' to (0.50f to 0.84f), 'b' to (0.60f to 0.84f),
        'n' to (0.70f to 0.84f), 'm' to (0.80f to 0.84f)
    )

    private fun dtwDistance(pathA: List<Pair<Float, Float>>, pathB: List<Pair<Float, Float>>): Float {
        if (pathA.isEmpty() || pathB.isEmpty()) return 0f
        val n = pathA.size
        val m = pathB.size
        val dp = Array(n + 1) { FloatArray(m + 1) { Float.POSITIVE_INFINITY } }
        dp[0][0] = 0f
        for (i in 1..n) {
            for (j in 1..m) {
                val dx = pathA[i - 1].first - pathB[j - 1].first
                val dy = pathA[i - 1].second - pathB[j - 1].second
                val cost = kotlin.math.sqrt(dx * dx + dy * dy)
                dp[i][j] = cost + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
        return dp[n][m] / maxOf(n, m)
    }

    private var ortEnv: ai.onnxruntime.OrtEnvironment? = null
    private var ortSession: ai.onnxruntime.OrtSession? = null

    @Volatile
    var isNeuralReady: Boolean = false
        private set

    fun initNeuralAsync(context: android.content.Context) {
        Thread {
            runCatching {
                val modelBytes = context.assets.open("neural_glide.onnx").use { it.readBytes() }
                val env = ai.onnxruntime.OrtEnvironment.getEnvironment()
                val opts = ai.onnxruntime.OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(2)
                    setOptimizationLevel(ai.onnxruntime.OrtSession.SessionOptions.OptLevel.ALL_OPT)
                }
                ortEnv = env
                ortSession = env.createSession(modelBytes, opts)
                isNeuralReady = true
            }
        }.start()
    }

    fun closeNeural() {
        isNeuralReady = false
        runCatching { ortSession?.close() }
        ortSession = null
    }

    fun predictNeuralSequence(normalizedPoints: FloatArray): String? {
        val env = ortEnv ?: return null
        val session = ortSession ?: return null
        val nPts = normalizedPoints.size / 2
        if (!isNeuralReady || nPts < 2) return null

        val targetLen = 48
        val cumLen = FloatArray(nPts)
        for (i in 1 until nPts) {
            val dx = normalizedPoints[i * 2] - normalizedPoints[(i - 1) * 2]
            val dy = normalizedPoints[i * 2 + 1] - normalizedPoints[(i - 1) * 2 + 1]
            cumLen[i] = cumLen[i - 1] + kotlin.math.hypot(dx, dy)
        }
        val totalLen = cumLen[nPts - 1]
        val rx = FloatArray(targetLen)
        val ry = FloatArray(targetLen)
        if (totalLen < 1e-5f) {
            rx.fill(normalizedPoints[0])
            ry.fill(normalizedPoints[1])
        } else {
            var seg = 0
            for (t in 0 until targetLen) {
                val targetDist = (t.toFloat() / (targetLen - 1)) * totalLen
                while (seg < nPts - 2 && cumLen[seg + 1] < targetDist) seg++
                val segSpan = (cumLen[seg + 1] - cumLen[seg]).coerceAtLeast(1e-6f)
                val alpha = ((targetDist - cumLen[seg]) / segSpan).coerceIn(0f, 1f)
                rx[t] = normalizedPoints[seg * 2] + alpha * (normalizedPoints[(seg + 1) * 2] - normalizedPoints[seg * 2])
                ry[t] = normalizedPoints[seg * 2 + 1] + alpha * (normalizedPoints[(seg + 1) * 2 + 1] - normalizedPoints[seg * 2 + 1])
            }
        }

        val features = FloatArray(targetLen * 6)
        for (t in 0 until targetLen) {
            val dx = when (t) {
                0 -> rx[1] - rx[0]
                targetLen - 1 -> rx[t] - rx[t - 1]
                else -> (rx[t + 1] - rx[t - 1]) * 0.5f
            }
            val dy = when (t) {
                0 -> ry[1] - ry[0]
                targetLen - 1 -> ry[t] - ry[t - 1]
                else -> (ry[t + 1] - ry[t - 1]) * 0.5f
            }
            val angle = kotlin.math.atan2(dy, dx)
            val base = t * 6
            features[base] = rx[t]
            features[base + 1] = ry[t]
            features[base + 2] = dx
            features[base + 3] = dy
            features[base + 4] = kotlin.math.sin(angle)
            features[base + 5] = kotlin.math.cos(angle)
        }

        val tensor = ai.onnxruntime.OnnxTensor.createTensor(
            env,
            java.nio.FloatBuffer.wrap(features),
            longArrayOf(1L, targetLen.toLong(), 6L)
        )
        return tensor.use { inTensor ->
            session.run(mapOf("swipe_features" to inTensor)).use { outputs ->
                @Suppress("UNCHECKED_CAST")
                val logits = outputs[0].value as Array<Array<FloatArray>>
                val sb = StringBuilder()
                var prevId = 0
                for (t in 0 until targetLen) {
                    val scores = logits[0][t]
                    var bestId = 0
                    var bestScore = Float.NEGATIVE_INFINITY
                    for (c in scores.indices) {
                        if (scores[c] > bestScore) {
                            bestScore = scores[c]
                            bestId = c
                        }
                    }
                    if (bestId != 0 && bestId != prevId) {
                        sb.append(('a'.code + bestId - 1).toChar())
                    }
                    prevId = bestId
                }
                sb.toString().takeIf { it.length >= 2 }
            }
        }
    }

    fun decode(
        swipedKeys: String,
        suggestionEngine: SuggestionEngine,
        prevWord1: String,
        prevWord2: String,
        limit: Int = 3,
        isVietnameseMode: Boolean = true,
        normalizedPoints: FloatArray? = null
    ): List<String> {
        val neuralSeq = if (normalizedPoints != null) predictNeuralSequence(normalizedPoints) else null
        val raw = collapseRepeats((neuralSeq ?: swipedKeys).lowercase())
        if (raw.length < 2) return emptyList()

        val swipePath = raw.mapNotNull { KEY_POSITIONS[it] }
        val firstChar = raw.first()
        val lastChar = raw.last()
        val vocab = suggestionEngine.getGlideVocabulary(isVietnameseMode)
        val contextWeights = suggestionEngine.getContextBoosts(prevWord2, prevWord1)

        val scored = mutableListOf<Pair<String, Int>>()
        for ((word, unigramWeight) in vocab) {
            val stripped = DiacriticUtils.strip(word).lowercase()
            if (stripped.length < 2) continue

            val collapsedTarget = collapseRepeats(stripped)
            val exactFirst = collapsedTarget.first() == firstChar
            val exactLast = collapsedTarget.last() == lastChar
            val nearFirst = exactFirst || isNeighbor(collapsedTarget.first(), firstChar)
            val nearLast = exactLast || isNeighbor(collapsedTarget.last(), lastChar)
            if (!nearFirst || !nearLast) continue

            val subseqMatched = isSubsequence(collapsedTarget, raw)
            val dist = levenshtein(collapsedTarget, raw)
            if (!subseqMatched && dist > (raw.length / 2).coerceAtLeast(2)) continue

            var score = unigramWeight
            if (exactFirst) score += 90 else score += 25
            if (exactLast) score += 90 else score += 25
            if (subseqMatched) score += 220
            if (dist == 0) score += 140
            val targetPath = collapsedTarget.mapNotNull { KEY_POSITIONS[it] }
            val dtw = dtwDistance(targetPath, swipePath)
            score -= (dtw * 180f).toInt()
            score -= dist * 45
            score -= kotlin.math.abs(collapsedTarget.length - raw.length) * 18
            score += (contextWeights[word] ?: 0) * 65
            scored.add(word to score)
        }

        return scored
            .sortedByDescending { it.second }
            .map { it.first }
            .distinct()
            .take(limit)
    }

    private fun isNeighbor(a: Char, b: Char): Boolean =
        a == b || KEY_NEIGHBORS[a]?.contains(b) == true

    private fun collapseRepeats(s: String): String {
        if (s.length <= 1) return s
        val sb = StringBuilder(s.length)
        var prev = '\u0000'
        for (c in s) {
            if (c != prev) {
                sb.append(c)
                prev = c
            }
        }
        return sb.toString()
    }

    private fun isSubsequence(target: String, source: String): Boolean {
        var ti = 0
        var si = 0
        while (ti < target.length && si < source.length) {
            if (target[ti] == source[si]) ti++
            si++
        }
        return ti == target.length
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = IntArray(n + 1) { it }
        for (i in 1..m) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..n) {
                val temp = dp[j]
                dp[j] = if (a[i - 1] == b[j - 1]) prev
                else 1 + minOf(prev, dp[j], dp[j - 1])
                prev = temp
            }
        }
        return dp[n]
    }
}
