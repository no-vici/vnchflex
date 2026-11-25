package com.vnchflex.vietkeyboard

import com.vnchflex.vietkeyboard.engine.VietnameseEngine
import com.vnchflex.vietkeyboard.ime.GlideDecoder
import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineAndGlideTest {

    @Test
    fun telexFreeDiacriticsAndSouthernDialect() {
        val engine = VietnameseEngine()
        assertEquals("không", engine.transformTelex("khongo".toList()))
        assertEquals("tiếng", engine.transformTelex("tieengs".toList()))
        assertEquals("việt", engine.transformTelex("vieetj".toList()))
        engine.southernDialectAndGenZ = true
        assertEquals("dzìa", engine.transformTelex("dziaf".toList()))
        assertEquals("dzách", engine.transformTelex("dzachs".toList()))
    }

    @Test
    fun bilingualSuggestionAndContractions() {
        val suggestion = SuggestionEngine(null)
        assertTrue(suggestion.isKnownWord("ok"))
        assertTrue(suggestion.isKnownWord("make"))
        assertTrue(suggestion.isKnownWord("chào"))

        val dontSugs = suggestion.suggest("dont", "", "", limit = 3)
        assertTrue("Expected don't in $dontSugs", dontSugs.contains("don't"))

        val biaSugs = suggestion.suggest("bia", "", "", limit = 3, includeEmoji = true)
        assertTrue("Expected 🍺 in $biaSugs", biaSugs.contains("🍺"))
    }

    @Test
    fun glideDecoderAccuracyAndBenchmark() {
        val suggestion = SuggestionEngine(null)
        val chao = GlideDecoder.decode("chao", suggestion, "", "", limit = 3, isVietnameseMode = true)
        assertEquals("chào", chao.firstOrNull())

        val love = GlideDecoder.decode("love", suggestion, "", "", limit = 3, isVietnameseMode = false)
        assertEquals("love", love.firstOrNull())

        // Micro-benchmark (100 iterations)
        val startNs = System.nanoTime()
        repeat(100) {
            GlideDecoder.decode("chao", suggestion, "xin", "", limit = 3, isVietnameseMode = true)
        }
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0
        println("GlideDecoder 100x decode took: $elapsedMs ms (${elapsedMs * 10} us/call)")
        assertTrue("GlideDecoder too slow: $elapsedMs ms", elapsedMs < 2000.0)
    }
}
