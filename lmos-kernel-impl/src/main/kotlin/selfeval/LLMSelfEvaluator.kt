/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.boot.selfeval

import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import org.eclipse.lmos.kernel.steps.Input
import org.eclipse.lmos.kernel.steps.SelfEvaluator
import org.eclipse.lmos.kernel.steps.context
import org.springframework.stereotype.Component

class LLMSelfEvaluator(
    val promptTemplateExecutor: PromptTemplateExecutor
) : SelfEvaluator {

    companion object {
        const val SELF_EVALUATION_PROMPT = "self_evaluation"
        const val DEFAULT_SELF_EVALUATION = -1
        const val QUESTION_VAR = "question"
        const val ANSWER_VAR = "answer"
        const val CONTEXT_VAR = "context"
    }

    override suspend fun evaluate(input: Input): Int {
        val content = input.content

        val variables = mapOf(
            ANSWER_VAR to content,
            QUESTION_VAR to input.context<String>(QUESTION_VAR),
            CONTEXT_VAR to input.context<String>(CONTEXT_VAR)
        )

        val response = promptTemplateExecutor.execute(SELF_EVALUATION_PROMPT, variables) ?: return DEFAULT_SELF_EVALUATION

        return getScore(response)
    }

    fun getScore(text: String): Int {
        val pattern = """(?<=\D|^)(-1|[0-5])(?=\D|$)""".toRegex()
        val match = pattern.find(text)
        val number = match?.value?.toInt()

        return number ?: DEFAULT_SELF_EVALUATION
    }

}