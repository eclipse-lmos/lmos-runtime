/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package services

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationAccessValidator
import org.eclipse.lmos.kernel.conversations.ConversationHistoryRepository
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.ConversationMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.services.EventLogger
import org.eclipse.lmos.kernel.steps.*
import com.telekom.lmos.platform.assistants.UnauthorizedUserProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EventLoggerTest {

    private var eventLogger: EventLogger = mockk<EventLogger>(relaxUnitFun = true)
    private var conversationHistoryRepository: ConversationHistoryRepository = mockk<ConversationHistoryRepository>()

    @BeforeEach
    fun setup() {
        coEvery { conversationHistoryRepository.getConversation(any()) } answers {
            if (firstArg<String>() == "convId2") {
                Conversation(
                    conversationId = "convId2",
                    status = RequestStatus.ONGOING,
                    currentTurnId = "turnId1",
                    history = listOf(UserMessage("1"), AssistantMessage("2"), UserMessage("3"))
                )
            } else null
        }
    }

    @Test
    fun `test that correct logging statements were indeed called when cache miss`(): Unit = runBlocking {
        val testHistory = listOf(UserMessage("1"), AssistantMessage("2"), UserMessage("3"))
        val testInput = testInput(testHistory, convId = "convId1")

        InitConversationHistory(
            conversationHistoryRepository,
            ConversationAccessValidator(),
            UnauthorizedUserProvider(),
            eventLogger
        ).execute(testInput)

        coVerify(exactly = 1) { eventLogger.logConversationInitiated(testInput) }
        coVerify(exactly = 1) { eventLogger.logTurnStarted(testInput) }
        coVerify(exactly = 0) { eventLogger.logTurnCompleted<Any>(any()) }
    }

    @Test
    fun `test that correct logging statements were indeed called when cache hit`(): Unit = runBlocking {
        val testHistory = listOf(UserMessage("1"), AssistantMessage("2"), UserMessage("3"))
        val testInput = testInput(testHistory, convId = "convId2")

        InitConversationHistory(
            conversationHistoryRepository,
            ConversationAccessValidator(),
            UnauthorizedUserProvider(),
            eventLogger
        ).execute(testInput)

        coVerify(exactly = 0) { eventLogger.logConversationInitiated(testInput) }
        coVerify(exactly = 1) { eventLogger.logTurnStarted(testInput) }
        coVerify(exactly = 0) { eventLogger.logTurnCompleted<Any>(any()) }
    }

    private fun testInput(testHistory: List<ConversationMessage>, convId: String) = Input(
        testHistory.last().content,
        RequestContext(convId, "natcoCode", "turnId", RequestStatus.ONGOING),
        mutableMapOf(PREVIOUS_CONVERSATION_HISTORY to testHistory)
    )
}
