/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import org.eclipse.lmos.kernel.ConfigurationException
import org.eclipse.lmos.kernel.Result

/**
 * Executes templates designed for LLM function responses.
 */
interface FunctionTemplateExecutor {

    /**
     * Applies the parameters to a template and returns the result.
     *
     * @param templateId the id of the template to apply the parameters to.
     * @param params the parameters.
     * @return the output of the parameters applied to the template.
     */
    suspend fun apply(templateId: String, params: Map<String, Any>): Result<String, FunctionTemplateException>

}

/**
 * Exceptions
 */
sealed class FunctionTemplateException(msg: String, cause: Exception? = null) : ConfigurationException(msg, cause)

class TemplateNotFoundException(templateId: String, cause: Exception? = null) : FunctionTemplateException("Failed to find template $templateId!!", cause)

class CompilationFailedException(templateId: String, cause: Exception? = null) : FunctionTemplateException("Failed to compile template $templateId!!", cause)

class ExecutionFailedException(templateId: String, cause: Exception? = null) : FunctionTemplateException("Unexpected exception executing template $templateId!!", cause)