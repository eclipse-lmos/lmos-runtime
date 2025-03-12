/*
 * // SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import org.eclipse.lmos.arc.api.AgentRequest
import org.eclipse.lmos.arc.api.AgentResult
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.model.*
import org.springframework.stereotype.Component

@Component
class ConversationSubscription(
    private val conversationHandler: ConversationHandler,
) : Subscription {
    @GraphQLDescription("Processes the user input and returns the result")
    suspend fun agent(
        agentName: String? = null,
        request: AgentRequest,
    ) = flow {
        coroutineScope {
            val assistantMessageFlow =
                conversationHandler.handleConversation(
                    conversation = request.toConversation(),
                    conversationId = request.conversationContext.conversationId,
                    tenantId = request.systemContext.first { it.key == "tenantId" }.value,
                    turnId = request.conversationContext.turnId ?: "unknown turnId",
                )

            assistantMessageFlow.collect {
                emit(
                    AgentResult(
                        messages =
                            listOf(
                                Message(
                                    role = "assistant",
                                    content = it.content,
                                    turnId = request.conversationContext.turnId,
                                ),
                            ),
                    ),
                )
            }
        }
    }
}

private fun AgentRequest.toConversation() =
    Conversation(
        inputContext =
            InputContext(
                messages = this.messages,
                anonymizationEntities = this.conversationContext.anonymizationEntities,
            ),
        systemContext =
            SystemContext(
                channelId = this.systemContext.first { it.key == "channelId" }.value,
                contextParams = this.systemContext.map { KeyValuePair(it.key, it.value) },
            ),
        userContext =
            UserContext(
                userId = this.userContext.userId ?: throw IllegalArgumentException("missing user id"),
                userToken = this.userContext.userToken,
                contextParams = this.userContext.profile.map { KeyValuePair(it.key, it.value) },
            ),
    )
