/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import org.eclipse.lmos.kernel.impl.functions.LLMFunction
import org.eclipse.lmos.kernel.impl.functions.LLMFunctionParam
import org.eclipse.lmos.kernel.Failure
import org.eclipse.lmos.kernel.impl.functions.AnnotatedLLMFunction
import org.eclipse.lmos.kernel.model.LLMFunctionException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AnnotatedLLMFunctionTest {

    // Define a sample function
    @LLMFunction(description = "Sample function", sensitive = true)
    fun sampleFunction(
        @LLMFunctionParam("x") x: Int,
        @LLMFunctionParam("y") y: Int
    ): Int = x + y

    @Test
    fun should_fail_with_LLMFunctionException(){
        val parentObject = Any() // Example parent object
        val functionObject = ::sampleFunction // Reference to the sampleFunction

        val annotatedFunction = AnnotatedLLMFunction(parentObject, functionObject)
        val map = mapOf<String, Any>()
        runBlocking {
            assertTrue { annotatedFunction.execute(map) is Failure<LLMFunctionException> }
        }
    }

}