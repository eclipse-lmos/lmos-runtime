/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package language

import com.github.pemistahl.lingua.api.Language
import com.github.pemistahl.lingua.api.LanguageDetector
import org.eclipse.lmos.kernel.impl.language.LinguaLanguageDetector
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinguaLanguageDetectorTest {
    private val detector = mockk<LanguageDetector>()
    private val linguaLanguageDetector = LinguaLanguageDetector(detector)

    @Test
    fun detect_english_language_of_input() {
        val input = "how are you?"
        val lang = Language.ENGLISH
        every { detector.detectLanguageOf(input) } returns lang
        assertEquals(Locale.ENGLISH, linguaLanguageDetector.detect(input))
    }

    @Test
    fun detect_german_language_of_input() {
        val input = "Wie geht es dir ?"
        val lang = Language.GERMAN
        every { detector.detectLanguageOf(input) } returns lang
        assertEquals(Locale.GERMAN, linguaLanguageDetector.detect(input))
    }

    @Test
    fun throw_exception_if_unable_to_detect_language() {
        val input = "Wie geht es dir ?"
        coEvery { detector.detectLanguageOf(input) } throws Exception()
        assertNull(linguaLanguageDetector.detect(input))
    }
}