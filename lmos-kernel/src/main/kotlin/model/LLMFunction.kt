/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.model

import org.eclipse.lmos.kernel.OneAIException
import org.eclipse.lmos.kernel.Result
import kotlin.reflect.KType

/**
 * Describes a function that can be passed to a Large Language Model.
 */
interface LLMFunction {
    val name: String
    val parameters: ParametersSchema
    val description: String
    val group: String
    val isSensitive: Boolean

    suspend fun execute(input: Map<String, Any?>): Result<String, LLMFunctionException>
}

/**
 * Exceptions thrown when an LLMFunction fails.
 */
class LLMFunctionException(msg: String, override val cause: OneAIException? = null) : Exception(msg, cause)

/**
 * Provides LLMFunctions.
 */
interface LLMFunctionProvider {

    fun provideByGroup(functionGroup: String): List<LLMFunction>
}

/**
 * Schema tha describes LLM Functions parameters.
 */
data class ParametersSchema(val required: List<String>, val parameters: List<ParameterSchema>) {
    fun asMap() = mapOf(
        "type" to "object",
        "required" to required,
        "properties" to parameters.associate { it.name to it.asMap() }
    )
}

data class ParameterSchema(
    val name: String,
                           val description: String,
                           val type: ParameterType,
                           val enum: List<String>
) {
    fun asMap() = mapOf(
        "type" to type.schemaType,
        "description" to description
    ).let {
        if(enum.isNotEmpty()) {
            it + ("enum" to enum)
        } else it
    }
}

/**
 * A parameter type that can be used by LLM Functions.
 */
class ParameterType(val schemaType: String) {

    companion object {
        fun from(type: KType) = ParameterType(
            when (type.classifier) {
                String::class -> "string"
                Boolean::class -> "boolean"
                Int::class -> "number"
                else -> error("Invalid input type for LLMFunctions! -> $type")
            }
        )
    }
}

