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
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HierarchicalChatTest {

    @Test
    fun should_call_execute_method(){
        val hierarchicalChat= HierarchicalChat()
        val input = Input(
            "Welche Geräte werden von diesem Dienst unterstützt? Widerruf",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99", RequestStatus.ONGOING, null, "12",
                    listOf()
                ),
                "intent_classification" to "faq",
                "ACCEPTED_OUTPUTS" to "output"
            )
        )
        runBlocking {
            assertEquals(Output(Status.CONTINUE, input), hierarchicalChat.execute(input))
        }
    }
}