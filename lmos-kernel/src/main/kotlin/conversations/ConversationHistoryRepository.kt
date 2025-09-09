/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.conversations

import org.eclipse.lmos.kernel.model.AnonymizationEntity
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.ConversationMessage
import org.eclipse.lmos.kernel.model.SystemMessage
import org.eclipse.lmos.kernel.steps.RequestStatus
import kotlinx.serialization.Serializable
import java.util.*

interface ConversationHistoryRepository {

    suspend fun getConversation(conversationId: String): Conversation?

    suspend fun saveConversation(conversation: Conversation): Conversation

    suspend fun deleteConversation(conversationId: String)
}

@Serializable
data class Conversation(
    val conversationId: String,
    val status: RequestStatus = RequestStatus.ONGOING,
    val classification: ConversationClassification? = null,
    val currentTurnId: String = UUID.randomUUID().toString(),
    val history: List<ConversationMessage> = emptyList(),
    val anonymizationEntities: List<AnonymizationEntity> = emptyList(),
    val profileId: String? = null,
    val partyId: String? = null,
) {

    fun isEmpty() = history.isEmpty()

    /**
     * Returns the AssistantMessage corresponding to the turn id.
     */
    fun getAssistantMessage(turnId: String) = history.firstOrNull { it is AssistantMessage && it.turnId == turnId }

    /**
     * Returns a new instance of the Conversation with the message appended to the end of the history.
     * The turnId of the Conversation is automatically applied.
     */
    fun add(message: ConversationMessage) = copy(history = history + message.applyTurn(currentTurnId))

    /**
     * Returns a new instance of the Conversation with the message inserted at the front of the history.
     * The turnId of the Conversation is automatically applied.
     */
    fun addFirst(message: ConversationMessage) = copy(history = listOf(message.applyTurn(currentTurnId)) + history)

    /**
     * Returns a new instance of the Conversation with the message added to the history.
     * If the message is a SystemMessage, it is added in the front otherwise the message is appended.
     * The turnId of the Conversation is automatically applied.
     */
    operator fun plus(message: ConversationMessage) = if (message is SystemMessage) addFirst(message) else add(message)

    /**
     * Returns true if one or more messages in the conversation history contain sensitive data.
     */
    fun hasSensitiveMessages() = history.any { it.sensitive }
}

inline fun <reified T : ConversationMessage> Conversation.latest(): T? = history.findLast { it is T } as T?

/**
 * Defined a classification for a conversation.
 */
interface ConversationClassification

/**
 * A classification for a conversation that is not yet classified.
 */

object UnclassifiedConversation : ConversationClassification