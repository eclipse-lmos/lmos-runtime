/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.model

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.RequestStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConversationMessagesTest {

    @Test
    fun reurn_newInstance_of_message_turn_id() {
        val message=UserMessage("was ist magenta tv?")
        val newInstance = message.applyTurn("12")
        assertEquals("12", newInstance.turnId)
    }

}