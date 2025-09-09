/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.prompt


import org.eclipse.lmos.kernel.CONTEXTUAL_KEY_PROMPT_COMPILER
import org.eclipse.lmos.kernel.METRIC_KEY_PROMPT_COMPILER
import org.eclipse.lmos.kernel.failWith
import org.eclipse.lmos.kernel.prompt.CompilationFailedException
import org.eclipse.lmos.kernel.prompt.PromptCompiler
import org.eclipse.lmos.kernel.prompt.PromptTemplate
import org.eclipse.lmos.kernel.result
import io.micrometer.observation.annotation.Observed
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory

open class MvelPromptCompiler : PromptCompiler {

    private val log = LoggerFactory.getLogger(javaClass)

    @Observed(name = METRIC_KEY_PROMPT_COMPILER, contextualName = CONTEXTUAL_KEY_PROMPT_COMPILER)
    override fun compile(promptTemplate: PromptTemplate, variables: Map<String, Any>) = result<String, CompilationFailedException> {
            try {
                TemplateRuntime.eval(promptTemplate.content, variables).toString()
            } catch (ex: Exception) {
                log.error("Failed to compile template: ${promptTemplate.id}:${promptTemplate.version}", ex)
                failWith { CompilationFailedException(promptTemplate.id, ex) }
            }
        }
}
