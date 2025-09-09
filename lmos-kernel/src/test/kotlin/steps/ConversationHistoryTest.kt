/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationAccessValidator
import org.eclipse.lmos.kernel.conversations.ConversationHistoryRepository
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.ConversationMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.services.EventLogger
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.CoroutineUserProvider
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConversationHistoryTest {
    private val conversationHistoryRepository= mockk<ConversationHistoryRepository>()
    private val eventLogger = mockk<EventLogger>(relaxUnitFun = true)
    private val initConversationHistory = InitConversationHistory(conversationHistoryRepository, ConversationAccessValidator(), CoroutineUserProvider(), eventLogger)
    private val saveConversationHistory = SaveConversationHistory(conversationHistoryRepository)
    private val deleteConversationHistory =DeleteConversationHistory(conversationHistoryRepository)
    private val conversationHistory: List<ConversationMessage> = listOf(
        UserMessage("was ist magenta tv?", "12"),
        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?", "12"),
        UserMessage("Welche Geräte werden von diesem Dienst unterstützt?", "12"),
        AssistantMessage("MagentaTV unterstützt verschiedene Geräte, darunter:\\n\\n- Samsung Smart TV\\n- LG Smart TV\\n- Android TV (Philips und Sony)\\n- Apple TV\\n- MagentaTV Stick\\n- Amazon Fire TV\\n- Sky Q\\n\\nBitte beachten Sie, dass nicht alle Modelle dieser Hersteller unterstützt werden und dass die Verfügbarkeit und Funktionalität der MagentaTV App auf Ihrem Gerät geprüft werden sollte. Benötigen Sie weitere Informationen?", "12")
    )

    @Test
    fun should_merge_previous_conversation_history_in_conversation_if_it_is_present(){
        val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                PREVIOUS_CONVERSATION_HISTORY to conversationHistory
            )
        )
        coEvery { conversationHistoryRepository.getConversation("99") } returns conversation
        val conversationAfterMerge= Conversation("99", RequestStatus.ONGOING, null, "12", conversationHistory
        )
        runBlocking {
            initConversationHistory.execute(input)
        }
        coVerify(exactly = 0) { eventLogger.logConversationInitiated(input) }
        coVerify(exactly = 1) { eventLogger.logTurnStarted(input) }
        coVerify(exactly = 0) { eventLogger.logTurnCompleted<Any>(any()) }
        assertEquals(conversationAfterMerge, input.stepContext[CONVERSATION_HISTORY])
    }

    @Test
    fun should_merge_input_content_in_history_in_conversation_if_previous_history_is_not_present(){
        val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                )
            )
        )
        coEvery { conversationHistoryRepository.getConversation("99") } returns conversation
        val conversationAfterMerge= conversation + UserMessage(input.content)
        runBlocking {
            initConversationHistory.execute(input)
        }
        coVerify(exactly = 0) { eventLogger.logConversationInitiated(input) }
        coVerify(exactly = 1) { eventLogger.logTurnStarted(input) }
        coVerify(exactly = 0) { eventLogger.logTurnCompleted<Any>(any()) }
        assertEquals(conversationAfterMerge, input.stepContext[CONVERSATION_HISTORY])
    }

    @Test
    fun should_save_conversation_successfully(){
        val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to conversation
            )
        )
        coEvery { conversationHistoryRepository.saveConversation(conversation) } returns conversation
        runBlocking {
            assertEquals(Output(Status.CONTINUE, input), saveConversationHistory.execute(input))
        }
    }

    @Test
    fun should_delete_conversation_successfully(){
        val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to conversation
            )
        )
        coEvery { conversationHistoryRepository.deleteConversation(input.requestContext.conversationId) } coAnswers { }
        runBlocking {
            assertEquals(Output(Status.CONTINUE, input), deleteConversationHistory.execute(input))
        }
    }

    @Test
    fun should_set_flag_initial_turn_2_true_for_new_conversation(){
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                PREVIOUS_CONVERSATION_HISTORY to conversationHistory
            )
        )

        coEvery { conversationHistoryRepository.getConversation("99") } returns null
        runBlocking {
            initConversationHistory.execute(input)
        }

        assertTrue(input.stepContext[IS_INITIAL_TURN] as Boolean)
    }

    @Test
    fun should_set_flag_initial_turn_2_false_for_existing_conversation(){
        val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
        val input = Input("Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                PREVIOUS_CONVERSATION_HISTORY to conversationHistory
            )
        )

        coEvery { conversationHistoryRepository.getConversation("99") } returns conversation
        runBlocking {
            initConversationHistory.execute(input)
        }

        assertFalse(input.stepContext[IS_INITIAL_TURN] as Boolean)
    }
}