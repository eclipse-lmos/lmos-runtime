/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package intent

import org.eclipse.lmos.kernel.impl.intent.LLMIntentClassifier
import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LLMIntentClassifierTest {

    private val promptTemplateExecutor= mockk<PromptTemplateExecutor>()
    private val promptTemplate = "intent_classification"
    private val llmIntentClassifier = LLMIntentClassifier(promptTemplateExecutor)
    @Test
    fun classify_LLM_intent(){
        val question = "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?"
        val variables = mapOf(LLMIntentClassifier.QUESTION to question)
        coEvery { promptTemplateExecutor.execute(promptTemplate, variables) } returns "faq"
        runBlocking {
            assertEquals("faq", llmIntentClassifier.classify(question))
        }
    }
}