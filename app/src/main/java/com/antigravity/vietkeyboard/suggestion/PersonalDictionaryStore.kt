package com.antigravity.vietkeyboard.suggestion

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PersonalWord(
    val word: String,
    val shortcut: String = "",
    val locale: String = "vi_VN",
)

/**
 * Gboard-style Personal Dictionary & Learned N-gram (Unigram / Bigram / Trigram) store.
 */
class PersonalDictionaryStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadPersonalWords(): List<PersonalWord> {
        val raw = prefs.getString(KEY_PERSONAL_WORDS, null) ?: return defaultPersonalWords()
        return runCatching {
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
    }

    fun savePersonalWords(words: List<PersonalWord>) {
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
        return prefs.getStringSet(KEY_LEARNED_WORDS, emptySet())?.toSet() ?: emptySet()
    }

    fun addLearnedWord(word: String) {
        val w = word.trim().lowercase()
        if (w.length < 2) return
        val current = loadLearnedWords().toMutableSet()
        current.add(w)
        prefs.edit().putStringSet(KEY_LEARNED_WORDS, current).apply()
    }

    fun loadLearnedBigrams(): Map<String, Map<String, Int>> {
        val raw = prefs.getString(KEY_LEARNED_BIGRAMS, null) ?: return emptyMap()
        return parseNgramJson(raw)
    }

    fun saveLearnedBigrams(bigrams: Map<String, Map<String, Int>>) {
        prefs.edit().putString(KEY_LEARNED_BIGRAMS, serializeNgramJson(bigrams)).apply()
    }

    fun loadLearnedTrigrams(): Map<String, Map<String, Int>> {
        val raw = prefs.getString(KEY_LEARNED_TRIGRAMS, null) ?: return emptyMap()
        return parseNgramJson(raw)
    }

    fun saveLearnedTrigrams(trigrams: Map<String, Map<String, Int>>) {
        prefs.edit().putString(KEY_LEARNED_TRIGRAMS, serializeNgramJson(trigrams)).apply()
    }

    /** Gboard: Delete learned words & n-gram history (Xóa các từ và ngữ cảnh đã học) */
    fun clearLearnedWords() {
        prefs.edit()
            .remove(KEY_LEARNED_WORDS)
            .remove(KEY_LEARNED_BIGRAMS)
            .remove(KEY_LEARNED_TRIGRAMS)
            .apply()
    }

    private fun parseNgramJson(raw: String): Map<String, Map<String, Int>> = runCatching {
        val root = JSONObject(raw)
        val out = mutableMapOf<String, Map<String, Int>>()
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
    }.getOrDefault(emptyMap())

    private fun serializeNgramJson(data: Map<String, Map<String, Int>>): String {
        val root = JSONObject()
        data.entries.take(MAX_STORED_CONTEXTS).forEach { (ctx, followers) ->
            val child = JSONObject()
            followers.entries
                .sortedByDescending { it.value }
                .take(MAX_FOLLOWERS_PER_CONTEXT)
                .forEach { (w, count) -> child.put(w, count) }
            root.put(ctx, child)
        }
        return root.toString()
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
        private const val MAX_STORED_CONTEXTS = 400
        private const val MAX_FOLLOWERS_PER_CONTEXT = 8
    }
}
