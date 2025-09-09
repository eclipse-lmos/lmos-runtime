/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.prompt

import org.eclipse.lmos.kernel.Result

interface PromptCompiler {
    fun compile(
        promptTemplate: PromptTemplate,
        variables: Map<String, Any>
    ): Result<String, CompilationFailedException>
}

class CompilationFailedException(promptTemplateId: String, cause: Exception) : Exception("Failed to compile template $promptTemplateId!!", cause)