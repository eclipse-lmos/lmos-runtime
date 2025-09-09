/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.ConfigurationException
import org.slf4j.LoggerFactory

interface Step {
    suspend fun execute(input: Input): Output
    suspend fun canHandle(input: Input): Boolean = true
}

abstract class AbstractStep : Step {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(input: Input): Output = try {
            executeInternal(input)
        } catch (e: Exception) {
            log.error("Task failed " + javaClass.superclass.simpleName, e)
            throw e
        }

    protected abstract suspend fun executeInternal(input: Input): Output
}

abstract class AbstractProcessingStep : AbstractStep()


enum class RequestStatus {
    UNKNOWN,
    RESOLVED,
    UNRESOLVED,
    ONGOING,
    AGENT_HANDOVER,
    AUTHORIZATION_REQUIRED,
    INSUFFICIENT_PERMISSIONS
}

class RequestContext(
    val conversationId: String,
    val turnId: String,
    val tenantId: String,
    var requestStatus: RequestStatus,
    val additionalInfo: MutableMap<String, Any?> = mutableMapOf()
)


data class Input(
    val content: String,
                 val requestContext: RequestContext,
                 val stepContext: MutableMap<String, Any?>,
                 val errorCause: Exception? = null
)

/**
 * Functions to access context values in a type-safe manner.
 * If name is null, the type T is used to find the first occurrence of a value.
 */
inline fun <reified T> Input.context(name: String? = null, defaultValue: T? = null): T = if (name != null) {
        stepContext[name]?.takeIf { it is T }?.let{ it as T } ?: defaultValue ?: throw MissingContextException(name)
    } else {
        stepContext.entries.firstOrNull { it.value is T }?.value?.let{ it as T } ?: defaultValue ?: throw MissingContextException(
            T::class.simpleName ?: ""
        )
    }

data class Output(
    val content: String,
                  val requestContext: RequestContext,
                  val status: Status,
                  val stepContext: MutableMap<String, Any?>,
                  val errorCause: Exception? = null
) {

    constructor(status: Status, input: Input) : this(input.content, input.requestContext, status, input.stepContext, input.errorCause) {

    }

    constructor(status: Status, input: Input, errorCause: Exception) : this(input.content, input.requestContext, status, input.stepContext, errorCause) {

    }

    constructor(content: String, status: Status, input: Input) : this(content, input.requestContext, status, input.stepContext, input.errorCause) {

    }

}

inline fun <reified T> Output.context(name: String? = null, defaultValue: T? = null): T = if (name != null) {
        stepContext[name]?.takeIf { it is T }?.let{ it as T } ?: defaultValue ?: throw MissingContextException(name)
    } else {
        stepContext.entries.firstOrNull { it.value is T }?.value?.let{ it as T } ?: defaultValue ?: throw MissingContextException(
            T::class.simpleName ?: ""
        )
    }

fun Output.toInput(): Input = Input(this.content, this.requestContext, this.stepContext, this.errorCause)

enum class Status {
    CONTINUE,
    BREAK
}

/**
 * Thrown when an item is not found in the context.
 */
class MissingContextException(name: String) : ConfigurationException("$name is missing from the Context!")

class StepFailedException(message: String) : Exception(message) {
    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }
}
