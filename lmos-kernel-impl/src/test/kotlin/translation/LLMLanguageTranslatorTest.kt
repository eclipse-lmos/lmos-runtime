/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package translation

import org.eclipse.lmos.kernel.impl.translation.LLMLanguageTranslator
import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LLMLanguageTranslatorTest {
    private val promptTemplateExecutor = mockk<PromptTemplateExecutor>()
    private val llmLanguageTranslator = LLMLanguageTranslator(promptTemplateExecutor)
    @Test
    fun translate_text_into_given_language() {
        val text = "how are you?"
        val language = "GERMAN"
        val answer="WIE GEHT ES DIR"
        val variables = mapOf("text" to text, "language" to language)
        coEvery { promptTemplateExecutor.execute("language_translator", variables) } returns answer
        runBlocking {
            assertEquals(answer.trim(), llmLanguageTranslator.translate(text, language))
        }
    }
}