/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.conversations

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationClassification
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.springframework.data.redis.serializer.RedisSerializer

/**
 * Kotlin Serialization Redis serializer for Conversations.
 */
class ConversationRedisSerializer : RedisSerializer<Conversation> {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(ConversationClassification::class) {
                subclass(BillingClassification::class)
                subclass(FAQClassification::class)
            }
        }
    }

    override fun serialize(conversation: Conversation?) = conversation?.let { json.encodeToString(conversation).toByteArray() }

    override fun deserialize(bytes: ByteArray?): Conversation? = bytes?.let { json.decodeFromString(it.decodeToString()) }
}