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
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelfEvaluateTest {
    private val selfEvaluator = mockk<SelfEvaluator>()
    private val selfEvaluate = SelfEvaluate(selfEvaluator)
    @Test
    fun should_self_evaluate_the_annswer() {
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
        coEvery { selfEvaluator.evaluate(input) } returns 2
        runBlocking {
            assertNull(input.stepContext["self_evaluation"])
            assertEquals(Output(Status.CONTINUE, input), selfEvaluate.execute(input))
            assertEquals(2, input.stepContext["self_evaluation"])
        }
        coVerify(exactly = 1) { selfEvaluator.evaluate(input) }
    }
}