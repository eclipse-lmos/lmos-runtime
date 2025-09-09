/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.observe.AgentTracer
import org.eclipse.lmos.kernel.observe.Tags
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.slf4j.MDC


abstract class TracedStep(private val tracer: AgentTracer, private val phase: String? = null) : AbstractStep() {

    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)
    private val additionalInfoAttributes = mutableListOf<String>()
    private val stepContextAttributes = mutableListOf<String>()


    override suspend fun execute(input: Input): Output = tracer.withSpan(javaClass.simpleName, mapOf("step" to javaClass.simpleName)) { tags ->
            executeWithTags(tags, input)
        }

    suspend fun executeWithTags(
        tags: Tags,
        input: Input
    ): Output = try {
        tags.tag("openinference.span.kind", "CHAIN")
        if (phase != null) tags.tag("phase", phase)
        tags.tag("input.value", input.content)
        val output = executeInternal(input)
        tags.tag(
            "output.value",
            input.stepContext["assistant_response"]?.toString() ?: output.content
        ) // TODO requires bigger change in the FAQAgent.
        tags.tag("input.requestStatus", input.requestContext.requestStatus.name)
        tags.tag("output.status", output.status.name)
        tags.tag("session.id", input.requestContext.conversationId)
        tags.tag("user.id", input.requestContext.conversationId)
        tags.tag(
            "metadata",
            Json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                (MDC.getCopyOfContextMap() ?: emptyMap())
            )
        )
        for (attribute in getAdditionalInfoAttributes()) {
            tags.tag(
                "input.requestStatus.additionalInfo.$attribute",
                input.requestContext.additionalInfo[attribute]?.toString() ?: ""
            )
        }
        for (attribute in getStepContextAttributes()) {
            tags.tag("input.stepContext.$attribute", input.stepContext[attribute]?.toString() ?: "")
        }
        output
    } catch (e: Exception) {
        tags.tag("exception.escaped", "true")
        tags.tag("exception.message", e.message.toString())
        tags.tag("exception.stacktrace", e.stackTraceToString())
        tags.tag("exception.type", e.javaClass.simpleName)
        throw e
    }

    open suspend fun getAdditionalInfoAttributes(): List<String> = additionalInfoAttributes

    open suspend fun getStepContextAttributes(): List<String> = stepContextAttributes
}

abstract class TracedAbstractProcessingStep(private val tracer: AgentTracer) : TracedStep(tracer)


