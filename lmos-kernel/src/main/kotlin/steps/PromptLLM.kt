/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.getOrThrow
import org.eclipse.lmos.kernel.model.LanguageModelExecutor
import org.eclipse.lmos.kernel.model.UserMessage

class PromptLLM(private val languageModelExecutor: LanguageModelExecutor) : AbstractStep() {
    override suspend fun executeInternal(input: Input): Output = ask(input)

    private suspend fun ask(input: Input): Output {
        val answer = languageModelExecutor.ask(UserMessage(input.content)).getOrThrow() // TODO
        return Output(answer.content, Status.CONTINUE, input)
    }
}