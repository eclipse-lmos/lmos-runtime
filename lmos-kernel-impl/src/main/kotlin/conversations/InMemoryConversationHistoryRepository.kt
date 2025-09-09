/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.conversations

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationHistoryRepository
import java.time.Duration


class InMemoryConversationHistoryRepository : ConversationHistoryRepository {

    private val conversationHistory: Cache<String, Conversation> = Caffeine.newBuilder()
        .maximumSize(100) // TODO
        .expireAfterWrite(Duration.ofHours(1)) // TODO
        .build()

    override suspend fun getConversation(conversationId: String): Conversation? = conversationHistory.getIfPresent(conversationId)

    override suspend fun saveConversation(conversation: Conversation): Conversation {
        conversationHistory.put(conversation.conversationId, conversation)
        return conversation
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversationHistory.invalidate(conversationId)
    }
}