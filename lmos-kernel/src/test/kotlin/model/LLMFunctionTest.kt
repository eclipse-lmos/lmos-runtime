/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.model

import org.eclipse.lmos.kernel.model.ParameterSchema
import org.eclipse.lmos.kernel.model.ParameterType
import org.eclipse.lmos.kernel.model.ParametersSchema
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class LLMFunctionTest {
    private val required: List<String> = listOf()
    private val parameterType= ParameterType("abc")
    private val parameterSchema = ParameterSchema("name", "description", parameterType, emptyList())
    private val parameters: List<ParameterSchema> = listOf(parameterSchema)
    private val parametersSchema = ParametersSchema(required, parameters)

    @Test
    fun should_convert_into_map(){
        val map=mapOf(
            "type" to "object",
            "required" to required,
            "properties" to parameters.associate { it.name to it.asMap() }
        )
        assertEquals(map, parametersSchema.asMap())
    }

    @Test
    fun create_ParameterType_from_string_type(){
        val type = typeOf<String>()
        val parameterType = ParameterType.from(type)
        assertEquals("string", parameterType.schemaType)
    }

    @Test
    fun create_ParameterType_from_boolean_type(){
        val type = typeOf<Boolean>()
        val parameterType = ParameterType.from(type)
        assertEquals("boolean", parameterType.schemaType)
    }

    @Test
    fun create_ParameterType_from_integer_type(){
        val type = typeOf<Int>()
        val parameterType = ParameterType.from(type)
        assertEquals("number", parameterType.schemaType)
    }

    @Test
    fun throw_exception_for_double_input_type() {
        val type = typeOf<Double>()
        assertThrows<IllegalStateException> { ParameterType.from(type) }
    }
}