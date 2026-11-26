package com.vnchflex.vietkeyboard.ime

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

/**
 * Persistent Gboard-style clipboard history store.
 * - Unpinned items expire automatically after 1 hour (3,600,000 ms).
 * - Pinned items are kept indefinitely and sorted before unpinned items.
 */
class ClipboardStore(context: Context) {
    private val prefs = context.getSharedPreferences("vnchflex_clipboard", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS = "clipboard_items_json"
        const val UNPINNED_TTL_MS = 60L * 60L * 1000L // 1 hour
        const val MAX_ITEMS = 20
    }

    fun getItems(now: Long = System.currentTimeMillis()): List<ClipboardItem> {
        val raw = loadRaw()
        val valid = raw.filter { it.isPinned || (now - it.timestamp) <= UNPINNED_TTL_MS }
        if (valid.size != raw.size) {
            saveRaw(valid)
        }
        return valid.sortedWith(
            compareByDescending<ClipboardItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
    }

    fun addClip(text: String, now: Long = System.currentTimeMillis()) {
        val trimmed = text
        if (trimmed.isBlank()) return
        val current = loadRaw().filter { it.isPinned || (now - it.timestamp) <= UNPINNED_TTL_MS }.toMutableList()
        val existingIdx = current.indexOfFirst { it.text == trimmed }
        if (existingIdx >= 0) {
            val existing = current.removeAt(existingIdx)
            current.add(0, existing.copy(timestamp = now))
        } else {
            val id = "${now}_${trimmed.hashCode()}"
            current.add(0, ClipboardItem(id = id, text = trimmed, timestamp = now, isPinned = false))
        }
        // Keep pinned items + most recent unpinned up to MAX_ITEMS
        val pinned = current.filter { it.isPinned }
        val unpinned = current.filter { !it.isPinned }.take((MAX_ITEMS - pinned.size).coerceAtLeast(0))
        saveRaw(pinned + unpinned)
    }

    fun togglePin(id: String) {
        val current = loadRaw().map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveRaw(current)
    }

    fun delete(id: String) {
        val current = loadRaw().filterNot { it.id == id }
        saveRaw(current)
    }

    private fun loadRaw(): List<ClipboardItem> {
        val json = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ClipboardItem(
                            id = obj.getString("id"),
                            text = obj.getString("text"),
                            timestamp = obj.getLong("timestamp"),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveRaw(items: List<ClipboardItem>) {
        val arr = JSONArray()
        for (item in items) {
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("text", item.text)
                    put("timestamp", item.timestamp)
                    put("isPinned", item.isPinned)
                }
            )
        }
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }
}
