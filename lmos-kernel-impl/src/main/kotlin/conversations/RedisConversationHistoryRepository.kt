/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.conversations

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationHistoryRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import java.time.Duration


class RedisConversationHistoryRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, Conversation>,
    private val ttlDuration: Duration,
) : ConversationHistoryRepository {

    override suspend fun getConversation(conversationId: String): Conversation? = redisTemplate.opsForValue().get(conversationId).awaitSingleOrNull()

    override suspend fun saveConversation(conversation: Conversation): Conversation {
        redisTemplate.opsForValue().set(conversation.conversationId, conversation, ttlDuration).awaitSingleOrNull()
        return conversation
    }

    override suspend fun deleteConversation(conversationId: String) {
        redisTemplate.delete(conversationId).awaitSingleOrNull()
    }
}