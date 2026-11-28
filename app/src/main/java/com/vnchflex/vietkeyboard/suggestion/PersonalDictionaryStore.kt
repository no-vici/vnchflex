package com.vnchflex.vietkeyboard.suggestion

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject

data class PersonalWord(
    val word: String,
    val shortcut: String = "",
    val locale: String = "vi_VN",
)

/**
 * Gboard-style Personal Dictionary & Learned N-gram (Unigram / Bigram / Trigram) store.
 * Caches all data in memory to avoid disk/JSON parse on every keystroke.
 */
class PersonalDictionaryStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    private var personalWordsCache: List<PersonalWord>? = null
    private var learnedWordsCache: MutableSet<String>? = null
    private var learnedBigramsCache: MutableMap<String, MutableMap<String, Int>>? = null
    private var learnedTrigramsCache: MutableMap<String, MutableMap<String, Int>>? = null
    private var ngramSavePending = false
    private var learnedWordsDirty = false
    private var learnedBigramsDirty = false
    private var learnedTrigramsDirty = false
    private val ngramSaveRunnable = Runnable {
        ngramSavePending = false
        flushNgrams()
    }

    fun loadPersonalWords(): List<PersonalWord> {
        personalWordsCache?.let { return it }
        val raw = prefs.getString(KEY_PERSONAL_WORDS, null)
        val loaded = if (raw == null) defaultPersonalWords() else runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                PersonalWord(
                    word = o.getString("word"),
                    shortcut = o.optString("shortcut", ""),
                    locale = o.optString("locale", "vi_VN"),
                )
            }
        }.getOrElse { defaultPersonalWords() }
        personalWordsCache = loaded
        return loaded
    }

    fun savePersonalWords(words: List<PersonalWord>) {
        personalWordsCache = words.toList()
        val arr = JSONArray()
        words.forEach { w ->
            arr.put(JSONObject().apply {
                put("word", w.word)
                put("shortcut", w.shortcut)
                put("locale", w.locale)
            })
        }
        prefs.edit().putString(KEY_PERSONAL_WORDS, arr.toString()).apply()
    }

    fun addPersonalWord(word: String, shortcut: String = "", locale: String = "vi_VN") {
        val cleanWord = word.trim()
        if (cleanWord.isEmpty()) return
        val current = loadPersonalWords().toMutableList()
        current.removeAll { it.word.equals(cleanWord, ignoreCase = true) && it.locale == locale }
        current.add(0, PersonalWord(cleanWord, shortcut.trim(), locale))
        savePersonalWords(current)
    }

    fun removePersonalWord(word: String) {
        val current = loadPersonalWords().filterNot { it.word.equals(word, ignoreCase = true) }
        savePersonalWords(current)
    }

    fun loadLearnedWords(): Set<String> {
        learnedWordsCache?.let { return it }
        val loaded = prefs.getStringSet(KEY_LEARNED_WORDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        learnedWordsCache = loaded
        return loaded
    }

    fun addLearnedWord(word: String) {
        val w = word.trim().lowercase()
        if (w.length < 2) return
        val current = learnedWordsCache ?: loadLearnedWords().toMutableSet().also { learnedWordsCache = it }
        if (current.add(w)) {
            learnedWordsDirty = true
            scheduleNgramSave()
        }
    }

    fun loadLearnedBigrams(): Map<String, Map<String, Int>> {
        learnedBigramsCache?.let { return it }
        val raw = prefs.getString(KEY_LEARNED_BIGRAMS, null)
        val loaded = if (raw == null) mutableMapOf() else parseNgramJson(raw)
        learnedBigramsCache = loaded
        return loaded
    }

    fun saveLearnedBigrams(bigrams: Map<String, Map<String, Int>>) {
        if (learnedBigramsCache !== bigrams) {
            learnedBigramsCache = bigrams.mapValues { it.value.toMutableMap() }.toMutableMap()
        }
        learnedBigramsDirty = true
        scheduleNgramSave()
    }

    fun loadLearnedTrigrams(): Map<String, Map<String, Int>> {
        learnedTrigramsCache?.let { return it }
        val raw = prefs.getString(KEY_LEARNED_TRIGRAMS, null)
        val loaded = if (raw == null) mutableMapOf() else parseNgramJson(raw)
        learnedTrigramsCache = loaded
        return loaded
    }

    fun saveLearnedTrigrams(trigrams: Map<String, Map<String, Int>>) {
        if (learnedTrigramsCache !== trigrams) {
            learnedTrigramsCache = trigrams.mapValues { it.value.toMutableMap() }.toMutableMap()
        }
        learnedTrigramsDirty = true
        scheduleNgramSave()
    }

    /** Gboard: Delete learned words & n-gram history (Xóa các từ và ngữ cảnh đã học) */
    fun clearLearnedWords() {
        handler.removeCallbacks(ngramSaveRunnable)
        ngramSavePending = false
        learnedWordsDirty = false
        learnedBigramsDirty = false
        learnedTrigramsDirty = false
        learnedWordsCache = mutableSetOf()
        learnedBigramsCache = mutableMapOf()
        learnedTrigramsCache = mutableMapOf()
        prefs.edit()
            .remove(KEY_LEARNED_WORDS)
            .remove(KEY_LEARNED_BIGRAMS)
            .remove(KEY_LEARNED_TRIGRAMS)
            .apply()
    }

    /** Xóa cache trên RAM để lần đọc tiếp theo lấy dữ liệu mới nhất từ SharedPreferences */
    fun invalidateCaches() {
        handler.removeCallbacks(ngramSaveRunnable)
        ngramSavePending = false
        learnedWordsDirty = false
        learnedBigramsDirty = false
        learnedTrigramsDirty = false
        personalWordsCache = null
        learnedWordsCache = null
        learnedBigramsCache = null
        learnedTrigramsCache = null
    }

    private fun scheduleNgramSave() {
        if (ngramSavePending) return
        ngramSavePending = true
        handler.postDelayed(ngramSaveRunnable, 1500L)
    }

    fun flushNgrams() {
        handler.removeCallbacks(ngramSaveRunnable)
        ngramSavePending = false
        val editor = prefs.edit()
        var hasWrites = false
        if (learnedWordsDirty) {
            learnedWordsCache?.let {
                editor.putStringSet(KEY_LEARNED_WORDS, HashSet(it))
                hasWrites = true
            }
            learnedWordsDirty = false
        }
        if (learnedBigramsDirty) {
            learnedBigramsCache?.let {
                val snapshot = it.mapValues { (_, v) -> HashMap(v) }
                editor.putString(KEY_LEARNED_BIGRAMS, serializeNgramJson(snapshot))
                hasWrites = true
            }
            learnedBigramsDirty = false
        }
        if (learnedTrigramsDirty) {
            learnedTrigramsCache?.let {
                val snapshot = it.mapValues { (_, v) -> HashMap(v) }
                editor.putString(KEY_LEARNED_TRIGRAMS, serializeNgramJson(snapshot))
                hasWrites = true
            }
            learnedTrigramsDirty = false
        }
        if (hasWrites) editor.apply()
    }

    private fun parseNgramJson(raw: String): MutableMap<String, MutableMap<String, Int>> = runCatching {
        val root = JSONObject(raw)
        val out = mutableMapOf<String, MutableMap<String, Int>>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val childObj = root.getJSONObject(k)
            val childMap = mutableMapOf<String, Int>()
            val cKeys = childObj.keys()
            while (cKeys.hasNext()) {
                val ck = cKeys.next()
                childMap[ck] = childObj.optInt(ck, 1)
            }
            if (childMap.isNotEmpty()) out[k] = childMap
        }
        out
    }.getOrDefault(mutableMapOf())

    private fun serializeNgramJson(data: Map<String, Map<String, Int>>): String {
        val root = JSONObject()
        data.entries
            .sortedByDescending { (_, followers) -> followers.values.sum() }
            .take(MAX_STORED_CONTEXTS)
            .forEach { (ctx, followers) ->
                val child = JSONObject()
                followers.entries
                    .sortedByDescending { it.value }
                    .take(MAX_FOLLOWERS_PER_CONTEXT)
                    .forEach { (w, count) -> child.put(w, count) }
                root.put(ctx, child)
            }
        return root.toString()
    }

    fun exportBackupJson(): JSONObject {
        flushNgrams()
        val root = JSONObject()
        val words = JSONArray()
        loadPersonalWords().forEach { w ->
            words.put(JSONObject().apply {
                put("word", w.word)
                put("shortcut", w.shortcut)
                put("locale", w.locale)
            })
        }
        root.put("personalWords", words)
        val learned = JSONArray()
        loadLearnedWords().forEach { learned.put(it) }
        root.put("learnedWords", learned)
        root.put("learnedBigrams", JSONObject(serializeNgramJson(loadLearnedBigrams())))
        root.put("learnedTrigrams", JSONObject(serializeNgramJson(loadLearnedTrigrams())))
        return root
    }

    fun importBackupJson(root: JSONObject, replace: Boolean): Int {
        var count = 0
        val personalArr = root.optJSONArray("personalWords")
        if (personalArr != null) {
            val current = if (replace) mutableListOf() else loadPersonalWords().toMutableList()
            for (i in 0 until personalArr.length()) {
                val o = personalArr.optJSONObject(i) ?: continue
                val word = o.optString("word", "").trim()
                if (word.isEmpty()) continue
                val shortcut = o.optString("shortcut", "")
                val locale = o.optString("locale", "vi_VN")
                current.removeAll { it.word.equals(word, ignoreCase = true) && it.locale == locale }
                current.add(PersonalWord(word, shortcut, locale))
                count++
            }
            savePersonalWords(current)
        }

        val learnedArr = root.optJSONArray("learnedWords")
        if (learnedArr != null) {
            val current = if (replace) mutableSetOf() else loadLearnedWords().toMutableSet()
            for (i in 0 until learnedArr.length()) {
                val w = learnedArr.optString(i, "").trim().lowercase()
                if (w.length >= 2 && current.add(w)) count++
            }
            learnedWordsCache = current
            learnedWordsDirty = false
            prefs.edit().putStringSet(KEY_LEARNED_WORDS, HashSet(current)).apply()
        }

        val bigramsObj = root.optJSONObject("learnedBigrams")
        if (bigramsObj != null) {
            val imported = parseNgramJson(bigramsObj.toString())
            val current = if (replace) mutableMapOf() else loadLearnedBigrams().mapValues { it.value.toMutableMap() }.toMutableMap()
            mergeNgrams(current, imported)
            learnedBigramsCache = current
            learnedBigramsDirty = true
            count += imported.size
        }

        val trigramsObj = root.optJSONObject("learnedTrigrams")
        if (trigramsObj != null) {
            val imported = parseNgramJson(trigramsObj.toString())
            val current = if (replace) mutableMapOf() else loadLearnedTrigrams().mapValues { it.value.toMutableMap() }.toMutableMap()
            mergeNgrams(current, imported)
            learnedTrigramsCache = current
            learnedTrigramsDirty = true
            count += imported.size
        }
        flushNgrams()
        return count
    }

    private fun mergeNgrams(
        target: MutableMap<String, MutableMap<String, Int>>,
        source: Map<String, Map<String, Int>>
    ) {
        source.forEach { (ctx, followers) ->
            val dest = target.getOrPut(ctx) { mutableMapOf() }
            followers.forEach { (w, c) ->
                dest[w] = (dest[w] ?: 0) + c
            }
        }
    }

    private fun defaultPersonalWords(): List<PersonalWord> = listOf(
        PersonalWord("Việt Nam", "vn", "vi_VN"),
        PersonalWord("vnchFlex", "vf", "vi_VN"),
        PersonalWord("Sen Việt", "sv", "vi_VN"),
    )

    companion object {
        private const val PREFS_NAME = "gboard_personal_dictionary"
        private const val KEY_PERSONAL_WORDS = "personal_words"
        private const val KEY_LEARNED_WORDS = "learned_words"
        private const val KEY_LEARNED_BIGRAMS = "learned_bigrams_v1"
        private const val KEY_LEARNED_TRIGRAMS = "learned_trigrams_v1"
        private const val MAX_STORED_CONTEXTS = 1500
        private const val MAX_FOLLOWERS_PER_CONTEXT = 8
    }
}
