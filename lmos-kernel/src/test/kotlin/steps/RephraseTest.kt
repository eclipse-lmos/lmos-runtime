/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.SystemMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RephraseTest {

    private val rephraser = mockk<Rephraser>()
    private val rephrase = Rephrase(rephraser)
    @Test
    fun should_rephrase_question(){
        val input = Input("Welche Geräte werden von diesem Dienst unterstützt?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation("99", RequestStatus.ONGOING, null, "12",
                    listOf(
                        SystemMessage("Willkommen bei Frag Magenta"),
                        UserMessage("was ist magenta tv?"),
                        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?")
                    )),
                "intent_classification" to "faq"
            )
        )
        val chatHistory = input.context<Conversation>(CONVERSATION_HISTORY).history
        val chatHistoryString = chatHistory.subList(0, chatHistory.size -1).joinToString("\n") {
            when(it) {
                is UserMessage -> "user: ${it.content}"
                is SystemMessage -> "system: ${it.content}"
                else -> "assistant: ${it.content}"
            }
        }
        val rephrasedText="Welche Geräte werden vom Margenta-Service unterstützt?"
        coEvery { rephraser.rephrase(input.content, chatHistoryString, input.requestContext.tenantId) } returns rephrasedText
        runBlocking {
            assertEquals(Output(rephrasedText, Status.CONTINUE, input), rephrase.execute(input))
        }
    }

    @Test
    fun should_fail_if_rephrase_method_return_null(){
        val input = Input("Welche Geräte werden von diesem Dienst unterstützt?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation("99", RequestStatus.ONGOING, null, "12",
                    listOf(
                        SystemMessage("Willkommen bei Frag Magenta"),
                        UserMessage("was ist magenta tv?"),
                        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?")
                    )),
                "intent_classification" to "faq"
            )
        )
        val chatHistory = input.context<Conversation>(CONVERSATION_HISTORY).history
        val chatHistoryString = chatHistory.subList(0, chatHistory.size -1).joinToString("\n") {
            when(it) {
                is UserMessage -> "user: ${it.content}"
                is SystemMessage -> "system: ${it.content}"
                else -> "assistant: ${it.content}"
            }
        }
        coEvery { rephraser.rephrase(input.content, chatHistoryString, input.requestContext.tenantId) } returns null
        runBlocking {
            assertFailsWith<StepFailedException>{ rephrase.execute(input)}
        }
    }
}