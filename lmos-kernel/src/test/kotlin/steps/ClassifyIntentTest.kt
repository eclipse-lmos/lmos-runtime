/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ClassifyIntentTest {

    private val intentClassifier = mockk<IntentClassifier>()
    private val classifyIntent = ClassifyIntent(intentClassifier)

    @Test
    fun should_classify_intent_successfully(){
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation("99", RequestStatus.ONGOING, null, "12",
                    listOf(
                        UserMessage("what is magentatv?"),
                        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?")
                    ))
            )
        )
        coEvery { intentClassifier.classify(input.content) } returns "faq"
        runBlocking {
            assertEquals(Output(Status.CONTINUE, input), classifyIntent.execute(input))
            assertEquals("faq", input.stepContext[ClassifyIntent.INTENT_CLASSIFICATION])

        }
    }
}