/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.intent

import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import org.eclipse.lmos.kernel.steps.IntentClassifier
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class LLMIntentClassifier(
    private val promptTemplateExecutor: PromptTemplateExecutor
) : IntentClassifier {

    companion object {
        const val PROMPT_TEMPLATE = "intent_classification"
        const val QUESTION = "question"
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val intentPattern = Pattern.compile("Intent: (\\w+)")

    override suspend fun classify(question: String): String? {

        log.debug("Question for classification: $question")

        val variables = mapOf(QUESTION to question)
        val answer = promptTemplateExecutor.execute(PROMPT_TEMPLATE, variables)

        log.debug("Intent classification answer: $answer")

        return answer?.let { getIntent(it) }
    }

    private fun getIntent(text: String): String {
        val matcher = intentPattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else text
    }
}