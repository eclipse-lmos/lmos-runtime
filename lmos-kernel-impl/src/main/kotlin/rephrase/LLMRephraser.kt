/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.rephrase


import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import org.eclipse.lmos.kernel.steps.Rephraser
import org.slf4j.LoggerFactory

private const val REPHRASE_PROMPT_TEMPLATE = "rephrase"

class LLMRephraser(private val promptTemplateExecutor: PromptTemplateExecutor) : Rephraser {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
            const val QUESTION = "question"
            const val CHAT_HISTORY = "chat_history"
    }

    override suspend fun rephrase(question: String, chatHistory: String, tenantId: String): String? {
        log.debug("Question for rephrasing: $question")

        val variables: Map<String, Any> = hashMapOf(QUESTION to question, CHAT_HISTORY to chatHistory)
        val answer = promptTemplateExecutor.execute(REPHRASE_PROMPT_TEMPLATE, variables)

        log.debug("Rephrase answer: $answer")
        return answer
    }
}