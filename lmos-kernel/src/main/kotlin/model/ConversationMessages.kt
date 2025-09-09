/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.model

import kotlinx.serialization.Serializable

/**
 * Conversation Messages.
 */
@Serializable
sealed class ConversationMessage {

    abstract val turnId: String?
    abstract val content: String
    abstract val sensitive: Boolean
    abstract val anonymized: Boolean

    /**
     * Returns a new instance of the message with the turn id applied.
     * The turn id is normally add when a message is added to a conversation.
     */
    abstract fun applyTurn(turnId: String): ConversationMessage
}

/**
 * A message sent by the user.
 */
@Serializable
data class UserMessage(
    override val content: String,
    override val turnId: String? = null,
    override val sensitive: Boolean = false,
    override val anonymized: Boolean = false,
) : ConversationMessage() {
    override fun applyTurn(turnId: String): UserMessage = copy(turnId = turnId)
}

/**
 * A message added by the Platform to help instruct or guide the LLM.
 */
@Serializable
data class SystemMessage(
    override val content: String,
    override val turnId: String? = null,
    override val sensitive: Boolean = false,
    override val anonymized: Boolean = false,
) : ConversationMessage() {
    override fun applyTurn(turnId: String): SystemMessage = copy(turnId = turnId)
}

/**
 * A message sent by the LLM Model.
 */
@Serializable
data class AssistantMessage(
    override val content: String,
    override val turnId: String? = null,
    override val sensitive: Boolean = false,
    override val anonymized: Boolean = false,
) : ConversationMessage() {
    override fun applyTurn(turnId: String): AssistantMessage = copy(turnId = turnId)
}

/**
 * A message added by the Mediator Platform
 */
@Serializable
data class MediatorMessage(
    override val content: String,
    override val turnId: String? = null,
    override val sensitive: Boolean = false,
    override val anonymized: Boolean = false,
) : ConversationMessage() {
    override fun applyTurn(turnId: String): MediatorMessage = copy(turnId = turnId)
}