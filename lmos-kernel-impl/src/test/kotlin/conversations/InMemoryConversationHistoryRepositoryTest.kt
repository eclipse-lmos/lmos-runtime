/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package conversations

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.impl.conversations.InMemoryConversationHistoryRepository
import org.eclipse.lmos.kernel.steps.RequestStatus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryConversationHistoryRepositoryTest {

    private val inMemoryConversationHistoryRepository= InMemoryConversationHistoryRepository()
    private val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())

    @Test
    fun test_save_and_get(){
        runBlocking {
            inMemoryConversationHistoryRepository.saveConversation(conversation)
            assertEquals(conversation, inMemoryConversationHistoryRepository.getConversation(conversation.conversationId))
            assertNull(inMemoryConversationHistoryRepository.getConversation("11"))
        }
    }

    @Test
    fun test_save_and_delete(){
        runBlocking {
            inMemoryConversationHistoryRepository.saveConversation(conversation)
            inMemoryConversationHistoryRepository.deleteConversation(conversation.conversationId)
            assertNull(inMemoryConversationHistoryRepository.getConversation(conversation.conversationId))
        }
    }
}