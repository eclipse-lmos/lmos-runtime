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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PseudonymizationTest {

    private val pseudonymizer = mockk<Pseudonymizer>()
    private val step = mockk<Step>()
    private val pseudonymization= Pseudonymization(pseudonymizer, step)

    @Test
    fun test_pseudonymization(){
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
                        UserMessage("what is magentatv?"),
                        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?")
                    ))
            )
        )
        val output=Output("Welche Geräte werden vom Magenta-Dienst unterstützt?", Status.CONTINUE, input)
        val anonymizedInput =Input("Welche Geräte werden vom Magenta-Dienst unterstützt?", input.requestContext, input.stepContext)
        every { pseudonymizer.anonymize(input) } returns "Welche Geräte werden vom Magenta-Dienst unterstützt?"
        every { pseudonymizer.deanonymize(output) } returns "Welche Geräte werden von diesem Dienst unterstützt?"
        coEvery { step.execute(anonymizedInput) } returns Output("Welche Geräte werden vom Magenta-Dienst unterstützt?", Status.CONTINUE, input)
        runBlocking {
            assertEquals(Output(pseudonymizer.deanonymize(output), input.requestContext, Status.CONTINUE, input.stepContext), pseudonymization.execute(input))
        }
    }
}