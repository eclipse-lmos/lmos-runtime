/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LogTest {
    private val step = mockk<Step>()
    private val log = Log(step)

    @Test
    fun test_logs(){
        val input = Input(
            "what is magentatv?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            stepContext =
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99",
                    RequestStatus.ONGOING,
                    null,
                    "12",
                    listOf(UserMessage("what is magentatv?"))
                ),
                "question" to "what is magentatv?",
                "context" to "magentatv"
            )
        )
        coEvery { step.execute(input) } returns Output(Status.BREAK, input)
        runBlocking {
            assertEquals(Output(Status.BREAK, input), log.execute(input))
        }
    }
}