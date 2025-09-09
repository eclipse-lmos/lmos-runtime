/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package conversations

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.impl.conversations.RedisConversationHistoryRepository
import org.eclipse.lmos.kernel.steps.RequestStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RedisConversationHistoryRepositoryTest {
    private val redisTemplate = mockk<ReactiveRedisTemplate<String, Conversation>>()
    private val ttlDuration = mockk<Duration>()
    private val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
    private val redisConversationHistoryRepository = RedisConversationHistoryRepository(redisTemplate, ttlDuration)

    @Test
    fun test_save_and_get(){
     coEvery { redisTemplate.opsForValue().get("99") } returns Mono.just(conversation)
     coEvery { redisTemplate.opsForValue().set(conversation.conversationId, conversation, ttlDuration) } returns Mono.just(true)
     runBlocking {
         redisConversationHistoryRepository.saveConversation(conversation)
         assertEquals(conversation, redisConversationHistoryRepository.getConversation(conversation.conversationId))
     }
    }

    @Test
    fun test_save_and_delete(){
        coEvery { redisTemplate.opsForValue().set(conversation.conversationId, conversation, ttlDuration) } returns Mono.just(true)
        coEvery { redisTemplate.delete("99") } returns Mono.empty()
        coEvery { redisTemplate.opsForValue().get("99") } returns Mono.empty()
        runBlocking {
            redisConversationHistoryRepository.saveConversation(conversation)
            redisConversationHistoryRepository.deleteConversation(conversation.conversationId)
            assertNull(redisConversationHistoryRepository.getConversation("99"))
        }
    }
}