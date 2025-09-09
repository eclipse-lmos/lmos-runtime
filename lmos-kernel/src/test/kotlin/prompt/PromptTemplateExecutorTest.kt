/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.prompt

import org.eclipse.lmos.kernel.Failure
import org.eclipse.lmos.kernel.Success
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.LanguageModelExecutor
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.prompt.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PromptTemplateExecutorTest {
    private val promptCompiler = mockk<PromptCompiler>()
    private val promptTemplateRepository = mockk<PromptTemplateRepository>()
    private val languageModelExecutor = mockk<LanguageModelExecutor>()

    private val promptTemplateExecutor = PromptTemplateExecutor(promptTemplateRepository, promptCompiler, languageModelExecutor)

    @Test
    fun should_classify_prompt_successfully(){
        val promptTemplateId = "intent_classification"
        val variable = mapOf("question" to "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?")
        val promptTemplate= PromptTemplate("intent_classification", "You are a conversation classifier.\n" +
                "Given the question in the backticks, determine the intent of the question.\n" +
                "\n" +
                "If related to billing or payment, answer with \"billing\".\n" +
                "Otherwise answer with \"faq\".\n" +
                "\n" +
                "Only answer with a single word.\n" +
                "\n" +
                "```\n" +
                "@{question}\n" +
                "```")
        val compiledContent = "You are a conversation classifier.\n" +
                "Given the question in the backticks, determine the intent of the question.\n" +
                "\n" +
                "If related to billing or payment, answer with \"billing\".\n" +
                "Otherwise answer with \"faq\".\n" +
                "\n" +
                "Only answer with a single word.\n" +
                "\n" +
                "```\n" +
                "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?\n" +
                "```"
            coEvery { promptTemplateRepository.findPromptTemplate(promptTemplateId) } returns promptTemplate
            coEvery { promptCompiler.compile(promptTemplate, variable) } returns Success(compiledContent)
            coEvery { languageModelExecutor.ask(UserMessage(compiledContent)) } returns Success(AssistantMessage("faq"))
            val result = runBlocking { promptTemplateExecutor.execute(promptTemplateId, variable) }
            assertEquals("faq", result)
    }

    @Test
    fun classification_of_prompt_should_failed_if_prompt_not_compiled(){
        val promptTemplateId = "intent_classification"
        val variable = mapOf("question" to "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?")
        val promptTemplate= PromptTemplate("intent_classification", "You are a conversation classifier.\n" +
                "Given the question in the backticks, determine the intent of the question.\n" +
                "\n" +
                "If related to billing or payment, answer with \"billing\".\n" +
                "Otherwise answer with \"faq\".\n" +
                "\n" +
                "Only answer with a single word.\n" +
                "\n" +
                "```\n" +
                "@{question}\n" +
                "```")
        coEvery { promptTemplateRepository.findPromptTemplate(promptTemplateId) } returns promptTemplate
        val compilationFailedException = CompilationFailedException("Failed to compile template $promptTemplateId!!", Exception("Ex"))
        val failureResult = Failure(compilationFailedException)
        coEvery { promptCompiler.compile(promptTemplate, variable) } returns failureResult
        assertNull(runBlocking { promptTemplateExecutor.execute(promptTemplateId, variable) })
    }

    @Test
    fun classification_of_prompt_should_failed_if_prompt_not_found(){
        val promptTemplateId = "intent_classification"
        val variable = mapOf("question" to "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?")
        coEvery { promptTemplateRepository.findPromptTemplate(promptTemplateId) } returns null
        assertNull(runBlocking { promptTemplateExecutor.execute(promptTemplateId, variable) })
    }
}