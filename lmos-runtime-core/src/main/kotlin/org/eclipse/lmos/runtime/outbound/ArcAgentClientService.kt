/*
 * SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import ai.ancf.lmos.arc.agent.client.graphql.GraphQlAgentClient
import ai.ancf.lmos.arc.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import org.eclipse.lmos.runtime.core.constants.LmosRuntimeConstants
import org.eclipse.lmos.runtime.core.exception.AgentClientException
import org.eclipse.lmos.runtime.core.model.Address
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.slf4j.LoggerFactory

class ArcAgentClientService : AgentClientService {
    private val log = LoggerFactory.getLogger(ArcAgentClientService::class.java)

    override suspend fun askAgent(
        conversation: Conversation,
        conversationId: String,
        turnId: String,
        agentName: String,
        agentAddress: Address,
        subset: String?,
    ): Flow<AssistantMessage> {
        return createGraphQlAgentClient(agentAddress).use { graphQlAgentClient ->

            val subsetHeader = subset?.let { mapOf(LmosRuntimeConstants.SUBSET to subset) } ?: emptyMap()

            val agentRequest = AgentRequest(
                conversationContext =
                ConversationContext(
                    conversationId = conversationId,
                    anonymizationEntities = conversation.inputContext.anonymizationEntities,
                ),
                systemContext =
                conversation.systemContext.contextParams.map { (key, value) ->
                    SystemContextEntry(key, value)
                }.toList(),
                userContext =
                UserContext(
                    userId = conversation.userContext.userId,
                    userToken = conversation.userContext.userToken,
                    profile =
                    conversation.userContext.contextParams.map { (key, value) ->
                        ProfileEntry(key, value)
                    }.toList(),
                ),
                messages = conversation.inputContext.messages,
            )


            return try {
                graphQlAgentClient.callAgent(agentRequest,
                requestHeaders = subsetHeader
            ).map { response ->
                log.info("Agent Response: $response")
                AssistantMessage(
                response.messages[0].content,
                response.anonymizationEntities,
            )
                }
                .onCompletion {
                    graphQlAgentClient.close()
                }
            } catch (e: Exception) {
                log.error("Error response from ArcAgentClient", e)
                throw AgentClientException(e.message)
            }

//            val agentResponse =
//                try {
//                    graphQlAgentClient.callAgent(
//                        agentRequest,
//                        requestHeaders = subsetHeader,
//                    )
////                        .toCollection(mutableListOf())
//                } catch (e: Exception) {
//                    log.error("Error response from ArcAgentClient", e)
//                    throw AgentClientException(e.message)
//                }
//
//            emit(AssistantMessage(
//                agentResponse.first().messages[0].content,
//                agentResponse.first().anonymizationEntities,
//            ))
        }
    }

    fun createGraphQlAgentClient(agentAddress: Address): GraphQlAgentClient {
        // TODO - remove hardcoded parts of agent url
        val agentUrl = "ws://${agentAddress.uri}:8080/subscriptions"
        log.info("Creating GraphQlAgentClient with url $agentUrl")
        val graphQlAgentClient = GraphQlAgentClient(agentUrl)
        return graphQlAgentClient
    }
}
