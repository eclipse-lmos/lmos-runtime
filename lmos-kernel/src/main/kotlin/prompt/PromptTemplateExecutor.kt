/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.prompt

import org.eclipse.lmos.kernel.getOrNull
import org.eclipse.lmos.kernel.getOrThrow
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.LanguageModelExecutor
import org.eclipse.lmos.kernel.model.UserMessage
import org.slf4j.LoggerFactory

class PromptTemplateExecutor(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val promptCompiler: PromptCompiler,
    private val languageModelExecutor: LanguageModelExecutor
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun execute(promptTemplateId: String, variables: Map<String, Any>): String? {
        val promptTemplate = promptTemplateRepository.findPromptTemplate(promptTemplateId)

        if (promptTemplate != null) {
            val compiledContent = promptCompiler.compile(promptTemplate, variables).getOrNull()

            log.info("Compiled template: $compiledContent")
            if (compiledContent != null) {
                val answer = ask(compiledContent).content
                log.info("Prompt template answer: $answer")
                return answer
            }
        }
        log.warn("Failed to execute prompt template: $promptTemplateId")
        return null
    }

    private suspend fun ask(input: String): AssistantMessage {
        return languageModelExecutor.ask(UserMessage(input)).getOrThrow() // TODO Handle exception
    }
}

