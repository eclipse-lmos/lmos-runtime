/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.llm.DefaultModelClient
import org.eclipse.lmos.router.llm.DefaultModelClientProperties
import org.eclipse.lmos.router.llm.LLMAgentRoutingSpecsResolver
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.AgentBuilder
import org.eclipse.lmos.runtime.core.model.AgentCapability
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.slf4j.LoggerFactory

class LmosAgentRoutingService(
    private val lmosRuntimeConfig: RuntimeConfiguration,
) : AgentRoutingService {
    private val log = LoggerFactory.getLogger(LmosAgentRoutingService::class.java)

    override suspend fun resolveAgentForConversation(
        conversation: Conversation,
        agentList: List<Agent>,
    ): Agent {
        val agentRoutingSpecResolver = initializeAgentRouter(agentList)
        val (context, input) = prepareConversationComponents(conversation)

        val agentRoutingSpec =
            withContext(Dispatchers.IO) { resolveAgent(agentRoutingSpecResolver, context, input) }
        log.info("Resolved agent: $agentRoutingSpec")
        return agentRoutingSpec?.toAgent() ?: throw AgentRoutingSpecResolverException("No agent resolved for user query")
    }

    private fun resolveAgent(
        agentRoutingSpecResolver: AgentRoutingSpecsResolver,
        context: Context,
        input: UserMessage,
    ): AgentRoutingSpec? {
        val agentRoutingSpec =
            agentRoutingSpecResolver.resolve(context, input).getOrThrow()
        return agentRoutingSpec
    }

    fun initializeAgentRouter(agentList: List<Agent>): AgentRoutingSpecsResolver {
        val routingSpecProvider = routingSpecProvider(agentList.toAgentRoutingSpec())
        val agentRoutingSpecResolver = agentRoutingSpecResolver(routingSpecProvider)
        return agentRoutingSpecResolver
    }

    private fun routingSpecProvider(agentRoutingSpec: List<AgentRoutingSpec>): SimpleAgentRoutingSpecProvider =
        SimpleAgentRoutingSpecProvider().apply {
            agentRoutingSpec.forEach { add(it) }
        }

    private fun agentRoutingSpecResolver(agentRoutingSpecsProvider: AgentRoutingSpecsProvider): AgentRoutingSpecsResolver {
        val openAIConfig = lmosRuntimeConfig.openAi ?: throw IllegalArgumentException("OpenAI configuration is missing")

        fun requireField(
            value: Any?,
            name: String,
        ): Any =
            when (value) {
                is String -> if (value.isNotBlank()) value else throw IllegalArgumentException("OpenAI config '$name' is blank")
                null -> throw IllegalArgumentException("OpenAI config '$name' is null")
                else -> value
            }

        with(openAIConfig) {
            val defaultModelClientProperties =
                DefaultModelClientProperties(
                    provider = requireField(provider, "provider") as String,
                    openAiUrl = requireField(url, "url") as String,
                    openAiApiKey = requireField(key, "key") as String,
                    model = requireField(model, "model") as String,
                    maxTokens = requireField(maxTokens, "maxTokens") as Int,
                    temperature = requireField(temperature, "temperature") as Double,
                    format = requireField(format, "format") as String,
                )

            return LLMAgentRoutingSpecsResolver(
                agentRoutingSpecsProvider,
                modelClient = DefaultModelClient(defaultModelClientProperties),
            )
        }
    }

    private fun prepareConversationComponents(conversation: Conversation): Pair<Context, UserMessage> {
        val userMessage =
            conversation.inputContext.messages
                .last()
                .content
        val conversationHistory =
            conversation.inputContext.messages.dropLast(1).map {
                when (it.role) {
                    "system" -> SystemMessage(it.content)
                    "user" -> UserMessage(it.content)
                    "assistant" -> AssistantMessage(it.content)
                    else -> throw IllegalArgumentException("Unsupported role: ${it.role}")
                }
            }

        val context = Context(conversationHistory)
        val input = UserMessage(userMessage)
        return Pair(context, input)
    }
}

fun List<Agent>.toAgentRoutingSpec(): List<AgentRoutingSpec> =
    this.map { agent ->
        AgentRoutingSpecBuilder()
            .name(agent.name)
            .version(agent.version)
            .description(agent.description)
            .apply {
                agent.capabilities.map { agentCapability ->
                    addCapability(
                        Capability(
                            agentCapability.name,
                            agentCapability.description,
                            agent.version,
                        ),
                    )
                }
                agent.addresses.map { address ->
                    address(Address(address.protocol, address.uri))
                }
            }.build()
    }

fun AgentRoutingSpec.toAgent(): Agent =
    AgentBuilder()
        .name(name)
        .description(description)
        .version(version)
        .addresses(
            addresses
                .map { address ->
                    org.eclipse.lmos.runtime.core.model
                        .Address(address.protocol, address.uri)
                }.toList(),
        ).apply {
            capabilities(
                capabilities.map { capability ->
                    AgentCapability("na", capability.name, capability.version, capability.description)
                },
            )
        }.build()
