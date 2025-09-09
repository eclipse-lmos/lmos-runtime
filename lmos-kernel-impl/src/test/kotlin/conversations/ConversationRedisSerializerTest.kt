/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package conversations

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.impl.conversations.ConversationRedisSerializer
import org.eclipse.lmos.kernel.steps.RequestStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConversationRedisSerializerTest {

    private val conversationRedisSerializer: ConversationRedisSerializer= ConversationRedisSerializer()

    @Test
    fun should_serialize_and_deserialize_conversation(){
        val conversation = Conversation("99", RequestStatus.ONGOING, null, "12", listOf())
        val result= conversationRedisSerializer.deserialize(conversationRedisSerializer.serialize(conversation))
        assertEquals(conversation, result)
    }
}
