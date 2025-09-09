/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants

import org.eclipse.lmos.kernel.Result
import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationAccessValidator
import org.eclipse.lmos.kernel.conversations.ConversationHistoryRepository
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.ConversationMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.result
import org.eclipse.lmos.kernel.services.EventLogger
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.steps.RequestStatus.AUTHORIZATION_REQUIRED
import org.eclipse.lmos.kernel.user.MissingUserException
import org.eclipse.lmos.kernel.user.UserInformation
import org.eclipse.lmos.kernel.user.UserProvider
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class InitConversationHistoryTest {

    @Test
    fun `Test the previous conversation history is used`(): Unit = runBlocking {
        val testHistory = listOf(UserMessage("1"), AssistantMessage("2"), UserMessage("3"))
        val testInput = testInput(testHistory)

        val output =
            InitConversationHistory(
                mock<ConversationHistoryRepository>(),
                ConversationAccessValidator(),
                UnauthorizedUserProvider(),
                mockk<EventLogger>(relaxUnitFun = true)
            ).execute(testInput)

        with(output.context<Conversation>(CONVERSATION_HISTORY)) {
            assertThat(history.size).isEqualTo(3)
            assertThat(history).containsAll(testHistory)
        }
    }

    @Test
    fun `Test that a conversation containing sensitive data requires authentication`(): Unit = runBlocking {
        val testHistory = listOf(UserMessage("1", sensitive = true))
        val testInput = testInput(testHistory)
        val conversationHistoryRepository = mock<ConversationHistoryRepository> {
            onBlocking { getConversation(testInput.requestContext.conversationId) } doReturn Conversation(
                conversationId = "123456",
                history = testHistory,
                profileId = "user",
            )
        }

        val output =
            InitConversationHistory(
                conversationHistoryRepository,
                ConversationAccessValidator(),
                UnauthorizedUserProvider(),
                mockk<EventLogger>(relaxUnitFun = true)
            ).execute(testInput)

        assertThat(output.requestContext.requestStatus).isEqualTo(AUTHORIZATION_REQUIRED)
    }

    private fun testInput(testHistory: List<ConversationMessage>) = Input(
        testHistory.last().content,
        RequestContext("conversationID", "natcoCode", "turnId", RequestStatus.ONGOING),
        mutableMapOf(PREVIOUS_CONVERSATION_HISTORY to testHistory)
    )
}

class UnauthorizedUserProvider : UserProvider {

    override suspend fun provideUser(): Result<UserInformation, MissingUserException> = result { UserInformation(profileId = "user", accessToken = null) }

    override suspend fun <T> setUser(user: UserInformation, fn: suspend () -> T): T = error("not implemented")
}