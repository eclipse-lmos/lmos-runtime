/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.SystemMessage
import org.eclipse.lmos.kernel.model.UserMessage


fun interface Rephraser {
    suspend fun rephrase(question: String, chatHistory: String, tenantId: String): String?
}

class Rephrase(private val rephraser: Rephraser) : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {

        val chatHistory = input.context<Conversation>(CONVERSATION_HISTORY).history
        val chatHistoryString = chatHistory.subList(0, chatHistory.size -1).joinToString("\n") {
            when(it) {
                is UserMessage -> "user: ${it.content}"
                is SystemMessage -> "system: ${it.content}"
                else -> "assistant: ${it.content}"
            }
        }
        val rephrasedText = rephraser.rephrase(input.content, chatHistoryString, input.requestContext.tenantId)
            ?: throw StepFailedException("Failed to rephrase output!")

        return Output(rephrasedText, Status.CONTINUE, input)
    }
}