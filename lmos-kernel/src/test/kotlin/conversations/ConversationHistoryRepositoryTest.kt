/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.conversations

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.latest
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.SystemMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.RequestStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConversationHistoryRepositoryTest {
    val conversation = Conversation("99", RequestStatus.ONGOING, null, "12",
        listOf(
            UserMessage("was ist magenta tv?", "12"),
            AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?", "12"),
            UserMessage("Welche Geräte werden von diesem Dienst unterstützt?", "12"),
            AssistantMessage("MagentaTV unterstützt verschiedene Geräte, darunter:\\n\\n- Samsung Smart TV\\n- LG Smart TV\\n- Android TV (Philips und Sony)\\n- Apple TV\\n- MagentaTV Stick\\n- Amazon Fire TV\\n- Sky Q\\n\\nBitte beachten Sie, dass nicht alle Modelle dieser Hersteller unterstützt werden und dass die Verfügbarkeit und Funktionalität der MagentaTV App auf Ihrem Gerät geprüft werden sollte. Benötigen Sie weitere Informationen?", "12")
        ))

    @Test
    fun should_return_false_as_history_is_not_empty(){
        assertFalse { conversation.isEmpty() }
    }

    @Test
    fun should_get_first_assistant_message_corresponding_to_turn_id(){
        assertEquals(AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?", "12"), conversation.getAssistantMessage("12"))
    }

    @Test
    fun should_return_newInstance_of_conversation_after_appending_message_to_the_end_of_the_history_with_turn_id_of_conversation(){
        val message=UserMessage("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?")
        val newInstance=conversation.add(message)
        assertEquals("12", newInstance.history.last().turnId)
        assertEquals(message.applyTurn("12"), newInstance.history.last())
    }

    @Test
    fun should_return_newInstance_of_conversation_after_inserting_message_at_the_front_of_the_history_with_turn_id_of_conversation(){
        val message=AssistantMessage("Willkommen bei Frag Magenta")
        val newInstance=conversation.addFirst(message)
        assertEquals("12", newInstance.history.first().turnId)
        assertEquals(message.applyTurn("12"), newInstance.history.first())
    }

    @Test
    fun should_return_newInstance_of_conversation_after_appending_message_to_the_end_of_the_history_because_message_is_user_message(){
        val message=UserMessage("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?")
        val newInstance=conversation.plus(message)
        assertEquals("12", newInstance.history.last().turnId)
        assertEquals(message.applyTurn("12"), newInstance.history.last())
    }

    @Test
    fun should_return_newInstance_of_conversation_after_inserting_message_at_the_front_of_the_history_because_message_is_system_message(){
        val message=SystemMessage("Willkommen bei Frag Magenta")
        val newInstance=conversation.plus(message)
        assertEquals("12", newInstance.history.first().turnId)
        assertEquals(message.applyTurn("12"), newInstance.history.first())
    }

    @Test
    fun should_return_latest_conversation_history(){

        assertEquals(UserMessage("Welche Geräte werden von diesem Dienst unterstützt?", "12"), conversation.latest<UserMessage>())
    }
}