/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.config

import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.classifier.core.AgentClassifier
import org.eclipse.lmos.classifier.llm.ChatModelClientProperties
import org.eclipse.lmos.classifier.llm.LangChainChatModelFactory
import org.eclipse.lmos.runtime.channelrouting.DefaultCachedChannelRoutingRepository
import org.eclipse.lmos.runtime.core.ChannelRoutingRepositoryType
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.channel.ChannelRepository
import org.eclipse.lmos.runtime.core.channelrouting.CachedChannelRoutingRepository
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.disambiguation.DefaultDisambiguationHandler
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationHandler
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.inbound.DefaultConversationHandler
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.eclipse.lmos.runtime.core.service.routing.ExplicitAgentRoutingService
import org.eclipse.lmos.runtime.outbound.ArcAgentClientService
import org.eclipse.lmos.runtime.outbound.DefaultAgentClassifierService
import org.eclipse.lmos.runtime.outbound.FileBasedChannelRoutingRepository
import org.eclipse.lmos.runtime.outbound.LmosAgentRoutingService
import org.eclipse.lmos.runtime.outbound.OperatorChannelRepository
import org.eclipse.lmos.runtime.outbound.OperatorChannelRoutingRepository
import org.eclipse.lmos.runtime.properties.RuntimeProperties
import org.eclipse.lmos.runtime.properties.Type
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(RuntimeProperties::class)
class RuntimeAutoConfiguration(
    private val runtimeProperties: RuntimeProperties,
) {
    @Bean
    @ConditionalOnMissingBean(ChannelRepository::class)
    fun channelRepository(runtimeConfig: RuntimeConfiguration): ChannelRepository = OperatorChannelRepository(runtimeConfig)

    @Bean
    @ConditionalOnMissingBean(CachedChannelRoutingRepository::class)
    fun cachedChannelRoutingRepository(channelRoutingRepository: ChannelRoutingRepository): CachedChannelRoutingRepository =
        DefaultCachedChannelRoutingRepository(
            channelRoutingRepository,
        )

    @Bean
    @ConditionalOnMissingBean(AgentClientService::class)
    fun agentClientService(): AgentClientService = ArcAgentClientService()

    @Bean
    @ConditionalOnMissingBean(ChannelRoutingRepository::class) // Corrected this line
    fun channelRoutingRepository(lmosRuntimeConfig: RuntimeConfiguration): ChannelRoutingRepository {
        val agentRegistryConfig = runtimeProperties.channelRoutingRepository
        return when (agentRegistryConfig.type) {
            ChannelRoutingRepositoryType.API -> {
                agentRegistryConfig.baseUrl
                    ?: throw IllegalArgumentException(
                        "LMOS runtime agent registry type is API, but 'lmos.runtime.channelRoutingRepository.base-url' is not configured.",
                    )
                OperatorChannelRoutingRepository(runtimeProperties)
            }
            ChannelRoutingRepositoryType.FILE -> {
                agentRegistryConfig.fileName
                    ?: throw IllegalArgumentException(
                        "LMOS runtime agent registry type is FILE, but 'lmos.runtime.channelRoutingRepository.filename' is not configured.",
                    )
                FileBasedChannelRoutingRepository(lmosRuntimeConfig)
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean(AgentRoutingService::class)
    fun agentRoutingService(): AgentRoutingService =
        when (runtimeProperties.router.type) {
            Type.EXPLICIT -> ExplicitAgentRoutingService()
            Type.LLM -> {
                runtimeProperties.openAi
                    ?.key
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("openAI configuration key is null or empty")
                LmosAgentRoutingService(runtimeProperties)
            }
        }

    @Bean
    @ConditionalOnMissingBean(AgentClassifierService::class)
    fun agentClassifierService(classifier: AgentClassifier): AgentClassifierService = DefaultAgentClassifierService(classifier)

    @Bean
    @Qualifier("disambiguationChatModel")
    @ConditionalOnProperty(
        prefix = "lmos.runtime.disambiguation",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false,
    )
    fun disambiguationChatModel(runtimeProperties: RuntimeProperties): ChatModel =
        // Registers a dedicated ChatModel for disambiguation, so that we can use
        // different models for disambiguation and agent classification.
        LangChainChatModelFactory.createClient(
            ChatModelClientProperties(
                provider = runtimeProperties.disambiguation.llm.provider,
                apiKey = runtimeProperties.disambiguation.llm.apiKey,
                baseUrl = runtimeProperties.disambiguation.llm.baseUrl,
                model = runtimeProperties.disambiguation.llm.model,
                maxTokens = runtimeProperties.disambiguation.llm.maxTokens,
                temperature = runtimeProperties.disambiguation.llm.temperature,
                logRequestsAndResponses = runtimeProperties.disambiguation.llm.logRequestsAndResponses,
            ),
        )

    @Bean
    @ConditionalOnMissingBean(DisambiguationHandler::class)
    @ConditionalOnProperty(
        prefix = "lmos.runtime.disambiguation",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false,
    )
    fun disambiguationHandler(
        @Qualifier("disambiguationChatModel") chatModel: ChatModel,
        lmosRuntimeProperties: RuntimeProperties,
    ): DisambiguationHandler =
        DefaultDisambiguationHandler(
            chatModel,
            lmosRuntimeProperties.disambiguation.introductionPrompt(),
            lmosRuntimeProperties.disambiguation.clarificationPrompt(),
        )

    @Bean
    @ConditionalOnMissingBean(ConversationHandler::class)
    fun conversationHandler(
        agentRoutingService: AgentRoutingService,
        agentClassifierService: AgentClassifierService,
        cachedChannelRoutingRepository: CachedChannelRoutingRepository,
        agentClientService: AgentClientService,
        disambiguationHandlerProvider: ObjectProvider<DisambiguationHandler>,
    ): ConversationHandler =
        DefaultConversationHandler(
            agentRoutingService,
            agentClassifierService,
            cachedChannelRoutingRepository,
            agentClientService,
            disambiguationHandlerProvider.ifAvailable,
        )
}
