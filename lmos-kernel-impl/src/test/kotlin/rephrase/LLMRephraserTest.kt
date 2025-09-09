/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package rephrase

import org.eclipse.lmos.kernel.impl.rephrase.LLMRephraser
import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LLMRephraserTest {

    private val promptTemplateExecutor=mockk<PromptTemplateExecutor>()
    private val llmRephraser=LLMRephraser(promptTemplateExecutor)
    @Test
    fun return_rephrased_question(){
        val question="Was kostet es?"
        val chatHistory = "Welche Abonnements bietet margentaTV an?"
        val variables: Map<String, Any> = hashMapOf(LLMRephraser.QUESTION to question, LLMRephraser.CHAT_HISTORY to chatHistory)
        coEvery { promptTemplateExecutor.execute("rephrase", variables) } returns "Was kostet das MargentaTV-Abonnement?"
        runBlocking { assertEquals("Was kostet das MargentaTV-Abonnement?", llmRephraser.rephrase(question, chatHistory, "de"))}
    }
}