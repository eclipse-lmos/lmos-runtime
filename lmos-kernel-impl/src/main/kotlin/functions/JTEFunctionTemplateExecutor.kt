/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import gg.jte.CodeResolver
import gg.jte.ContentType.Plain
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import org.eclipse.lmos.kernel.failWith
import org.eclipse.lmos.kernel.result
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

/**
 * Implementation of the FunctionTemplateExecutor using the Java Template Engine.
 * https://jte.gg/
 */
class JTEFunctionTemplateExecutor(codeResolverProvider: JTECodeResolverProvider) : FunctionTemplateExecutor {

    private val log = LoggerFactory.getLogger(javaClass)

    private val engine = TemplateEngine.create(
        codeResolverProvider.provide(),
        Path.of("working"),
        Plain,
        Thread.currentThread().contextClassLoader
    )

    override suspend fun apply(templateId: String, params: Map<String, Any>) = result<String, CompilationFailedException> {
            log.debug("Calling function template with $templateId...")
            try {
                val output = StringOutput()
                engine.render("$templateId.kte", params, output)
                output.toString()
            } catch (ex: Exception) {
                log.error("Failed to compile template: ${templateId}!", ex)
                failWith { CompilationFailedException(templateId, ex) }
            }
        }
}

/**
 * CodeResolvers are used to find and load JTE templates.
 */
class JTECodeResolverProvider(private val templatesFolder: File) {

    fun provide(): CodeResolver = DirectoryCodeResolver(templatesFolder.toPath())
}