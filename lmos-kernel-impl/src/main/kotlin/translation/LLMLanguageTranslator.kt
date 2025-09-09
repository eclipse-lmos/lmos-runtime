/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.translation


import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import org.eclipse.lmos.kernel.steps.LanguageTranslator
import org.slf4j.LoggerFactory

private const val LANGUAGE_TRANSLATOR_PROMPT_TEMPLATE = "language_translator"

class LLMLanguageTranslator(
    private val promptTemplateExecutor: PromptTemplateExecutor
) : LanguageTranslator {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun translate(text: String, language: String): String? {
        log.debug("Input for translation: $text")

        val variables = mapOf("text" to text, "language" to language)
        val answer = promptTemplateExecutor.execute(LANGUAGE_TRANSLATOR_PROMPT_TEMPLATE, variables)

        log.debug("Translated text: $answer")
        return answer?.trim()
    }
}