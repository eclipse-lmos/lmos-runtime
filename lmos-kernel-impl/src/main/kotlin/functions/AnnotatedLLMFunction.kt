/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import org.eclipse.lmos.kernel.*
import org.eclipse.lmos.kernel.model.*
import org.eclipse.lmos.kernel.model.LLMFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.eclipse.lmos.kernel.impl.functions.LLMFunction as LLMFunctionAnnotation

/**
 * Bridges a kotlin function that is annotated with the @LLMFunction to the LLMFunction interface.
 */
class AnnotatedLLMFunction(private val parent: Any, private val function: KFunction<*>) : LLMFunction {

    /**
     * Implement parameters.
     */
    init {
        validateFunctionParameters(function)
    }

    /**
     * Implement LLMFunction interface.
     */
    //override val name = function.name.camelToSnakeCase()
    override val name = function.name
    override val group = parent::class.findAnnotation<LLMFunctions>()?.group ?: ""
    override val parameters = function.convertToSchema()

    private val llmFunctionAnnotation =
        function.findAnnotation<LLMFunctionAnnotation>() ?: error("${function.name} is missing LLMFunction annotation!")
    override val description = llmFunctionAnnotation.description
    override val isSensitive = llmFunctionAnnotation.sensitive

    /**
     * Executes the function and returns the result.
     */
    override suspend fun execute(input: Map<String, Any?>): Result<String, LLMFunctionException> = try {
            val args = (listOf(parent) + function.parameters.names().map { input[it] }).toTypedArray()
            when (val result = function.callSuspend(*args)) {
                is Success<*> -> Success(result.value.toString())
                is Failure<*> -> Failure(
                    LLMFunctionException(
                        "LLMFunction call $name failed! ",
                        result.reason.convert()
                    )
                )

                else -> Success(result.toString())
            }
        } catch (ex: Exception) {
            Failure(LLMFunctionException("LLMFunction call $name failed! ", ServerException(cause = ex)))
        }

    private fun Exception.convert() = if (this is OneAIException) this else ServerException(cause = this)

    /**
     * Converts the parameters of a kotlin function to a Schema that can be understood by LLMs.
     */
    private fun KFunction<*>.convertToSchema(): ParametersSchema = ParametersSchema(
            required = parameters.names(),
            parameters = parameters.filter { it.name != null }.map { it.convertToSchema() }
        )

    private fun List<KParameter>.names() = mapNotNull { it.name }.toList()

    /**
     * Converts a parameter of a kotlin function to a Schema that can be understood by LLMs.
     */
    private fun KParameter.convertToSchema(): ParameterSchema {
        val param = findAnnotations<LLMFunctionParam>().first()
        return ParameterSchema(
            name = name!!,
            type = ParameterType.from(type),
            description = param.description,
            enum = param.enum.toList()
        )
    }

    /**
     * Validates a kotlin function that is annotated with the LLMFunctionAnnotation.
     */
    private fun validateFunctionParameters(function: KFunction<*>) {
        function.parameters.filter { it.name != null }.map {
            it.findAnnotations<LLMFunctionParam>().firstOrNull()
                ?: error("Function $function is missing LLMFunctionParam annotation on $it!")
        }
    }
}