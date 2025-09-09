/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import org.eclipse.lmos.kernel.Failure
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class JTEFunctionTemplateExecutorTest {

    private val codeResolverProvider = JTECodeResolverProvider(File("mock/dir"))
    private val jteFunctionTemplateExecutor = JTEFunctionTemplateExecutor(codeResolverProvider)

    @Test
    fun should_fail_to_apply_parameters_if_path_is_invalid() {
        runBlocking {
            assertTrue { jteFunctionTemplateExecutor.apply("abc", mapOf()) is Failure }
        }
    }
}