/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.model

import org.eclipse.lmos.kernel.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

abstract class LanguageModelExecutor(private val meterRegistry: MeterRegistry? = null) {

    private val clientMap = mutableMapOf<String, LanguageModelClient>()

    abstract suspend fun getLanguageModelName(): String

    abstract suspend fun <T> setLanguageModel(model: String, fn: suspend () -> T): T

    fun registerClient(client: LanguageModelClient) {
        clientMap[client.getLanguageModel().id] = client
    }

    suspend fun ask(
        messages: List<ConversationMessage>,
        functions: List<LLMFunction>? = emptyList()
    ): Result<AssistantMessage, LanguageModelException> {
        val provider = clientMap[getLanguageModelName()] ?: throw LanguageModelException("No language model provider defined for ${getLanguageModelName()}!")

        val result: Result<AssistantMessage, LanguageModelException>
        val time = measureTime {
            result = provider.ask(messages, functions)
        }

        timer(provider)?.record(time.toJavaDuration())

        return result
    }

    suspend fun ask(
        message: ConversationMessage,
        functions: List<LLMFunction>? = emptyList()
    ): Result<AssistantMessage, LanguageModelException> = ask(listOf(message), functions)

    /**
     * Creates a metrics timer with given name with preconfigured parameters.
     */
    private fun timer(client: LanguageModelClient): Timer? = if (meterRegistry != null) Timer.builder(METRIC_KEY_LANGUAGE_MODEL_EXECUTOR)
            .tags("languageModelProvider", client.getLanguageModel().provider,
                "languageModelId", client.getLanguageModel().id,
                "languageModelName", client.getLanguageModel().modelName)
            .distributionStatisticExpiry(Duration.ofMinutes(5))
            .distributionStatisticBufferLength(50) //limit memory usage
            .publishPercentiles(0.5, 0.95)
            .percentilePrecision(2)
            .register(meterRegistry) else null
}

interface LanguageModelClient {

    fun getLanguageModel(): LanguageModel

    suspend fun ask(
        messages: List<ConversationMessage>,
        functions: List<LLMFunction>? = emptyList()
    ): Result<AssistantMessage, LanguageModelException>
}

/**
 * Exceptions
 */
open class LanguageModelException(msg: String, cause: OneAIException? = null) : ConfigurationException(msg, cause)